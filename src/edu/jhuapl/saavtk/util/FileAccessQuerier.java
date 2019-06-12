package edu.jhuapl.saavtk.util;

import java.io.IOException;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.DownloadableFileInfo.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;

public class FileAccessQuerier extends UrlAccessQuerier
{
    public static FileAccessQuerier of(UrlInfo urlInfo, FileInfo fileInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        Preconditions.checkNotNull(urlInfo);
        Preconditions.checkNotNull(fileInfo);

        return new FileAccessQuerier(urlInfo, fileInfo, forceUpdate, serverAccessEnabled);
    }

    private final FileInfo fileInfo;

    protected FileAccessQuerier(UrlInfo urlInfo, FileInfo fileInfo, boolean forceUpdate, boolean serverAccessEnabled)
    {
        super(urlInfo, forceUpdate, serverAccessEnabled);
        this.fileInfo = fileInfo;
    }

    public DownloadableFileState getDownloadableFileState()
    {
        return DownloadableFileState.of(getUrlState(), fileInfo.getState());
    }

    @Override
    public void query() throws IOException, InterruptedException
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
        firePropertyChange(isCancelled() ? CANCELED_PROPERTY : DONE_PROPERTY, null, fileInfo);
    }

}