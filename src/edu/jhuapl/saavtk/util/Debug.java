package edu.jhuapl.saavtk.util;

import java.io.PrintStream;

import org.apache.commons.io.output.NullOutputStream;

/**
 * Debug messaging utility. Provides methods to retrieve debugging output and
 * error streams, which are either the corresponding {@link System} output/error
 * streams (if debugging is enabled) or a null output stream that ignores what
 * is sent to it (if debugging is disabled).
 * <p>
 * Developers can dynamically control debug messages at a global or local level
 * using this class's static methods. However, individual instances of this
 * class are completely immutable. This is to make it less confusing to use this
 * facility in a multi-threading environment. For information on how to control
 * debugging at runtime, see the method descriptions.
 * <p>
 * This class itself is thread safe.
 */
public abstract class Debug
{
    /**
     * The static {@link Debug} object that forwards everything to the corresponding
     * standard stream defined by the {@link System} class.
     */
    private static final Debug DO_DEBUG = new Debug() {

        @Override
        public PrintStream err()
        {
            return System.err;
        }

        @Override
        public PrintStream out()
        {
            return System.out;
        }

        @Override
        public String toString()
        {
            return "Debug messages enabled";
        }

    };

    /**
     * A {@link PrintStream} that just ignores whatever output is sent its way.
     */
    private static final PrintStream NULL_STREAM = new PrintStream(new NullOutputStream());

    /**
     * The static {@link Debug} object that forwards everything to the stream that
     * ignores whatever output is sent its way.
     */
    private static final Debug DONT_DEBUG = new Debug() {

        @Override
        public PrintStream err()
        {
            return NULL_STREAM;
        }

        @Override
        public PrintStream out()
        {
            return NULL_STREAM;
        }

        @Override
        public String toString()
        {
            return "Debug messages disabled";
        }

    };

    /**
     * Runtime-switchable pointer to the current {@link Debug} object.
     */
    private static volatile Debug debug = DONT_DEBUG;

    /**
     * Return a flag indicating whether the global debug state is enabled or
     * disabled.
     * 
     * @return true if debugging messages are forwarded to {@link System} streams,
     *         false if debugging messages are ignored.
     */
    public static boolean isEnabled()
    {
        return debug == DO_DEBUG;
    }

    /**
     * Set the global debug state to enabled or disabled. The current setting of
     * this field determines which {@link Debug} object is returned by the
     * {@link #of()} method.
     * 
     * @param enabled if true, the global state is debug enabled, if false disabled
     */
    public static void setEnabled(boolean enabled)
    {
        debug = enabled ? DO_DEBUG : DONT_DEBUG;
    }

    /**
     * Return the current global {@link Debug} object. The object returned will
     * provide streams defined in {@link System} if global debugging is enabled, or
     * an object that returns a null stream if global debugging is disabled.
     * <p>
     * Because instances of {@link Debug} are completely immutable, it is
     * recommended you do not cache the output of this method in production code.
     * Doing so would make the affected code unresponsive to changes in the global
     * debug state.
     * 
     * @return the default global {@link Debug} object currently in effect
     * @see {@link #setEnabled(boolean)}
     */
    public static Debug of()
    {
        return debug;
    }

    /**
     * Get a {@link Debug} object with the desired functional state, either enabled
     * or disabled. Note that the returned object is immutable, so it will either
     * always be enabled or disabled.
     * <p>
     * Because instances of {@link Debug} are completely immutable, it is
     * recommended that the output of this method not be cached in production code.
     * Instead this method should be called each time an output stream is needed.
     * <p>
     * This method is not affected by the global debug status. Thus be very
     * selective about using it in production code, since it can be used to make
     * create debugging output that cannot easily be suppressed. This method is
     * mainly intended for temporary use when debugging a specific piece of code.
     * 
     * @param enable if true the returned object will return streams defined by the
     *            {@link System} class. If false, the returned object's streams will
     *            ignore output sent to them.
     * @return the desired {@link Debug} object
     */
    public static Debug of(boolean enable)
    {
        return enable ? DO_DEBUG : DONT_DEBUG;
    }

    private Debug()
    {

    }

    public abstract PrintStream err();

    public abstract PrintStream out();

}
