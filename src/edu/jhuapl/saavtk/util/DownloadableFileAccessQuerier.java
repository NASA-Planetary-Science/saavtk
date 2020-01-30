package edu.jhuapl.saavtk.util;

import java.io.IOException;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;

public class DownloadableFileAccessQuerier extends UrlAccessQuerier
{
    public static DownloadableFileAccessQuerier of(UrlInfo urlInfo, FileInfo fileInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        Preconditions.checkNotNull(urlInfo);
        Preconditions.checkNotNull(fileInfo);

        return new DownloadableFileAccessQuerier(urlInfo, fileInfo, forceUpdate, serverAccessEnabled);
    }

    private final FileInfo fileInfo;

    protected DownloadableFileAccessQuerier(UrlInfo urlInfo, FileInfo fileInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        super(urlInfo, forceUpdate, serverAccessEnabled);
        this.fileInfo = fileInfo;
    }

    public DownloadableFileState getDownloadableFileState()
    {
        return DownloadableFileState.of(getUrlState(), fileInfo.getState());
    }

    @Override
    public void query() throws IOException
    {
        super.query();

        FileState fileState = fileInfo.getState();

        if (isForceUpdate() || fileState.getStatus() == FileStatus.UNKNOWN)
        {
            fileInfo.update();
        }
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        query();

        return null;
    }

    @Override
    public void done()
    {
        firePropertyChange(isCancelled() ? QUERY_CANCELED : QUERY_DONE, null, fileInfo);
    }

}