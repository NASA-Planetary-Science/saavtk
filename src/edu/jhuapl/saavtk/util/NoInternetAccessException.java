package edu.jhuapl.saavtk.util;

import java.net.URL;

// TODO this should extend Exception and thus be checked.
public class NoInternetAccessException extends RuntimeException
{
    private static final long serialVersionUID = -1250977612922624246L;
    private final URL url;

    public NoInternetAccessException(String cause, URL url)
    {
        super(cause);
        this.url = url;
    }

    public NoInternetAccessException(Exception cause, URL url)
    {
        super(cause);
        this.url = url;
    }

    public URL getURL()
    {
        return url;
    }
}