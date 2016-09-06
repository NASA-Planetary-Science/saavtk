package edu.jhuapl.saavtk.example;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import com.jgoodies.looks.LookUtils;

import edu.jhuapl.saavtk.gui.OSXAdapter;
import edu.jhuapl.saavtk.util.Configuration;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and other initialization.
 * The main function may take one optional argument. If there are no
 * arguments specified, then the tool starts up as usual showing a generic shape
 * by default. If one argument is specified, it is assumed to be a path
 * to a temporary shape model which is then loaded as a custom view
 * though it is not retained the next time the tool starts.
 */
public class ExampleTool
{
    static
    {
        if (Configuration.isMac())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            ImageIcon erosIcon = new ImageIcon("data/black-sphere.png");
            OSXAdapter.setDockIconImage(erosIcon.getImage());
        }
    }

    private static void setupLookAndFeel()
    {
        try
        {
            if (!Configuration.isMac())
            {
                UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            }
//            else
//                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        }
        catch (Exception e)
        {
            e.printStackTrace();

            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }

    public static void main(final String[] args)
    {
        Configuration.setAppName("exampletool");
        Configuration.setAPLVersion(true);
        Configuration.setUseFileCache(false);

        setupLookAndFeel();

        // The following line appears to be needed on some systems to prevent server redirect errors.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        try
        {
            javax.swing.SwingUtilities.invokeLater(new ExampleRunnable(args));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
