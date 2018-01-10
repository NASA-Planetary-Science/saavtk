package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import edu.jhuapl.saavtk.util.FileCache.FileInfo;
import edu.jhuapl.saavtk.util.FileCache.FileInfo.YesOrNo;

public class Configuration
{
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
	private static boolean passwordAuthenticationSetup = false;

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

	public static void setupPasswordAuthentication(final String restrictedAccessRoot, final String restrictedFileName, Iterable<Path> passwordFilesToTry)
	{
		if (restrictedAccessRoot == null || restrictedFileName == null || passwordFilesToTry == null)
		{
			throw new NullPointerException();
		}
		if (!passwordFilesToTry.iterator().hasNext())
		{
			throw new IllegalArgumentException();
		}

		for (Path passwordFile : passwordFilesToTry)
        {
            if (passwordFile.toFile().exists())
            {
                List<String> credentials;
				try {
					credentials = FileUtil.getFileLinesAsStringList(passwordFile.toString());
					if (credentials.size() >= 2)
					{
						String user = credentials.get(0);
						String pass = credentials.get(1);
						
						if (user != null && user.trim().length() > 0 && !user.trim().toLowerCase().contains("replace-with-") &&
								pass != null && pass.trim().length() > 0)
						{
							setupPasswordAuthentication(user.trim(), pass.trim().toCharArray());
							FileInfo info = FileCache.getFileInfoFromServer(restrictedAccessRoot, restrictedFileName);
							if (!info.isURLAccessAuthorized().equals(YesOrNo.YES))
							{
								promptUserForPassword(restrictedAccessRoot, restrictedFileName, passwordFile);
							}
							return;
						}
					}
				} catch (@SuppressWarnings("unused") IOException e) {
					// Ignore -- maybe the next one will work.
				}
            }
        }

        // If we get here, no password file was found, so try to create the first one in the collection.
        promptUserForPassword(restrictedAccessRoot, restrictedFileName, passwordFilesToTry.iterator().next());
	}

	static private void promptUserForPassword(final String restrictedAccessRoot, final String restrictedFileName, Path passwordFile)
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JLabel promptLabel = new JLabel("If you have credentials to access restricted models, enter them here.");
		JLabel requestAccess = new JLabel("(Email sbmt@jhuapl.edu to request access)");
		
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

		mainPanel.add(promptLabel);
		mainPanel.add(requestAccess);
		mainPanel.add(namePanel);
		mainPanel.add(passwordPanel);

		boolean validOrCancel = false;
		while (!validOrCancel)
		{
			int selection = JOptionPane.showConfirmDialog(null, mainPanel, "Optional password for restricted model access", JOptionPane.OK_CANCEL_OPTION);
			if (selection == JOptionPane.OK_OPTION)
			{
				String name = nameField.getText();
				char[] password = passwordField.getPassword();
				setupPasswordAuthentication(name, password);
				FileInfo info = FileCache.getFileInfoFromServer(restrictedAccessRoot, restrictedFileName);
				if (info.isURLAccessAuthorized().equals(YesOrNo.YES))
				{
					validOrCancel = true;
					writePasswordFile(passwordFile, name, password);
				}
			}
			else
			{
				validOrCancel = true;
			}
		}
	}

	static public void setupPasswordAuthentication(final String username, final char[] password)
    {
        try
        {
            java.net.Authenticator.setDefault(new java.net.Authenticator()
            {
                @Override
				protected java.net.PasswordAuthentication getPasswordAuthentication()
                {
                    return new java.net.PasswordAuthentication(username, password);
                }
            });
            passwordAuthenticationSetup = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

	static public void writePasswordFile(final Path passwordFile, String name, char[] password)
	{
		try (PrintStream outStream = new PrintStream(Files.newOutputStream(passwordFile))) {
			outStream.println(name);
			outStream.println(password);
		} catch (IOException e) {
			// Unfortunate but there's nothing more we can do at this point.
			e.printStackTrace();
		}
	}

	static public boolean isPasswordAuthenticationSetup()
    {
    	return passwordAuthenticationSetup;
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
            cacheDir = Configuration.getApplicationDataDir() + File.separator +
            "cache" + File.separator + cacheVersion;
        }

        return cacheDir;
    }

//    public static String getWebURL()
//    {
//        return webURL;
//    }
//
//    public static void setWebURL(String webURL)
//    {
//        Configuration.webURL = webURL;
//        Configuration.rootURL = webURL + "/sbmt";
//    }

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
                rootURL = "http://sbmt.jhuapl.edu/internal/sbmt";
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

}
