package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.FileInfo.FileState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlState;

public class DownloadableFileInfo
{

    public static DownloadableFileInfo of(URL url, File file)
    {
        DownloadableFileInfo result = new DownloadableFileInfo(url, file);

        return result;
    }

    private final PropertyChangeSupport pcs;
    private final AtomicReference<DownloadableFileState> state;

    protected DownloadableFileInfo(URL url, File file)
    {
        this.pcs = new PropertyChangeSupport(this);
        this.state = new AtomicReference<>(DownloadableFileState.of(url, file));
    }

    public DownloadableFileState getState()
    {
        synchronized (this.state)
        {
            return state.get();
        }
    }

    public void update(UrlState urlState)
    {
        Preconditions.checkNotNull(urlState);

        FileState fileState;
        synchronized (this.state)
        {
            fileState = state.get().getFileState();
            update(urlState, fileState);
        }
    }

    public void update(FileState fileState)
    {
        Preconditions.checkNotNull(fileState);

        UrlState urlState;
        synchronized (this.state)
        {
            urlState = state.get().getUrlState();
            update(urlState, fileState);
        }
    }

    public void update(DownloadableFileState downloadableFileState)
    {
        Preconditions.checkNotNull(downloadableFileState);

        update(downloadableFileState.getUrlState(), downloadableFileState.getFileState());
    }

    protected void update(UrlState urlState, FileState fileState)
    {
        Preconditions.checkNotNull(urlState);
        Preconditions.checkNotNull(fileState);

        DownloadableFileState state = DownloadableFileState.of(urlState, fileState);

        boolean changed = false;
        synchronized (this.state)
        {
            if (!this.state.get().equals(state))
            {
                this.state.set(state);
                changed = true;
            }
        }

        if (changed)
        {
            fireStateChange();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        synchronized (this.pcs)
        {
            pcs.addPropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        synchronized (this.pcs)
        {
            pcs.removePropertyChangeListener(listener);
        }
    }

    @Override
    public String toString()
    {
        synchronized (this.state)
        {
            return state.toString();
        }
    }

    protected void fireStateChange()
    {
        synchronized (this.pcs)
        {
            pcs.firePropertyChange(DownloadableFileState.STATE_PROPERTY, null, state.get());
        }
    }

}