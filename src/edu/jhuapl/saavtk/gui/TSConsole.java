package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.google.common.base.Preconditions;

/**
 * Static utility that redirects standard output and error streams so that
 * messages sent to these streams appear in a GUI-displayable form
 * ({@link JTextArea}). This utility also can "tee" the standard streams so that
 * messages sent to them are also sent to secondary output and/or error streams.
 * These two capabilities function separately, allowing the caller to choose
 * either or both behaviors.
 * <p>
 * Once the console has been successfully configured, all its methods are safe
 * to call when running headless, though those that return GUI objects (e.g.,
 * {@link JFrame}) will throw exceptions if the GUI mode was not enabled at
 * configure time.
 * <p>
 * When running headless, secondary streams can still be used after the console
 * is configured. See
 * {@link #configure(boolean, String, PrintStream, PrintStream)} for more
 * details.
 * <p>
 * A copy of this class is maintained in the saatvk repository for use by the
 * SBMT. If revising it, need to test it in both contexts. The "original" class
 * is part of the timeseries package used by MIDL.
 * 
 * @author Larry Brown, heavily revised 2022-07-03 by James Peachey
 *
 */
public class TSConsole
{

    private static final AtomicReference<Boolean> IsHeadless = new AtomicReference<>();
    private static final AtomicReference<TSConsole> Instance = new AtomicReference<>();

    public static void configure(boolean enableGui)
    {
        configure(enableGui, null, null, null);
    }

    public static void configure(boolean enableGui, String title)
    {
        configure(enableGui, title, null, null);
    }

    /**
     * Configure the {@link TSConsole}, choosing to enable the GUI (or not), giving
     * a title for the GUI window (if enabled), and optional secondary streams for
     * error and output messages. Depending on the inputs, standard error and output
     * streams may be redirected. More specifically:
     * <ol>
     * <li>enableGui == false, secondaryErr == secondaryOut == null: no streams
     * redirected, primary error stream remains {@link System#err} and primary
     * output stream remains {@link System#out}
     * <li>enableGui == true: both {@link System#err} and {@link System#out} will be
     * redirected to the new primary stream, which is shown in the Console window
     * when it is being displayed
     * <li>secondaryErr and/or secondaryOut non-null: {@link System#err} and/or
     * {@link System#out} will be "tee"d so that messages appear on the primary
     * stream and the relevant secondary stream
     * </ol>
     * <p>
     * This method will throw an exception if it is called more than once.
     * 
     * @param enableGui if true, the console's GUI behavior will be enabled
     * @param title given to the GUI window (ignored if enableGui is false)
     * @param secondaryErr optional secondary stream to which to display error
     *            messages, or null for no secondary error stream
     * @param secondaryOut optional secondary stream to which to display output
     *            messages, or null for no secondary output stream
     */
    public static void configure(boolean enableGui, String title, PrintStream secondaryErr, PrintStream secondaryOut)
    {
        synchronized (Instance)
        {
            Preconditions.checkState(Instance.get() == null, "Console may only be configured once.");

            String frameTitle = title != null ? title : "MIDL Console";

            // Determine whether to redirect standard streams based on whether it would be
            // allowed. Note
            // that this is handled completely separately from the issue of whether or not
            // the GUI
            // behavior of the console will be enabled.
            boolean doRedirect = true;
            Exception securityException = null;
            try
            {
                // Check whether redirecting standard streams is allowed in this runtime
                // environment.
                SecurityManager manager = System.getSecurityManager();
                if (manager != null)
                    manager.checkPermission(new RuntimePermission("setIO"));
            }
            catch (Exception e)
            {
                doRedirect = false;
                securityException = e;
            }

            // Determine whether to disable the GUI behavior of the console (if running
            // headless).
            boolean forceDisable = false;
            if (enableGui && isHeadless())
            {
                enableGui = false;
                forceDisable = true;
            }

            AtomicReference<JFrame> frameReference = new AtomicReference<>();
            AtomicReference<JTextArea> textAreaReference = new AtomicReference<>();

            // Set up the GUI behavior.
            boolean guiWasEnabled = false;
            if (enableGui)
            {
                guiWasEnabled = runOnEdt(true, () -> {
                    // Add Copy/Cut/Paste popup menu to Text Components (specifically our Console
                    // replacement)
                    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TCPopupEventQueue());

                    JFrame f = new JFrame(frameTitle);
                    JTextArea ta = new JTextArea(40, 80);

                    JScrollPane consolePane = new JScrollPane(ta);
                    f.add(consolePane);

                    frameReference.set(f);
                    textAreaReference.set(ta);
                }, secondaryErr);
            }

            // Create a stream for writing to the GUI.
            OutputStream textAreaStream = guiWasEnabled ? createStream(textAreaReference.get()) : null;

            if (doRedirect)
            {
                boolean autoFlush = false;

                // In principle, redirect standard streams, but only do it if it will actually
                // change where
                // they go.
                if (textAreaStream != null || secondaryErr != null)
                {
                    OutputStream err = textAreaStream != null ? textAreaStream : System.err;
                    try
                    {
                        System.setErr(new PrintStream(createStream(err, secondaryErr), autoFlush));
                    }
                    catch (Exception e)
                    {
                        throw new AssertionError("Unexpected exception when redirecting standard error stream", e);
                    }
                }

                if (textAreaStream != null || secondaryOut != null)
                {
                    OutputStream out = textAreaStream != null ? textAreaStream : System.out;
                    try
                    {
                        System.setOut(new PrintStream(createStream(out, secondaryOut), autoFlush));
                    }
                    catch (Exception e)
                    {
                        throw new AssertionError("Unexpected exception when redirecting standard output stream", e);
                    }
                }
            }

            Instance.set(new TSConsole(frameReference.get()));

            // Done with setting up the console. The remainder of this method is for
            // reporting anything
            // that didn't quite set up right, using whichever streams are available.
            StringBuilder sb = new StringBuilder();

            String delim = "";
            if (forceDisable)
            {
                sb.append(delim);
                sb.append("Unable to set up GUI console (running headless).");
            }
            else if (enableGui && !guiWasEnabled)
            {
                sb.append(delim);
                sb.append("Failed to set up GUI console.");
            }
            delim = "\n";

            if (securityException != null)
            {
                sb.append(delim);

                if (textAreaStream != null)
                {
                    sb.append("Unable to redirect streams; GUI console cannot receive messages.");
                }
                else
                {
                    sb.append("Unable to redirect streams.");
                }

                delim = "\n";
            }

            String message = sb.toString();

            // Print any messages to the standard error stream in any case.
            if (!message.isEmpty())
            {
                System.err.println(message);
                if (securityException != null)
                {
                    securityException.printStackTrace();
                }
            }

            if (!doRedirect)
            {
                // Implies securityException is non-null, and message is not empty. Also,
                // streams were not
                // redirected, so report exceptions to the other streams individually.

                if (secondaryErr != null && secondaryErr != System.err)
                {
                    secondaryErr.println(message);
                    if (securityException != null)
                    {
                        securityException.printStackTrace(secondaryErr);
                    }
                }

                // Write the message and close the textAreaStream stream, since it will be going
                // out of
                // scope.
                try (PrintStream ps = new PrintStream(textAreaStream))
                {
                    ps.println(message);
                    if (securityException != null)
                    {
                        securityException.printStackTrace(ps);
                    }
                }

            }

            if (!message.isEmpty())
            {
                runOnEdt(false, () -> {
                    JOptionPane.showMessageDialog(null, message);
                });
            }

        }
    }

    public static boolean isHeadless()
    {
        Boolean isHeadless;

        // Don't do it this way because then the call to System.getProperty would be
        // made every time,
        // thus defeating whatever performance gain comes of caching this result:
        //
        // IsHeadless.compareAndSet(null,
        // Boolean.valueOf(System.getProperty("java.awt.headless",
        // "false")));
        // return IsHeadless.get();
        //
        synchronized (IsHeadless)
        {
            isHeadless = IsHeadless.get();

            if (isHeadless == null)
            {
                try
                {
                    isHeadless = Boolean.valueOf(System.getProperty("java.awt.headless", "false"));
                }
                catch (Exception e)
                {
                    // This probably won't ever happen. Hope for the best, but the most likely thing
                    // here
                    // would be a SecurityException. If we got that for this call, probably more
                    // trouble lies
                    // ahead.
                    isHeadless = Boolean.FALSE;
                    e.printStackTrace();
                }

                IsHeadless.set(isHeadless);
            }
        }

        return isHeadless.booleanValue();
    }

    /**
     * Return a flag that indicates whether the console has been configured using
     * one of the <code>configure(...)</code> overloads. Except as noted, this
     * class's other methods throw exceptions if they are called before the console
     * has been configured.
     * 
     * @return true if the console has been configured, false otherwise
     */
    public static boolean isConfigured()
    {
        synchronized (Instance)
        {
            return Instance.get() != null;
        }
    }

    /**
     * Return a flag that indicates whether the console has been configured with its
     * GUI behavior enabled. If enabled, standard stream output will appear in a GUI
     * window when it is bewing displayed. This method gets information on the state
     * of the console only, and it is safe to call anytime.
     * 
     * @return true if the GUI mode of the console was enabled at configure time
     */
    public static boolean isEnabled()
    {
        synchronized (Instance)
        {
            TSConsole instance = Instance.get();
            return instance != null && instance.consoleFrame != null;
        }
    }

    /**
     * Return the frame containing the console, so that the caller may display/hide
     * it.
     * 
     * @return the console frame, or null if GUI mode is not enabled
     */
    public static JFrame getConsoleFrame()
    {
        return getConsole().consoleFrame;
    }

    /**
     * Display the console's GUI, assuming that the console is being embedded in
     * some other window. If the console is not enabled, a message to that effect is
     * written to the error stream(s) instead.
     */
    public static void showConsole()
    {
        getConsole().doShowConsole(false);
    }

    /**
     * Display the console's GUI in a standalone top-level window. If the console is
     * not enabled, a message to that effect is written to the error stream(s)
     * instead.
     */
    public static void showStandaloneConsole()
    {
        getConsole().doShowConsole(true);
    }

    /**
     * If the console is enabled, hide it asynchronously. Does nothing if the
     * console is not enabled.
     */
    public static void hideConsole()
    {
        if (isEnabled())
        {
            runOnEdt(false, () -> {
                JFrame consoleFrame = getConsole().consoleFrame;

                consoleFrame.setVisible(false);
                consoleFrame.setLocationByPlatform(false);
            });
        }
    }

    /**
     * If the console is enabled, set the location where it will appear, and do so
     * synchronously. Does not actually display the console. Does nothing if the
     * console is not enabled (always safe to call).
     * 
     * @param relativeTo component relative to which to locate the console
     */
    public static void setDefaultLocation(Component relativeTo)
    {
        if (isEnabled())
        {
            runOnEdt(true, () -> {
                JFrame consoleFrame = getConsole().consoleFrame;

                consoleFrame.setLocationRelativeTo(relativeTo);
            });
        }
    }

    /**
     * If the console is enabled, asynchronously add to the specified menu bar a
     * "Consoles" menu item that can show or hide the console. Does nothing if the
     * console is not enabled (always safe to call).
     * 
     * @param menuBar the menu bar to which to add the console menu
     */
    public static void addConsoleMenu(final JMenuBar menuBar)
    {
        if (isEnabled())
        {
            runOnEdt(false, () -> {
                JMenu consoleMenu = new JMenu("Console");

                JMenuItem toggleConsoleItem = new JMenuItem("Toggle Console");
                consoleMenu.add(toggleConsoleItem);
                toggleConsoleItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        if (isEnabled())
                        {
                            if (getConsole().consoleFrame.isVisible())
                            {
                                hideConsole();
                            }
                            else
                            {
                                showConsole();
                            }
                        }
                        else
                        {
                            reportConsoleDisabled();
                        }
                    }
                });

                menuBar.add(consoleMenu);
            });
        }
    }

    /**
     * Return the singleton console instance, throwing an
     * {@link IllegalStateException} if it was not configured. Never returns null.
     * 
     * @return the console
     */
    private static TSConsole getConsole()
    {
        TSConsole instance;

        synchronized (Instance)
        {
            instance = Instance.get();
        }

        Preconditions.checkState(instance != null, "Must initialize console before using it");

        return instance;
    }

    private final JFrame consoleFrame;

    private TSConsole(JFrame consoleFrame)
    {
        this.consoleFrame = consoleFrame;
    }

    private void doShowConsole(boolean exitOnClose)
    {
        if (isEnabled())
        {
            runOnEdt(false, () -> {
                int operation = exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.HIDE_ON_CLOSE;

                consoleFrame.setDefaultCloseOperation(operation);
                consoleFrame.pack();
                consoleFrame.setVisible(true);
                consoleFrame.toFront();
                consoleFrame.repaint();
            });
        }
        else
        {
            reportConsoleDisabled();
        }
    }

    private static void reportConsoleDisabled()
    {
        String message = "In-app console is disabled.";
        System.err.println(message);

        runOnEdt(false, () -> {
            JOptionPane.showMessageDialog(null, message);
        });
    }

    /**
     * Run a {@link Runnable} on the EDT, either synchronously (waiting until the
     * runnable has run before returning) or asynchronously (using
     * {@link EventQueue#invokeLater(Runnable)}). In headless mode, this method does
     * nothing.
     * 
     * @param sync if true, invoke later; if false, wait for the runnable to finish
     * @param r the runnable to run
     */
    private static boolean runOnEdt(boolean sync, Runnable r)
    {
        return runOnEdt(sync, r, null);
    }

    private static synchronized boolean runOnEdt(boolean sync, Runnable r, PrintStream secondaryErr)
    {

        if (isHeadless())
        {
            return false;
        }

        try
        {

            if (sync)
            {
                if (EventQueue.isDispatchThread())
                {
                    r.run();
                }
                else
                {
                    EventQueue.invokeAndWait(r);
                }
            }
            else
            {
                EventQueue.invokeLater(r);
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();

            if (secondaryErr != null && secondaryErr != System.err)
            {
                e.printStackTrace(secondaryErr);
            }

            return false;
        }
    }

    private static final int BufSize = 8192;

    private static OutputStream createStream(JTextArea textArea)
    {

        return new AbstractBufferingOutputStream(BufSize) {

            @Override
            protected void doWrite(byte[] b, int off, int len) throws IOException
            {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < len; ++i)
                {
                    sb.append((char) b[off + i]);
                }

                String s = sb.toString();

                if (s.endsWith("Thre"))
                {
                    new Object();
                }

                Runnable r = () -> {
                    // Put the string version of the buffer into the text area
                    textArea.append(s);
                    // Make sure the bottom of the data is showing in the window
                    textArea.setCaretPosition(textArea.getDocument().getLength());

                };

                runOnEdt(false, r);

            }

        };
    }

    /**
     * Return an {@link OutputStream} instance that fans out all characters sent to
     * it to the specified streams (os). Duplicate streams and null streams in the
     * arguments are ignored. At least one non-null stream must be specified as an
     * argument.
     * 
     * @param os the streams to which to fan out
     * @return the gestalt stream
     * @throws IllegalArgumentException if none of the specified streams are
     *             non-null
     * @throws NullPointerException if this is called with an explicitly null
     *             argument list
     */
    public static OutputStream createStream(OutputStream... os)
    {
        Preconditions.checkNotNull(os, "Cannot call this method explicitly passing null");

        // Validate os and make a list of all the non-null streams.
        LinkedHashSet<OutputStream> osSet = new LinkedHashSet<>();
        for (OutputStream s : os)
        {
            if (s != null)
            {
                osSet.add(s);
            }
        }

        // Make sure at least one non-null stream was specified.
        Preconditions.checkArgument(!osSet.isEmpty(), "No non-null streams to merge");

        if (osSet.size() == 1)
        {
            return osSet.iterator().next();
        }
        else
        {

            return new AbstractBufferingOutputStream(BufSize) {

                @Override
                protected void doWrite(byte[] b, int off, int len) throws IOException
                {
                    IOException e0 = null;
                    for (OutputStream s : osSet)
                    {
                        try
                        {
                            s.write(b, off, len);
                        }
                        catch (Exception e)
                        {
                            if (e0 == null)
                            {
                                e0 = new IOException(e);
                            }
                        }
                    }
                }

                @Override
                public void flush() throws IOException
                {
                    IOException e0 = null;
                    for (OutputStream s : osSet)
                    {
                        try
                        {
                            s.flush();
                        }
                        catch (Exception e)
                        {
                            if (e0 == null)
                            {
                                e0 = new IOException(e);
                            }
                        }
                    }

                    super.flush();

                    if (e0 != null)
                    {
                        throw e0;
                    }
                }

            };
        }
    }

    // public static void main(String[] args) {
    // File file = Paths.get(System.getProperty("user.home"),
    // "TSConsole.txt").toFile();
    // try (PrintStream log = new PrintStream(file)) {
    //
    // try {
    // configure(true, "Test console", log, log);
    // // configure(true, "Test console");
    //
    // showStandaloneConsole();
    //
    // ExecutorService pool = Executors.newCachedThreadPool();
    //
    // int maxThreads = 10;
    // int numberIterations = 50;
    // int maxSleepTime = 667;
    //
    // for (int i = 0; i < maxThreads; ++i) {
    // int threadIndex = i;
    //
    // pool.submit(() -> {
    // boolean even = threadIndex % 2 == 0;
    // long tId = Thread.currentThread().getId();
    // Random r = new Random();
    //
    // PrintStream s;
    // if (even) {
    // s = System.out;
    // } else {
    // s = System.err;
    // }
    //
    // for (int j = 0; j < numberIterations; ++j) {
    // try {
    // Thread.sleep((long) (maxSleepTime * r.nextFloat()));
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // return;
    // }
    // s.print("Thread " + tId + " ");
    // s.print(tId);
    // s.print(" " + tId + " > ");
    // s.print(even ? tId + " out " + j + "\n" : tId + " err " + j + "\n");
    // }
    //
    // });
    // }
    //
    // pool.shutdown();
    // try {
    // pool.awaitTermination(numberIterations * maxSleepTime,
    // TimeUnit.MILLISECONDS);
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // return;
    // }
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    //
    // } finally {
    // System.out.flush();
    // System.err.flush();
    // }
    //
    // } catch (Exception e) {
    //
    // }
    //
    // }
    //
}
