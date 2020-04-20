package edu.jhuapl.saavtk.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import edu.jhuapl.saavtk.util.CloseableUrlConnection.HttpRequestMethod;

public class ServerSettingsManager
{
    public final class ServerSettings
    {
        private final boolean isServerAccessible;
        private final int queryLength;
        private final int sleepInterval;

        private ServerSettings(boolean isServerAccessible, int queryLength, int sleepInterval)
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

    private static final int AbsoluteMinimumQueryLength = 100; // ~1 URL.
    private static final int AbsoluteMaximumQueryLength = 10000; // ~100 URLs.
    private static final int AbsoluteMinimumSleepInterval = 5000; // 5 sec.
    private static final int AbsoluteMaximumSleepInterval = 120000; // 2 min.
    private static final AtomicReference<ServerSettingsManager> Instance = new AtomicReference<>(null);

    private final URL checkServerScriptUrl;
    private final AtomicReference<ServerSettings> settings;

    protected ServerSettingsManager(URL checkServerScriptURL)
    {
        this.checkServerScriptUrl = checkServerScriptURL;
        this.settings = new AtomicReference<>(new ServerSettings(false, AbsoluteMaximumQueryLength, AbsoluteMinimumSleepInterval));
    }

    public URL getServerScriptURL()
    {
        return checkServerScriptUrl;
    }

    public ServerSettings get()
    {
        synchronized (this.settings)
        {
            return this.settings.get();
        }
    }

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
                    if (line.matches(("^<html>.*Request Rejected.*")))
                    {
                        FileCacheMessageUtil.debugCache().err().println("Request rejected for URL info from server-side script " + conn.getURL());
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
            int sleepInterval = AbsoluteMinimumSleepInterval;

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
        catch (Exception e)
        {
            settings = null;
            FileCacheMessageUtil.debugCache().err().println(e.getClass().getName() + ": " + e.getMessage());
        }

        synchronized (this.settings)
        {
            if (settings != null)
            {
                this.settings.set(settings);
            }
            else
            {
                ServerSettings currentSettings = this.settings.get();
                settings = currentSettings.isServerAccessible ? new ServerSettings(false, currentSettings.queryLength, currentSettings.sleepInterval) : currentSettings;
            }
        }

        return settings;
    }

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
