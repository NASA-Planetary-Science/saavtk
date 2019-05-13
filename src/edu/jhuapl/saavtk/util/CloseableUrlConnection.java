package edu.jhuapl.saavtk.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.google.common.base.Preconditions;

public class CloseableUrlConnection implements Closeable
{

    public enum HttpRequestMethod
    {
        GET,
        HEAD
    }

    public static CloseableUrlConnection of(UrlInfo urlInfo, HttpRequestMethod method) throws IOException
    {
        Preconditions.checkNotNull(urlInfo);
        Preconditions.checkNotNull(method);

        return new CloseableUrlConnection(urlInfo, method);
    }

    private final String description;
    private final URLConnection connection;

    protected CloseableUrlConnection(UrlInfo urlInfo, HttpRequestMethod method) throws IOException
    {
        this.description = "Connection with method \"" + method + "\" from " + urlInfo.toString();
        URL url = urlInfo.getState().getUrl();
        this.connection = open(url, urlInfo, method);
    }

    public URLConnection getConnection()
    {
        return connection;
    }

    /**
     * Careful overriding this; it is called in the base class constructor. In
     * particular, don't call getConnection() from any implementation, as this
     * method is used to create the connection field.
     * 
     * @param url TODO
     * @param urlInfo
     * @param method
     * 
     * @return
     * @throws IOException
     */
    protected URLConnection open(URL url, UrlInfo urlInfo, HttpRequestMethod method) throws IOException
    {
        URL uncachedUrl;
        try
        {
            // Force URLConnection not to return results cached by client or server by
            // requiring that they
            // be cached right now.
            uncachedUrl = new URL(url + "?cacheFrom=" + System.currentTimeMillis());
        }
        catch (MalformedURLException e)
        {
            throw new AssertionError(e);
        }

        URLConnection connection = uncachedUrl.openConnection();

        Debug.out().println("Opened connection " + (method == HttpRequestMethod.HEAD ? "for info to " : "to download ") + url);

        if (connection instanceof HttpURLConnection)
        {
            ((HttpURLConnection) connection).setRequestMethod(method.toString());
        }

        connection.setUseCaches(false);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        return connection;
    }

    @Override
    public void close() throws IOException
    {
        if (connection instanceof HttpURLConnection)
        {
            ((HttpURLConnection) connection).disconnect();
        }
    }

    @Override
    public String toString()
    {
        return description;
    }

    private static class MyConnection extends CloseableUrlConnection
    {
        MyConnection(UrlInfo urlInfo, HttpRequestMethod method) throws IOException
        {
            super(urlInfo, method);
        }

        @Override
        protected URLConnection open(@SuppressWarnings("unused") URL url, @SuppressWarnings("unused") UrlInfo urlInfo, @SuppressWarnings("unused") HttpRequestMethod method) throws IOException
        {
            System.err.println("bleh!");

            return null;
        }

    }

    public static void main(String[] args)
    {
        try
        {
            UrlInfo urlInfo = UrlInfo.of(new URL("http://spud.com"));
            try (CloseableUrlConnection connection = new MyConnection(urlInfo, HttpRequestMethod.HEAD))
            {
                System.err.println(connection);

                System.err.println(new URL("http://MyCaseSensie/c:\\url\\foRMed\\file.txt").getPath());
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
}
