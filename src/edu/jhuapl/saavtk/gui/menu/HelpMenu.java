package edu.jhuapl.saavtk.gui.menu;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.OSXAdapter;
import edu.jhuapl.saavtk.util.Configuration;


public class HelpMenu extends JMenu
{
    private JPanel rootPanel;

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

        mi = new JMenuItem(new ShowSourceOfDataAction());
        this.add(mi);

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
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("showAbout", (Class[])null));
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
            catch (NoSuchMethodException e)
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
            super("Recent Changes");
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
            super("Tutorial");
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
