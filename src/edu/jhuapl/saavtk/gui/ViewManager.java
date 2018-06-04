package edu.jhuapl.saavtk.gui;

import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.menu.FavoritesMenu;
import edu.jhuapl.saavtk.gui.menu.FileMenu;
import edu.jhuapl.saavtk.gui.menu.HelpMenu;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache.UnauthorizedAccessException;

public abstract class ViewManager extends JPanel
{
	private static final long serialVersionUID = 1L;
	private List<View> builtInViews = new ArrayList<>();
	private List<View> customViews = new ArrayList<>();
	private View currentView;
	private final StatusBar statusBar;
	private final Frame frame;
	private String tempCustomShapeModelPath;

	private static String defaultModelName = null;
	private final static Path defaultModelFile = Paths.get(Configuration.getApplicationDataDir() + File.separator + "defaultModelToLoad");

	protected FileMenu fileMenu = null;
	protected ViewMenu viewMenu = null;
	protected HelpMenu helpMenu = null;
	protected FavoritesMenu favoritesMenu = null;
	protected RecentlyViewed recentsMenu = null;

	/**
	 * The top level frame is required so that the title can be updated when the
	 * view changes.
	 *
	 * @param statusBar
	 * @param frame
	 * @param tempCustomShapeModelPath path to shape model. May be null. If
	 *            non-null, the main window will create a temporary custom view of
	 *            the shape model which will be shown first. This temporary view is
	 *            not saved into the custom application folder and will not be
	 *            available unless explicitely imported.
	 */
	public ViewManager(StatusBar statusBar, Frame frame, String tempCustomShapeModelPath)
	{
		super(new CardLayout());
		setBorder(BorderFactory.createEmptyBorder());
		this.currentView = null;
		this.statusBar = statusBar;
		this.frame = frame;
		this.tempCustomShapeModelPath = tempCustomShapeModelPath;

		// Subclass constructors should call this. It should not be called here because
		// it is not final.
		// setupViews();
	}

	protected void createMenus(JMenuBar menuBar)
	{
		fileMenu = new FileMenu(this);
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		recentsMenu = new RecentlyViewed(this);
		viewMenu = new ViewMenu(this, recentsMenu);
		viewMenu.setMnemonic('V');

		menuBar.add(viewMenu);

		favoritesMenu = new FavoritesMenu(this);

		JMenuItem passwordMenu = createPasswordMenu();

		viewMenu.add(new JSeparator());
		viewMenu.add(favoritesMenu);
		viewMenu.add(passwordMenu);
		viewMenu.add(new JSeparator());
		viewMenu.add(recentsMenu);

		Console.addConsoleMenu(menuBar);

		helpMenu = new HelpMenu(this);
		helpMenu.setMnemonic('H');
		menuBar.add(helpMenu);
	}

	protected JMenuItem createPasswordMenu()
	{
		JMenuItem updatePassword = new JMenuItem("Update Password...");
		updatePassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt)
			{
				try
				{
					Configuration.updatePassword();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Error trying to save user name and password.", "Unable to save changes", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		return updatePassword;
	}

	protected void addBuiltInView(View view)
	{
		builtInViews.add(view);
	}

	protected void addBuiltInViews(@SuppressWarnings("unused") StatusBar statusBar)
	{

	}

	protected void setupViews()
	{
		// Add in any built-in views.
		addBuiltInViews(statusBar);

		// Add built-in views to the top-level JPanel.
		for (View view : getBuiltInViews())
			add(view, view.getUniqueName());

		View initialView = null;
		// Add a new model passed on the command line, if any.
		final String tempCustomShapeModel = getTempCustomShapeModelPath();
		if (tempCustomShapeModel != null)
		{
			initialView = addCustomView(statusBar, tempCustomShapeModel);
			if (initialView == null)
			{
				// Not sure this is even possible, but just in case.
				System.err.println("Unable to load custom model specified on command line \"" + tempCustomShapeModel + "\"");
			}
		}

		// Load in any other custom views found in the configuration directory
		loadCustomViews(statusBar);

		// Add custom views to the top-level JPanel
		for (View view : getCustomViews())
			add(view, view.getUniqueName());

		// If no model was specified on the command line, try to determine
		// some other body to load initially.
		if (tempCustomShapeModel == null)
		{
			// First try the default model, which may be built-in or custom.
			final String defaultModelName = getDefaultBodyToLoad();
			initialView = getBuiltInView(defaultModelName);
			if (initialView == null)
			{
				initialView = getCustomView(defaultModelName);
			}

			// Default model is not available. Try to find the first accessible model.
			if (initialView == null)
			{
				System.err.println("\nDefault model " + defaultModelName + " is not available.");
				for (View view : getAllViews())
				{
					if (view.getConfig().isAccessible())
					{
						initialView = view;
						break;
					}
				}

				// Report substitition for the default model.
				if (initialView != null)
				{
					System.err.println("Starting with first available model: " + initialView.getPathRepresentation());
				}
			}

			if (initialView == null)
			{
				System.err.println("Cannot find another available model to start with.");
			}

		}

		if (initialView != null)
		{
			setCurrentView(initialView);
		}
	}

	public void setDefaultBodyToLoad(String uniqueName)
	{
		try
		{
			defaultModelName = uniqueName;
			if (defaultModelFile.toFile().exists())
				defaultModelFile.toFile().delete();
			defaultModelFile.toFile().createNewFile();
			FileWriter writer = new FileWriter(defaultModelFile.toFile());
			writer.write(defaultModelName);
			writer.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getDefaultBodyToLoad()
	{
		defaultModelName = ViewConfig.getFirstTimeDefaultModelName();
		if (defaultModelFile.toFile().exists())
		{
			try (Scanner scanner = new Scanner(ViewManager.defaultModelFile.toFile()))
			{
				if (scanner.hasNextLine())
					defaultModelName = scanner.nextLine();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return defaultModelName;
	}

	public void resetDefaultBodyToLoad()
	{
		if (defaultModelFile.toFile().exists())
			defaultModelFile.toFile().delete();
	}

	/**
	 * Returns the View whose unique name matches the supplied unique name.
	 * 
	 * This method does *NOT* guarantee that the returned View is actually
	 * accessible.
	 * 
	 * @param uniqueName the name of the View to return
	 * @return the View
	 * @throws IllegalArgumentException if the View was not found
	 */
	public View getView(String uniqueName) throws IllegalArgumentException
	{
		for (View view : getBuiltInViews())
		{
			if (view.getUniqueName().equals(uniqueName))
			{
				return view;
			}
		}
		for (View view : getCustomViews())
		{
			if (view.getUniqueName().equals(uniqueName))
			{
				return view;
			}
		}
		throw new IllegalArgumentException("Could not find a model/view with name " + uniqueName);
	}

	protected void loadCustomViews(StatusBar statusBar)
	{
		File modelsDir = new File(Configuration.getImportedShapeModelsDir());
		File[] dirs = modelsDir.listFiles();
		if (dirs != null && dirs.length > 0)
		{
			Arrays.sort(dirs);
			for (File dir : dirs)
			{
				if (new File(dir, "model.vtk").isFile())
				{
					addCustomView(createCustomView(statusBar, dir.getName(), false));
				}
			}
		}
	}

	protected View addCustomView(StatusBar statusBar, String shapeModelPath)
	{
		View customView = createCustomView(statusBar, shapeModelPath, true);
		addCustomView(customView);
		return customView;
	}

	public List<View> getBuiltInViews()
	{
		return builtInViews;
	}

	public List<View> getCustomViews()
	{
		return customViews;
	}

	public void setCustomViews(List<View> customViews)
	{
		this.customViews = customViews;
	}

	public String getTempCustomShapeModelPath()
	{
		return tempCustomShapeModelPath;
	}

	public View getCurrentView()
	{
		return currentView;
	}

	public void setCurrentView(View view)
	{
		Preconditions.checkNotNull(view);

		initializeStateManager(); // Call this here for insurance, even if it is called elsewhere.
		if (view == currentView)
		{
			return;
		}
		if (currentView != null)
			currentView.renderer.viewDeactivating();

		CardLayout cardLayout = (CardLayout) (getLayout());
		cardLayout.show(this, view.getUniqueName());

		// defer initialization of View until we show it.
		repaint();
		validate();
		try
		{
			view.initialize();
			currentView = view;
		}
		catch (UnauthorizedAccessException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Access to this model is restricted. Please email sbmt@jhuapl.edu to request access.", "Access not authorized", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (currentView != null)
			currentView.renderer.viewActivating();

		updateRecents();
		frame.setTitle(view.getPathRepresentation());
	}

	public View getBuiltInView(int i)
	{
		return builtInViews.get(i);
	}

	public int getNumberOfBuiltInViews()
	{
		return builtInViews.size();
	}

	public View getCustomView(int i)
	{
		return customViews.get(i);
	}

	public int getNumberOfCustomViews()
	{
		return customViews.size();
	}

	protected void addCustomView(View view)
	{
		customViews.add(view);
	}

	public View addCustomView(String name)
	{
		View view = createCustomView(statusBar, name, false);
		addCustomView(view);
		add(view, view.getUniqueName());
		return view;
	}

	public View removeCustomView(String name)
	{
		for (View view : customViews)
		{
			if (view.getConfig().getShapeModelName().equals(name))
			{
				customViews.remove(view);
				remove(view);
				return view;
			}
		}

		return null;
	}

	protected abstract View createCustomView(StatusBar statusBar, String name, boolean temporary);

	protected abstract void initializeStateManager();

	/**
	 * Return the built-in View that matches the supplied name. Note that this
	 * method does check that the view be accessible.
	 * 
	 * @param uniqueName name of the view
	 * @return the view with the name, or null if it's not found, or not accessible
	 */
	View getBuiltInView(String uniqueName)
	{
		for (View view : getBuiltInViews())
		{
			if (view.getUniqueName().equals(uniqueName) && view.getConfig().isAccessible())
			{
				return view;
			}
		}

		return null;
	}

	/**
	 * Return the custom View that matches the supplied name. Note that this method
	 * does check that the view be accessible.
	 * 
	 * @param uniqueName name of the view
	 * @return the view with the name, or null if it's not found, or not accessible
	 */
	View getCustomView(String uniqueName)
	{
		for (View view : getCustomViews())
		{
			if (view.getUniqueName().equals(uniqueName) && view.getConfig().isAccessible())
			{
				return view;
			}
		}

		return null;
	}

	public List<View> getAllViews()
	{
		List<View> allViews = new ArrayList<>();
		allViews.addAll(builtInViews);
		allViews.addAll(customViews);
		return allViews;
	}

	private void updateRecents()
	{
		if (recentsMenu != null && currentView != null)
		{
			recentsMenu.updateMenu(currentView);
		}
	}
}
