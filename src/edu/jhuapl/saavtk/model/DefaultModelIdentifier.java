package edu.jhuapl.saavtk.model;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import edu.jhuapl.saavtk.util.Configuration;

/**
 * All static class to handle operations concerning which model to load when a
 * new client launches. Thread-safe.
 *
 * @author James Peachey
 *
 */
public class DefaultModelIdentifier
{
    /**
     * This is the client-specific "factory" default model identifier. Normally this
     * should be set exactly once when the client starts.
     */
    private static final AtomicReference<String> ClientDefaultModel = new AtomicReference<>();

    /**
     * User-selected default model identifier. Kept in sync with the file that makes
     * the default model persistent between runs.
     */
    private static final AtomicReference<String> UserDefaultModel = new AtomicReference<>();

    /**
     * The current default model, which is most likely (but not necessarily) either
     * the user default or the client default. Either of the other defaults could be
     * null or unavailable.
     */
    private static final AtomicReference<String> DefaultModel = new AtomicReference<>();

    /**
     * File used to store the user default model identifier.
     */
    private static final AtomicReference<File> UserDefaultModelFile = new AtomicReference<>();

    /**
     * All static class.
     */
    private DefaultModelIdentifier()
    {
        throw new AssertionError();
    }

    /**
     * @return the client-specific "factory" default model identifier. May be null
     *         if {@link #setClientDefaultModel(String)} has never been called, or
     *         called with a null String.
     */
    public static String getClientDefaultModel()
    {
        return ClientDefaultModel.get();
    }

    /**
     * Set the client-specific "factory" default model identifier to the specified
     * model.
     *
     * @param modelId client-specific default model; may be null
     */
    public static void setClientDefaultModel(String modelId)
    {
        ClientDefaultModel.set(modelId);
    }

    /**
     * Return the user-selected default model identifier. A return value of null
     * indicates that no user-selected default model identifier is currently
     * defined, nor is there a file recording any user-selected default.
     *
     * @return the user-selected default model identifier; may be null
     */
    public static String getUserDefaultModel()
    {
        synchronized (UserDefaultModel)
        {
            initUserDefaultModel();

            return UserDefaultModel.get();
        }
    }

    /**
     * Set the user-selected default model identifier in the running client and in
     * the file that makes persistent the user's selected default model.
     * <p>
     * Calling this method with a null argument will also erase the file used to
     * make persistent the user-selected default model. Before you do that, consider
     * calling {@link #factoryReset()}, which calls this method but also ensures
     * this class will be left in a self-consistent state.
     *
     * @param modelId the new user-selected default model; may be null
     * @see #factoryReset()
     */
    public static void setUserDefaultModel(String modelId)
    {
        synchronized (UserDefaultModel)
        {
            initUserDefaultModel();

            UserDefaultModel.set(modelId);

            File file = UserDefaultModelFile.get();

            if (modelId != null)
            {
                try (FileWriter writer = new FileWriter(file))
                {
                    writer.write(modelId);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    file.delete();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Return the "as-flown" default model identifier. If
     * {@link #setDefaultModel(String)} was most recently called with a non-null
     * value, this method will return that value. Otherwise, if
     * {@link #getUserDefaultModel()} returns a non-null value, that will be
     * returned. If that too is null, then the value returned by
     * {@link #getClientDefaultModel()} will be returned.
     * 
     * @return the "as-flown" default model identifier
     */
    public static String getDefaultModel()
    {
        synchronized (UserDefaultModel)
        {
            String defaultModel = DefaultModel.get();

            if (defaultModel == null)
            {
                defaultModel = getUserDefaultModel();
            }

            if (defaultModel == null)
            {
                defaultModel = getClientDefaultModel();
            }

            return defaultModel;
        }
    }

    /**
     * Set the "as-flown" default model identifier to the specified model. This is
     * used to indicate if a model other than one designated by
     * {@link #getUserDefaultModel()} or {@link #getClientDefaultModel()} was
     * loaded.
     *
     * @param modelId actual default model loaded; may be null
     */
    public static void setDefaultModel(String modelId)
    {
        synchronized (UserDefaultModel)
        {
            DefaultModel.set(modelId);
        }
    }

    /**
     * Reset this class to its "factory" setting. This method clears any defined
     * user default model, and removes the file used to make persistent the user
     * default model identifier.
     * <p>
     * If the model returned by {@link #getDefaultModel()} is/was the same as the
     * user-selected model, it too will be cleared.
     */
    public static void factoryReset()
    {
        synchronized (UserDefaultModel)
        {
            String userDefault = getUserDefaultModel();
            String asFlownDefault = getDefaultModel();

            setUserDefaultModel(null);

            if (userDefault == asFlownDefault || (userDefault != null && userDefault.equals(asFlownDefault)))
            {
                setDefaultModel(null);
            }
        }
    }

    /**
     * Initialization that is performed only once the first time any method that
     * uses {@link #UserDefaultModel}. The first time this is called, it attempts to
     * read the user-selected default model identifier from persistent storage and
     * sets the retrieved value in {@link #UserDefaultModel}.
     * <p>
     * This method identifies the first time it is called by checking whether
     * {@value #UserDefaultModelFile} has been set. If it is set to a non-null
     * value, this method does NOTHING.
     * <p>
     * At the end of this method, it is guaranteed that
     * {@link #UserDefaultModelFile}'s content is non-null, and that
     * {@value #UserDefaultModel} is set to the value that was in the persistent
     * file, if it exists, or null if it does not.
     */
    private static void initUserDefaultModel()
    {
        synchronized (UserDefaultModel)
        {
            File file = UserDefaultModelFile.get();
            if (file == null)
            {
                file = Paths.get(Configuration.getApplicationDataDir(), "defaultModelToLoad").toFile();
                UserDefaultModelFile.set(file);

                if (file.isFile())
                {
                    String defaultModel = null;
                    try (Scanner scanner = new Scanner(file))
                    {
                        if (scanner.hasNextLine())
                        {
                            defaultModel = scanner.nextLine();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    UserDefaultModel.set(defaultModel);
                }
            }
        }
    }

}
