package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;
import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.ServerSettingsManager.ServerSettings;

public class DownloadableFileManager
{
    private static final String UrlEncoding = "UTF-8";
    private static final SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final int SleepIntervalAfterLOC = 50000; // 50 sec., empirically good value.
    private static volatile Boolean headless = null;

    public interface StateListener
    {
        void respond(DownloadableFileState fileState);
    }

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

    public static String getURLEncoding()
    {
        return UrlEncoding;
    }

    /**
     * Set the prefix under which all profiling directories will be written. Call
     * this early, before any file-cache-related action has started. The first time
     * is called, the prefix will be set. Subsequent calls will be ignored.
     * 
     * @param prefix the prefix to use.
     */
    public static void setProfileAreaPrefix(String prefix)
    {
        profileAreaPrefix.compareAndSet(null, prefix);
    }

    private final UrlAccessManager urlManager;
    private final FileAccessManager fileManager;
    private final Map<String, Map<StateListener, PropertyChangeListener>> listenerMap;
    private final ExecutorService accessMonitor;
    private volatile boolean enableMonitor;
    private final AtomicBoolean enableAccessChecksOnServer;
    private final AtomicReference<URL> checkFileAccessScriptURL;
    private final AtomicInteger consecutiveFileNotFoundExceptionCount;
    private static final int maximumConsecutiveFileNotFoundExceptions = 2;
    private static final AtomicReference<String> profileAreaPrefix = new AtomicReference<>(null);
    private final AtomicReference<Profiler> checkProfiler;
    private final AtomicReference<Profiler> dropProfiler;

    protected DownloadableFileManager(UrlAccessManager urlManager, FileAccessManager fileManager)
    {
        this.urlManager = urlManager;
        this.fileManager = fileManager;
        this.listenerMap = new HashMap<>();
        this.accessMonitor = Executors.newCachedThreadPool();
        this.checkFileAccessScriptURL = new AtomicReference<>();
        this.enableMonitor = false;
        // Currently no option to change this field through a method call; this is just
        // for debugging:
        this.enableAccessChecksOnServer = new AtomicBoolean(true);
        this.consecutiveFileNotFoundExceptionCount = new AtomicInteger();
        this.checkProfiler = new AtomicReference<>();
        this.dropProfiler = new AtomicReference<>();
    }

    /**
     * The access monitor runs on a dedicated background thread continually to check
     * accessibility of all the URLs managed by this manager. Checks are performed
     * in one of two ways:
     * <p>
     * 1. Using the {@link #queryAll()} method, which queries the server URL-by-URL
     * for the accessibility of each one. The speed and robustness of these checks
     * is limited by the quality of the internet connection and may be impacted by
     * server load levels.
     * <p>
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

            Profiler checkProfiler = getCheckProfiler();
            Profiler dropProfiler = getDropProfiler();

            // Do not explicitly start checkProfiler. Want the checkProfiler to measure
            // time between subsequent checks.
            // checkProfiler.start();

            // Explicitly start the drop profiler as soon as profiling is enabled. Want the
            // drop checker to measure every drop, including the very first.
            // Note this will take effect only once: the first time this loop executes with
            // profiling enabled.
            dropProfiler.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                checkProfiler.summarizeAllPerformance("Times elapsed between successful server access checks");
                dropProfiler.summarizeAllPerformance("Times elapsed between connection status changes. First time is time of first DROPPED connection.");

                checkProfiler.deleteProfileArea();
                dropProfiler.deleteProfileArea();
            }));

            accessMonitor.execute(() -> {

                while (enableMonitor)
                {
                    boolean initiallyEnabled = isServerAccessEnabled();

                    ServerSettings serverSettings = ServerSettingsManager.instance().update();
                    boolean currentlyEnabled = serverSettings.isServerAccessible();

                    if (currentlyEnabled)
                    {
                        checkProfiler.accumulate();
                    }

                    setEnableServerAccess(currentlyEnabled);

                    boolean statusChanged = initiallyEnabled != currentlyEnabled;

                    // Determine accessibility of root URL. Maybe print diagnotic info.
                    if (statusChanged)
                    {
                        String timeStamp = DateFormat.format(new Date(System.currentTimeMillis()));
                        FileCacheMessageUtil.info().println(timeStamp + ( //
                        currentlyEnabled ? " Connected to server. Re-enabling online access." : " Failed to connect to server. Disabling online access for now." //
                        ));

                        dropProfiler.accumulate();
                    }

                    updateAccessAllUrls(currentlyEnabled && Configuration.getAuthorizor().isAuthorized(), false);

                    checkProfiler.reportElapsedTimes();
                    dropProfiler.reportElapsedTimes();

                    try
                    {
                        Thread.sleep(serverSettings.getSleepInterval());
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
     * Check access for all URLs that are managed by the cache. There are two
     * mechanisms for performing these checks. The preferred way uses a script that
     * checks access to multiple URLs locally on the server. This uses the script
     * checkfilesystemaccess.php, which in turn uses the
     * {@link edu.jhuapl.sbmt.tools.CheckUserAccess} tool.
     * <p>
     * Should an exception indicate the server-side script is not present several
     * times in a row, this method falls back on less desirable legacy behavior, in
     * which each file is checked one-at-a-time.
     * <p>
     * Because the cache connects each URL with a local file, both of these
     * mechanisms also perform local file system checks. This is the reason for the
     * odd-looking enableServerCheck parameter, which in effect disables only the
     * URL/online portion of the update, but still checks file accessibility.
     * <p>
     * This method catches all exceptions.
     * 
     * @param enableServerCheck if true, check both URL on server and local file
     *            system accessibility. If false, check only file system
     * @param forceUpdate if true, all files will be updated even if they were
     *            previously checked. If false, only "new" URLs that were added to
     *            the cache since the last check will be checked.
     */
    protected void updateAccessAllUrls(boolean enableServerCheck, boolean forceUpdate)
    {
        try
        {
            if (consecutiveFileNotFoundExceptionCount.get() >= maximumConsecutiveFileNotFoundExceptions)
            {
                FileCacheMessageUtil.debugCache().err().println("URL status check on server failed too many times; disabling.");
                enableAccessChecksOnServer.set(false);
            }

            // Only call doAccessCheckOnServer if web access is currently enabled.
            if (enableAccessChecksOnServer.get() && enableServerCheck)
            {
                try
                {
                    doAccessCheckOnServer(forceUpdate);
                }
                catch (FileNotFoundException e)
                {
                    // Probably this means the script really is not there on the server.
                    consecutiveFileNotFoundExceptionCount.incrementAndGet();
                    FileCacheMessageUtil.debugCache().err().println("Could not find URL checking script " + checkFileAccessScriptURL.get());
                    throw e;
                }
            }
            else
            {
                if (!enableAccessChecksOnServer.get())
                {
                    FileCacheMessageUtil.debugCache().err().println("Falling back on file-by-file access check");
                }

                // Safe and necessary to call this, even if server access is currently disabled,
                // because this method will skip URL checks but still perform file-system
                // accessibility checks. Also it informs listeners of the change of
                // accessibility status of URLs.
                queryAll(enableServerCheck, forceUpdate);
            }

            consecutiveFileNotFoundExceptionCount.set(0);
            enableAccessChecksOnServer.set(true);
        }
        catch (Exception e)
        {
            // Probably this indicates a transient problem with the internet connection. Log
            // the failure if debugging, but otherwise just continue; this check will be
            // attempted again.
            e.printStackTrace(FileCacheMessageUtil.debugCache().err());
        }
    }

    public synchronized void stopAccessMonitor()
    {
        enableMonitor = false;
    }

    /**
     * Attempt to use a server-side script to check accessibility of all the URLs
     * known to this manager. After one successful check (in which the URL is found
     * to be accessible, unauthorized, not found, etc.), a URL is not checked again,
     * even if the internet connection is lost or restored. Checks are repeated only
     * if the previous check resulted in a time-out or other transient condition. If
     * no URLs require checking, the server-side script is not invoked.
     * <p>
     * This implementation iterates through the whole collection of URLs, sending
     * them in batches to the server-side script. This is for two reasons: 1) the
     * server has a limit on the size of string that can be passed and 2) it is
     * useful to get items in batches to avoid all-or-nothing checks.
     * <p>
     * VERY IMPORTANT: this method needs to be kept in synch with the way clients
     * work to accept the queries, run them on the server and return the results. In
     * particular, this method expects the server to have a script named
     * "checkfilesystemaccess.php" in the query root directory.
     * 
     * @param forceUpdate force FileInfo portion of the update. The server-side
     *            update (UrlInfo) is performed once in any case.
     * 
     * @return true if all the access checks succeeded, false if any checks failed.
     *         Note this is checking whether the checks themselves succeeeded, not
     *         whether or not the checked URLs are accessible.
     * @throws IOException if a connection cannot be opened to the server-side
     *             script
     */
    protected void doAccessCheckOnServer(boolean forceUpdate) throws IOException
    {
        if (checkFileAccessScriptURL.get() == null)
        {
            checkFileAccessScriptURL.set(new URL(SafeURLPaths.instance().getString(Configuration.getQueryRootURL(), "checkfilesystemaccess.php")));
        }

        URL getUserAccessPhp = checkFileAccessScriptURL.get();

//        boolean enableServerCheck = urlManager.isServerAccessEnabled();
        boolean enableServerCheck = true;

        // Only ask about URLs for which no previous successful check has been made.
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (String url : urlManager.getUrlList())
        {
            UrlInfo urlInfo = urlManager.getInfo(url);
            UrlState state = urlInfo.getState();
            if (urlManager.getRootUrl().equals(state.getUrl()) || SafeURLPaths.instance().hasFileProtocol(url))
            {
                doQuery(state.getUrl(), enableServerCheck, forceUpdate);
                continue;
            }

            UrlStatus status = state.getLastKnownStatus();
            if (forceUpdate || status.equals(UrlStatus.CONNECTION_ERROR) || status.equals(UrlStatus.HTTP_ERROR) || status.equals(UrlStatus.UNKNOWN))
            {
                builder.add(url);
            }
            else
            {
                // Update not needed, but flag this URL as having been checked-online.
                urlInfo.update(state.update(true));
            }
        }

        ImmutableList<String> urlList = builder.build();

        ListIterator<String> iterator = urlList.listIterator();
        while (iterator.hasNext())
        {
            doAccessCheckOnServer(getUserAccessPhp, iterator, forceUpdate);
        }
    }

    /**
     * Iterate over the provided {@link #iterator} of URLs as Strings. For each, use
     * the {@link #getUserAccessPhp} script to check accessibility. Check at most
     * {@link #maximumQueryCount} URLs.
     * <p>
     * VERY IMPORTANT: this method needs to be kept in synch with the way clients
     * work to accept the queries, run them on the server and return the results. In
     * particular, this method escapes HTTP code, which must be "unescaped" by the
     * script that checks the files on the server. See
     * {@link edu.jhuapl.sbmt.tools.CheckUserAccess}.
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
    protected void doAccessCheckOnServer(URL getUserAccessPhp, ListIterator<String> iterator, boolean forceUpdate) throws IOException
    {
        String userName = Configuration.getAuthorizor().getUserName();
        if (userName == null)
        {
            return;
        }

        try (CloseableUrlConnection closeableConn = CloseableUrlConnection.of(getUserAccessPhp, HttpRequestMethod.GET))
        {
            URLConnection conn = closeableConn.getConnection();
            conn.setDoOutput(true);

            // Important: DO NOT DO THIS! It would break opening the output stream below.
            // closeableConn.connect();

            try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()))
            {
                URL rootUrl = Configuration.getRootURL();
                String rootUrlString = rootUrl.toString();
                URL dataRootUrl = Configuration.getDataRootURL();
                String dataRootUrlString = dataRootUrl.toString();

                StringBuilder sb = new StringBuilder();
                sb.append("rootURL=").append(rootUrlString);
                sb.append("&userName=").append(URLEncoder.encode(userName, getURLEncoding()));
                sb.append("&args=-encode");
                sb.append("&stdin=");

                boolean first = true;
                while (iterator.hasNext())
                {
                    String url = iterator.next();

                    // Skip any local file: URLs.
                    if (SafeURLPaths.instance().hasFileProtocol(url))
                    {
                        continue;
                    }

                    // For brevity, truncate the root prefix. The server-side script will add it
                    // back.
                    String shortUrl = url.replaceFirst(dataRootUrlString, "");
                    if (shortUrl.equals(url))
                    {
                        // Did not match the root prefix, so this file is not under the root and the
                        // server should not try to check it.
                        continue;
                    }

                    if (!shortUrl.matches(".*\\S.*"))
                    {
                        // Don't check the root prefix path itself.
                        continue;
                    }

                    FileCacheMessageUtil.debugCache().err().println("Adding to server-side access check: " + shortUrl);

                    shortUrl = URLEncoder.encode(shortUrl, getURLEncoding());

                    // Make sure the maximum query length would not be exceeded with the current URL
                    // plus a newline. For purposes of ensuring this doesn't happen, newline
                    // is counted as two characters (CR/LF).
                    if ((sb.length() + shortUrl.length() + 2) >= ServerSettingsManager.instance().get().getQueryLength())
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

                    sb.append(shortUrl);
                }
                String queryString = sb.toString();
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
                        FileCacheMessageUtil.debugCache().err().println("Server-side access check returned null");
                        break;
                    }
                    else if (CloseableUrlConnection.detectRejectionMessages(line))
                    {
                        FileCacheMessageUtil.debugCache().err().println("Request rejected for URL info from server-side script " + conn.getURL());
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
                        UrlState state = urlInfo.getState().update(status, contentLength, lastModified).update(true);
                        urlInfo.update(state);

                        // Update FileInfo aspect.
                        FileInfo fileInfo = getFileInfo(urlString);
                        FileState fileState = fileInfo.getState();
                        if (forceUpdate || fileState.getStatus().equals(FileStatus.UNKNOWN))
                        {
                            fileInfo.update();
                        }
                    }
                }
                if (!someOutput)
                {
                    FileCacheMessageUtil.debugCache().err().println("Server-side access check returned empty list");
                }
            }
        }
    }

    public boolean isServerAccessEnabled()
    {
        return urlManager.isServerAccessEnabled();
    }

    public void setEnableServerAccess(boolean enableServerAccess)
    {
        urlManager.setEnableServerAccess(enableServerAccess);
    }

    public UrlState queryRootState()
    {
        return urlManager.queryRootState(true, urlManager.isServerAccessEnabled());
    }

    public void addRootStateListener(StateListener stateListener)
    {
        addStateListener(urlManager.getRootUrl().toString(), stateListener, true);
    }

    public void removeRootStateListener(StateListener stateListener)
    {
        removeStateListener(urlManager.getRootUrl().toString(), stateListener);
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
        return getFileInfo(urlString).getState().getFile();
    }

    public DownloadableFileState getState(String urlString)
    {
        return DownloadableFileState.of(urlManager.getInfo(urlString).getState(), getFileInfo(urlString).getState());
    }

    public DownloadableFileAccessQuerier getQuerier(String urlString, boolean enableServerCheck, boolean forceUpdate)
    {
        UrlInfo urlInfo = urlManager.getInfo(urlString);
        FileInfo fileInfo = getFileInfo(urlString);

        return DownloadableFileAccessQuerier.of(urlInfo, fileInfo, forceUpdate, enableServerCheck);
    }

    public DownloadableFileState query(String urlString, boolean forceUpdate)
    {
        try
        {
            return doQuery(urlManager.getUrl(urlString), urlManager.isServerAccessEnabled(), forceUpdate);
        }
        catch (Exception e)
        {
            System.err.println("Problem querying server about " + urlString);
            e.printStackTrace();
        }

        return getState(urlString);
    }

    protected void queryAll(boolean enableServerCheck, boolean forceUpdate)
    {
        queryAll(enableServerCheck, forceUpdate, 3, SleepIntervalAfterLOC);
    }

    /**
     * Query the server about the accessibility for all URLs/files tracked by this
     * manager. If server checking is performed, this opens a new connection for
     * each such check, so it is time-consuming and generates a lot of queries.
     * <p>
     * If server-side access is currrently disabled (most likely because of internet
     * connectivity problems), this method will skip ALL server queries rather than
     * try them all, which would otherwise generate many queries destined to fail.
     * <p>
     * This method also triggers file-system checks for the local accessibility of
     * cached files. It does these checks whether or not server-side access checking
     * is currently enabled. Thus, this method is safe and beneficial to call
     * regardless of the state of the internet connection.
     * <p>
     * Because internet connections can be finicky, this method handles exceptions
     * in a specific way to reduce latency and improve odds of getting accurate
     * information for most URLs:
     * <p>
     * 1. If an {@link UnknownHostException} is thrown when checking any one URL, no
     * further URLs will be checked and the method will return. This exception is
     * interpreted to mean that there is no (reliable) internet connection.
     * <p>
     * 2. If a {@link SocketException} or {@link SocketTimeoutException} is thrown,
     * the method will pause for the number of milliseconds indicated by the
     * sleepInterval parameter before retrying the check. It will do this up to the
     * limit set by the maximumNumberTries parameter. After that it will go on to
     * the next URL.
     * <p>
     * 3. If any other {@link Exception} type is thrown when checking any URL, the
     * method will go on to check the next URL.
     * 
     * @param enableServerCheck if true, the method will attempt to check URLs on
     *            the server. If false, only file-system checks will be performed.
     * @param forceUpdate if true, forces a fresh check of access even if a previous
     *            check succeeded. This is useful for forcing a refresh of
     *            accessibility states. If false, checks will be performed only if a
     *            successful check was not previously performed.
     * @param maximumNumberTries the number of times to retry online checks for URLs
     *            that failed because of socket/latency problems.
     * @param sleepIntervalAfterFailure the number of milliseconds to sleep
     *            following an unsuccessful online URL check before retrying the
     *            check.
     */
    protected void queryAll(boolean enableServerCheck, boolean forceUpdate, int maximumNumberTries, int sleepIntervalAfterFailure)
    {
        URL rootUrl = urlManager.getRootUrl();
        String rootUrlString = rootUrl.toString();

        ImmutableList<String> urlList = urlManager.getUrlList();

        for (String urlString : urlList)
        {
            if (urlString.equals(rootUrlString))
            {
                // Do not check the root URL itself.
                continue;
            }

            boolean checkCompleted = false;
            boolean doCheck = true;
            for (int index = 0; index < maximumNumberTries && doCheck; ++index)
            {
                try
                {
                    URL url = urlManager.getUrl(urlString);
                    doQuery(url, enableServerCheck, forceUpdate);
                    doCheck = false;
                    checkCompleted = true;
                }
                catch (SocketException ignored)
                {
                    FileCacheMessageUtil.debugCache().err().println("SocketException on " + urlString);
                }
                catch (SocketTimeoutException ignored)
                {
                    FileCacheMessageUtil.debugCache().err().println("Timeout on " + urlString);
                }
                catch (UnknownHostException ignored)
                {
                    doCheck = false;
                    FileCacheMessageUtil.debugCache().err().println("Unknown host exception on " + urlString);
                }
                catch (Exception e)
                {
                    doCheck = false;
                    e.printStackTrace(FileCacheMessageUtil.debugCache().err());
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
                        FileCacheMessageUtil.debugCache().err().println("Pausing before retrying " + urlString);
                        Thread.sleep(sleepIntervalAfterFailure);
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore.
                    }
                }
            }

            // If check did not complete, don't continue with other URLs.
            if (!checkCompleted)
            {
                break;
            }
        }
    }

    public void queryAllInBackground(boolean forceUpdate)
    {
        accessMonitor.execute(() -> {
            updateAccessAllUrls(isServerAccessEnabled(), forceUpdate);
        });
    }

    public FileDownloader getDownloader(String urlString, boolean forceDownload)
    {
        UrlInfo urlInfo = urlManager.getInfo(urlString);
        FileInfo fileInfo = getFileInfo(urlString);

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

    public void addStateListener(String urlString, StateListener listener)
    {
        addStateListener(urlString, listener, false);
    }

    public void addStateListener(String urlString, StateListener listener, boolean urlListenerOnly)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(listener);

        UrlInfo urlInfo = urlManager.getInfo(urlString);
        URL url = urlInfo.getState().getUrl();

        // Reset the string so that it has a canonically consistent value no matter how
        // messy the supplied argument was.
        urlString = url.toString();

        FileInfo fileInfo = getFileInfo(urlString);

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
                    String propertyName = e.getPropertyName();
                    Object propertyValue = e.getNewValue();

                    DownloadableFileState state;
                    if (DownloadableFileState.STATE_PROPERTY.equals(propertyName))
                    {
                        state = (DownloadableFileState) e.getNewValue();
                    }
                    else if (UrlInfo.STATE_PROPERTY.equals(propertyName))
                    {
                        state = DownloadableFileState.of((UrlState) propertyValue, fileInfo.getState());
                    }
                    else if (FileInfo.STATE_PROPERTY.equals(propertyName))
                    {
                        state = DownloadableFileState.of(urlInfo.getState(), (FileState) propertyValue);
                    }
                    else
                    {
                        return;
                    }

                    if (isHeadless())
                    {
                        listener.respond(state);
                    }
                    else
                    {
                        EventQueue.invokeLater(() -> {
                            listener.respond(state);
                        });
                    }
                };

                propertyListenerMap.put(listener, propertyListener);
                urlInfo.addPropertyChangeListener(propertyListener);
                if (!urlListenerOnly)
                {
                    fileInfo.addPropertyChangeListener(propertyListener);
                }

                DownloadableFileState state = DownloadableFileState.of(urlInfo.getState(), fileInfo.getState());
                // Immediately "fire" just the newly added listener.
                if (isHeadless())
                {
                    listener.respond(state);
                }
                else
                {
                    EventQueue.invokeLater(() -> {
                        listener.respond(state);
                    });
                }
            }
        }
    }

    public void removeStateListener(String urlString, StateListener listener)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(listener);

        UrlInfo urlInfo = urlManager.getInfo(urlString);
        URL url = urlInfo.getState().getUrl();

        // Reset the string so that it has a canonically consistent value no matter how
        // messy the supplied argument was.
        urlString = url.toString();

        FileInfo fileInfo = getFileInfo(urlString);

        synchronized (this.listenerMap)
        {
            Map<StateListener, PropertyChangeListener> propertyListenerMap = listenerMap.get(urlString);
            if (propertyListenerMap != null)
            {
                PropertyChangeListener propertyListener = propertyListenerMap.get(listener);
                if (propertyListener != null)
                {
                    urlInfo.removePropertyChangeListener(propertyListener);
                    fileInfo.removePropertyChangeListener(propertyListener);
                    propertyListenerMap.remove(listener);
                }
            }
        }
    }

    protected FileInfo getFileInfo(String urlString)
    {
        Path urlPath = urlManager.getDownloadPath(urlString);
        return fileManager.getInfo(urlPath);
    }

    protected DownloadableFileState doQuery(URL url, boolean enableServerCheck, boolean forceUpdate) throws IOException
    {
        Preconditions.checkNotNull(url);

        DownloadableFileAccessQuerier querier = getQuerier(url.toString(), enableServerCheck, forceUpdate);
        querier.query();

        return querier.getDownloadableFileState();
    }

    /**
     * Clears out previous profiler runs, then starts profiler. Need to call this
     * before calling {@link #startAccessMonitor()} to catch all events monitored by
     * profiling.
     */
    public void enableProfiling()
    {
        getCheckProfiler().deleteProfileArea();
        getDropProfiler().deleteProfileArea();

        Profiler.globalEnableProfiling(true);
    }

    protected Profiler getCheckProfiler()
    {
        checkProfiler.compareAndSet(null, createProfiler("checks"));

        return checkProfiler.get();
    }

    protected Profiler getDropProfiler()
    {
        dropProfiler.compareAndSet(null, createProfiler("drops"));

        return dropProfiler.get();
    }

    protected Profiler createProfiler(String name)
    {
        String prefix = profileAreaPrefix.get();
        prefix = prefix != null ? SafeURLPaths.instance().getString(prefix, name) : name;

        return Profiler.of(prefix);
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
            URL getUserAccessPhp = new URL("https://sbmt.jhuapl.edu/sbmt/prod/query/" + "checkfilesystemaccess.php");
            try (CloseableUrlConnection closeableConn = CloseableUrlConnection.of(getUserAccessPhp, HttpRequestMethod.GET))
            {
                URLConnection conn = closeableConn.getConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestProperty("User-Agent", "Mozilla/4.0");
                try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()))
                {
                    wr.write("rootURL=https://sbmt.jhuapl.edu/sbmt/prod&userName=sbmt-test&password=wide-open&args=&stdin=/prometheus/gaskell/Gaskell_Prometheus_v7.8.json\n/GASKELL/EROS/Gaskell_433_Eros_v7.8.json");
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
