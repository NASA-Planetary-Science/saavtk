package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.DownloadableFileInfo.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;

public class DownloadableFileManager
{
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

        return new DownloadableFileManager(urlManager, fileManager);
    }

    public static DownloadableFileManager of(UrlAccessManager urlManager, FileAccessManager fileManager)
    {
        return new DownloadableFileManager(urlManager, fileManager);
    }

    private final UrlAccessManager urlManager;
    private final FileAccessManager fileManager;
    private final ConcurrentMap<URL, DownloadableFileInfo> downloadInfoCache;
    private final ExecutorService accessMonitor;
    private volatile Boolean enableMonitor;
    private volatile long sleepInterval;

    protected DownloadableFileManager(UrlAccessManager urlManager, FileAccessManager fileManager)
    {
        this.urlManager = urlManager;
        this.fileManager = fileManager;
        this.downloadInfoCache = new ConcurrentHashMap<>();
        this.accessMonitor = Executors.newSingleThreadExecutor();
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
                    for (URL url : downloadInfoCache.keySet())
                    {
                        try
                        {
                            query(url.toString(), false);
                        }
                        catch (@SuppressWarnings("unused") Exception e)
                        {
                            System.err.println("Exception querying server about " + url);
                        }
                    }

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

    public UrlAccessManager getUrlManager()
    {
        return urlManager;
    }

    public FileAccessManager getFileManager()
    {
        return fileManager;
    }

    public boolean isAccessible(String urlString)
    {
        return query(urlString).getState().isAccessible();
    }

    public DownloadableFileInfo getInfo(String urlString)
    {
        Preconditions.checkNotNull(urlString);

        URL url = urlManager.getUrl(urlString);

        DownloadableFileInfo result = downloadInfoCache.get(url);
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
                    EventQueue.invokeLater(() -> {
                        downloadableInfo.update((UrlState) e.getNewValue());
                    });
                }
            });

            fileInfo.addPropertyChangeListener(e -> {
                if (e.getPropertyName().equals(FileInfo.STATE_PROPERTY))
                {
                    EventQueue.invokeLater(() -> {
                        downloadableInfo.update((FileState) e.getNewValue());
                    });
                }
            });

            result = downloadableInfo;

            downloadInfoCache.put(url, result);
        }

        return result;
    }

    public FileAccessQuerier getQuerier(String urlString, boolean forceUpdate)
    {
        Preconditions.checkNotNull(urlString);

        DownloadableFileInfo downloadableInfo = getInfo(urlString);

        UrlInfo urlInfo = urlManager.getInfo(urlString);

        File file = downloadableInfo.getState().getFileState().getFile();
        FileInfo fileInfo = fileManager.getInfo(file);

        return FileAccessQuerier.of(urlInfo, fileInfo, forceUpdate, urlManager.isServerAccessEnabled());
    }

    public DownloadableFileInfo query(String urlString)
    {
        try
        {
            return query(urlString, false);
        }
        catch (Exception e)
        {
            System.err.println("Problem querying server about " + urlString);
            e.printStackTrace();
        }

        return getInfo(urlString);
    }

    public DownloadableFileInfo query(String urlString, boolean forceUpdate) throws IOException, InterruptedException
    {
        Preconditions.checkNotNull(urlString);

        DownloadableFileInfo result = getInfo(urlString);
        FileAccessQuerier querier = getQuerier(urlString, forceUpdate);
        querier.query();
        result.update(querier.getUrlInfo().getState(), querier.getFileInfo().getState());

        return result;
    }

    public void query(String urlString, StateListener whenFinished, boolean forceUpdate)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(whenFinished);

        FileAccessQuerier querier = getQuerier(urlString, forceUpdate);

        DownloadableFileInfo info = getInfo(urlString);

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
        DownloadableFileState fileState = getInfo(urlString).getState();

        if (urlManager.isServerAccessEnabled() && (forceDownload || (fileState.isDownloadMayBePossible() && fileState.isDownloadNecessary())))
        {
            FileDownloader downloader = getDownloader(urlString, forceDownload);
            downloader.download();
            fileState = getInfo(urlString).getState();
        }

        return fileState;
    }

    public void getDownloadedFile(String urlString, StateListener whenFinished, boolean forceDownload)
    {
        DownloadableFileState fileState = getInfo(urlString).getState();

        if (urlManager.isServerAccessEnabled() && (forceDownload || (fileState.isDownloadMayBePossible() && fileState.isDownloadNecessary())))
        {
            FileDownloader downloader = getDownloader(urlString, forceDownload);

            downloader.addPropertyChangeListener(e -> {
                String propertyName = e.getPropertyName();
                if (propertyName.equals(FileDownloader.DONE_PROPERTY) || propertyName.equals(FileDownloader.CANCELED_PROPERTY))
                {
                    whenFinished.respond(getInfo(urlString).getState());
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
        Preconditions.checkNotNull(listener);

        getInfo(urlString).addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals(DownloadableFileInfo.STATE_PROPERTY))
            {
                EventQueue.invokeLater(() -> {
                    listener.respond((DownloadableFileState) e.getNewValue());
                });
            }
        });
    }

    public void removeStateListener(String urlString, StateListener listener)
    {
        Preconditions.checkNotNull(listener);

        // TODO: this is obviously wrong; need a better way to manage this.
        getInfo(urlString).removePropertyChangeListener(e -> {
            if (e.getPropertyName().equals(DownloadableFileInfo.STATE_PROPERTY))
            {
                listener.respond((DownloadableFileState) e.getNewValue());
            }
        });
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
