package edu.jhuapl.saavtk.util;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.DownloadableFileInfo.DownloadableFileState;
import edu.jhuapl.saavtk.util.UrlInfo.UrlStatus;

/**
 * Static class containing general settings needed by any application.
 */
public class Configuration
{
    private static Boolean headless = null;
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();
    private static final int DEFAULT_MAXIMUM_NUMBER_TRIES = 3;

    private static final String INITIAL_MESSAGE =
            "<html>The Small Body Mapping Tool will work without a password, but data for some models is restricted.<br>If you have credentials to access restricted models, enter them here.</html>";

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
    private static volatile boolean userPasswordAccepted = false;
    private static URL restrictedAccessRoot = null;
    private static Iterable<Path> passwordFilesToTry = null;
    private static final AtomicBoolean authenticationSuccessful = new AtomicBoolean(false);

    // Uncomment the following to enable the startup script (which can be changed by
    // the user)
    // to specify the web server URL:
    //
    // static
    // {
    // // If the user sets the sbmt.root.url property then use that
    // // as the root URL. Otherwise use the default.
    // String rootURLProperty = System.getProperty("sbmt.root.url");
    // if (rootURLProperty != null)
    // rootURL = rootURLProperty;
    // }

    public static boolean isHeadless()
    {
        if (headless == null)
        {
            headless = Boolean.parseBoolean(System.getProperty("java.awt.headless"));
        }

        return headless;
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

    public static void setupPasswordAuthentication(final URL restrictedAccessRoot, final Iterable<Path> passwordFilesToTry)
    {
        if (restrictedAccessRoot == null || passwordFilesToTry == null)
        {
            throw new NullPointerException();
        }
        if (!passwordFilesToTry.iterator().hasNext())
        {
            throw new IllegalArgumentException();
        }

        if (authenticationSuccessful.get())
        {
            return;
        }

        Configuration.restrictedAccessRoot = restrictedAccessRoot;
        Configuration.passwordFilesToTry = passwordFilesToTry;

        boolean foundEmptyPasswordFile = false;
        boolean userPasswordAccepted = false;
        final int maximumNumberTries = 1;

        String restrictedAccessString = restrictedAccessRoot.toString();

        // The only condition that can be addressed here is if the user is not
        // authorized. If that's not the "problem", don't do anything.
        DownloadableFileState info = FileCache.getState(restrictedAccessString);
        if (info.getUrlState().getStatus() != UrlStatus.NOT_AUTHORIZED)
        {
            return;
        }
        for (Path passwordFile : passwordFilesToTry)
        {
            if (passwordFile.toFile().exists())
            {
                List<String> credentials;
                try
                {
                    boolean foundCredentials = false;
                    credentials = FileUtil.getFileLinesAsStringList(passwordFile.toString());
                    Iterator<String> iterator = credentials.iterator();
                    if (iterator.hasNext())
                    {
                        String userName = iterator.next().trim();
                        if (iterator.hasNext())
                        {
                            char[] password = iterator.next().trim().toCharArray();
                            if (!userName.isEmpty() && password.length > 0)
                            {
                                foundCredentials = true;
                                setupPasswordAuthentication(userName, password, maximumNumberTries);
                                info = FileCache.refreshStateInfo(restrictedAccessString);
                                if (info.getUrlState().getStatus() == UrlStatus.ACCESSIBLE)
                                {
                                    userPasswordAccepted = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!foundCredentials)
                    {
                        foundEmptyPasswordFile = true;
                    }
                }
                catch (@SuppressWarnings("unused") IOException e)
                {
                    // Ignore -- maybe the next one will work.
                }
            }
        }

        if (!userPasswordAccepted && !foundEmptyPasswordFile)
        {
            userPasswordAccepted = promptUserForPassword(restrictedAccessString, passwordFilesToTry.iterator().next(), false);
        }
        if (!userPasswordAccepted)
        {
            setupPasswordAuthentication("public", "wide-open".toCharArray(), maximumNumberTries);
            info = FileCache.refreshStateInfo(restrictedAccessString);
        }

        Configuration.userPasswordAccepted = userPasswordAccepted;

        authenticationSuccessful.set(userPasswordAccepted || foundEmptyPasswordFile);

        FileCache.instance().queryAllInBackground(true);
    }

    private static boolean promptUserForPassword(final String restrictedAccessUrl, final Path passwordFile, final boolean updateMode)
    {
        // Prevent re-issuing prompts after valid credentials were used once.
        if (authenticationSuccessful.get() && !updateMode)
        {
            return true;
        }
        else if (isHeadless())
        {
            return false;
        }

        try
        {
            runAndWaitOnEDT(() -> {
                JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                JLabel promptLabel = new JLabel(INITIAL_MESSAGE);
                JLabel requestAccess = new JLabel("<html><br>(Email sbmt@jhuapl.edu to request access)</html>@");

                JPanel namePanel = new JPanel();
                namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
                namePanel.add(new JLabel("Username:"));
                JTextField nameField = new JTextField(15);
                namePanel.add(nameField);

                JPanel passwordPanel = new JPanel();
                passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
                passwordPanel.add(new JLabel("Password:"));
                JPasswordField passwordField = new JPasswordField(15);
                passwordPanel.add(passwordField);

                JCheckBox rememberPasswordCheckBox = new JCheckBox("Do not prompt for a password in the future (save/clear credentials).");
                rememberPasswordCheckBox.setSelected(true);

                mainPanel.add(promptLabel);
                mainPanel.add(requestAccess);
                mainPanel.add(namePanel);
                mainPanel.add(passwordPanel);
                mainPanel.add(rememberPasswordCheckBox);

                boolean repromptUser = false;
                final int maximumNumberTries = 1;
                do
                {
                    repromptUser = false;
                    int selection = JOptionPane.showConfirmDialog(null, mainPanel, "Small Body Mapping Tool: Optional Password", JOptionPane.OK_CANCEL_OPTION);
                    boolean rememberPassword = rememberPasswordCheckBox.isSelected();
                    String name = nameField.getText().trim();
                    char[] password = passwordField.getPassword();
                    if (selection == JOptionPane.OK_OPTION)
                    {
                        if (name.isEmpty())
                        {
                            // Blank password is acceptable, but is not considered "valid" in the sense of
                            // this method.
                            name = null;
                            password = null;
                        }
                        else
                        {
                            // Attempt authentication.
                            setupPasswordAuthentication(name, password, maximumNumberTries);
                            DownloadableFileState state = FileCache.refreshStateInfo(restrictedAccessUrl);
                            UrlStatus status = state.getUrlState().getStatus();
                            if (status == UrlStatus.NOT_AUTHORIZED)
                            {
                                // Try again.
                                promptLabel.setText("<html>Invalid user name or password. Try again, or click \"Cancel\" to continue without password. Some models may not be available.</html>");
                                repromptUser = true;
                                continue;
                            }
                            else if (status != UrlStatus.ACCESSIBLE)
                            {
                                // Try again.
                                promptLabel.setText("<html>Server problem. Try again, or click \"Cancel\" to continue without password. If this persists, contact sbmt.jhuapl.edu. Some models may not be available without a password.</html>");
                                repromptUser = true;
                                continue;
                            }
                            authenticationSuccessful.set(status == UrlStatus.ACCESSIBLE);
                        }
                        try
                        {
                            if (rememberPassword)
                            {
                                writePasswordFile(passwordFile, name, password);
                            }
                            else
                            {
                                deleteFile(passwordFile);
                            }
                            if (updateMode)
                            {
                                JOptionPane.showMessageDialog(null, "Password updated.", "Password changes saved", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Unable to update password. See console for more details.", "Failed to save password", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                while (repromptUser);

            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return authenticationSuccessful.get();
    }

    public static void setupPasswordAuthentication(final String username, final char[] password)
    {
        setupPasswordAuthentication(username, password, DEFAULT_MAXIMUM_NUMBER_TRIES);
    }

    public static void setupPasswordAuthentication(final String username, final char[] password, final int maximumNumberTries)
    {
        if (username == null || password == null)
        {
            throw new NullPointerException();
        }
        if (maximumNumberTries < 1)
        {
            throw new IllegalArgumentException();
        }
        try
        {
            // Clear out any previous credentials.
            java.net.Authenticator.setDefault(null);

            // Now try to set up authentication using the new credentials.
            java.net.Authenticator.setDefault(new java.net.Authenticator() {
                final Map<String, Integer> triedCount = new HashMap<>();

                @Override
                protected java.net.PasswordAuthentication getPasswordAuthentication()
                {
                    final URL url = getRequestingURL();
                    final String urlString = url.toString();
                    int count = triedCount.containsKey(urlString) ? triedCount.get(urlString) : 0;
                    if (count < maximumNumberTries)
                    {
                        triedCount.put(urlString, count + 1);
                        return new java.net.PasswordAuthentication(username, password);
                    }
                    // Oddly enough, returning null (eee below) prevents repeatedly trying a wrong
                    // password, while throwing a RuntimeException doesn't work. It appears that
                    // null is interpreted as meaning the user failed to provide credentials, so it
                    // just returns an appropriate HTTP code back up the stack. Nice! By contrast,
                    // the RuntimeException was not catchable because the authorization attempt
                    // occurred in a different thread.
                    return null;
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void writePasswordFile(final Path passwordFile, final String name, final char[] password) throws IOException
    {
        try (PrintStream outStream = new PrintStream(Files.newOutputStream(passwordFile)))
        {
            if (name != null && password != null)
            {
                outStream.println(name);
                outStream.println(password);
            }
        }
    }

    public static void updatePassword() throws IOException
    {
        if (restrictedAccessRoot == null || passwordFilesToTry == null)
        {
            throw new AssertionError("Cannot update password; authentication was not properly initialized.");
        }
        promptUserForPassword(restrictedAccessRoot.toString(), passwordFilesToTry.iterator().next(), true);
        FileCache.instance().queryAllInBackground(true);

    }

    public static boolean wasUserPasswordAccepted()
    {
        return userPasswordAccepted;
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

    public static void setCacheDir(String cacheDir)
    {
        Preconditions.checkNotNull(cacheDir);

        Configuration.cacheDir = SafeURLPaths.instance().getString(cacheDir);
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

    private static void deleteFile(Path path) throws IOException
    {
        try
        {
            Files.delete(path);
        }
        catch (@SuppressWarnings("unused") NoSuchFileException e)
        {
            // Give me a break. Deleting a file that doesn't exist throws an exception?
            // Who cares?
        }
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
