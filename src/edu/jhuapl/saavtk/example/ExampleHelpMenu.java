package edu.jhuapl.saavtk.example;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.menu.HelpMenu;
import edu.jhuapl.saavtk.util.Configuration;


public class ExampleHelpMenu extends HelpMenu
{
    public ExampleHelpMenu(JPanel rootPanel)
    {
        super(rootPanel);
     }

    public void showAbout()
    {
        final String COPYRIGHT  = "\u00a9";

        String versionString = "Version 0.9\n";

        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getRootPanel()),
                "Example SAAVTK Tool\n" + versionString +
                COPYRIGHT + " 2016 The Johns Hopkins University Applied Physics Laboratory\n",
                "About Example SAAVTK Tool",
                JOptionPane.PLAIN_MESSAGE);
    }

    protected void showHelp()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://www.jhuapl.edu"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void showDataSources()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://www.jhuapl.edu"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void showRecentChanges()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
           java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://www.jhuapl.edu"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void showTutorial()
    {
        String helpRootUrl = Configuration.getHelpRootURL();
        try
        {
           java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://www.jhuapl.edu"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
