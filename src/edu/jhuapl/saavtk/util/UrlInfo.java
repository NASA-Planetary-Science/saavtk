package edu.jhuapl.saavtk.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

import com.google.common.base.Preconditions;

public class UrlInfo
{
    public enum UrlStatus
    {
        ACCESSIBLE, // Connection made, resource is accessible. Future access likely to succeed.
        NOT_AUTHORIZED, // Connection made, but user is not authorized.
        HTTP_ERROR, // Connection made, but HTTP error code was returned. Future access unknown.
        UNKNOWN, // Have not succesfully obtained information about the URL.
    }

    public static UrlInfo of(URL url, Path relativePath)
    {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(relativePath);

        return new UrlInfo(url, relativePath);
    }

    private final URL url;
    private final Path relativePath;
    private volatile UrlStatus status;
    private volatile long lastModified;

    protected UrlInfo(URL url, Path relativePath)
    {
        this.url = url;
        this.relativePath = relativePath;
        this.status = UrlStatus.UNKNOWN;
        this.lastModified = 0;
    }

    public URL getUrl()
    {
        return url;
    }

    public Path getRelativePath()
    {
        return relativePath;
    }

    public UrlStatus getStatus()
    {
        return status;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    public void update(UrlStatus status, long lastModified)
    {
        Preconditions.checkNotNull(status);

        synchronized (this.status)
        {
            this.status = status;
            this.lastModified = lastModified;
        }
    }

    public void update(URLConnection connection) throws IOException
    {
        Preconditions.checkNotNull(connection);

        UrlStatus status;
        long lastModified;

        synchronized (this.status)
        {
            status = this.status;
            lastModified = this.lastModified;
        }

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
                    lastModified = httpConnection.getLastModified();
                }
                else if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN)
                {
                    status = UrlStatus.NOT_AUTHORIZED;
                }
                else
                {
                    Debug.err().println("Received response code " + code + " for URL " + getUrl());
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

        update(status, lastModified);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + url.hashCode();
        result = prime * result + relativePath.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof UrlInfo)
        {
            UrlInfo that = (UrlInfo) other;
            if (!this.url.equals(that.url))
            {
                return false;
            }
            if (!this.relativePath.equals(that.relativePath))
            {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        synchronized (this.status)
        {
            return url.toString() + " (status=" + status + ", lastModified=" + lastModified + ")";
        }
    }

}