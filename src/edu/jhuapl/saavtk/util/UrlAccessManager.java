package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

/**
 * A facility for managing server access to URLs online. An instance of this
 * class has a root URL and manages a cache of information about URLs (which may
 * or may not be "under" this root URL's hierarchy). It also provides facilities
 * for locating and downloading URLs to the local file system.
 * 
 * @author peachjm1
 *
 */
public class UrlAccessManager
{
    protected static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    private static volatile boolean showDotsForFiles = false;

    public static boolean isShowDotsForFiles()
    {
        return showDotsForFiles;
    }

    public static void setShowDotsForFiles(boolean showDotsForFiles)
    {
        UrlAccessManager.showDotsForFiles = showDotsForFiles;
    }

    /**
     * Create a new UrlAccessManager using the arguments as the root level URL for
     * relative path queries. If server access is enabled, this method attempts to
     * open a connection and determine whether this process can successfully access
     * the root-level server.
     * 
     * @param rootUrl the root level of the server
     * @return the manager
     */
    public static UrlAccessManager of(URL rootUrl)
    {
        Preconditions.checkNotNull(rootUrl);

        UrlAccessManager result = new UrlAccessManager(rootUrl);

        UrlInfo rootInfo = result.getInfo(rootUrl);
        try (CloseableUrlConnection connection = CloseableUrlConnection.of(rootInfo, HttpRequestMethod.HEAD))
        {
            Debug.out().println("Querying server about root URL " + rootUrl);
            rootInfo.update(connection.getConnection());
        }
        catch (FileNotFoundException | ConnectException | UnknownHostException e)
        {
            // Serious problem. Disable server access pending resolution.
            System.err.println("Problem connecting to server. Disabling server access for now");
            e.printStackTrace();
            result.setEnableServerAccess(false);
            rootInfo.update(UrlState.of(rootUrl));
        }
        catch (Exception e)
        {
            // Any other exception (e.g. SocketTimeoutException) most likely indicates
            // a transient problem, so report it but do not disable access.
            e.printStackTrace();
            rootInfo.update(UrlState.of(rootUrl));
        }

        return result;
    }

    private final URL rootUrl;
    private final ConcurrentMap<URL, UrlInfo> urlInfoCache;
    private volatile boolean enableServerAccess;

    protected UrlAccessManager(URL rootUrl)
    {
        this.rootUrl = rootUrl;
        this.urlInfoCache = new ConcurrentHashMap<>();
        this.enableServerAccess = true;
    }

    public boolean isServerAccessEnabled()
    {
        return enableServerAccess;
    }

    public void setEnableServerAccess(boolean enableServerAccess)
    {
        this.enableServerAccess = enableServerAccess;
    }

    public URL getRootUrl()
    {
        return rootUrl;
    }

    /**
     * Transforms a string into a URL. If the argument already represents a complete
     * and valid URL it will be simply converted to a URL object and returned.
     * Otherwise, the returned URL is formed by appending the argument to this
     * manager's root URL.
     * <p>
     * For example, if the root URL were "http://mydomain.com/mytool/data", the
     * string "/path/to/my/file.txt" would be converted to the URL
     * "http://mydowmain.com/mytool/data/path/to/my/file.txt", while the string
     * "http://some.other.domain.com/path/to/my/file.txt" would be converted to a
     * URL as-is. Note that the Windows-style path "C:\Users\myName\file.txt" would
     * be converted to "http://mydomain.com/mytool/data/C:/Users/myName/file.txt".
     * <p>
     * This method never queries the server, so there is no guarantee that the URL
     * exists or is accessible, only that it is syntactically correct.
     * 
     * @param urlString URL string, either absolute or relative to the root URL
     * @return the URL object
     * @throws IllegalArgumentException if the constructed URL string argument is
     *             malformed
     */
    public URL getUrl(String urlString)
    {
        Preconditions.checkNotNull(urlString);

        try
        {
            // Create the URL object. If the provided string has a protocol, use it as-is.
            URL url;
            if (SAFE_URL_PATHS.hasProtocol(urlString))
            {
                url = new URL(urlString);
            }
            else
            {
                // No protocol: assume this URL is relative to the root URL.
                url = new URL(SAFE_URL_PATHS.getUrl(SAFE_URL_PATHS.getString(rootUrl.toString(), urlString)));
            }

            return url;
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Transform a string containing a URL into a RELATIVE path that identifies a
     * download location for the file referenced by the URL. The argument is first
     * converted to a URL object in the same way as described in the getUrl(...)
     * method. Then, if the URL starts with this manager's root URL, the root URL is
     * trimmed off and the rest of the string is converted into a path. If the
     * constructed URL does not fall under this manager's root URL, then the domain
     * will be trimmed and the rest of the string returned.
     * <p>
     * For example, if the root URL were "http://mydomain.com/mytool/data", the
     * strings "/path/to/my/file.txt" and
     * "http://some.other.domain.com/path/to/my/file.txt" would both be converted to
     * the path "path/to/my/file.txt". This means two different source URLs could
     * both download to the same local path.
     * <p>
     * Note that the Windows-style path "C:\Users\myName\file.txt" would be
     * converted to "Users\myName\file.txt", that is, the drive letter and colon
     * following the drive letter would be dropped in order for the resulting path
     * to be a valid RELATIVE path on the Windows operating system.
     * <p>
     * This method never queries the server, so there is no guarantee that the URL
     * exists or is accessible, only that it is syntactically correct.
     * 
     * @param urlString URL string, either absolute or relative to the root URL
     * @return the Path object
     * @throws IllegalArgumentException if the URL string argument is malformed
     */
    public Path getDownloadPath(String urlString)
    {
        return getDownloadPath(getUrl(urlString));
    }

    public Path getDownloadPath(URL url)
    {
        Preconditions.checkNotNull(url);

        URL rootUrl = getRootUrl();

        // Get the root path from the root URL, and the other path from the supplied
        // URL. Include an extra slash in the root URL to be sure it ends with slash.
        // SafeUrlPaths will remove duplicate slashes.
        String rootPathString = SAFE_URL_PATHS.getString(rootUrl.toString() + "/");
        String urlPathString = SAFE_URL_PATHS.getString(url.toString());

        // Construct the relative path by removing the root path from front of the URL
        // path if it matches.
        String pathString;
        boolean relativize = false;
        int rootLength = rootPathString.length();
        if (urlPathString.length() >= rootLength && urlPathString.substring(0, rootLength).equalsIgnoreCase(rootPathString))
        {
            pathString = urlPathString.substring(rootLength);
            relativize = true;
        }
        else
        {
            // The supplied URL is not under the root URL hierarchy, so extract the full
            // path from the url.
            pathString = SAFE_URL_PATHS.getString(url.getPath().toString());

            // File protocol URLs may need special handling.
            if (SAFE_URL_PATHS.hasFileProtocol(urlPathString))
            {
                // Files that are (g)zipped will need to be (g)unzipped without messing with the
                // original file or its parent, so treat these as relative paths.
                if (pathString.matches(".*\\.[([gG][zZ])([zZ][iI][pP])]$"))
                {
                    relativize = true;
                }
            }
        }

        if (relativize)
        {
            // Clean up the relative path.
            // Trim off leading slash or backslash to relativize this path.
            pathString = pathString.replaceFirst("^[/\\\\]+", "");

            // Deal with the colon in Windows paths.
            pathString = pathString.replaceFirst("^(\\w):[/\\\\]*", "\1/");

            // This is not ideal. UrlDownloader automatically
            // gunzips files if they end with .gz, but it would be better not to couple
            // this code to that (current) implementation detail. Zip files are unzipped
            // after download, so don't strip the .zip suffix here.
            pathString = pathString.replaceFirst("\\.[gG][zZ]$", "");
        }

        return SAFE_URL_PATHS.get(pathString);
    }

    public UrlInfo getInfo(String urlString)
    {
        return getInfo(getUrl(urlString));
    }

    /**
     * Looks up available information but does not query the server.
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public UrlInfo getInfo(URL url)
    {
        Preconditions.checkNotNull(url);

        // Retrieve a cached URLInfo object, or else add one to the cache.
        UrlInfo result = urlInfoCache.get(url);
        if (result == null)
        {
            result = UrlInfo.of(url);
            urlInfoCache.put(url, result);
        }

        return result;
    }

    /**
     * Return information associated with the URL identified by the first argument.
     * The argument is first converted to a URL object in the same way as described
     * in the getUrl(...) method. Then a connection *may* be opened to the URL to
     * obtain the necessary information. This can take a long time to run and/or
     * time-out, so be careful calling this method from a thread where this can
     * impact performance.
     * <p>
     * More specifically, this method attempts to open a connection and obtain
     * information if server access is enabled (see the setServerAccessEnabled(...)
     * method) AND one of the following two conditions holds:
     * <p>
     * 1. This method has not already successfully opened a connection to the URL
     * identified by the URL string argument in the currently running instance,
     * and/or:
     * <p>
     * 2. The forceUpdate argument is true.
     * <p>
     * The information obtained from a successful connection is cached so that
     * subsequent calls will run more quickly (though the information is not
     * refreshed in that case).
     * <p>
     * No connections are made whenever server access is disabled. See the
     * {@link UrlInfo} class for more details about the information it provides when
     * there is no server access.
     * 
     * @param urlString URL string, either absolute or relative to the root URL
     * @param forceUpdate if true, the server will be queried and the cached
     *            information updated. If false, this will only happen the first
     *            time for each URL.
     * @return the information object
     * @throws IllegalArgumentException if the URL string argument is malformed
     */
    public UrlInfo queryServer(String urlString, boolean forceUpdate)
    {
        return queryServer(getUrl(urlString), forceUpdate);
    }

    public UrlInfo queryServer(URL url, boolean forceUpdate)
    {
        Preconditions.checkNotNull(url);

        UrlInfo result = getInfo(url);

        if (isServerAccessEnabled() || forceUpdate)
        {
            UrlState state = result.getState();
            if (state.getStatus() == UrlStatus.UNKNOWN || forceUpdate)
            {
                try (CloseableUrlConnection connection = CloseableUrlConnection.of(result, HttpRequestMethod.HEAD))
                {
                    Debug.out().println("Querying server about " + url);
                    result.update(connection.getConnection());
                }
                catch (@SuppressWarnings("unused") Exception ignored)
                {
                    result.update(UrlState.of(url));
                }
            }
        }

        return result;
    }

    @Override
    public String toString()
    {
        return "UrlAccessManager(" + rootUrl + ")";
    }

    public static void main(String[] args)
    {
        Debug.setEnabled(true);
        boolean checkStuff = false;
        if (checkStuff)
        {
            UrlAccessManager manager;
            try
            {
                manager = UrlAccessManager.of(new URL("http://spud.com"));
                System.err.println(manager.getDownloadPath("/absolute/looking/path"));
                System.err.println(manager.getDownloadPath("\\absolute\\looking\\hinky\\windows\\path"));
                System.err.println(manager.getDownloadPath("c:\\absolute\\looking\\windows\\path"));
                System.err.println(manager.getDownloadPath("relative/looking/path"));
                System.err.println(manager.getDownloadPath("relative\\looking\\windows\\path"));
                System.err.println(manager.getDownloadPath("file://spud.com/complete/url/path"));
                System.err.println(manager.getDownloadPath("http://spUD.com/complete/url/path"));
                System.err.println(manager.getDownloadPath("http://spud.com/c:/windows/url/path"));
                System.err.println(manager.getDownloadPath("http://spud.com/c:\\windows\\native\\path"));
            }
            catch (MalformedURLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
        try
        {
            System.out.println("Setting up remote test cache");
            UrlAccessManager remoteTestCache = UrlAccessManager.of(Configuration.getDataRootURL());

            System.out.println("\nRunning remote tests the first time");
            runRemoteTests(remoteTestCache);

            System.out.println("\nRunning remote tests the second time with server access disabled (should see no connections)");
            remoteTestCache.setEnableServerAccess(false);
            runRemoteTests(remoteTestCache);
            remoteTestCache.setEnableServerAccess(true);

            System.out.println("\nRunning remote tests the third time (should see one connection)");
            runRemoteTests(remoteTestCache);

            System.out.println("\nTry setting up a manager using an invalid root url");

            System.out.println("\nSetting up a manager for a local file URL");
            String cacheDir = Configuration.getCacheDir();
            UrlAccessManager localTestCache = UrlAccessManager.of(new URL(SAFE_URL_PATHS.getUrl(cacheDir)));

            System.out.println("\nRunning local tests first time");
            runLocalTests(localTestCache);

            System.out.println("\nRunning local tests second time");
            runLocalTests(localTestCache);

            System.out.println("Done");
        }
        catch (IOException e)
        {
            System.err.println("Test set-up failed");
            e.printStackTrace();
        }

    }

    private static void runRemoteTests(UrlAccessManager testCache)
    {
        URL rootUrl = testCache.getRootUrl();
        String host = rootUrl.getProtocol() + "://" + rootUrl.getHost();

        testCache.testGetInfo(host, UrlStatus.ACCESSIBLE, 0l, null);
        testCache.testGetInfo(host + "/sbmt/prod/help/sbmt-linux-x64.zip", UrlStatus.ACCESSIBLE, 1521665260000l, null);
        testCache.testGetInfo(host + "/sbmt/prod/help/index.php", UrlStatus.ACCESSIBLE, 0l, null);
        testCache.testGetInfo(host + "/sbmt/prod/data/bennu/altwg-spc-v20181217", UrlStatus.NOT_AUTHORIZED, 0l, null);
        testCache.testGetInfo("\\bennu\\altwg-spc-v20181217", UrlStatus.NOT_AUTHORIZED, 0l, null);
        testCache.testGetInfo(host + "/non-existent-url", UrlStatus.HTTP_ERROR, 0l, FileNotFoundException.class);
        testCache.testGetInfo("http://sbmt.jhuBOZOapl.edu", UrlStatus.UNKNOWN, 0l, IOException.class);
    }

    private static void runLocalTests(UrlAccessManager testCache) throws IOException
    {
        URL rootUrl = testCache.getRootUrl();
        String host = rootUrl.getProtocol() + "://" + rootUrl.getHost();
        String path = rootUrl.getPath();

        String testUrlString;
        File testFile;

        testUrlString = host;
        testCache.testGetInfo(testUrlString, UrlStatus.ACCESSIBLE, null, null);
        testFile = SAFE_URL_PATHS.get(testCache.getUrl(testUrlString).getPath()).toFile();
        System.out.println("URL " + testUrlString + " = file " + testFile + "; exists? " + testFile.exists());

        testUrlString = host + path;
        testCache.testGetInfo(testUrlString, UrlStatus.ACCESSIBLE, null, null);
        testFile = SAFE_URL_PATHS.get(testCache.getUrl(testUrlString).getPath()).toFile();
        System.out.println("URL " + testUrlString + " = file " + testFile + "; exists? " + testFile.exists());

        testUrlString = host + "/non-existent-path";
        testCache.testGetInfo(testUrlString, UrlStatus.ACCESSIBLE, null, null);
        testFile = SAFE_URL_PATHS.get(testCache.getUrl(testUrlString).getPath()).toFile();
        System.out.println("URL " + testUrlString + " = file " + testFile + "; exists? " + testFile.exists());

        File file = createTestFile(rootUrl.getPath());

        testUrlString = host + file.getAbsolutePath();
        testCache.testGetInfo(testUrlString, UrlStatus.ACCESSIBLE, null, null);
        testFile = SAFE_URL_PATHS.get(testCache.getUrl(testUrlString).getPath()).toFile();
        System.out.println("URL " + testUrlString + " = file " + testFile + "; exists? " + testFile.exists());
    }

    private static File createTestFile(String parentString) throws IOException
    {
        UUID uuid = UUID.randomUUID();

        File result = new File(SAFE_URL_PATHS.getString(parentString, uuid.toString()));
        result.deleteOnExit();

        result.getParentFile().mkdirs();
        result.createNewFile();

        return result;
    }

    private void testGetInfo(String urlString, UrlStatus expectedStatus, Long expectedModificationTime, Class<?> expectedException)
    {
        try
        {
            UrlState info = queryServer(urlString, false).getState();

            if (info.getStatus() != expectedStatus)
            {
                System.err.println("testInfo FAILED for " + urlString + ": got status " + info.getStatus() + ", not " + expectedStatus);
            }
            if (expectedModificationTime != null && !expectedModificationTime.equals(info.getLastModified()))
            {
                System.err.println("testInfo FAILED for " + urlString + ": got modification time " + info.getLastModified() + ", not " + expectedModificationTime);
            }
        }
        catch (Exception e)
        {
            if (expectedException != null)
            {
                if (!expectedException.isAssignableFrom(e.getClass()))
                {
                    System.err.println("testInfo FAILED for " + urlString + ": unexpected exception type " + e.getClass());
                    e.printStackTrace();
                }
            }
        }
    }

}
