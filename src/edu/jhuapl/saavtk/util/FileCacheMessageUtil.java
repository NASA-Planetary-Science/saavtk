package edu.jhuapl.saavtk.util;

import java.io.PrintStream;

/**
 * Utility that uses {@link Debug} to handle different types of messages. This
 * class is public because file cache code is contained in multiple packages.
 * However, this code is intended for use only by file-cache related utilities.
 * 
 * @author James Peachey
 *
 */
public class FileCacheMessageUtil
{
    private static volatile boolean enableDebugCache = false;
    private static volatile boolean enableInfoMessages = true;

    /**
     * Return the appropriate {@link Debug} object for the current mode of cache
     * debugging, as described in the {@link #isDebugCache()} and
     * {@link #enableDebugCache} methods.
     * <p>
     * The returned object is intended to be used for messages of use for debugging
     * the file cache itself.
     * 
     * @return the {@link Debug} object
     */
    public static synchronized Debug debugCache()
    {
        return Debug.of(enableDebugCache);
    }

    /**
     * Return a flag indicating whether cache debugging is enabled. If this method
     * returns true, then the {@link Debug} object returned by the
     * {@link #debugCache()} method will provide functioning streams, i.e., debug
     * messages sent to that stream will be displayed.
     * 
     * @return the flag indicating whether cache debugging is enabled
     */
    public static synchronized boolean isDebugCache()
    {
        return enableDebugCache;
    }

    /**
     * Enable or disable the streams used by the {@link Debug} object returnedf by
     * the {{@link #debugCache()} method.
     * 
     * @param enableCacheDebug true to enable debugging streams, false to disable
     */
    public static synchronized void enableDebugCache(boolean enableCacheDebug)
    {
        FileCacheMessageUtil.enableDebugCache = enableCacheDebug;
    }

    /**
     * Enable or disable the streams returned by the {@link #info()} and
     * {@link #err()} methods.
     * 
     * @param enableInfoMessages true to enable messages, false to disable
     */
    public static synchronized void enableInfoMessages(boolean enableInfoMessages)
    {
        FileCacheMessageUtil.enableInfoMessages = enableInfoMessages;
    }

    /**
     * Return a stream that may be used to display informational messages concerning
     * the function of the file cache. These messages are normally visible to the
     * user in the console log, so this should be used sparingly for user-friendly
     * information only.
     * <p>
     * The returned stream will not display any messages if
     * {@link #enableInfoMessages(boolean)} has been called with an argument of
     * false.
     * 
     * @return the informational stream
     */
    public static synchronized PrintStream info()
    {
        return Debug.of(enableInfoMessages).out();
    }

    /**
     * Return a stream that may be used to display error messages concerning the
     * function of the file cache. These messages are normally visible to the user
     * in the console log, so this should be used sparingly for user-friendly
     * information only.
     * <p>
     * The returned stream will not display any messages if
     * {@link #enableInfoMessages(boolean)} has been called with an argument of
     * false.
     * 
     * @return the informational stream
     */
    public static synchronized PrintStream err()
    {
        return Debug.of(enableInfoMessages).err();
    }

}
