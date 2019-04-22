package edu.jhuapl.saavtk.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import edu.jhuapl.saavtk.gui.menu.FileMenu;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers"
 * used through out the program.
 */
public abstract class MainWindow extends JFrame
{
    private static Window mainWindow = null;

    public static Window getMainWindow()
    {
        return mainWindow;
    }

    public static void setMainWindow(Window window)
    {
        if (mainWindow != null)
        {
            throw new IllegalStateException("Cannot call setMainWindow more than once");
        }
        mainWindow = window;
    }

    private static final long serialVersionUID = -1837887362465597229L;
    private StatusBar statusBar;
    protected ViewManager rootPanel;

    /**
     * @param tempCustomShapeModelPath path to shape model. May be null. If
     *            non-null, the main window will create a temporary custom view of
     *            the shape model which will be shown first. This temporary view is
     *            not saved into the custom application folder and will not be
     *            available unless explicitely imported.
     */
    protected MainWindow(String tempCustomShapeModelPath, boolean editableLeftStatusLabel)
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createStatusBar(editableLeftStatusLabel);

        rootPanel = createViewManager(statusBar, tempCustomShapeModelPath);

        rootPanel.setPreferredSize(new Dimension(800, 600));

        createMenus();

        this.add(rootPanel, BorderLayout.CENTER);

        ImageIcon icon = createImageIcon();

        setIconImage(icon.getImage());

        // // Center the application on the screen.
        // Dimension prefSize = this.getPreferredSize();
        // Dimension parentSize;
        // java.awt.Point parentLocation = new java.awt.Point(0, 0);
        // parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        // int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
        // int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
        // this.setLocation(x, y);
        // this.setResizable(true);
    }

    protected MainWindow(String tempCustomShapeModelPath)
    {
        this(tempCustomShapeModelPath, true);
    }

    protected ImageIcon createImageIcon()
    {
        return new ImageIcon("data/yin-yang.gif");
    }

    protected abstract ViewManager createViewManager(StatusBar statusBar, String tempCustomShapeModelPath);

    protected FileMenu createFileMenu(ViewManager rootPanel)
    {
        return new FileMenu(rootPanel);
    }

    private final void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();
        rootPanel.createMenus(menuBar);
        setJMenuBar(menuBar);
    }

    protected void createStatusBar(boolean editableLeftLabel)
    {
        statusBar = new StatusBar(editableLeftLabel);
        this.getContentPane().add(statusBar, BorderLayout.PAGE_END);
    }
}
