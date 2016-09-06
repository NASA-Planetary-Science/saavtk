package edu.jhuapl.saavtk.gui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;


/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 */
public abstract class MainWindow extends JFrame
{
    private StatusBar statusBar;
    private FileMenu fileMenu;
    private ViewMenu viewMenu;
    private HelpMenu helpMenu;
    private ViewManager rootPanel;
    private FavoritesMenu favoritesMenu;
    private RecentlyViewed recentsMenu;
    /**
     * @param tempCustomShapeModelPath path to shape model. May be null.
     * If non-null, the main window will create a temporary custom view of the shape model
     * which will be shown first. This temporary view is not saved into the custom application
     * folder and will not be available unless explicitely imported.
     */
    public MainWindow(String tempCustomShapeModelPath)
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createStatusBar();

        rootPanel = createViewManager(statusBar, this, tempCustomShapeModelPath);

        createMenus();

        this.add(rootPanel, BorderLayout.CENTER);

        ImageIcon icon = createImageIcon();

        setIconImage(icon.getImage());
        pack();

//        // Center the application on the screen.
//        Dimension prefSize = this.getPreferredSize();
//        Dimension parentSize;
//        java.awt.Point parentLocation = new java.awt.Point(0, 0);
//        parentSize = Toolkit.getDefaultToolkit().getScreenSize();
//        int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
//        int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
//        this.setLocation(x, y);
//        this.setResizable(true);
    }

    protected ImageIcon createImageIcon()
    {
        return new ImageIcon("data/yin-yang.gif");
    }

    protected abstract ViewManager createViewManager(StatusBar statusBar, MainWindow mainWindow, String tempCustomShapeModelPath);

    protected FileMenu createFileMenu(ViewManager rootPanel)
    {
        return new FileMenu(rootPanel);
    }

    protected RecentlyViewed createRecentsMenu(ViewManager rootPanel)
    {
        return new RecentlyViewed(rootPanel);
    }

    protected ViewMenu createViewMenu(ViewManager rootPanel, RecentlyViewed recentsMenu)
    {
        return new ViewMenu(rootPanel, recentsMenu);
    }

    protected FavoritesMenu createFavoritesMenu(ViewManager rootPanel)
    {
        return new FavoritesMenu(new FavoritesFile(), rootPanel);
    }

    protected HelpMenu createHelpMenu(ViewManager rootPanel)
    {
        return new HelpMenu(rootPanel);
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        fileMenu = createFileMenu(rootPanel);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        recentsMenu = createRecentsMenu(rootPanel);
        viewMenu = createViewMenu(rootPanel, recentsMenu);
        viewMenu.setMnemonic('V');

        menuBar.add(viewMenu);

        favoritesMenu = createFavoritesMenu(rootPanel);
        viewMenu.add(new JSeparator());
        viewMenu.add(favoritesMenu);
        viewMenu.add(new JSeparator());
        viewMenu.add(recentsMenu);

        helpMenu = createHelpMenu(rootPanel);
        helpMenu.setMnemonic('H');
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    protected void createStatusBar()
    {
        statusBar = new StatusBar();
        this.getContentPane().add(statusBar, BorderLayout.PAGE_END);
    }
}
