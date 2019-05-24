package edu.jhuapl.saavtk.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingWorker;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;
import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public class FileAccessQuerier extends SwingWorker<Void, Void>
{
    protected static final DecimalFormat PF = new DecimalFormat("0%");
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static final String PROGRESS_PROPERTY = "infoQueryProgress";
    public static final String DONE_PROPERTY = "infoQueryDone";
    public static final String CANCELED_PROPERTY = "infoQueryCanceled";

    public static FileAccessQuerier of(UrlInfo urlInfo, FileInfo fileInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        Preconditions.checkNotNull(urlInfo);
        Preconditions.checkNotNull(fileInfo);

        return new FileAccessQuerier(urlInfo, fileInfo, forceUpdate, serverAccessEnabled);
    }

    private final UrlInfo urlInfo;
    private final FileInfo fileInfo;
    private final boolean forceUpdate;
    private final boolean serverAccessEnabled;

    protected FileAccessQuerier(UrlInfo urlInfo, FileInfo fileInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        this.urlInfo = urlInfo;
        this.fileInfo = fileInfo;
        this.forceUpdate = forceUpdate;
        this.serverAccessEnabled = serverAccessEnabled;
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
        FileState fileState = fileInfo.getState();
        UrlState urlState = urlInfo.getState();

        // Begin DEBUG message.
        if (forceUpdate)
        {
            Debug.out().println("Querying FS and server (forced) for info about " + urlState.getUrl());
        }
        else if (fileState.getStatus() == FileStatus.UNKNOWN)
        {
            Debug.out().println("Querying FS " + //
                    (urlState.getStatus() == UrlStatus.UNKNOWN ? (serverAccessEnabled ? "and server " : "ONLY (server access disabled) ") : "") + //
                    "for info about " + urlState.getUrl());
        }
        else if (urlState.getStatus() == UrlStatus.UNKNOWN)
        {
            Debug.out().println((serverAccessEnabled ? "Querying server " : "NOT querying server (access disabled) ") + //
                    "for info about " + urlState.getUrl());
        }

        if (forceUpdate || fileState.getStatus() == FileStatus.UNKNOWN)
        {
            fileInfo.update();
        }
        // End DEBUG message.

        if (serverAccessEnabled)
        {
            if (forceUpdate || urlState.getStatus() == UrlStatus.UNKNOWN)
            {
                try (CloseableUrlConnection closeableConnection = CloseableUrlConnection.of(urlInfo, HttpRequestMethod.HEAD))
                {
                    urlInfo.update(closeableConnection.getConnection());
                }
            }
        }
        else
        {
            urlInfo.update(UrlState.of(urlState.getUrl()));
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