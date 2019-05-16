package edu.jhuapl.saavtk.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingWorker;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public class FileAccessQuerier extends SwingWorker<Void, Void>
{
    protected static final DecimalFormat PF = new DecimalFormat("0%");
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static final String PROGRESS_PROPERTY = "infoQueryProgress";
    public static final String DONE_PROPERTY = "infoQueryDone";
    public static final String CANCELED_PROPERTY = "infoQueryCanceled";

    public static FileAccessQuerier of(UrlInfo urlInfo, FileInfo fileInfo, boolean forceUpdate)
    {
        Preconditions.checkNotNull(urlInfo);
        Preconditions.checkNotNull(fileInfo);

        return new FileAccessQuerier(urlInfo, fileInfo, forceUpdate);
    }

    private final UrlInfo urlInfo;
    private final FileInfo fileInfo;
    private final boolean forceUpdate;

    protected FileAccessQuerier(UrlInfo urlInfo, FileInfo fileInfo, boolean forceUpdate)
    {
        this.urlInfo = urlInfo;
        this.fileInfo = fileInfo;
        this.forceUpdate = forceUpdate;
    }

    public UrlInfo getUrlInfo()
    {
        return urlInfo;
    }

    public FileInfo getFileInfo()
    {
        return fileInfo;
    }

    public void query() throws IOException, InterruptedException
    {
        if (forceUpdate || fileInfo.getState().getStatus() == FileStatus.UNKNOWN)
        {
            fileInfo.update();
        }

        if (forceUpdate || urlInfo.getState().getStatus() == UrlStatus.UNKNOWN)
        {
            try (CloseableUrlConnection closeableConnection = CloseableUrlConnection.of(urlInfo, HttpRequestMethod.HEAD))
            {
                urlInfo.update(closeableConnection.getConnection());
            }
        }
    }

    public void queryInBackground()
    {
        THREAD_POOL.execute(this);
    }

    @Override
    public void done()
    {
        firePropertyChange(isCancelled() ? CANCELED_PROPERTY : DONE_PROPERTY, null, fileInfo);
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        query();

        return null;
    }

}