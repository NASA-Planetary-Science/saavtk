package edu.jhuapl.saavtk.gui;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;

import edu.jhuapl.saavtk.gui.menu.FavoritesMenu;
import edu.jhuapl.saavtk.gui.menu.FileMenu;
import edu.jhuapl.saavtk.gui.menu.HelpMenu;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.UnauthorizedAccessException;

public abstract class ViewManager extends JPanel
{
    private static final long serialVersionUID = 1L;
    private List<View> builtInViews = new ArrayList<>();
    private List<View> customViews = new ArrayList<>();
    private View currentView;
    protected final StatusBar statusBar;
    private final Frame frame;
    private String tempCustomShapeModelPath;

    protected FileMenu fileMenu = null;
    protected ViewMenu viewMenu = null;
    protected HelpMenu helpMenu = null;
    protected FavoritesMenu favoritesMenu = null;
    protected RecentlyViewed recentsMenu = null;
    private volatile boolean initialViewSet;

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
        this.initialViewSet = false;

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
                if (Configuration.getSwingAuthorizor().updateCredentials())
                {
                    FileCache.instance().queryAllInBackground(true);
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
            final String defaultModelName = getDefaultModelName();
            initialView = getBuiltInView(defaultModelName);
            if (initialView == null)
            {
                initialView = getCustomView(defaultModelName);
            }

            // Default model is not available. Try to find the first accessible model.
            if (initialView == null)
            {
                if (defaultModelName == null)
                {
                    System.err.println("\nNo default model is set.");
                }
                else
                {                    
                    System.err.println("\nDefault model " + defaultModelName + " is not available.");
                }
                for (View view : getAllViews())
                {
                    if (view.isAccessible())
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
                String modelName = provideBasicModel();
                if (modelName != null)
                {
                    initialView = createCustomView(statusBar, modelName, false);
                    modelName = initialView.getUniqueName();

                    addCustomView(initialView);
                    add(initialView, modelName);
                    setDefaultModelName(modelName);
                    
                    System.err.println("Starting with one basic/demo model. No other models are currently available.");
                    System.err.println("Restart with a stable internet connection to see all available models");
                }
            }

            if (initialView == null)
            {
                System.err.println("Cannot find another available model to start with.");
            }
        }

        setCurrentView(initialView);
    }

    /**
     * Return the name of the default model, if any is defined. The base
     * implementation just returns null, meaning there is no built-in factory
     * default model.
     * 
     * @return the default model name
     */
    public String getDefaultModelName()
    {
        return null;
    }

    /**
     * Set the default model to the supplied name. The base implementation is a
     * no-op. Override this to actually set the model name, which should be returned
     * by future calls to {@link #getDefaultModelName()}.
     * 
     * @param modelName the model name to use for the default model
     */
    @SuppressWarnings("unused")
    public void setDefaultModelName(String modelName)
    {

    }

    /**
     * Make the current default model name persistent. The base implementation is a
     * no-op. Override this to save the current default model returned by
     * {@link #getDefaultModelName()}.
     */
    public void saveDefaultModelName()
    {

    }

    /**
     * Revert the current default model name to the factory default value. The base
     * implementation is a no-op. Implementations may override this to provide any
     * desired behavior, such as returning a value loaded from a persistent state or
     * set to a hardwired default value.
     */
    public void resetDefaultModelName()
    {

    }

    /**
     * Provide a basic/dummy/failsafe {@link GenericPolyhedralModel} and return its
     * unique name. The base implementation returns null. If not null, the name
     * returned by this method is used to create a {@link View} if no other model is
     * available to be viewed. (See {@link #setupViews()}.
     * 
     * @return the name of the created model, or null if no basic/failsafe model may
     *         be provided.
     */
    protected String provideBasicModel()
    {
        return null;
    }

    public boolean isReady()
    {
        return initialViewSet && (currentView == null || currentView.isInitialized());
    }

    /**
     * Returns the View whose unique name matches the supplied unique name.
     * Returns null if the supplied name is null.
     * 
     * This method does *NOT* guarantee that the returned View is actually
     * accessible.
     * 
     * @param uniqueName the name of the View to return
     * @return the View
     * @throws IllegalArgumentException if a View was not found with the supplied name.
     */
    public View getView(String uniqueName) throws IllegalArgumentException
    {
        if (uniqueName == null)
        {
            return null;
        }

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
                    if (new File(dir, "model.json").isFile())
                    {
                        View view = createCustomView(dir.getName(), false, new File(dir, "model.json"));
                        if (view != null)
                            addCustomView(view);
                    }
                    else
                    {
                        View view = createCustomView(statusBar, dir.getName(), false);
                        if (view != null)
                            addCustomView(view);
                    }
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
        initializeStateManager(); // Call this here for insurance, even if it is called elsewhere.

        if (view == currentView)
        {
            initialViewSet = true;
            return;
        }

        SwingWorker<Boolean, Void> initializer = new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception
            {
                if (view != null)
                {
                    try
                    {
                        view.initialize();
                    }
                    catch (UnauthorizedAccessException e)
                    {
                        e.printStackTrace();
                        EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(null, "Access to this model is restricted. Please email sbmt@jhuapl.edu to request access.", "Access not authorized", JOptionPane.ERROR_MESSAGE));
                        cancel(true);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(null, "Unable to switch to model " + view.getUniqueName() + ". See console for more information", "Problem initializing model", JOptionPane.ERROR_MESSAGE));
                        cancel(true);
                    }
                }

                return null;
            }

            @Override
            protected void done()
            {
                if (!isCancelled())
                {
                    if (currentView != null)
                        currentView.getRenderer().viewDeactivating();

                    if (view != null)
                    {
                        CardLayout cardLayout = (CardLayout) (getLayout());
                        cardLayout.show(ViewManager.this, view.getUniqueName());
                    }

                    // defer initialization of View until we show it.
                    repaint();
                    validate();

                    currentView = view;

                    if (view != null)
                    {
                        view.getRenderer().viewActivating();
                    }

                    updateRecents();

                    frame.setTitle(view != null ? view.getPathRepresentation() : "");
                }

                initialViewSet = true;
            }
        };

        initializer.execute();
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

    public View addMetadataBackedCustomView(View view)
    {
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

    public abstract View createCustomView(String name, boolean temporary, File metadata);

    protected abstract void initializeStateManager();

    /**
     * Return the built-in View that matches the supplied name. Note that this
     * method does check that the view be accessible.
     * 
     * @param uniqueName name of the view
     * @return the view with the name, or null if it's not found, or not accessible
     */
    protected View getBuiltInView(String uniqueName)
    {
        for (View view : getBuiltInViews())
        {
            if (view.getUniqueName().equals(uniqueName) && view.isAccessible())
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
    public View getCustomView(String uniqueName)
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
