package edu.jhuapl.saavtk.util;

import java.net.URL;

import com.google.common.base.Preconditions;

public class UrlState
{
    protected static final long UNKNOWN_LENGTH = -1;
    protected static final long UNKNOWN_LAST_MODIFIED = 0;

    public static UrlState of(URL url)
    {
        UrlStatus initialStatus = url.toString().contains(" ") ? UrlStatus.INVALID_URL : UrlStatus.UNKNOWN;

        return of(url, initialStatus, UNKNOWN_LENGTH, UNKNOWN_LAST_MODIFIED);
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