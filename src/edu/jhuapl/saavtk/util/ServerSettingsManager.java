package edu.jhuapl.saavtk.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;

/**
 * This class manages settings concerning server availability, query latency and
 * the number of characters that may be used in a query. This class has the
 * ability to connect to the server and query a access-checking PHP script (if
 * present) to get a hint from the server what settings to use for the next
 * query.
 * <p>
 * If a successful query is made to the access-checking script, the
 * {@link ServerSettingsManager} assumes the server is accessible, and it
 * interprets the values returned from that connection to determine the wait
 * time before another query and the maximum length of the query.
 * <p>
 * If an exception is thrown while trying to query the access-checking script,
 * the {@link ServerSettingsManager} makes educated guesses about the cause and
 * returns heuristically tuned settings.
 * <p>
 * This class is thread-safe.
 * 
 * @author James Peachey
 *
 */
public class ServerSettingsManager
{
    /**
     * Immutable class that holds a snapshot of the server connection settings.
     */
    public final class ServerSettings
    {
        private final boolean isServerAccessible;
        private final int queryLength;
        private final int sleepInterval;

        protected ServerSettings(boolean isServerAccessible, int queryLength, int sleepInterval)
        {
            this.isServerAccessible = isServerAccessible;
            this.queryLength = queryLength;
            this.sleepInterval = sleepInterval;
        }

        public boolean isServerAccessible()
        {
            return isServerAccessible;
        }

        public int getQueryLength()
        {
            return queryLength;
        }

        public int getSleepInterval()
        {
            return sleepInterval;
        }

        @Override
        public String toString()
        {
            return "ServerSettings [isServerAccessible=" + isServerAccessible + ", queryLength=" + queryLength + ", sleepInterval=" + sleepInterval + "]";
        }
    }

    public static ServerSettingsManager instance()
    {
        ServerSettingsManager result;
        synchronized (Instance)
        {
            result = Instance.get();
            if (result == null)
            {
                URL checkServerScriptUrl;
                try
                {
                    String checkServerScriptName = SafeURLPaths.instance().getString(Configuration.getQueryRootURL(), "checkserver.php");
                    checkServerScriptUrl = new URL(checkServerScriptName);
                }
                catch (MalformedURLException e)
                {
                    throw new AssertionError(e);
                }

                result = new ServerSettingsManager(checkServerScriptUrl);
                result.update();

                Instance.set(result);
            }
        }

        return result;
    }

    protected static final int AbsoluteMinimumQueryLength = 100; // ~1 URL.
    protected static final int AbsoluteMaximumQueryLength = 10000; // ~100 URLs.

    // Under no circumstance check again before this.
    protected static final int AbsoluteMinimumSleepInterval = 4000;

    // Under no circumstance wait longer than this to check again.
    protected static final int AbsoluteMaximumSleepInterval = 40000;

    // This seems a good default for the current servers in the absence of a hint
    // from the server.
    protected static final int DefaultSleepInterval = 6000;

    // This seems a good amout of time to wait after a query that times out due to a
    // (hopefully) transient problem. Note this is for the benefit of the server, in
    // case the failed connection resulted from a high load on the server.
    protected static final int AfterTimeoutSleepInterval = 16000;

    protected static final AtomicReference<ServerSettingsManager> Instance = new AtomicReference<>(null);

    private final URL checkServerScriptUrl;
    private final AtomicReference<ServerSettings> settings;

    protected ServerSettingsManager(URL checkServerScriptURL)
    {
        this.checkServerScriptUrl = checkServerScriptURL;
        this.settings = new AtomicReference<>(new ServerSettings(false, AbsoluteMaximumQueryLength, DefaultSleepInterval));
    }

    public URL getServerScriptURL()
    {
        return checkServerScriptUrl;
    }

    /**
     * Return the current (as of the most recent check) server settings, without
     * triggering another check.
     * 
     * @return the current server settings
     */
    public ServerSettings get()
    {
        synchronized (this.settings)
        {
            return this.settings.get();
        }
    }

    /**
     * Attempt to connect to the server to check access using the access-checking
     * script. This updates the current server settings and returns the new
     * settings.
     * <p>
     * This method interprets any exceptions thrown to make guesses as to the server
     * access and, depending on the suspected cause, updates the settings
     * accordingly.
     * 
     * @return the new server settings
     */
    public ServerSettings update()
    {
        ServerSettings settings;
        try (CloseableUrlConnection closeableConn = CloseableUrlConnection.of(checkServerScriptUrl, HttpRequestMethod.GET))
        {
            URLConnection conn = closeableConn.getConnection();

            // Process the results of the query.
            List<String> lines = new ArrayList<>();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())))
            {
                String line;
                while ((line = in.readLine()) != null)
                {
                    if (CloseableUrlConnection.detectRejectionMessages(line))
                    {
                        FileCacheMessageUtil.debugCache().err().println("Request rejected for URL info from server-side script " + closeableConn.getUrl());
                        break;
                    }
                    else
                    {
                        lines.add(line);
                    }
                }
            }

            int numberLines = lines.size();

            int queryLength = AbsoluteMaximumQueryLength;
            int sleepInterval = DefaultSleepInterval;

            if (numberLines > 0)
            {
                queryLength = parseInt(lines.get(0), queryLength, AbsoluteMinimumQueryLength, AbsoluteMaximumQueryLength);
            }
            if (numberLines > 1)
            {
                sleepInterval = parseInt(lines.get(1), sleepInterval, AbsoluteMinimumSleepInterval, AbsoluteMaximumSleepInterval);
            }

            settings = new ServerSettings(true, queryLength, sleepInterval);
            FileCacheMessageUtil.debugCache().out().println("Updated server settings: " + settings);
        }
        catch (FileNotFoundException e)
        {
            // This could be a transient problem but more likely means that this script is
            // missing. Assume the server connection itself is OK, and wait the default time
            // before checking again.
            settings = new ServerSettings(true, AbsoluteMaximumQueryLength, DefaultSleepInterval);
            FileCacheMessageUtil.debugCache().err().println("No script to check server access: " + e.getClass().getName() + ": " + e.getMessage() + "\n\tUpdated server settings " + settings);
            e.printStackTrace(FileCacheMessageUtil.debugCache().err());
        }
        catch (IOException e)
        {
            // It is hoped this is a transient network issue, but in any case, the server is
            // probably not available now. Wait the standard post-timeout amount of time.
            settings = new ServerSettings(false, AbsoluteMaximumQueryLength, AfterTimeoutSleepInterval);
            FileCacheMessageUtil.debugCache().err().println("IOException checking server access: " + e.getClass().getName() + ": " + e.getMessage() + "\n\tUpdated server settings " + settings);
            e.printStackTrace(FileCacheMessageUtil.debugCache().err());
        }
        catch (Exception e)
        {
            // Not sure how this could happen, but guess that if the cause is an
            // IOException, the server is probably not available now. Wait the standard
            // post-timeout amount of time.
            Throwable t = e.getCause();
            if (t instanceof IOException)
            {
                settings = new ServerSettings(false, AbsoluteMaximumQueryLength, AfterTimeoutSleepInterval);
                FileCacheMessageUtil.debugCache().err().println(e.getClass().getName() + ": " + e.getMessage() + "\ncaused by " + t.getClass().getName() + ": " + t.getMessage() + "\n\tUpdated server settings " + settings);
            }
            else
            {
                // Cause is not IO exception. Not sure how this could even happen, but hope for
                // the best.
                settings = new ServerSettings(true, AbsoluteMaximumQueryLength, DefaultSleepInterval);
                FileCacheMessageUtil.debugCache().err().println(e.getClass().getName() + ": " + e.getMessage() + "\n\tUpdated server settings " + settings);
            }
            e.printStackTrace(FileCacheMessageUtil.debugCache().err());
        }

        synchronized (this.settings)
        {
            this.settings.set(settings);
        }

        return settings;
    }

    /**
     * Parse a string to get an integer. If parsing fails, return a default value.
     * If parsing succeeds, enforce a closed range.
     * 
     * @param intString the string that (it is hoped) may be parsed as an integer
     * @param defaultValue value to return should parsing fail
     * @param minimumValue minimum allowed value, if parsed value is smaller, return the minimum
     * @param maximumValue maximum allowed value, if parsed value is larger, return the maximum
     * @return
     */
    protected int parseInt(String intString, int defaultValue, int minimumValue, int maximumValue)
    {
        try
        {
            int result = Integer.parseInt(intString);

            if (result < minimumValue)
            {
                result = minimumValue;
            }
            else if (result > maximumValue)
            {
                result = maximumValue;
            }

            return result;
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

}
