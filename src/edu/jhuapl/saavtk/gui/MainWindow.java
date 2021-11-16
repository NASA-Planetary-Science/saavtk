package edu.jhuapl.saavtk.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import edu.jhuapl.saavtk.gui.menu.FileMenu;
import edu.jhuapl.saavtk.main.MainWinCfg;
import edu.jhuapl.saavtk.main.io.MainWinConfigUtil;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.status.gui.StatusBarPanel;
import glum.gui.info.WindowCfg;
import glum.task.ConsoleTask;
import glum.task.SilentTask;
import glum.util.ThreadUtil;

/**
 * This class defines the top level window. Typically only one
 * {@link MainWindow} is allowed.
 * <p>
 * This object instantiates the {@link ViewManager} and as a result instantiates
 * all the "managers" used through out the program.
 */
public abstract class MainWindow extends JFrame
{
	/** Global that holds the {@link MainWindow} singleton. */
	private static MainWindow globMainWindow = null;

	// State vars
	private MainWinCfg workMainAppCfg;

	// Gui vars
	private final StatusBarPanel statusBarPanel;
	protected ViewManager rootPanel;

	/**
	 * Returns the (global) singleton {@link MainWindow}.
	 * <p>
	 * Returns null if a {@link MainWindow} has not been instantiated.
	 */
	public static MainWindow getMainWindow()
	{
		return globMainWindow;
	}

	public static void setMainWindow(MainWindow window)
	{
		if (globMainWindow != null)
		{
			throw new IllegalStateException("Cannot call setMainWindow more than once");
		}
		globMainWindow = window;
	}

	/**
	 * @param tempCustomShapeModelPath path to shape model. May be null. If
	 *                                 non-null, the main window will create a
	 *                                 temporary custom view of the shape model
	 *                                 which will be shown first. This temporary
	 *                                 view is not saved into the custom application
	 *                                 folder and will not be available unless
	 *                                 explicitely imported.
	 */
	protected MainWindow(String tempCustomShapeModelPath)
	{
		// Load the MainWindow configuration
		workMainAppCfg = MainWinCfg.formDefaultConfiguration();
		workMainAppCfg = MainWinConfigUtil.loadConfiguration(new ConsoleTask(), workMainAppCfg);

		// Set up the gui
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		statusBarPanel = new StatusBarPanel();
		add(statusBarPanel, BorderLayout.PAGE_END);

		rootPanel = createViewManager(statusBarPanel, tempCustomShapeModelPath);
		add(rootPanel, BorderLayout.CENTER);

		createMenus();

		ImageIcon icon = createImageIcon();
		setIconImage(icon.getImage());

		// Install the MainWindow configuration
		var mainWC = workMainAppCfg.mainWC();
		setLocation(mainWC.posX(), mainWC.posY());
		setPreferredSize(new Dimension(mainWC.dimX(), mainWC.dimY()));

		// Register for events of interest
		ThreadUtil.addShutdownHook(() -> shutdownPanel());
	}

	/**
	 * Returns the {@link MainWinCfg}.
	 */
	public MainWinCfg getMainAppCfg()
	{
		return workMainAppCfg;
	}

	public boolean isReady()
	{
		return rootPanel.isReady();
	}

	protected ImageIcon createImageIcon()
	{
		return new ImageIcon("data/yin-yang.gif");
	}

	protected abstract ViewManager createViewManager(StatusNotifier aStatusNotifier, String tempCustomShapeModelPath);

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

	/**
	 * Helper method that handles the shutdown logic.
	 */
	private void shutdownPanel()
	{
		var tmpMainWC = new WindowCfg(this);

		var tmpMainSplitSize = workMainAppCfg.mainSplitSize();
		var tmpViewManager = ViewManager.getGlobalViewManager();
		if (tmpViewManager != null)
		{
			var tmpView = tmpViewManager.getCurrentView();
			if (tmpView != null)
				tmpMainSplitSize = tmpView.getMainSplitPane().getDividerLocation();
		}

		var tmpMainAppCfg = new MainWinCfg(tmpMainWC, tmpMainSplitSize);
		MainWinConfigUtil.saveConfiguration(new SilentTask(), tmpMainAppCfg);
	}

}
