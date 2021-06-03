package edu.jhuapl.saavtk.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern[] Patterns = new Pattern[] { //
            Pattern.compile("^<html>.*Request Rejected"), //
            Pattern.compile("^<meta http-equiv=\"refresh\" content=\"0;URL=/hub\">")
    };

    /**
     * Check for error messages formatted in HTML. Such messages may be returned
     * instead of the requested resource and downloaded as if they were that
     * resource. The result may be a downloaded or cached file with the correct name
     * but containing just the text of the error message, which can lead to
     * obfuscated problems downstream. This method returns true if it identifies any
     * known messages of this type in the input string. If the method returns true,
     * the caller may infer that the file content is probably not what was expected.
     * <p>
     * This method is a general utility, completely independent of all other aspects
     * of the {@link CloseableUrlConnection} class. It may safely be called/used in
     * any context, for example, even if one is just using {@link URLConnection}
     * directly.
     * 
     * @param line a line of text, presumably one that was read from an input stream
     *            opened from a {@link URLConnection} object
     * @return true if the text appears to be a known spurious error message, false
     *         otherwise
     */
    public static boolean detectRejectionMessages(String line)
    {
        boolean result = false;
        if (line != null)
        {
            for (Pattern pattern : Patterns)
            {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find())
                {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    private final URL url;
    private final String description;
    private final URLConnection connection;

    protected CloseableUrlConnection(URL url, HttpRequestMethod method) throws IOException
    {
        this.url = url;
        this.description = "Connection with method \"" + method + "\" from " + url.toString();
        this.connection = open(url, method);
    }

    public URL getUrl()
    {
        return url;
    }

    /**
     * Return the underlying {@link URLConnection}. The connection's
     * {@link URLConnection#connect()} method is not called during instantiation, so
     * the connection will *initially* be "unconnected". However it may become
     * connected after instantiation by either calling this class's
     * {@link #connect()} method or by calling {@link URLConnection#connect()}
     * directly on the connection returned by this method.
     * 
     * @return the connection
     */
    public URLConnection getConnection()
    {
        return connection;
    }

    /**
     * Connect the underlying connection and return its state. It is safe to call
     * this more than once to get an updated state, since
     * {@link URLConnection#connect()} ignores repeated calls.
     * <p>
     * This method does get attributes from the underlying connection, which counts
     * as reading from the stream, so do not call this method at all if it is
     * necessary to write to the connection.
     * 
     * @return the state
     * @throws IOException if any exceptions are thrown by any Java built-in classes
     */
    public UrlState connect() throws IOException
    {
        UrlStatus status;

        URLConnection connection = getConnection();

        if (connection instanceof HttpURLConnection)
        {
            try
            {
                int code = ((HttpURLConnection) connection).getResponseCode();

                // Codes in the 200 series are generally "ok" so treat any of them as
                // successful.
                if (code >= HttpURLConnection.HTTP_OK && code < HttpURLConnection.HTTP_MULT_CHOICE)
                {
                    status = UrlStatus.ACCESSIBLE;
                }
                else if (code == HttpURLConnection.HTTP_UNAUTHORIZED)
                {
                    status = UrlStatus.NOT_AUTHORIZED;
                }
                else if (code == HttpURLConnection.HTTP_NOT_FOUND || code == HttpURLConnection.HTTP_FORBIDDEN)
                {
                    status = UrlStatus.NOT_FOUND;
                }
                else
                {
                    status = UrlStatus.HTTP_ERROR;
                }
                debugConnectionMessage("response code = " + code + ", status = " + status);
            }
            catch (ProtocolException e)
            {
                // This indicates the request method isn't supported. That should not happen.
                throw new AssertionError(e);
            }
        }
        else
        {
            status = UrlStatus.ACCESSIBLE;
            debugConnectionMessage("non-http connection, status = " + status);
        }

        UrlState state;
        if (status == UrlStatus.ACCESSIBLE)
        {
            state = UrlState.of(url, status, connection.getContentLengthLong(), connection.getLastModified(), true);
        }
        else
        {
            state = UrlState.of(url, status, true);
        }

        return state;
    }

    /**
     * Open a URLConnection and initialize some settings. This method must not
     * invoke the {@link URLConnection#connect()} method, either directly or
     * indirectly. This is so that calling code may change settings on the
     * URLConnection before it is connected.
     * <p>
     * Also, be very careful if ever overriding this; it is called in the base class
     * constructor. In particular, don't call {@link #getConnection()} from any
     * implementation, as this method is used to create the connection field
     * returned by .
     * 
     * @param url the URL to open
     * @param method the request method for the connection
     * 
     * @return the {@link URLConnection}
     * @throws IOException if any Java built-in method throws one
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
        connection.setReadTimeout(30000);

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

    private void debugConnectionMessage(String message)
    {
        FileCacheMessageUtil.debugCache().err().println("Connected to " + url + ": " + message);
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
