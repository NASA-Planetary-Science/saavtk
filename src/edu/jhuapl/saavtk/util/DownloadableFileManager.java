package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.HashMap;
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
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;

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

    protected DownloadableFileManager(UrlAccessManager urlManager, FileAccessManager fileManager)
    {
        this.urlManager = urlManager;
        this.fileManager = fileManager;
        this.downloadInfoCache = new ConcurrentHashMap<>();
        this.listenerMap = new HashMap<>();
        this.accessMonitor = Executors.newCachedThreadPool();
        this.enableMonitor = Boolean.FALSE;
        this.sleepInterval = 5000;
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

                    queryAll(forceUpdate);

                    try
                    {
                        Thread.sleep(sleepInterval);
                    }
                    catch (@SuppressWarnings("unused") InterruptedException ignored)
                    {

                    }
                }

            });
        }
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

    public FileAccessQuerier getQuerier(String urlString, boolean forceUpdate)
    {
        Preconditions.checkNotNull(urlString);

        UrlInfo urlInfo = urlManager.getInfo(urlString);

        DownloadableFileState state = getState(urlString);
        File file = state.getFileState().getFile();
        FileInfo fileInfo = fileManager.getInfo(file);

        return FileAccessQuerier.of(urlInfo, fileInfo, forceUpdate, urlManager.isServerAccessEnabled());
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

            boolean doCheck = true;
            boolean unknownHost = false;
            while (doCheck)
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

    protected DownloadableFileInfo doQuery(URL url, boolean forceUpdate) throws IOException, InterruptedException
    {
        Preconditions.checkNotNull(url);

        DownloadableFileInfo result = getInfo(url);

        FileAccessQuerier querier = getQuerier(url.toString(), forceUpdate);
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

}
