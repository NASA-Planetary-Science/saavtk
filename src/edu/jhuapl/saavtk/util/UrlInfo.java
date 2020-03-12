package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
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
    private static volatile boolean enableDebug = false;

    public static void enableDebug(boolean enable)
    {
        enableDebug = enable;
    }

    protected static Debug debug()
    {
        return Debug.of(enableDebug);
    }

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

    public UrlState update(URLConnection connection) throws IOException
    {
        Preconditions.checkNotNull(connection);

        synchronized (this.state)
        {
            UrlState state = this.state.get();
            UrlStatus status = state.getStatus();

            if (status != UrlStatus.INVALID_URL)
            {
                long contentLength = state.getContentLength();
                long lastModified = state.getLastModified();
                if (connection instanceof HttpURLConnection)
                {
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    try
                    {
                        int code = httpConnection.getResponseCode();

                        // Codes in the 200 series are generally "ok" so treat any of them as
                        // successful.
                        if (code >= HttpURLConnection.HTTP_OK && code < HttpURLConnection.HTTP_MULT_CHOICE)
                        {
                            status = UrlStatus.ACCESSIBLE;
                            contentLength = httpConnection.getContentLengthLong();
                            lastModified = httpConnection.getLastModified();
                        }
                        else if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN)
                        {
                            status = UrlStatus.NOT_AUTHORIZED;
                        }
                        else if (code == HttpURLConnection.HTTP_NOT_FOUND)
                        {
                            status = UrlStatus.NOT_FOUND;
                        }
                        else
                        {
                            debug().err().println("Received response code " + code + " for URL " + state.getUrl());
                            status = UrlStatus.HTTP_ERROR;
                        }
                    }
                    catch (ProtocolException e)
                    {
                        // This indicates the request method isn't supported. That should not happen.
                        throw new AssertionError(e);
                    }
                    catch (SocketException | SocketTimeoutException e)
                    {
                        update(UrlStatus.CONNECTION_ERROR, contentLength, lastModified);
                        throw e;
                    }
                }
                else
                {
                    // Probably this is a file-type URL. May need to add access checks for that, but
                    // for now, be optimistic and assume it's accessible.
                    status = UrlStatus.ACCESSIBLE;
                }

                state = update(status, contentLength, lastModified);
            }

            return state;
        }
    }

    public UrlState update(UrlStatus status, long contentLength, long lastModified)
    {
        Preconditions.checkNotNull(status);

        synchronized (this.state)
        {
            URL url = state.get().getUrl();
            return update(UrlState.of(url, status, contentLength, lastModified));
        }

    }

    public UrlState update(UrlState state)
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

        return state;
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

}