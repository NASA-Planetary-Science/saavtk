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

    public static CloseableUrlConnection of(URL url, HttpRequestMethod method) throws IOException
    {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(method);

        return new CloseableUrlConnection(url, method);
    }

    private final String description;
    private final URLConnection connection;

    protected CloseableUrlConnection(URL url, HttpRequestMethod method) throws IOException
    {
        this.description = "Connection with method \"" + method + "\" from " + url.toString();
        this.connection = open(url, method);
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
     * @param method
     * 
     * @return
     * @throws IOException
     */
    protected URLConnection open(URL url, HttpRequestMethod method) throws IOException
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

        if (connection instanceof HttpURLConnection)
        {
            ((HttpURLConnection) connection).setRequestMethod(method.toString());
        }

        connection.setUseCaches(false);
        connection.setRequestProperty("User-Agent", "Mozilla/4.0");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

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

//    private static class MyConnection extends CloseableUrlConnection
//    {
//        MyConnection(URL url, HttpRequestMethod method) throws IOException
//        {
//            super(url, method);
//        }
//
//        @Override
//        protected URLConnection open(@SuppressWarnings("unused") URL url, @SuppressWarnings("unused") HttpRequestMethod method) throws IOException
//        {
//            return null;
//        }
//
//    }
//
//    public static void main(String[] args)
//    {
//        try
//        {
//            UrlInfo urlInfo = UrlInfo.of(new URL("http://spud.com"));
//            try (CloseableUrlConnection connection = new MyConnection(urlInfo.getState().getUrl(), HttpRequestMethod.HEAD))
//            {
//                System.err.println(connection);
//
//                System.err.println(new URL("http://MyCaseSensie/c:\\url\\foRMed\\file.txt").getPath());
//            }
//            catch (IOException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        catch (MalformedURLException e)
//        {
//            e.printStackTrace();
//        }
//    }
}
