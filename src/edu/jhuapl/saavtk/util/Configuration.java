package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;

import edu.jhuapl.saavtk.util.FileCache.FileInfo;
import edu.jhuapl.saavtk.util.FileCache.FileInfo.YesOrNo;

public class Configuration
{
	private static final int DEFAULT_MAXIMUM_NUMBER_TRIES = 3;

	private static final String INITIAL_MESSAGE =
		"<html>The Small Body Mapping Tool will work without a password, but data for some models is restricted.<br>If you have credentials to access restricted models, enter them here.</html>";
	
    static private String webURL = "http://sbmt.jhuapl.edu";
    static private String rootURL = webURL + "/sbmt";
    static private String helpURL = webURL;

    static private String appName = null;
    static private String appDir = null;
    static private String appTitle = null;
    static private String cacheDir = null;
    static private String cacheVersion = "";
    static private boolean useFileCache = true;
    static private String mapMaperDir = null;

    // Flag indicating if this version of the tool is APL in-house only ("private")
    static private boolean APLVersion = false;
	private static boolean userPasswordAccepted = false;
	private static String restrictedAccessRoot = null;
	private static String restrictedFileName = null;
	private static Iterable<Path> passwordFilesToTry = null;

// Uncomment the following to enable the startup script (which can be changed by the user)
// to specify the web server URL:
//
//    static
//    {
//        // If the user sets the sbmt.root.url property then use that
//        // as the root URL. Otherwise use the default.
//        String rootURLProperty = System.getProperty("sbmt.root.url");
//        if (rootURLProperty != null)
//            rootURL = rootURLProperty;
//    }

	public static void setupPasswordAuthentication(final String restrictedAccessRoot, final String restrictedFileName, final Iterable<Path> passwordFilesToTry) throws IOException
	{
		if (restrictedAccessRoot == null || restrictedFileName == null || passwordFilesToTry == null)
		{
			throw new NullPointerException();
		}
		if (!passwordFilesToTry.iterator().hasNext())
		{
			throw new IllegalArgumentException();
		}

		Configuration.restrictedAccessRoot = restrictedAccessRoot;
		Configuration.restrictedFileName = restrictedFileName;
		Configuration.passwordFilesToTry = passwordFilesToTry;

		boolean foundEmptyPasswordFile = false;
		boolean userPasswordAccepted = false;
		final int maximumNumberTries = 1;

		// First confirm queries for information at least work. If not, don't try to update credentials.
		FileInfo info = FileCache.getFileInfoFromServer(restrictedAccessRoot, restrictedFileName);
		if (!info.isURLAccessAuthorized().equals(YesOrNo.NO))
		{
			return;
		}
		for (Path passwordFile : passwordFilesToTry)
        {
            if (passwordFile.toFile().exists())
            {
                List<String> credentials;
				try {
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
								info = FileCache.getFileInfoFromServer(restrictedAccessRoot, restrictedFileName);
								if (info.isURLAccessAuthorized().equals(YesOrNo.YES))
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
			userPasswordAccepted = promptUserForPassword(restrictedAccessRoot, restrictedFileName, passwordFilesToTry.iterator().next(), false);
		}
		if (!userPasswordAccepted)
		{			
			setupPasswordAuthentication("public", "wide-open".toCharArray(), maximumNumberTries);
		}
		Configuration.userPasswordAccepted = userPasswordAccepted;
	}

	private static boolean promptUserForPassword(final String restrictedAccessRoot, final String restrictedFileName, final Path passwordFile, final boolean updateMode) throws IOException
	{
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
		boolean validPasswordEntered = false;
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
					// Blank password is acceptable, but is not considered "valid" in the sense of this method.
					name = null;
					password = null;
				}
				else
				{
					// Attempt authentication.
					setupPasswordAuthentication(name, password, maximumNumberTries);					
					FileInfo info = FileCache.getFileInfoFromServer(restrictedAccessRoot, restrictedFileName);
					if (!info.isURLAccessAuthorized().equals(YesOrNo.YES))
					{
						// Try again.
						promptLabel.setText("<html>Invalid user name or password. Try again, or click \"Cancel\" to continue without password. Some models may not be available.</html>");
						repromptUser = true;
						continue;
					}
					validPasswordEntered = true;
				}
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
					JOptionPane.showMessageDialog(null, "You must restart the tool for this change to take effect.", "Password changes saved", JOptionPane.INFORMATION_MESSAGE);	
				}
			}
		} while (repromptUser);
		return validPasswordEntered;
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
            java.net.Authenticator.setDefault(new java.net.Authenticator()
            {
            	final Map<URL, Integer> triedCount = new HashMap<>();

            	@Override
				protected java.net.PasswordAuthentication getPasswordAuthentication()
                {
            		final URL url = getRequestingURL();
            		int count = triedCount.containsKey(url) ? triedCount.get(url) : 0;
            		if (count < maximumNumberTries)
            		{
            			triedCount.put(url, count + 1);
            			return new java.net.PasswordAuthentication(username, password);
            		}
            		// Oddly enough, this does the trick to prevent repeatedly trying a wrong password,
            		// while throwing a RuntimeException doesn't work. It appears that null is interpreted
            		// as meaning the user failed to provide credentials, so it just returns an appropriate
            		// HTTP code back up the stack. Nice!
            		// By contrast, the RuntimeException was not catchable because the authorization attempt
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
		try (PrintStream outStream = new PrintStream(Files.newOutputStream(passwordFile))) {
			if (name != null && password != null)
			{
				outStream.println(name);
				outStream.println(password);				
			}
		}
	}

	public static void updatePassword() throws IOException {
		if (restrictedAccessRoot == null || restrictedFileName == null || passwordFilesToTry == null)
		{
			throw new AssertionError("Cannot update password; authentication was not properly initialized.");
		}
		promptUserForPassword(restrictedAccessRoot, restrictedFileName, passwordFilesToTry.iterator().next(), true);
		
	}

	public static boolean wasUserPasswordAccepted()
    {
    	return userPasswordAccepted;
    }

    /**
     * @return Return the location where all application specific files should be stored. This is within
     * the .neartool folder located in the users home directory.
     */
    static public String getApplicationDataDir()
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
     * @return
     */
    static public String getCacheDir()
    {
        if (cacheDir == null)
        {
            cacheDir = SafePaths.getString(Configuration.getApplicationDataDir(), "cache", cacheVersion);
        }

        return cacheDir;
    }

    public static String getRootURL()
    {
        return rootURL;
    }

    public static void setRootURL(String rootURL)
    {
        Configuration.rootURL = rootURL;
    }

    /**
     * @return Return the url of the server where data is downloaded from.
     */
    static public String getDataRootURL()
    {
        return rootURL + "/data";
    }

    static public String getQueryRootURL()
    {
        return rootURL + "/query";
    }

    static public String getHelpRootURL()
    {
        if ( isAPLVersion() ) {
            return helpURL + "/internal/";
        }
        else {
            return helpURL + "/";
        }
    }

    static public String getImportedShapeModelsDir()
    {
        return getApplicationDataDir() + File.separator + "models";
    }

    static public String getCustomGalleriesDir()
    {
        String tmpDir = getApplicationDataDir() + File.separator + "custom-galleries";
        File dir = new File(tmpDir);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        return tmpDir;
    }
    
    static public String getMapmakerDir()
    {
        return mapMaperDir;
    }

    static public void setMapmakerDir(String folder)
    {
        mapMaperDir = folder;
    }

    static public boolean isMac()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("mac");
    }

    static public boolean isLinux()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    static public boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    /**
     * Get a short name for the application, from which is derived the location for SAAVTK to
     * cache files on the user's machine.
     * @return the current application name.
     */
    static public String getAppName()
    {
    	return appName;
    }

    /**
     * Set a short name for the application, from which is derived the location for SAAVTK to
     * cache files on the user's machine. This method must be called exactly one time, and it
     * should not contain any whitespace or newlines.
     * @param name the short name
     */
    static public void setAppName(String name)
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
     * Return the application's title, if any, which may be used for cosmetic purposes to identify
     * a specific application, e.g., when reporting version information or naming the application
     * on a startup page.
     * @return the title
     */
    public static String getAppTitle()
    {
    	return appTitle;
    }

    /**
     * Set the application's title. The title may be used for cosmetic purposes to identify
     * a specific application, e.g., when reporting version information or naming the application
     * on a startup page. It should be concise but meaningful like any good title. This attribute
     * is optional, and may not contain contain any printable characters except newlines.
     * @param appTitle the title
     */
    public static void setAppTitle(String appTitle)
    {
    	Configuration.appTitle = appTitle;
    }

    static public void setCacheVersion(String cv)
    {
        cacheVersion = cv;
    }

    static public void setAPLVersion(boolean b)
    {
        APLVersion = b;

        // If APL version, then change root URL to the default internal root URL
        // unless user set sbmt.root.url property.
        if (APLVersion)
        {
            String rootURLProperty = System.getProperty("sbmt.root.url");
            if (rootURLProperty != null)
            {
            	rootURL = rootURLProperty;
            }
            else
            {
                rootURL = "http://sbmt.jhuapl.edu/sbmt/prod";
            }
        }
    }

    static public void setUseFileCache(boolean use)
    {
        useFileCache = use;
    }

    static public boolean useFileCache() { return useFileCache; }

    static public boolean isAPLVersion()
    {
        return APLVersion;
    }

    static public String getCustomDataFolderForBuiltInViews()
    {
        return getApplicationDataDir() + File.separator + "custom-data";
    }

    static public String getTempFolder()
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

	public static void clearCache() {
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
}
