package edu.jhuapl.saavtk.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.config.IViewConfig;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.IPositionOrientationManager;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.DefaultPicker;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.status.LegacyStatusHandler;
import edu.jhuapl.saavtk.status.LocationStatusHandler;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Preferences;
import edu.jhuapl.saavtk.view.light.LightUtil;

/**
 * A view is a container which contains a control panel and renderer as well as
 * a collection of managers. A view is unique to a specific body. This class is
 * used to build all built-in and custom views. All the configuration details of
 * all the built-in and custom views are contained in this class.
 */
public abstract class View extends JPanel
{
	private static final long serialVersionUID = 1L;
    protected JSplitPane splitPane;
    protected Renderer renderer;
    protected JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private WindowManager infoPanelManager;
    private WindowManager spectrumPanelManager;
    protected IPositionOrientationManager positionOrientationManager;
    private final StatusNotifier refStatusNotifier;
    private LegacyStatusHandler legacyStatusHandler;
    protected final AtomicBoolean initialized = new AtomicBoolean(false);
    protected IViewConfig config;
    protected String uniqueName;
    protected String shapeModelName;
    protected String configURL;

    // accessor methods

    public JTabbedPane getControlPanel()
    {
        return controlPanel;
    }

    public void setControlPanel(JTabbedPane controlPanel)
    {
        this.controlPanel = controlPanel;
    }

    public PopupManager getPopupManager()
    {
        return popupManager;
    }

    public void setPopupManager(PopupManager popupManager)
    {
        this.popupManager = popupManager;
    }

    public WindowManager getInfoPanelManager()
    {
        return infoPanelManager;
    }

    public void setInfoPanelManager(WindowManager infoPanelManager)
    {
        this.infoPanelManager = infoPanelManager;
    }

    public WindowManager getSpectrumPanelManager()
    {
        return spectrumPanelManager;
    }

    public void setSpectrumPanelManager(WindowManager spectrumPanelManager)
    {
        this.spectrumPanelManager = spectrumPanelManager;
    }

    public LegacyStatusHandler getLegacyStatusHandler()
    {
   	 return legacyStatusHandler;
    }

    public StatusNotifier getStatusNotifier()
    {
   	 return refStatusNotifier;
    }

    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    public void setModelManager(ModelManager modelManager)
    {
        this.modelManager = modelManager;
        legacyStatusHandler = new LegacyStatusHandler(refStatusNotifier, modelManager);
    }

    public void setPickManager(PickManager aPickManager)
    {
      pickManager = aPickManager;

  		DefaultPicker tmpDefaultPicker = aPickManager.getDefaultPicker();

  		LegacyStatusHandler tmpLegacyStatusHandler = new LegacyStatusHandler(refStatusNotifier, modelManager);
  		tmpDefaultPicker.addListener(tmpLegacyStatusHandler);

  		LocationStatusHandler tmpLocationStatusHandler = new LocationStatusHandler(refStatusNotifier, renderer);
  		tmpDefaultPicker.addListener(tmpLocationStatusHandler);
    }

    protected void setConfig(IViewConfig config)
    {
    	this.config = config;
    }

    public boolean isAccessible()
    {
    	return getConfig().isAccessible();
    }

    public String getShapeModelName()
    {
    	return shapeModelName;
    }

    /**
     * By default a view should be created empty. Only when the user requests to
     * show a particular View, should the View's contents be created in order to
     * reduce memory and startup time. Therefore, this function should be called
     * prior to first time the View is shown in order to cause it
     */
    public View(StatusNotifier aStatusNotifier, IViewConfig config)
    {
        super(new BorderLayout());
        refStatusNotifier = aStatusNotifier;
        this.config = config;
        if (config != null)
        	this.uniqueName = config.getUniqueName();
    }

    protected void addTab(String name, Component component)
    {
        controlPanel.addTab(name, component);
    }

    protected abstract void setupTabs();

    protected void initialize() throws InvocationTargetException, InterruptedException
    {
        synchronized (initialized)
        {
            if (initialized.get())
                return;

            Configuration.runAndWaitOnEDT(() -> {
                setupModelManager();
            });

            Configuration.runAndWaitOnEDT(() -> {
                setupInfoPanelManager();
            });

            Configuration.runAndWaitOnEDT(() -> {
                setupSpectrumPanelManager();
            });

            Configuration.runAndWaitOnEDT(() -> {
                setupPositionOrientationManager();
            });

            
            Configuration.runAndWaitOnEDT(() -> {
                setupRenderer();
            });

            Configuration.runAndWaitOnEDT(() -> {
                setupPopupManager();
            });

            Configuration.runAndWaitOnEDT(() -> {
                setupPickManager();
            });

            Configuration.runAndWaitOnEDT(() -> {
                controlPanel = new JTabbedPane();
                controlPanel.setBorder(BorderFactory.createEmptyBorder());

                setupTabs();
            });

            Configuration.runAndWaitOnEDT(() -> {
                // add capability to right click on tab title regions and set as default tab to
                // load
                controlPanel.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        showDefaultTabSelectionPopup(e);
                    }

                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        showDefaultTabSelectionPopup(e);
                    }

                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        showDefaultTabSelectionPopup(e);
                    }
                });
                int tabIndex = FavoriteTabsFile.getInstance().getFavoriteTab(uniqueName);
                controlPanel.setSelectedIndex(tabIndex); // load default tab (which is 0 if not specified in favorite tabs file)

                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, renderer);

                splitPane.setOneTouchExpandable(true);

                int splitLocation = (int) Preferences.getInstance().getAsLong(Preferences.CONTROL_PANEL_WIDTH, 320L);
                splitPane.setDividerLocation(splitLocation);

                splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(@SuppressWarnings("unused") PropertyChangeEvent pce)
                    {
                        LinkedHashMap<String, String> map = new LinkedHashMap<>();
                        map.put(Preferences.RENDERER_PANEL_WIDTH, new Long(splitPane.getWidth() - splitPane.getDividerLocation()).toString());
                        map.put(Preferences.CONTROL_PANEL_WIDTH, new Long(splitPane.getDividerLocation()).toString());
                        Preferences.getInstance().put(map);
                    }
                });
                int rendererWidth = splitPane.getWidth() - splitLocation;

                int height = (int) Preferences.getInstance().getAsLong(Preferences.RENDERER_PANEL_HEIGHT, 800L);
                renderer.setMinimumSize(new Dimension(100, 100));
                controlPanel.setMinimumSize(new Dimension(320, 100));

                renderer.setPreferredSize(new Dimension(rendererWidth, height));
                controlPanel.setPreferredSize(new Dimension(splitLocation, height));

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    private LinkedHashMap<String, String> map = new LinkedHashMap<>();

                    @Override
                    public void run()
                    {
                        map.put(Preferences.RENDERER_PANEL_WIDTH, new Long(splitPane.getWidth() - splitPane.getDividerLocation()).toString());
                        map.put(Preferences.RENDERER_PANEL_HEIGHT, new Long(renderer.getHeight()).toString());
                        map.put(Preferences.CONTROL_PANEL_WIDTH, new Long(splitPane.getDividerLocation()).toString());
                        map.put(Preferences.CONTROL_PANEL_HEIGHT, new Long(controlPanel.getHeight()).toString());
                        Preferences.getInstance().put(map);
                    }
                });

                this.add(splitPane, BorderLayout.CENTER);

                renderer.getRenderWindowPanel().resetCamera();

                initializeStateManager();

                initialized.set(true);
            });
        }
    }

    protected final boolean isInitialized()
    {
        synchronized (initialized)
        {
            return initialized.get();
        }
    }

    protected void showDefaultTabSelectionPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu tabMenu = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem("Set instrument as default");
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
                {
                    FavoriteTabsFile.getInstance().setFavoriteTab(uniqueName, controlPanel.getSelectedIndex());
                }
            });
            tabMenu.add(menuItem);
            tabMenu.show(controlPanel, e.getX(), e.getY());
        }

    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public PickManager getPickManager()
    {
        return pickManager;
    }

    protected void registerPopup(Model model, PopupMenu menu)
    {
        popupManager.registerPopup(model, menu);
    }

    protected List<Model> getModel(ModelNames name)
    {
        return modelManager.getModel(name);
    }

    /**
     * Return a unique name for this view. No other view may have this name. Note
     * that only applies within built-in views or custom views but a custom view can
     * share the name of a built-in one or vice versa. By default simply return the
     * author concatenated with the name if the author is not null or just the name
     * if the author is null.
     *
     * @return
     */
    public String getUniqueName()
    {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName)
	{
		this.uniqueName = uniqueName;
	}

	/**
     * Return a hierarchical path representation of this view.
     *
     * @return the representation.
     */
    public abstract String getPathRepresentation();

    /**
     * Return the display name for this view (the name to be shown in the menu).
     * This name need not be unique among all views.
     *
     * @return the name to display
     */
    public abstract String getDisplayName();

    /**
     * Similar to {@link getDisplayName()}, this returns a suitable-for-display name
     * that uniquely identifies the model. This name must be unique among all views.
     *
     * @return
     */
    public abstract String getModelDisplayName();

    public IViewConfig getConfig()
    {
        return config;
    }

    //
    // Setup methods, to be defined by subclasses
    //

    public String getConfigURL()
	{
		return configURL;
	}

	public void setConfigURL(String configURL)
	{
		this.configURL = configURL;
	}

	protected abstract void setupModelManager();

    protected abstract void setupPopupManager();

    protected abstract void setupInfoPanelManager();

    protected abstract void setupSpectrumPanelManager();
    
    protected abstract void setupPositionOrientationManager();

    protected void setupRenderer()
    {
        ModelManager manager = getModelManager();
        Renderer renderer = new Renderer(manager.getPolyhedralModel());
        renderer.setLightCfg(LightUtil.getSystemLightCfg());
        renderer.addVtkPropProvider(modelManager);
        renderer.addPropertyChangeListener(manager);
        setRenderer(renderer);

        // Force the renderer's camera to the "reset" default view
        renderer.getCamera().reset();
    }

    protected abstract void setupPickManager();

    protected abstract void initializeStateManager();

    @Override
    public String toString()
    {
        if (config != null)
        {
            return "View of " + config.toString();
        }
        return "View of (null)";
    }

}
