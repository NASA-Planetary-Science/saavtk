package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.FileInfo.FileStatus;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

public class DownloadableFileInfo
{
    public static final String STATE_PROPERTY = "downloadableFileState";

    public static class DownloadableFileState
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
            return urlState.getStatus() == UrlStatus.NOT_AUTHORIZED;
        }

        public boolean isURLNotFound()
        {
            return urlState.getStatus() == UrlStatus.NOT_FOUND;
        }

        public boolean isDownloadMayBePossible()
        {
            UrlStatus urlStatus = urlState.getStatus();

            return urlStatus != UrlStatus.NOT_AUTHORIZED && urlStatus != UrlStatus.NOT_FOUND;
        }

        public boolean isDownloadNecessary()
        {
            UrlStatus urlStatus = urlState.getStatus();

            boolean result = false;

            switch (urlStatus)
            {
            case ACCESSIBLE:
                result = fileState.getStatus() != FileStatus.ACCESSIBLE || urlState.getLastModified() > fileState.getLastModified();
                break;
            case NOT_AUTHORIZED:
            case NOT_FOUND:
                result = false;
                break;
            case UNKNOWN:
                result = true;
                break;
            case HTTP_ERROR:
            default:
                result = fileState.getStatus() != FileStatus.ACCESSIBLE;
                break;
            }

            return result;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;

            result = prime * result + fileState.hashCode();
            result = prime * result + urlState.hashCode();

            return result;
        }

        @Override
        public boolean equals(Object other)
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

    public static DownloadableFileInfo of(URL url, File file)
    {
        DownloadableFileInfo result = new DownloadableFileInfo(url, file);

        return result;
    }

    private final PropertyChangeSupport pcs;
    private volatile DownloadableFileState state;

    protected DownloadableFileInfo(URL url, File file)
    {
        this.pcs = new PropertyChangeSupport(this);
        this.state = DownloadableFileState.of(url, file);
        pcs.addPropertyChangeListener(e -> {
            PropertyChangeSupport spud = pcs;
            Object value = e.getPropertyName();
            if (value == Boolean.FALSE)
            {
                System.err.println("ping");
            }
        });
    }

    public DownloadableFileState getState()
    {
        return state;
    }

    public void update(UrlState urlState)
    {
        Preconditions.checkNotNull(urlState);

        state = DownloadableFileState.of(urlState, state.getFileState());

        pcs.firePropertyChange(STATE_PROPERTY, null, state);
    }

    public void update(FileState fileState)
    {
        Preconditions.checkNotNull(fileState);

        state = DownloadableFileState.of(state.getUrlState(), fileState);

        pcs.firePropertyChange(STATE_PROPERTY, null, state);
    }

    public void update(UrlState urlState, FileState fileState)
    {
        Preconditions.checkNotNull(urlState);
        Preconditions.checkNotNull(fileState);

        state = DownloadableFileState.of(urlState, fileState);

        pcs.firePropertyChange(STATE_PROPERTY, null, state);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public String toString()
    {
        return state.toString();
    }

}