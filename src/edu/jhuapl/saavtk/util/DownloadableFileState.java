package edu.jhuapl.saavtk.util;

import java.io.File;
import java.net.URL;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;

/**
 * Immutable class that combines a {@link UrlState} and {@link FileState} to
 * represent the state of a resource located via a {@link URL} and downloadable
 * to a {@link File} on the local file system.
 * 
 * @author James Peachey
 *
 */
public class DownloadableFileState
{
    public static DownloadableFileState of(URL url, File file)
    {
        return new DownloadableFileState(UrlState.of(url), FileState.of(file));
    }

    public static DownloadableFileState of(UrlState urlState, FileState fileState)
    {
        Preconditions.checkNotNull(urlState);
        Preconditions.checkNotNull(fileState);

        return new DownloadableFileState(urlState, fileState);
    }

    private final UrlState urlState;
    private final FileState fileState;
    public static final String STATE_PROPERTY = "downloadableFileState";

    protected DownloadableFileState(UrlState urlState, FileState fileState)
    {
        this.urlState = urlState;
        this.fileState = fileState;
    }

    public UrlState getUrlState()
    {
        return urlState;
    }

    public FileState getFileState()
    {
        return fileState;
    }

    public boolean isAccessible()
    {
        return isLocalFileAvailable() || isUrlAccessible();
    }

    public boolean isLocalFileAvailable()
    {
        return fileState.getStatus() == FileStatus.ACCESSIBLE;
    }

    public boolean isUrlAccessible()
    {
        return urlState.getStatus() == UrlStatus.ACCESSIBLE;
    }

    public boolean isUrlUnauthorized()
    {
        return urlState.getLastKnownStatus() == UrlStatus.NOT_AUTHORIZED;
    }

    public boolean isURLNotFound()
    {
        return urlState.getLastKnownStatus() == UrlStatus.NOT_FOUND;
    }

    public boolean isDownloadNecessary()
    {
        boolean result;

        if (urlState.wasCheckedOnline())
        {
            UrlStatus urlStatus = urlState.getStatus();

            switch (urlStatus)
            {
            case ACCESSIBLE:
                result = fileState.getStatus() != FileStatus.ACCESSIBLE || urlState.getLastModified() > fileState.getLastModified();
                break;
            case UNKNOWN:
                result = true;
                break;
            default:
                result = fileState.getStatus() != FileStatus.ACCESSIBLE;
                break;
            }
        }
        else
        {
            result = fileState.getStatus() != FileStatus.ACCESSIBLE;
        }

        return result;
    }

    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;

        result = prime * result + fileState.hashCode();
        result = prime * result + urlState.hashCode();

        return result;
    }

    @Override
    public final boolean equals(Object other)
    {
        if (this == other)
            return true;

        if (other instanceof DownloadableFileState)
        {
            DownloadableFileState that = (DownloadableFileState) other;
            if (!fileState.equals(that.fileState))
                return false;
            if (!urlState.equals(that.urlState))
                return false;

            return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "DownloadableFileState [urlState=" + urlState + ", fileState=" + fileState + "]";
    }

}