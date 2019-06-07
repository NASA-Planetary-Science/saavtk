package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
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
    public interface StateListener
    {
        void respond(DownloadableFileState fileState);
    }

    private static volatile boolean showDotsForFiles = false;

    public static void setShowDotsForFiles(boolean showDotsForFiles)
    {
        DownloadableFileManager.showDotsForFiles = showDotsForFiles;
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

    private static boolean headless = Boolean.parseBoolean(System.getProperty("java.awt.headless"));
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
                        System.out.println(currentlyEnabled ? //
                        "Connected to server. Re-enabling online access." : //
                        "Failed to connect to server. Disabling online access for now.");
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

    public void query(String urlString, StateListener whenFinished, boolean forceUpdate)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(whenFinished);

        FileAccessQuerier querier = getQuerier(urlString, forceUpdate);

        DownloadableFileInfo info = getInfo(urlManager.getUrl(urlString));

        querier.addPropertyChangeListener(e -> {
            String propertyName = e.getPropertyName();
            if (propertyName.equals(FileAccessQuerier.DONE_PROPERTY) || propertyName.equals(FileAccessQuerier.CANCELED_PROPERTY))
            {
                info.update(querier.getUrlInfo().getState(), querier.getFileInfo().getState());
                whenFinished.respond(info.getState());
            }
        });

        querier.queryInBackground();
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
            try
            {
                URL url = urlManager.getUrl(urlString);
                doQuery(url, forceUpdate);
            }
            catch (@SuppressWarnings("unused") SocketTimeoutException ignored)
            {
                // Hit a time-out. Likely the rest will also, so break now and come back to this
                // later.
                Debug.err().println("Timeout on " + urlString);
                break;
            }
            catch (@SuppressWarnings("unused") ConnectException | UnknownHostException ignored)
            {
                Debug.err().println("Unknown host exception on " + urlString);
                break;
            }
            catch (Exception e)
            {
                e.printStackTrace(Debug.err());
                continue;
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

        if (!Debug.isEnabled() && showDotsForFiles)
        {
            System.out.print(".");
        }

        URL url = urlManager.getUrl(urlString);
        UrlInfo urlInfo = urlManager.getInfo(urlString);
        Path downloadPath = urlManager.getDownloadPath(url);

        FileInfo fileInfo = fileManager.getInfo(downloadPath);

        return FileDownloader.of(urlInfo, fileInfo, forceDownload);
    }

    public DownloadableFileState getDownloadedFile(String urlString, boolean forceDownload) throws IOException, InterruptedException
    {
        Preconditions.checkNotNull(urlString);

        if (!Debug.isEnabled() && showDotsForFiles)
        {
            System.out.print(".");
        }

        DownloadableFileState fileState = getState(urlString);

        if (urlManager.isServerAccessEnabled())
        {
            FileDownloader downloader = getDownloader(urlString, forceDownload);
            downloader.download();
            fileState = getState(urlString);
        }

        return fileState;
    }

    public void getDownloadedFile(String urlString, StateListener whenFinished, boolean forceDownload)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(whenFinished);

        if (!Debug.isEnabled() && showDotsForFiles)
        {
            System.out.print(".");
        }

        DownloadableFileState fileState = getState(urlString);

        if (urlManager.isServerAccessEnabled())
        {
            FileDownloader downloader = getDownloader(urlString, forceDownload);

            downloader.addPropertyChangeListener(e -> {
                String propertyName = e.getPropertyName();
                if (propertyName.equals(FileDownloader.DONE_PROPERTY) || propertyName.equals(FileDownloader.CANCELED_PROPERTY))
                {
                    whenFinished.respond(getState(urlString));
                }
            });

            downloader.downloadInBackground();
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
        result.update(querier.getUrlInfo().getState(), querier.getFileInfo().getState());

        return result;
    }

    protected static boolean isHeadless()
    {
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
