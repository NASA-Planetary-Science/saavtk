package edu.jhuapl.saavtk.example;

import javax.swing.ImageIcon;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.RecentlyViewed;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.ViewMenu;
import edu.jhuapl.saavtk.gui.menu.HelpMenu;



/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 */
public class ExampleMainWindow extends MainWindow
{
    public ExampleMainWindow(String tempCustomShapeModelPath)
    {
        super(tempCustomShapeModelPath);
   }

    protected ViewManager createViewManager(StatusBar statusBar, MainWindow mainWindow, String tempCustomShapeModelPath)
    {
        return new ExampleViewManager(statusBar, this, tempCustomShapeModelPath);
    }

    protected ViewMenu createViewMenu(ViewManager rootPanel, RecentlyViewed recentsMenu)
    {
        return new ExampleViewMenu(rootPanel, recentsMenu);
    }

    protected ImageIcon createImageIcon()
    {
//        return new ImageIcon(getClass().getResource("/edu/jhuapl/saavtk/data/black-sphere.png"));
        return new ImageIcon("black-sphere.png");
    }

    protected HelpMenu createHelpMenu(ViewManager rootPanel)
    {
        return new ExampleHelpMenu(rootPanel);
    }
}
