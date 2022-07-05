package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TSConsole
{
    private static Boolean headless = null;
    private static TSConsole CONSOLE = null;
    private final PrintStream outputFile;
    private final JFrame consoleFrame;
    private final PrintStream out;

    private TSConsole(boolean enable, PrintStream outputFile)
    {
        this.outputFile = outputFile;

        // Start with settings for enable == false.
        JFrame consoleFrame = null;
        PrintStream out = System.out;

        if (enable)
        {
            try
            {
                // Add Copy/Cut/Paste popup menu to Text Components (specifically our Console
                // replacement)
                Toolkit.getDefaultToolkit().getSystemEventQueue().push(new PopupEventQueue());

                // Check whether redirecting streams is allowed in this runtime environment.
                SecurityManager manager = System.getSecurityManager();
                if (manager != null)
                    manager.checkPermission(new RuntimePermission("setIO"));
                // if (true) throw new SecurityException("fake security exception");

                JTextArea consoleTextArea = new JTextArea(20, 60);
                // I updated this to not set the entire content of the text area every time,
                // but just to append to it as it gets read. This has a HUGE effect
                // on performance (more than a factor of 100) for cases where the
                // console is getting spammed with hundreds or thousands of lines.
                out = new PrintStream(new TextAreaStream(consoleTextArea));

                System.setErr(out);
                System.setOut(out);

                consoleFrame = new JFrame("Message Console");

                JScrollPane consolePane = new JScrollPane(consoleTextArea);
                consoleFrame.add(consolePane);
            }
            catch (Exception e)
            {
                // Restore local variables to same state as if enable == false.
                consoleFrame = null;
                out = System.out;
                e.printStackTrace(out);
                JOptionPane.showMessageDialog(null, "Unable to redirect standard streams to console.");
            }
        }
        this.consoleFrame = consoleFrame;
        this.out = out;
    }

    public static void configure(boolean enable) throws InvocationTargetException, InterruptedException
    {
        configure(enable, null);
    }

    public static void configure(boolean enable, PrintStream outputFile) throws InvocationTargetException, InterruptedException
    {
        if (isHeadless())
        {
            CONSOLE = new TSConsole(false, outputFile);
            if (enable)
            {
                CONSOLE.out.println("Console is forced to be disabled when running headless.");
            }
            return;
        }

        Runnable runnable = () -> {
            if (CONSOLE != null)
                throw new UnsupportedOperationException("Console may only be configured once.");
            CONSOLE = new TSConsole(enable, outputFile);
        };

        if (EventQueue.isDispatchThread())
        {
            runnable.run();
        }
        else
        {
            EventQueue.invokeAndWait(runnable);
        }
    }

    public static boolean isConfigured()
    {
        return CONSOLE != null;
    }

    protected static boolean isHeadless()
    {
        if (headless == null)
        {
            headless = Boolean.parseBoolean(System.getProperty("java.awt.headless"));
        }

        return headless;
    }

    public static boolean isEnabled()
    {
        return isConfigured() && CONSOLE.consoleFrame != null;
    }

    public static JFrame getConsoleFrame()
    {
        return CONSOLE.consoleFrame;
    }

    public static void showConsole()
    {
        if (isEnabled())
        {
            CONSOLE.consoleFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }

        doShowConsole();
    }

    public static void showStandaloneConsole()
    {
        if (isEnabled())
        {
            CONSOLE.consoleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        doShowConsole();
    }

    public static void hideConsole()
    {
        if (isEnabled())
        {
            CONSOLE.consoleFrame.setVisible(false);
            CONSOLE.consoleFrame.setLocationByPlatform(false);
        }
    }

    public static void setDefaultLocation(Component relativeTo)
    {
        if (isEnabled())
        {
            CONSOLE.consoleFrame.setLocationRelativeTo(relativeTo);
        }
    }

    public static void addConsoleMenu(final JMenuBar menuBar)
    {
        JMenu consoleMenu = new JMenu("Console");

        JMenuItem toggleConsoleItem = new JMenuItem("Toggle Console");
        consoleMenu.add(toggleConsoleItem);
        toggleConsoleItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
            {
                if (isEnabled())
                {
                    if (CONSOLE.consoleFrame.isVisible())
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
    }

    public static PrintStream getStream()
    {
        if (!isConfigured())
            throw new UnsupportedOperationException();
        return CONSOLE.out;
    }

    private static void reportConsoleDisabled()
    {
        String message = "In-app console is disabled.";
        System.err.println(message);
        if (!isHeadless())
        {
            JOptionPane.showMessageDialog(null, message);
        }
    }

    private static void doShowConsole()
    {
        if (isEnabled())
        {
            CONSOLE.consoleFrame.pack();
            CONSOLE.consoleFrame.setVisible(true);
            CONSOLE.consoleFrame.toFront();
            CONSOLE.consoleFrame.repaint();
        }
        else
        {
            reportConsoleDisabled();
        }
    }

    private class TextAreaStream extends OutputStream
    {
        private JTextArea textArea;

        public TextAreaStream(JTextArea ta)
        {
            this.textArea = ta;
        }

        @Override
        public void write(int b) throws IOException
        {
        	SwingUtilities.invokeLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					// put the string version of each character into the text area
					textArea.append(String.valueOf((char) b)); // make sure the bottom of the data is showing in the window
		            textArea.setCaretPosition(textArea.getDocument().getLength());
		            if (outputFile != null)
		            {
		                outputFile.append((char) b);
		            }
				}
			});
        }
    }
}
