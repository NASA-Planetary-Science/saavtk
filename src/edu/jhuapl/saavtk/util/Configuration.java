package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

/**
 * Static class containing general settings needed by any application.
 */
public class Configuration
{
    private static Boolean headless = null;
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    private static String webURL = "http://sbmt.jhuapl.edu";
    private static URL rootURL = createUrl(webURL + "/sbmt/prod");
    private static URL dataRootURL = createUrl(rootURL + "/data");
    private static String helpURL = webURL;

    private static String appName = null;
    private static String appDir = null;
    private static String appTitle = null;
    private static String cacheDir = null;
    private static String cacheVersion = "";
    private static boolean useFileCache = true;
    private static String mapMaperDir = null;
    private static String databaseSuffix = "";

    // Flag indicating if this version of the tool is APL in-house only ("private")
    private static boolean APLVersion = false;

    private static final AtomicReference<AuthorizorSwingUtil> SwingAuthorizor = new AtomicReference<>();

    public static boolean isHeadless()
    {
        if (headless == null)
        {
            headless = GraphicsEnvironment.isHeadless();
        }

        return headless;
    }

    public static void authenticate()
    {
        getAuthorizor().loadCredentials();
    }

    public static Authorizor getAuthorizor()
    {
        return getSwingAuthorizor().getAuthorizor();
    }

    public static AuthorizorSwingUtil getSwingAuthorizor()
    {
        SwingAuthorizor.compareAndSet(null, AuthorizorSwingUtil.of(SAFE_URL_PATHS.get(getApplicationDataDir(), "password.txt")));

        return SwingAuthorizor.get();
    }

    public static void runOnEDT(Runnable runnable)
    {
        Preconditions.checkNotNull(runnable);

        if (isHeadless())
        {
            runnable.run();
        }
        else
        {
            EventQueue.invokeLater(runnable);
        }
    }

    public static void runAndWaitOnEDT(Runnable runnable) throws InvocationTargetException, InterruptedException
    {
        Preconditions.checkNotNull(runnable);

        if (isHeadless())
        {
            runnable.run();
        }
        else if (EventQueue.isDispatchThread())
        {
            runnable.run();
        }
        else
        {
            EventQueue.invokeAndWait(runnable);
        }
    }

    /**
     * @return Return the location where all application specific files should be
     *         stored. This is within the .neartool folder located in the users home
     *         directory.
     */
    public static String getApplicationDataDir()
    {
        if (appDir == null)
        {
            if (appName == null)
            {
                appName = "saavtk";
                System.err.println("Warning: application name was not set; setting it to the default value of \"" + appName + "\"");
            }
            appDir = System.getProperty("user.home") + File.separator + "." + appName;

            // if the directory does not exist, create it
            File dir = new File(appDir);
            if (!dir.exists())
            {
                dir.mkdir();
            }
        }

        return appDir;
    }

    /**
     * The cache folder is where files downloaded from the server are placed. The
     * URL of server is returned by getDataRootURL()
     * 
     * @return
     */
    public static String getCacheDir()
    {
        if (cacheDir == null)
        {
            cacheDir = SafeURLPaths.instance().getString(Configuration.getApplicationDataDir(), "cache", cacheVersion);
        }

        return cacheDir;
    }

    public static URL getRootURL()
    {
        return rootURL;
    }

    public static void setRootURL(String rootURL)
    {
        Configuration.rootURL = createUrl(rootURL);
        Configuration.dataRootURL = createUrl(SAFE_URL_PATHS.getString(rootURL, "data"));
    }

    /**
     * @return Return the url of the server where data is downloaded from.
     */
    public static URL getDataRootURL()
    {
        return dataRootURL;
    }

    public static String getQueryRootURL()
    {
        return rootURL + "/query";
    }

    public static String getHelpRootURL()
    {
        if (isAPLVersion())
        {
            return helpURL + "/internal/";
        }
        else
        {
            return helpURL + "/";
        }
    }

    public static String getImportedShapeModelsDir()
    {
        return getApplicationDataDir() + File.separator + "models";
    }

    public static String getCustomGalleriesDir()
    {
        String tmpDir = getApplicationDataDir() + File.separator + "custom-galleries";
        File dir = new File(tmpDir);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        return tmpDir;
    }

    public static String getMapmakerDir()
    {
        return mapMaperDir;
    }

    public static void setMapmakerDir(String folder)
    {
        mapMaperDir = folder;
    }

    public static boolean isMac()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("mac");
    }

    public static boolean isLinux()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    public static boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    public static void setDatabaseSuffix(String suffix)
    {
        databaseSuffix = suffix;
    }

    public static String getDatabaseSuffix()
    {
        return databaseSuffix;
    }

    /**
     * Get a short name for the application, from which is derived the location for
     * SAAVTK to cache files on the user's machine.
     * 
     * @return the current application name.
     */
    public static String getAppName()
    {
        return appName;
    }

    /**
     * Set a short name for the application, from which is derived the location for
     * SAAVTK to cache files on the user's machine. This method must be called
     * exactly one time, and it should not contain any whitespace or newlines.
     * 
     * @param name the short name
     */
    public static void setAppName(String name)
    {
        if (appName == null)
        {
            appName = name;
        }
        else
        {
            throw new UnsupportedOperationException("Cannot change the app name -- it was already set to " + appName);
        }
    }

    /**
     * Return the application's title, if any, which may be used for cosmetic
     * purposes to identify a specific application, e.g., when reporting version
     * information or naming the application on a startup page.
     * 
     * @return the title
     */
    public static String getAppTitle()
    {
        return appTitle;
    }

    /**
     * Set the application's title. The title may be used for cosmetic purposes to
     * identify a specific application, e.g., when reporting version information or
     * naming the application on a startup page. It should be concise but meaningful
     * like any good title. This attribute is optional, and may not contain contain
     * any printable characters except newlines.
     * 
     * @param appTitle the title
     */
    public static void setAppTitle(String appTitle)
    {
        Configuration.appTitle = appTitle;
    }

    public static void setCacheVersion(String cv)
    {
        cacheVersion = cv;
    }

    public static void setAPLVersion(boolean b)
    {
        APLVersion = b;
    }

    public static void setUseFileCache(boolean use)
    {
        useFileCache = use;
    }

    public static boolean useFileCache()
    {
        return useFileCache;
    }

    public static boolean isAPLVersion()
    {
        return APLVersion;
    }

    public static String getCustomDataFolderForBuiltInViews()
    {
        return getApplicationDataDir() + File.separator + "custom-data";
    }

    public static String getTempFolder()
    {
        String tmpDir = getApplicationDataDir() + File.separator + "tmp";
        File dir = new File(tmpDir);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        return tmpDir;
    }

    public static void clearCache()
    {
        String cacheDir = getCacheDir();
        if (cacheDir != null)
        {
            System.err.println("Clearing the cache for all models in the directory " + cacheDir);
            File file = new File(cacheDir);
            if (file.exists())
            {
                try
                {
                    FileUtils.deleteDirectory(file);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private static URL createUrl(String url)
    {
        try
        {
            return new URL(url);
        }
        catch (MalformedURLException e)
        {
            throw new AssertionError(e);
        }
    }

    private Configuration()
    {
        throw new AssertionError();
    }

}
