package edu.jhuapl.saavtk.util;

import java.net.URL;

import com.google.common.base.Preconditions;

/**
 * Immutable class that encapsulates a snapshot of the state of a resource
 * located by a {@link URL} at a particular moment.
 * 
 * @author James Peachey
 *
 */
public class UrlState
{
    protected static final long UNKNOWN_LENGTH = -1;
    protected static final long UNKNOWN_LAST_MODIFIED = 0;

    public static UrlState of(URL url)
    {
        UrlStatus initialStatus = url.toString().contains(" ") ? UrlStatus.INVALID_URL : UrlStatus.UNKNOWN;

        return of(url, initialStatus, UNKNOWN_LENGTH, UNKNOWN_LAST_MODIFIED, false);
    }

    public static UrlState of(URL url, UrlStatus status, boolean checkedOnline)
    {
        return of(url, status, UNKNOWN_LENGTH, UNKNOWN_LAST_MODIFIED, checkedOnline);
    }

    public static UrlState of(URL url, UrlStatus status, long contentLength, long lastModified, boolean checkedOnline)
    {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(status);

        return new UrlState(url, status, contentLength, lastModified, checkedOnline);
    }

    private final URL url;
    private final UrlStatus status;
    private final long contentLength;
    private final long lastModified;
    private final boolean checkedOnline;

    protected UrlState(URL url, UrlStatus status, long contentLength, long lastModified, boolean checkedOnline)
    {
        this.url = url;
        this.status = status;
        this.contentLength = contentLength;
        this.lastModified = lastModified;
        this.checkedOnline = checkedOnline;
    }

    public URL getUrl()
    {
        return url;
    }

    public UrlStatus getStatus()
    {
        return checkedOnline ? status : UrlStatus.UNKNOWN;
    }

    public UrlStatus getLastKnownStatus()
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

    public boolean wasCheckedOnline()
    {
        return checkedOnline;
    }

    /**
     * Return a {@link UrlState} object with the specified checkedOnline property,
     * but otherwise identical to the object from which the method is invoked. Note
     * that {@link UrlState} objects are immutable; this method simply returns the
     * object from which it was invoked if that object already has the specified
     * checkedOnline property.
     * 
     * @param checkedOnline the new checkedOnline status
     * @return the (possibly new) state.
     */
    public UrlState update(boolean checkedOnline)
    {
        return this.checkedOnline == checkedOnline ? this : of(url, status, contentLength, lastModified, checkedOnline);
    }

    /**
     * Return a {@link UrlState} object with the specified {@link UrlStatus}, but
     * otherwise identical to the object from which the method is invoked. Note that
     * {@link UrlState} objects are immutable; this method simply returns the object
     * from which it was invoked if that object already has the specified
     * {@link UrlStatus}.
     * 
     * @param checkedOnline the new checkedOnline status
     * @return the (possibly new) state.
     */
    public UrlState update(UrlStatus status)
    {
        return this.status == status //
                ? this : of(url, status, UNKNOWN_LENGTH, UNKNOWN_LAST_MODIFIED, checkedOnline);
    }

    /**
     * Return a {@link UrlState} object with the specified properties, but otherwise
     * identical to the object from which the method is invoked. Note that
     * {@link UrlState} objects are immutable; this method simply returns the object
     * from which it was invoked if that object already has the specified
     * properties.
     * 
     * @param checkedOnline the new checkedOnline status
     * @return the (possibly new) state.
     */
    public UrlState update(UrlStatus status, long contentLength, long lastModified)
    {
        return this.status == status && this.contentLength == contentLength && this.lastModified == lastModified //
                ? this : of(url, status, contentLength, lastModified, checkedOnline);
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
        result = prime * result + (checkedOnline ? 1 : 0);

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
            if (this.checkedOnline != that.checkedOnline)
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
                ", lastModified=" + lastModified + ", checkedOnline=" + checkedOnline + "]";
    }

}