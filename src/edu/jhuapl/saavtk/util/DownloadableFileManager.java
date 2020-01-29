package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;
import edu.jhuapl.saavtk.util.DownloadableFileInfo.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public class DownloadableFileManager
{
    private static final String UrlEncoding = "UTF-8";
    private static volatile Boolean headless = null;
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

    public static String getURLEncoding()
    {
        return UrlEncoding;
    }

    private final UrlAccessManager urlManager;
    private final FileAccessManager fileManager;
    private final Map<String, DownloadableFileInfo> downloadInfoCache;
    private final List<String> urlList;
    private final Map<String, Map<StateListener, PropertyChangeListener>> listenerMap;
    private final ExecutorService accessMonitor;
    private volatile boolean enableMonitor;
    private volatile long sleepIntervalBetweenChecks;
    private final AtomicBoolean enableAccessChecksOnServer;
    private volatile int maximumQueryLength;
    private final AtomicInteger consecutiveServerSideCheckExceptionCount;
    private static final int maximumConsecutiveServerSideCheckExceptions = 1;

    protected DownloadableFileManager(UrlAccessManager urlManager, FileAccessManager fileManager)
    {
        this.urlManager = urlManager;
        this.fileManager = fileManager;
        this.downloadInfoCache = new ConcurrentHashMap<>();
        this.urlList = new LinkedList<>();
        this.listenerMap = new HashMap<>();
        this.accessMonitor = Executors.newCachedThreadPool();
        this.enableMonitor = false;
        this.sleepIntervalBetweenChecks = 5000;
        // Currently no option to change this field through a method call; this is just
        // for debugging:
        this.enableAccessChecksOnServer = new AtomicBoolean(Boolean.TRUE);
        this.maximumQueryLength = 10000; // Set to match the limit imposed by the web server.
        this.consecutiveServerSideCheckExceptionCount = new AtomicInteger();
    }

    /**
     * The access monitor runs on a dedicated background thread continually to check
     * accessibility of all the URLs managed by this manager. Checks are performed
     * in one of two ways:
     * 
     * 1. Using the {@link #queryAll()} method, which queries the server URL-by-URL
     * for the accessibility of each one. The speed and robustness of these checks
     * is limited by the quality of the internet connection and may be impacted by
     * server load levels.
     * 
     * 2. Using a server-side script, checkfileaccess.php, which also uses the
     * {@link #queryAll()} method, but runs the checks in batches of URLs on the
     * server. This is faster and more reliable because it makes the many URL-by-URL
     * connections from the server to the server.
     */
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
                        urlManager.queryRootUrl();
                    }
                    catch (Exception e)
                    {
                        exception = e;
                    }

                    boolean currentlyEnabled = urlManager.isServerAccessEnabled();

                    boolean forceUpdate = initiallyEnabled != currentlyEnabled;

                    // Maybe print diagnotic info.
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

                    try
                    {
                        if (consecutiveServerSideCheckExceptionCount.get() >= maximumConsecutiveServerSideCheckExceptions)
                        {
                            Debug.err().println("URL status check on server failed too many times. Falling back to file-by-file check.");
                            enableAccessChecksOnServer.set(false);
                        }

                        // Only call doAccessCheckOnServer if web access is currently enabled.
                        if (enableAccessChecksOnServer.get() && currentlyEnabled)
                        {
                            if (!doAccessCheckOnServer(forceUpdate))
                            {
                                Debug.err().println("URL status check on server did not complete. Falling back to file-by-file check.");
                                queryAll(forceUpdate);
                            }
                        }
                        else
                        {
                            // Safe and necessary to call this, even if server access is currently disabled,
                            // because this method will skip URL checks but still perform file-system
                            // accessibility checks.
                            queryAll(forceUpdate);
                        }

                        consecutiveServerSideCheckExceptionCount.set(0);
                        enableAccessChecksOnServer.set(true);
                    }
                    catch (Exception e)
                    {
                        // Probably this indicates a problem with the internet connection. This will be
                        // tested the next time the loop executes.
                        e.printStackTrace(Debug.err());
                        consecutiveServerSideCheckExceptionCount.incrementAndGet();
                    }

                    try
                    {
                        Thread.sleep(sleepIntervalBetweenChecks);
                    }
                    catch (InterruptedException ignored)
                    {
                        enableMonitor = false;
                    }
                }

            });
        }
    }

    /**
     * Attempt to use a server-side script to check accessibility of all the URLs
     * known to this manager.
     * <p>
     * This implementation iterates through the whole collection of URLs, sending
     * them in batches to the server-side script. This is for two reasons: 1) the
     * server has a limit on the size of string that can be passed and 2) it is
     * useful to get items in batches to avoid all-or-nothing checks.
     * <p>
     * VERY IMPORTANT: this method needs to be kept in synch with the way clients
     * work to accept the queries, run them on the server and return the results. In
     * particular, this method expects the server to have a script named
     * "checkfileaccess.php" in the query root directory.
     * 
     * @param forceUpdate force FileInfo portion of the update. The server-side
     *            update (UrlInfo) is performed in any case.
     * 
     * @return true if all the access checks succeeded, false if any checks failed.
     *         Note this is checking whether the checks themselves succeeeded, not
     *         whether or not the checked URLs are accessible.
     * @throws IOException if a connection cannot be opened to the server-side
     *             script
     */
    protected boolean doAccessCheckOnServer(boolean forceUpdate) throws IOException
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

        ImmutableList<String> urlList;
        synchronized (this.downloadInfoCache)
        {
            urlList = ImmutableList.copyOf(this.urlList);
        }

        ListIterator<String> iterator = urlList.listIterator();
        while (iterator.hasNext())
        {
            if (!doAccessCheckOnServer(getUserAccessPhp, iterator, forceUpdate))
            {
                result = false;
                break;
            }
        }
//        if (result)
//        {
//            Debug.err().println("Successfully ran URL status check on server");
//        }

        return result;
    }

    /**
     * Iterate over the provided {@link #iterator} of URLs as Strings. For each, use
     * the {@link #getUserAccessPhp} script to check accessibility. Check at most
     * {@link #maximumQueryCount} URLs.
     * <p>
     * VERY IMPORTANT: this method needs to be kept in synch with the way clients
     * work to accept the queries, run them on the server and return the results. In
     * particular, this method escapes HTTP code, which must be "unescaped" by the
     * script that checks the files on the server.
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
     * @throws IOException if a connection cannot be opened to the server-side
     *             script
     * 
     */
    protected boolean doAccessCheckOnServer(URL getUserAccessPhp, ListIterator<String> iterator, boolean forceUpdate) throws IOException
    {
        boolean result = true;

        try (CloseableUrlConnection closeableConn = CloseableUrlConnection.of(getUserAccessPhp, HttpRequestMethod.GET))
        {
            URLConnection conn = closeableConn.getConnection();
            conn.setDoOutput(true);
            // Make query string that contains a batch of URLs currently in the cache.
            String queryString;
            try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()))
            {
                URL rootUrl = Configuration.getRootURL();
                String rootUrlString = rootUrl.toString();
                URL dataRootUrl = Configuration.getDataRootURL();
                String dataRootUrlString = dataRootUrl.toString();

                StringBuilder sb = new StringBuilder();
                sb.append("rootURL=").append(rootUrlString);
                sb.append("&userName=").append(URLEncoder.encode(Configuration.getUserName(), getURLEncoding()));
                sb.append("&password=").append(URLEncoder.encode(String.valueOf(Configuration.getPassword()), getURLEncoding()));
                sb.append("&args=");
                sb.append("&stdin=");
//                sb = new StringBuilder(URLEncoder.encode(sb.toString(), getURLEncoding()));

                boolean first = true;
                while (iterator.hasNext())
                {
                    String url = iterator.next().replaceFirst(dataRootUrlString, "");
                    if (!url.matches(".*\\S.*"))
                    {
                        continue;
                    }
                    url = URLEncoder.encode(url, getURLEncoding());

                    // Make sure the maximum query length would not be exceeded with the current URL
                    // plus a newline. For purposes of ensuring this doesn't happen, newline
                    // is counted as two characters (CR/LF).
                    if ((sb.length() + url.length() + 2) >= maximumQueryLength)
                    {
                        // Move back one position so this URL is included in the next batch of queries.
                        // Then exit the loop so this query will go forward.
                        iterator.previous();
                        break;
                    }

                    if (!first)
                    {
                        sb.append("\n");
                    }
                    first = false;

                    sb.append(url);
                }
                queryString = sb.toString();
                wr.write(queryString);
                wr.flush();
            }

            // Now process the results of the query.
            try (InputStreamReader isr = new InputStreamReader(conn.getInputStream()))
            {
                BufferedReader in = new BufferedReader(isr);
                boolean someOutput = false;
                while (in.ready())
                {
                    someOutput = true;
                    String line = in.readLine();
                    if (line == null)
                    {
                        Debug.err().println("Server-side access check returned null");
                        result = false;
                        break;
                    }
                    else if (line.matches(("^<html>.*Request Rejected.*")))
                    {
                        Debug.err().println("Request for URL info was rejected by the server: " + queryString);
//                        Debug.err().println("Request for URL info was rejected by the server.");
                        result = false;
                        break;
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
                if (!someOutput)
                {
                    Debug.err().println("Server=side access check returned empty list");
                    result = false;
                }
            }
            catch (FileNotFoundException e)
            {
                Debug.err().println("Server-side access check failed");
                e.printStackTrace(Debug.err());
                result = false;
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
        queryAll(forceUpdate, 3, 50000);
    }

    /**
     * Query the server about the accessibility for all URLs/files tracked by this
     * manager. This opens a new connection for each such check, so it is
     * time-consuming and generates a lot of queries. This is best performed on a
     * background threa or on the server itself.
     * 
     * If server-side access is currrently disabled (most likely because of internet
     * connectivity problems), this method will skip ALL server queries rather than
     * try them all, thus generating many queries that are likely to fail.
     * 
     * This method also triggers file-system checks for the local accessibility of
     * cached files. It does these checks whether or not server-side access checking
     * is currently enabled. Thus this method is safe and beneficial to call
     * regardless of the state of the internet connection.
     * 
     * Because internet connections can be finicky, this method handles exceptions
     * in a specific way to reduce latency and improve odds of getting accurate
     * infomration for most URLs:
     * 
     * 1. If an {@link UnknownHostException} is thrown when checking any one URL, no
     * further URLs will be checked and the method will return. This exception is
     * interpreted to mean that there is no (reliable) internet connection.
     * 
     * 2. If a {@link SocketException} or {@link SocketTimeoutException} is thrown,
     * the method will pause for the number of milliseconds indicated by the
     * sleepInterval parameter before retrying the check. It will do this up to the
     * limit set by the maximumNumberTries parameter. After that it will go on to
     * the next URL.
     * 
     * 3. If any other {@link Exception} type is thrown when checking any URL, the
     * method will go on to check the next URL.
     * 
     * @param forceUpdate if true, forces both a server-side check of the URL and
     *            file-system check for the file, even if a previous check
     *            succeeded. This is useful for forcing a refresh of accessibility
     *            states. If false, checks will be performed only if a successful
     *            check was not previously performed.
     * @param maximumNumberTries the number of times to retry checks for URLs that
     *            failed because of socket/latency problems.
     * @param sleepIntervalAfterFailure the number of milliseconds to sleep
     *            following an unsuccessful URL check before retrying the check.
     */
    public void queryAll(boolean forceUpdate, int maximumNumberTries, int sleepIntervalAfterFailure)
    {
        URL rootUrl = urlManager.getRootUrl();
        String rootUrlString = rootUrl.toString();

        ImmutableList<String> urlList;
        synchronized (this.downloadInfoCache)
        {
            urlList = ImmutableList.copyOf(this.urlList);
        }

        for (String urlString : urlList)
        {
            if (urlString.equals(rootUrlString))
            {
                // Do not check the root URL itself.
                continue;
            }

            boolean unknownHost = false;
            boolean doCheck = true;
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
                        Thread.sleep(sleepIntervalAfterFailure);
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

                urlList.add(urlString);
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
            URL getUserAccessPhp = new URL("http://sbmt.jhuapl.edu/sbmtspud/prod/query/" + "checkfileaccess.php");
            try (CloseableUrlConnection closeableConn = CloseableUrlConnection.of(getUserAccessPhp, HttpRequestMethod.GET))
            {
                URLConnection conn = closeableConn.getConnection();
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
                    while (in.ready())
                    {
                        String line = in.readLine();
                        System.out.println(line);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
