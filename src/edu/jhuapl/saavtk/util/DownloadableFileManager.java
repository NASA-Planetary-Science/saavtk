package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.util.DownloadableFileInfo.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public class DownloadableFileManager
{
    private static Boolean headless = null;
    private static volatile boolean silenceInfoMessages = false;

    public interface StateListener
    {
        void respond(DownloadableFileState fileState);
    }

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static DownloadableFileManager of(URL rootUrl, File cacheDir)
    {
        Preconditions.checkNotNull(rootUrl);
        Preconditions.checkNotNull(cacheDir);

        UrlAccessManager urlManager = UrlAccessManager.of(rootUrl);
        FileAccessManager fileManager = createFileCacheManager(cacheDir);

        return of(urlManager, fileManager);
    }

    public static DownloadableFileManager of(UrlAccessManager urlManager, FileAccessManager fileManager)
    {
        return new DownloadableFileManager(urlManager, fileManager);
    }

    public static void setSilenceInfoMessages(boolean enable)
    {
        silenceInfoMessages = enable;
    }

    private final UrlAccessManager urlManager;
    private final FileAccessManager fileManager;
    private final ConcurrentMap<String, DownloadableFileInfo> downloadInfoCache;
    private final Map<String, Map<StateListener, PropertyChangeListener>> listenerMap;
    private final ExecutorService accessMonitor;
    private volatile Boolean enableMonitor;
    private volatile long sleepInterval;
    private volatile boolean disableAccessChecksOnServer;

    protected DownloadableFileManager(UrlAccessManager urlManager, FileAccessManager fileManager)
    {
        this.urlManager = urlManager;
        this.fileManager = fileManager;
        this.downloadInfoCache = new ConcurrentHashMap<>();
        this.listenerMap = new HashMap<>();
        this.accessMonitor = Executors.newCachedThreadPool();
        this.enableMonitor = Boolean.FALSE;
        this.sleepInterval = 10000;
        this.disableAccessChecksOnServer = false;
    }

    public synchronized void startAccessMonitor()
    {
        if (!enableMonitor)
        {
            enableMonitor = true;

            accessMonitor.execute(() -> {
                while (enableMonitor)
                {
                    boolean initiallyEnabled = urlManager.isServerAccessEnabled();
                    Exception exception = null;
                    try
                    {
                        urlManager.queryRootUrl().getState().getUrl();
                    }
                    catch (Exception e)
                    {
                        exception = e;
                    }

                    boolean currentlyEnabled = urlManager.isServerAccessEnabled();

                    boolean forceUpdate = initiallyEnabled != currentlyEnabled;

                    if (forceUpdate)
                    {
                        if (!silenceInfoMessages)
                        {
                            System.out.println(currentlyEnabled ? //
                            "Connected to server. Re-enabling online access." : //
                            "Failed to connect to server. Disabling online access for now.");
                        }
                        if (exception != null)
                        {
                            exception.printStackTrace(Debug.err());
                        }
                    }

                    if (disableAccessChecksOnServer || !doAccessCheckOnServer(forceUpdate))
                    {
//                        Debug.err().println("URL status check on server failed; falling back to file-by-file check");
                        queryAll(forceUpdate);
                    }

                    try
                    {
                        Thread.sleep(sleepInterval);
                    }
                    catch (InterruptedException ignored)
                    {

                    }
                }

            });
        }
    }

    /**
     * Attempt to use a server-side script to check accessibility of all the URLs
     * known to this manager.
     * 
     * This implementation iterates through the whole collection of URLs, sending
     * them in batches to the server-side script. This is for two reasons: 1) the
     * server has a limit on the size of string that can be passed and 2) it is
     * useful to get items in batches to avoid all-or-nothing checks.
     * 
     * @param forceUpdate force FileInfo portion of the update. The server-side
     *            update (UrlInfo) is performed in any case.
     * 
     * @return true if all the access checks succeeded, false if any checks failed.
     *         Note this is checking whether the checks themselves succeeeded, not
     *         whether or not the checked URLs are accessible.
     */
    protected boolean doAccessCheckOnServer(boolean forceUpdate)
    {
        boolean result = true;

        String checkFileAccessScriptName = SafeURLPaths.instance().getString(Configuration.getQueryRootURL(), "checkfileaccess.php");
        URL getUserAccessPhp;
        try
        {
            getUserAccessPhp = new URL(checkFileAccessScriptName);
        }
        catch (MalformedURLException e)
        {
            throw new AssertionError(e);
        }

        Set<String> urlSet;
        synchronized (this.downloadInfoCache)
        {
            urlSet = ImmutableSet.copyOf(downloadInfoCache.keySet());
        }

        Iterator<String> iterator = urlSet.iterator();
        while (iterator.hasNext())
        {
            if (!doAccessCheckOnServer(getUserAccessPhp, iterator, 32, forceUpdate))
            {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * Iterate over the provided {@link #iterator} of URLs as Strings. For each, use
     * the {@link #getUserAccessPhp} script to check accessibility. Check at most
     * {@link #maximumQueryCount} URLs.
     * 
     * @param getUserAccessPhp the URL of the server-side script for performing the
     *            check
     * @param iterator that traverses the URLs (as strings) to check
     * @param maximumQueryCount the maximum number of URLs that will be checked in a
     *            single call to the script
     * @param forceUpdate force FileInfo portion of the update. The server-side
     *            update (UrlInfo) is performed in any case.
     * 
     * @return true if the access check succeeded, false if not. Note this indicates
     *         whether the check itself succeeeded, not whether or not the checked
     *         URLs are accessible.
     * 
     */
    protected boolean doAccessCheckOnServer(URL getUserAccessPhp, Iterator<String> iterator, int maximumQueryCount, boolean forceUpdate)
    {
        boolean result = false;

        URLConnection conn = null;
        try
        {
            conn = getUserAccessPhp.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0");

            // Make query string that contains all the URLs currently in the cache.
            try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()))
            {
                URL rootUrl = Configuration.getRootURL();
                String rootUrlString = rootUrl.toString();
                URL dataRootUrl = Configuration.getDataRootURL();
                String dataRootUrlString = dataRootUrl.toString();

                StringBuilder sb = new StringBuilder();
                sb.append("rootURL=").append(rootUrlString);
                sb.append("&userName=").append(Configuration.getUserName());
                sb.append("&password=").append(Configuration.getPassword());
                sb.append("&args=");
                sb.append("&stdin=");

                boolean first = true;
                for (int index = 0; index < maximumQueryCount; ++index)
                {
                    if (!iterator.hasNext())
                    {
                        break;
                    }
                    // Encode colons as pipes. This is to get the query string through the web
                    // server, which rejects queries containing colons.
                    String url = iterator.next().replaceFirst(dataRootUrlString, "").replace(":", "|");
                    if (!url.matches(".*\\S.*"))
                    {
                        continue;
                    }

                    if (!first)
                    {
                        sb.append("\n");
                    }
                    first = false;

                    sb.append(url);
                }
                wr.write(sb.toString());
                wr.flush();
            }

            try (InputStreamReader isr = new InputStreamReader(conn.getInputStream()))
            {
                BufferedReader in = new BufferedReader(isr);
                String line;
                while ((line = in.readLine()) != null)
                {
                    if (line.matches(("^<html>.*Request Rejected.*")))
                    {
                        throw new IOException("Request for file was rejected by the server.");
                    }
                    String[] splitLine = line.split(",");
                    if (splitLine.length > 3)
                    {
                        String urlString = splitLine[0];
                        UrlStatus status = UrlStatus.valueOf(splitLine[1]);
                        long contentLength = Long.parseLong(splitLine[2]);
                        long lastModified = Long.parseLong(splitLine[3]);

                        // Update UrlInfo aspect.
                        UrlInfo urlInfo = urlManager.getInfo(urlString);
                        urlInfo.update(status, contentLength, lastModified);

                        // Update FileInfo aspect.
                        DownloadableFileState state = getState(urlString);
                        FileState fileState = state.getFileState();
                        if (forceUpdate || fileState.getStatus().equals(FileStatus.UNKNOWN))
                        {
                            FileInfo fileInfo = fileManager.getInfo(fileState.getFile());
                            fileInfo.update();
                        }
                    }
                }
            }
            result = true;
//            Debug.err().println("Successfully ran URL status check on server");
        }
        catch (IOException e)
        {
            Debug.err().println("Exception perofmring server-side check: could not open connection to " + getUserAccessPhp);
            e.printStackTrace(Debug.err());
        }
        finally
        {
            if (conn instanceof HttpURLConnection)
            {
                ((HttpURLConnection) conn).disconnect();
            }
        }

        return result;
    }

    public synchronized void stopAccessMonitor()
    {
        enableMonitor = false;
    }

    public boolean isServerAccessEnabled()
    {
        return urlManager.isServerAccessEnabled();
    }

    public void setEnableServerAccess(boolean enableServerAccess)
    {
        urlManager.setEnableServerAccess(enableServerAccess);
    }

    public DownloadableFileInfo getRootInfo()
    {
        return getInfo(urlManager.getRootUrl());
    }

    public boolean isAccessible(String urlString)
    {
        return query(urlString, false).isAccessible();
    }

    public URL getUrl(String urlString)
    {
        return urlManager.getUrl(urlString);
    }

    public File getFile(String urlString)
    {
        return getInfo(getUrl(urlString)).getState().getFileState().getFile();
    }

    public DownloadableFileState getState(String urlString)
    {
        return getInfo(urlManager.getUrl(urlString)).getState();
    }

    public DownloadableFileAccessQuerier getQuerier(String urlString, boolean forceUpdate)
    {
        Preconditions.checkNotNull(urlString);

        UrlInfo urlInfo = urlManager.getInfo(urlString);

        DownloadableFileState state = getState(urlString);
        File file = state.getFileState().getFile();
        FileInfo fileInfo = fileManager.getInfo(file);

        return DownloadableFileAccessQuerier.of(urlInfo, fileInfo, forceUpdate, urlManager.isServerAccessEnabled());
    }

    public DownloadableFileState query(String urlString, boolean forceUpdate)
    {
        try
        {
            return doQuery(urlManager.getUrl(urlString), forceUpdate).getState();
        }
        catch (Exception e)
        {
            System.err.println("Problem querying server about " + urlString);
            e.printStackTrace();
        }

        return getState(urlString);
    }

    public void queryAll(boolean forceUpdate)
    {
        URL rootUrl = urlManager.getRootUrl();
        String rootUrlString = rootUrl.toString();

        Set<String> urlSet;
        synchronized (this.downloadInfoCache)
        {
            urlSet = ImmutableSet.copyOf(downloadInfoCache.keySet());
        }

        for (String urlString : urlSet)
        {
            if (urlString.equals(rootUrlString))
            {
                // Do not check the root URL itself.
                continue;
            }

            boolean unknownHost = false;
            boolean doCheck = true;
            int maximumNumberTries = 3;
            for (int index = 0; index < maximumNumberTries && doCheck; ++index)
            {
                try
                {
                    URL url = urlManager.getUrl(urlString);
                    doQuery(url, forceUpdate);
                    doCheck = false;
                }
                catch (SocketException ignored)
                {
                    Debug.err().println("SocketException on " + urlString);
                }
                catch (SocketTimeoutException ignored)
                {
                    Debug.err().println("Timeout on " + urlString);
                }
                catch (UnknownHostException ignored)
                {
                    unknownHost = true;
                    doCheck = false;
                    Debug.err().println("Unknown host exception on " + urlString);
                }
                catch (Exception e)
                {
                    doCheck = false;
                    e.printStackTrace(Debug.err());
                }

                if (doCheck)
                {
                    // 50 seconds is the result of heuristic tuning. It seems to be a good length of
                    // time to wait before trying again to get info about the URL. Much less
                    // time and two back-to-back time-outs become likely (which effectively doubles
                    // the pause length). On the other hand, pausing longer than 50 s does not
                    // seem to reduce the number of timeouts, thus 50 seems to be the "sweet spot".
                    try
                    {
                        Debug.err().println("Pausing before retrying " + urlString);
                        Thread.sleep(50000);
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore.
                    }
                }
            }

            // If host is unknown, stop trying to check accessibility.
            if (unknownHost)
            {
                break;
            }
        }
    }

    public void queryAllInBackground(boolean forceUpdate)
    {
        accessMonitor.execute(() -> {
            queryAll(forceUpdate);
        });
    }

    public FileDownloader getDownloader(String urlString, boolean forceDownload)
    {
        Preconditions.checkNotNull(urlString);

        URL url = urlManager.getUrl(urlString);
        UrlInfo urlInfo = urlManager.getInfo(urlString);
        Path downloadPath = urlManager.getDownloadPath(url);

        FileInfo fileInfo = fileManager.getInfo(downloadPath);

        return FileDownloader.of(urlInfo, fileInfo, forceDownload);
    }

    public DownloadableFileState getDownloadedFile(String urlString, boolean forceDownload) throws IOException, InterruptedException
    {
        Preconditions.checkNotNull(urlString);

        DownloadableFileState fileState = getState(urlString);

        if (urlManager.isServerAccessEnabled())
        {
            FileDownloader downloader = getDownloader(urlString, forceDownload);
            downloader.downloadAndUnzip();
            fileState = getState(urlString);
        }

        return fileState;
    }

    public void getDownloadedFile(String urlString, StateListener whenFinished, boolean forceDownload)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(whenFinished);

        DownloadableFileState fileState = getState(urlString);

        if (urlManager.isServerAccessEnabled())
        {
            FileDownloader downloader = getDownloader(urlString, forceDownload);

            downloader.addPropertyChangeListener(e -> {
                String propertyName = e.getPropertyName();
                if (propertyName.equals(FileDownloader.DOWNLOAD_DONE) || propertyName.equals(FileDownloader.DOWNLOAD_CANCELED))
                {
                    // Either way, respond to the state change, if any.
                    whenFinished.respond((DownloadableFileState) e.getNewValue());
                }
            });

            THREAD_POOL.execute(downloader);
        }
        else
        {
            whenFinished.respond(fileState);
        }
    }

    public void addStateListener(String urlString, StateListener listener)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(listener);

        URL url = urlManager.getUrl(urlString);

        // Reset the string so that it has a canonically consistent value no matter how
        // messy the supplied argument was.
        urlString = url.toString();

        DownloadableFileInfo info = getInfo(url);

        synchronized (this.listenerMap)
        {
            Map<StateListener, PropertyChangeListener> propertyListenerMap = listenerMap.get(urlString);
            if (propertyListenerMap == null)
            {
                propertyListenerMap = new HashMap<>();
                listenerMap.put(urlString, propertyListenerMap);
            }

            if (!propertyListenerMap.containsKey(listener))
            {
                PropertyChangeListener propertyListener = e -> {
                    if (e.getPropertyName().equals(DownloadableFileInfo.STATE_PROPERTY))
                    {
                        if (isHeadless())
                        {
                            listener.respond((DownloadableFileState) e.getNewValue());
                        }
                        else
                        {
                            EventQueue.invokeLater(() -> {
                                listener.respond((DownloadableFileState) e.getNewValue());
                            });
                        }
                    }
                };
                propertyListenerMap.put(listener, propertyListener);
                info.addPropertyChangeListener(propertyListener);

                // Immediately "fire" just the newly added listener.
                if (isHeadless())
                {
                    listener.respond(info.getState());
                }
                else
                {
                    EventQueue.invokeLater(() -> {
                        listener.respond(info.getState());
                    });
                }
            }
        }
    }

    public void removeStateListener(String urlString, StateListener listener)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(listener);

        URL url = urlManager.getUrl(urlString);

        // Reset the string so that it has a canonically consistent value no matter how
        // messy the supplied argument was.
        urlString = url.toString();

        DownloadableFileInfo info = getInfo(url);

        synchronized (this.listenerMap)
        {
            Map<StateListener, PropertyChangeListener> propertyListenerMap = listenerMap.get(urlString);
            if (propertyListenerMap != null)
            {
                PropertyChangeListener propertyListener = propertyListenerMap.get(listener);
                if (propertyListener != null)
                {
                    info.removePropertyChangeListener(propertyListener);
                    propertyListenerMap.remove(listener);
                }
            }
        }
    }

    protected DownloadableFileInfo getInfo(URL url)
    {
        DownloadableFileInfo result;
        String urlString = url.toString();
        synchronized (this.downloadInfoCache)
        {
            result = downloadInfoCache.get(urlString);
            if (result == null)
            {
                UrlInfo urlInfo = urlManager.getInfo(url);
                Path downloadPath = urlManager.getDownloadPath(url);
                FileInfo fileInfo = fileManager.getInfo(downloadPath.toString());

                File file = fileInfo.getState().getFile();

                final DownloadableFileInfo downloadableInfo = DownloadableFileInfo.of(url, file);

                urlInfo.addPropertyChangeListener(e -> {
                    if (e.getPropertyName().equals(UrlInfo.STATE_PROPERTY))
                    {
                        if (isHeadless())
                        {
                            downloadableInfo.update((UrlState) e.getNewValue());
                        }
                        else
                        {
                            EventQueue.invokeLater(() -> {
                                downloadableInfo.update((UrlState) e.getNewValue());
                            });
                        }
                    }
                });

                fileInfo.addPropertyChangeListener(e -> {
                    if (e.getPropertyName().equals(FileInfo.STATE_PROPERTY))
                    {
                        if (isHeadless())
                        {
                            downloadableInfo.update((FileState) e.getNewValue());
                        }
                        else
                        {
                            EventQueue.invokeLater(() -> {
                                downloadableInfo.update((FileState) e.getNewValue());
                            });
                        }
                    }
                });

                result = downloadableInfo;

                downloadInfoCache.put(urlString, result);
            }
        }

        return result;
    }

    protected DownloadableFileInfo doQuery(URL url, boolean forceUpdate) throws IOException
    {
        Preconditions.checkNotNull(url);

        DownloadableFileInfo result = getInfo(url);

        DownloadableFileAccessQuerier querier = getQuerier(url.toString(), forceUpdate);
        querier.query();

        result.update(querier.getDownloadableFileState());

        return result;
    }

    protected static boolean isHeadless()
    {
        if (headless == null)
        {
            headless = Boolean.parseBoolean(System.getProperty("java.awt.headless"));
        }

        return headless;
    }

    private static FileAccessManager createFileCacheManager(File cacheDir)
    {
        FileAccessManager result = null;
        try
        {
            result = FileAccessManager.of(cacheDir);
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }

        return result;
    }

    public static void main(String[] args)
    {
        try
        {
            URL getUserAccessPhp = new URL(Configuration.getQueryRootURL() + "/" + "checkfileaccess.php");
            URLConnection conn = null;
            try
            {
                conn = getUserAccessPhp.openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestProperty("User-Agent", "Mozilla/4.0");
                try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()))
                {
                    wr.write("rootURL=http://sbmt.jhuapl.edu/sbmt/prod&userName=sbmt-test&password=wide-open&args=&stdin=/prometheus/gaskell/Gaskell_Prometheus_v7.8.json\n/GASKELL/EROS/Gaskell_433_Eros_v7.8.json");
                    wr.flush();
                }

                try (InputStreamReader isr = new InputStreamReader(conn.getInputStream()))
                {
                    BufferedReader in = new BufferedReader(isr);
                    String line;
                    while ((line = in.readLine()) != null)
                    {
                        System.out.println(line);
                    }

                }
            }
            finally
            {
                if (conn instanceof HttpURLConnection)
                {
                    ((HttpURLConnection) conn).disconnect();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
