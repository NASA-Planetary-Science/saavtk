package edu.jhuapl.saavtk.util;

import java.net.URL;

// TODO this should extend Exception and thus be checked.
public class NonexistentRemoteFile extends RuntimeException
{
    private static final long serialVersionUID = 7671006960310656926L;
    private final URL url;

    public NonexistentRemoteFile(String cause, URL url)
    {
        super(cause);
        this.url = url;
    }

    public NonexistentRemoteFile(Exception cause, URL url)
    {
        super(cause);
        this.url = url;
    }

    public URL getURL()
    {
        return url;
    }
}