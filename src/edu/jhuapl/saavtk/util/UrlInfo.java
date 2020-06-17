package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;

/**
 * Collect and store essential properties associated with a remote resource.
 * These properties are useful for determining whether it is necessary and/or
 * possible to download a remote file, or to determine the size of the file.
 */
public class UrlInfo
{
    public static final String STATE_PROPERTY = "urlInfoState";

    public static UrlInfo of(URL url)
    {
        return new UrlInfo(url);
    }

    private final PropertyChangeSupport pcs;
    private final AtomicReference<UrlState> state;

    protected UrlInfo(URL url)
    {
        this.pcs = new PropertyChangeSupport(this);
        this.state = new AtomicReference<>(UrlState.of(url));
    }

    public UrlState getState()
    {
        synchronized (this.state)
        {
            return state.get();
        }
    }

    public void update(CloseableUrlConnection connection) throws IOException
    {
        Preconditions.checkNotNull(connection);
        UrlState state = getState();

        Preconditions.checkArgument(connection.getUrl().equals(state.getUrl()));

        UrlStatus status = state.getLastKnownStatus();

        if (status == UrlStatus.INVALID_URL)
        {
            debugConnectionMessage(state, "invalid URL");
        }
        else
        {
            try
            {
                state = connection.connect();
            }           
            catch (IOException e)
            {
                update(state.update(UrlStatus.CONNECTION_ERROR).update(false));
                throw e;
            }

            update(state);
        }
    }

    private void debugConnectionMessage(UrlState state, String message)
    {
        FileCacheMessageUtil.debugCache().err().println("Connected to " + state.getUrl() + ": " + message);
    }

    public void update(UrlState state)
    {
        Preconditions.checkNotNull(state);

        boolean changed = false;

        synchronized (this.state)
        {
            Preconditions.checkArgument(this.state.get().getUrl().equals(state.getUrl()));

            if (!this.state.get().equals(state))
            {
                this.state.set(state);
                changed = true;
            }
        }

        if (changed)
        {
            synchronized (this.pcs)
            {
                pcs.firePropertyChange(STATE_PROPERTY, null, state);
            }
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
        return getState().toString();
    }

}