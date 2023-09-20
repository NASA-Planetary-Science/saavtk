package edu.jhuapl.saavtk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

/**
 * Facility for wetting up user-name/password-based authentication using
 * {@link Authenticator}. It also includes support for storing/loading
 * credentials to/from a file.
 *
 * @author James Peachey
 *
 */
public class Authorizor
{
    private class SecureAuthenticator extends Authenticator
    {
        private final String userName;
        private final char[] password;

        private SecureAuthenticator(String userName, char[] password)
        {
            this.userName = userName;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(userName, password);
        }
    }
    
    @FunctionalInterface
	public interface UrlChecker {
		UrlState checkAccess();
	}

    private final Path credentialsFilePath;
    private final AtomicReference<SecureAuthenticator> authenticator;
    private final UrlChecker urlChecker;

    /**
     * Construct an authorizor that uses the provided path for the file used to
     * load/store credentials.
     *
     * @param credentialsFilePath the path to the credentials file
     */
	protected Authorizor(Path credentialsFilePath, UrlChecker urlChecker) {
		this.credentialsFilePath = credentialsFilePath;
		this.authenticator = new AtomicReference<>(null);
		this.urlChecker = urlChecker;
	}

    /**
     * Get the user name currently in use by the authorizor utility. May be null,
     * which indicates no valid credentials have been defined.
     *
     * @return the user name, or null if no valid credentials have been defined
     */
    public String getUserName()
    {
        SecureAuthenticator authenticator = this.authenticator.get();

        return authenticator != null ? authenticator.userName : null;
    }

    /**
     * Get the password currently in use by the Authorizor utility. May be null,
     * which indicates no valid credentials have been defined.
     * <P>
     * The returned char array is a copy of the current credential. You are responsible for
     * clearing out the copy of the returned array.
     *
     * @return the password, or null if no valid credentials have been defined
     */
    public char[] getPassword()
    {
        SecureAuthenticator authenticator = this.authenticator.get();

        if (authenticator == null)
      	  return null;
        if (authenticator.password == null)
      	  return null;

        return Arrays.copyOf(authenticator.password, authenticator.password.length);
    }

    /**
     * Return a flag indicating whether valid credentials (possibly the default
     * credentials) are defined for this authorizor.
     *
     * @return
     */
    public boolean isAuthorized()
    {
        return getUserName() != null;
    }

    /**
     * Return a flag indicating whether valid non-default credentials are defined
     * for this authorizor. Valid non-default credentials are defined if either the
     * {@link #loadCredentials()} or {@link #applyCredentials(String, char[])}
     * methods were called successfully, and if the defined user name that would be
     * returned by the {@link #getUserName()} method is *not* the same name that
     * would be returned by {@link #getDefaultUserName()}.
     * <p>
     * If this method returns true, so would {@link #isAuthorized()}
     *
     * @return true if valid credentials are defined, false otherwise
     */
    public boolean isValidCredentialsLoaded()
    {
        String userName = getUserName();

        return userName != null && !userName.equals(getDefaultUserName());
    }

    /**
     * Load credentials from the credentials file path and attempt authentication.
     * The result of the authentication attempt is indicated by the returned
     * {@line UrlState} object. If the results were successful, the user name will
     * be retained and may be obtained by future calls to the {@link #getUserName()}
     * method. If authentication did not succeed, future calls to {@link #getUserName()}
     * will return null.
     * <p>
     * This method may be called any number of times. The {@link #getUserName()}
     * method will always return the latest name defined (or null).
     *
     * @return the result of attempting authentication using the credentials loaded
     *         from the credentials file
     */
    public UrlState loadCredentials()
    {
        char[] password = null;
        try
        {
            String userName = null;

            try (BufferedReader reader = Files.newBufferedReader(credentialsFilePath))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    // Skip any blank lines.
                    if (line.matches("^\\s*$"))
                    {
                        continue;
                    }
                    if (userName == null)
                    {
                        userName = line;
                    }
                    else if (password == null)
                    {
                        password = line.toCharArray();
                    }
                    else
                    {
                        break;
                    }
                }

            }
            catch (Exception e)
            {

            }

            if (password == null)
            {
                userName = getDefaultUserName();
                char[] defaultPassword = getDefaultPassword();
                if (defaultPassword != null)
                {
                    password = Arrays.copyOf(defaultPassword, defaultPassword.length);
                }
            }

            UrlState result;
            if (userName != null && password != null)
            {
                result = applyCredentials(userName, password);
            }
            else
            {
                result = urlChecker.checkAccess();
            }

            return result;
        }
        finally
        {
            clearArray(password);
        }
    }

    /**
     * Attempt authentication using the provided credentials. The result of the
     * authentication attempt is indicated by the returned {@line UrlState} object.
     * If the results were successful, the user name will be retained and may be
     * obtained by future calls to the {@link #getUserName()} method. If
     * authentication did not succeed, future calls to {@link #getUserName()} will
     * return null.
     * <p>
     * This method may be called any number of times. The {@link #getUserName()}
     * method will always return the latest name defined (or null).
     * <p>
     * This method does not automatically save credentials to the credentials file.
     *
     * @param userName the user name to use in the attempt to define credentials
     * @param password the password to use in the attempt to define credentials
     * @return the result of attempting authentication using the credentials loaded
     *         from the credentials file
     */
    public UrlState applyCredentials(String userName, char[] password)
    {
        Preconditions.checkNotNull(userName);
        Preconditions.checkNotNull(password);

        char[] copyOfPassword = Arrays.copyOf(password, password.length);
        SecureAuthenticator authenticator = new SecureAuthenticator(userName, copyOfPassword);

        Authenticator.setDefault(null);
        Authenticator.setDefault(authenticator);

        UrlState rootState = urlChecker.checkAccess();

        if (rootState.getStatus() == UrlStatus.ACCESSIBLE)
        {
            // Accept the new authenticator; clean up the old one.
            authenticator = this.authenticator.getAndSet(authenticator);
            if (authenticator != null)
            {
                clearArray(authenticator.password);
            }
        }
        else
        {
            // Reject the new authenticator; restore the old one.
            Authenticator.setDefault(null);
            Authenticator.setDefault(this.authenticator.get());
            clearArray(copyOfPassword);
        }

        return rootState;
    }

    /**
     * Save current credentials to the disk file identified by this
     * {@link Authorizor}'s credentials path. If no valid credentials are defined,
     * this method will remove the credentials file. If the default credentials are
     * being used, the saved credentials file will exist, but it will be empty.
     * <p>
     * Parent directories will be created if necessary.
     *
     * @throws IOException if the file is not successfully created/updated/removed
     */
    public void saveCredentials() throws IOException
    {
        String userName = getUserName();
        if (userName != null)
        {
            java.nio.file.Files.createDirectories(credentialsFilePath.getParent());
            try (PrintStream stream = new PrintStream(new FileOutputStream(credentialsFilePath.toFile())))
            {
                if (!userName.equals(getDefaultUserName()))
                {
                    stream.println(userName);
                    stream.println(authenticator.get().password);
                }
            }
        }
        else
        {
            try
            {
                java.nio.file.Files.delete(credentialsFilePath);
            }
            catch (Exception e)
            {

            }
        }
    }

    /**
     * Return the default user name, that is, a user name that may be used to obtain
     * authorization in the absence of credentials defined by calling the
     * {@link #loadCredentials()} or {@link #applyCredentials(String, char[])}
     * method.
     * <p>
     * The base implementation does not define a default user name, but this method
     * may be overridden in subclasses to provide one. If this method returns
     * anon-null default user name, the {@link #getDefaultPassword()} method must
     * also return a valid default password.
     *
     * @return the default user name, which may be null (and will be null if the
     *         base implementation is called)
     */
    public String getDefaultUserName()
    {
        return null;
    }

    /**
     * Return the default password, that is, a password that may be used to obtain
     * authorization in the absence of credentials defined by calling the
     * {@link #loadCredentials()} or {@link #applyCredentials(String, char[])}
     * method.
     * <p>
     * The base implementation does not define a default password, but this method
     * may be overridden in subclasses to provide one. If this method returns
     * anon-null default password, the {@link #getDefaultUserName()} method must
     * also return a valid default user name.
     *
     * @return the default password, which may be null (and will be null if the base
     *         implementation is called)
     */
    protected char[] getDefaultPassword()
    {
        return null;
    }

    public static void main(String[] args)
    {
        URL dataRootUrl = Configuration.getDataRootURL();
        System.out.println("Testing authorizations against " + dataRootUrl);

        Authorizor auth = new Authorizor(SafeURLPaths.instance().get(System.getProperty("user.home"), "authorizer-password.txt"), () -> { return FileCache.instance().queryRootState(); });

        String userName = "joe";
        char[] password = null;

        try
        {
            password = setTestPassword(null, "joe's password");
            try
            {
                auth.saveCredentials();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            testCredentials(auth, userName, password);

            password = setTestPassword(password, "wide-open");

            testCredentials(auth, "public", password);
        }
        finally
        {
            clearArray(password);
        }
    }

    private static void testCredentials(Authorizor auth, String userName, char[] password)
    {
        try
        {
            UrlState urlState = auth.applyCredentials(userName, password);
            if (urlState.wasCheckedOnline())
            {
                System.out.println("User \"" + userName + "\", password \"" + new String(password) + "\": " + (urlState.getStatus() == UrlStatus.ACCESSIBLE ? "" : "not ") + "authorized");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static char[] setTestPassword(char[] password, String newPassword)
    {
        clearArray(password);

        return newPassword.toCharArray();
    }

    /**
     * Set to zero each element of a character array. Useful for cleaning up
     * password arrays when they are no longer needed (for security purposes).
     *
     * @param array the array to clear
     */
    static void clearArray(char[] array)
    {
        if (array != null)
        {
            for (int index = 0; index < array.length; ++index)
            {
                array[index] = 0;
            }
        }
    }

}
