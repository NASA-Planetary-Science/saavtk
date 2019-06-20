package edu.jhuapl.saavtk.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
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

    public enum UrlStatus
    {
        ACCESSIBLE, // Connection made, resource is accessible. Future access likely to succeed.
        NOT_AUTHORIZED, // Connection made, but user is not authorized.
        NOT_FOUND, // Connection made, but resource was not found.
        HTTP_ERROR, // Connection made, but HTTP error code was returned. Future access unknown.
        UNKNOWN, // Have not succesfully obtained information about the URL.
    }

    public static class UrlState
    {
        protected static final long UNKNOWN_LENGTH = -1;
        protected static final long UNKNOWN_LAST_MODIFIED = 0;

        public static UrlState of(URL url)
        {
            return of(url, UrlStatus.UNKNOWN, UNKNOWN_LENGTH, UNKNOWN_LAST_MODIFIED);
        }

        public static UrlState of(URL url, UrlStatus status)
        {
            return of(url, status, UNKNOWN_LENGTH, UNKNOWN_LAST_MODIFIED);
        }

        public static UrlState of(URL url, UrlStatus status, long contentLength, long lastModified)
        {
            Preconditions.checkNotNull(url);
            Preconditions.checkNotNull(status);

            return new UrlState(url, status, contentLength, lastModified);
        }

        private final URL url;
        private final UrlStatus status;
        private final long contentLength;
        private final long lastModified;

        protected UrlState(URL url, UrlStatus status, long contentLength, long lastModified)
        {
            this.url = url;
            this.status = status;
            this.contentLength = contentLength;
            this.lastModified = lastModified;
        }

        public URL getUrl()
        {
            return url;
        }

        public UrlStatus getStatus()
        {
            return status;
        }

        public long getContentLength()
        {
            return contentLength;
        }

        public long getLastModified()
        {
            return lastModified;
        }

        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = 1;
            // Work around a bug in Java: under some circumstances, URL.hashCode() gives
            // different results depending on whether there was an internet connection when
            // the URL was instantiated.
            result = prime * result + url.toString().hashCode();
            result = prime * result + (int) (contentLength ^ (contentLength >>> 32));
            result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
            result = prime * result + status.hashCode();

            return result;
        }

        @Override
        public final boolean equals(Object other)
        {
            if (this == other)
                return true;
            if (other instanceof UrlState)
            {
                UrlState that = (UrlState) other;
                if (!this.url.equals(that.url))
                    return false;
                if (this.contentLength != that.contentLength)
                    return false;
                if (this.lastModified != that.lastModified)
                    return false;
                if (this.status != that.status)
                    return false;

                return true;
            }

            return false;
        }

        @Override
        public String toString()
        {
            return "UrlState [" + (url != null ? "url=" + url + ", " : "") + //
                    "status=" + status + ", contentLength=" + contentLength + //
                    ", lastModified=" + lastModified + "]";
        }

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
                        Debug.err().println("Received response code " + code + " for URL " + state.getUrl());
                        status = UrlStatus.HTTP_ERROR;
                    }
                }
                catch (ProtocolException e)
                {
                    // This indicates the request method isn't supported. That should not happen.
                    throw new AssertionError(e);
                }
            }
            else
            {
                // Probably this is a file-type URL. May need to add access checks for that, but
                // for now, be optimistic and assume it's accessible.
                status = UrlStatus.ACCESSIBLE;
            }

            return update(status, contentLength, lastModified);
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