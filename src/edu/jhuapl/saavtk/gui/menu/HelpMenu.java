package edu.jhuapl.saavtk.gui.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.util.Configuration;


public class HelpMenu extends JMenu
{
    private JPanel rootPanel;
    protected JMenuItem dataSourceMenuItem;

    protected JPanel getRootPanel()
    {
        return rootPanel;
    }

    public HelpMenu(JPanel rootPanel)
    {
        super("Help");
        this.rootPanel = rootPanel;

        JMenuItem mi = new JMenuItem(new ShowHelpContentsAction());
        this.add(mi);

        dataSourceMenuItem = new JMenuItem(new ShowSourceOfDataAction());
        this.add(dataSourceMenuItem);

        if (Configuration.isAPLVersion())
        {
            mi = new JMenuItem(new ShowRecentChangesAction());
            this.add(mi);

            mi = new JMenuItem(new ShowTutorialAction());
            this.add(mi);
        }

        // On macs the about action is in the Application menu not the help menu
        if (!Configuration.isMac())
        {
            this.addSeparator();

            mi = new JMenuItem(new AboutAction());
            this.add(mi);
        }
        else
        {
            try
            {
                Desktop.getDesktop().setAboutHandler(new java.awt.desktop.AboutHandler() {
                    public void handleAbout(java.awt.desktop.AboutEvent e) {
                        try {
                            getClass().getDeclaredMethod("showAbout", (Class[])null);
                        } catch (NoSuchMethodException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void showAbout()
    {
    }

    protected void showHelp()
    {
    }

    protected void showDataSources()
    {
    }

    protected void showRecentChanges()
    {
    }

    protected void showTutorial()
    {
    }

    private class ShowHelpContentsAction extends AbstractAction
    {
        public ShowHelpContentsAction()
        {
            super("Help Contents");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            showHelp();
        }
    }

    private class ShowSourceOfDataAction extends AbstractAction
    {
        public ShowSourceOfDataAction()
        {
            super("Where does the data come from?");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            showDataSources();
        }
    }

    private class ShowRecentChangesAction extends AbstractAction
    {
        public ShowRecentChangesAction()
        {
            super("Release Notes");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            showRecentChanges();
        }
    }

    private class ShowTutorialAction extends AbstractAction
    {
        public ShowTutorialAction()
        {
            super("Tutorials");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            showTutorial();
        }
    }

    private class AboutAction extends AbstractAction
    {
        public AboutAction()
        {
            super("About...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            showAbout();
        }
    }
}
