package edu.jhuapl.saavtk.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModelConfig;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.popupmenus.PopupManager;
import edu.jhuapl.saavtk.popupmenus.PopupMenu;
import edu.jhuapl.saavtk.util.Preferences;


/**
 * A view is a container which contains a control panel and renderer
 * as well as a collection of managers. A view is unique to a specific
 * body. This class is used to build all built-in and custom views.
 * All the configuration details of all the built-in and custom views
 * are contained in this class.
 */
public abstract class View extends JPanel
{
    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private WindowManager infoPanelManager;
    private WindowManager spectrumPanelManager;
    private StatusBar statusBar;
    private boolean initialized = false;
    private PolyhedralModelConfig polyhedralModelConfig;
    static private boolean initializedPanelSizing = false;

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

    public void setSpectrumPanelManager(
            WindowManager spectrumPanelManager)
    {
        this.spectrumPanelManager = spectrumPanelManager;
    }

    public StatusBar getStatusBar()
    {
        return statusBar;
    }

    public void setStatusBar(StatusBar statusBar)
    {
        this.statusBar = statusBar;
    }

    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    public void setModelManager(ModelManager modelManager)
    {
        this.modelManager = modelManager;
    }

    public void setPickManager(PickManager pickManager)
    {
        this.pickManager = pickManager;
    }



    /**
     * By default a view should be created empty. Only when the user
     * requests to show a particular View, should the View's contents
     * be created in order to reduce memory and startup time. Therefore,
     * this function should be called prior to first time the View is
     * shown in order to cause it
     */
    public View(
            StatusBar statusBar,
            PolyhedralModelConfig polyhedralModelConfig)
    {
        super(new BorderLayout());
        this.statusBar = statusBar;
        this.polyhedralModelConfig = polyhedralModelConfig;
    }

    protected void addTab(String name, JComponent component)
    {
        controlPanel.addTab(name, component);
    }

    protected abstract void setupTabs();

    public void initialize()
    {
        if (initialized)
            return;

        setupModelManager();

        setupInfoPanelManager();

        setupSpectrumPanelManager();

        setupRenderer();

        setupPopupManager();

        setupPickManager();

        controlPanel = new JTabbedPane();
        controlPanel.setBorder(BorderFactory.createEmptyBorder());

        setupTabs();

        // add capability to right click on tab title regions and set as default tab to load
        controlPanel.addMouseListener(new MouseAdapter()
        {

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
        int tabIndex=FavoriteTabsFile.getInstance().getFavoriteTab(polyhedralModelConfig.getUniqueName());
        controlPanel.setSelectedIndex(tabIndex);    // load default tab (which is 0 if not specified in favorite tabs file)

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, renderer);
        splitPane.setOneTouchExpandable(true);

        if (!initializedPanelSizing)
        {
            int width = (int)Preferences.getInstance().getAsLong(Preferences.RENDERER_PANEL_WIDTH, 800L);
            int height = (int)Preferences.getInstance().getAsLong(Preferences.RENDERER_PANEL_HEIGHT, 800L);

            renderer.setMinimumSize(new Dimension(100, 100));
            renderer.setPreferredSize(new Dimension(width, height));

            width = (int)Preferences.getInstance().getAsLong(Preferences.CONTROL_PANEL_WIDTH, 320L);
            height = (int)Preferences.getInstance().getAsLong(Preferences.CONTROL_PANEL_HEIGHT, 800L);

            controlPanel.setMinimumSize(new Dimension(320, 100));
            controlPanel.setPreferredSize(new Dimension(width, height));

            // Save out the size of the control panel and renderer when the tool exits
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                private LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

                @Override
                public void run()
                {
                    map.put(Preferences.RENDERER_PANEL_WIDTH, new Long(renderer.getWidth()).toString());
                    map.put(Preferences.RENDERER_PANEL_HEIGHT, new Long(renderer.getHeight()).toString());
                    map.put(Preferences.CONTROL_PANEL_WIDTH, new Long(controlPanel.getWidth()).toString());
                    map.put(Preferences.CONTROL_PANEL_HEIGHT, new Long(controlPanel.getHeight()).toString());
                    Preferences.getInstance().put(map);
                }
            });

            initializedPanelSizing = true;
        }
        else
        {
            renderer.setMinimumSize(new Dimension(100, 100));
            renderer.setPreferredSize(new Dimension(800, 800));
            controlPanel.setMinimumSize(new Dimension(320, 100));
            controlPanel.setPreferredSize(new Dimension(320, 800));
        }


        this.add(splitPane, BorderLayout.CENTER);

        initialized = true;
    }

    private void showDefaultTabSelectionPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu tabMenu=new JPopupMenu();
            JMenuItem menuItem=new JMenuItem("Set instrument as default");
            menuItem.addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    FavoriteTabsFile.getInstance().setFavoriteTab(polyhedralModelConfig.getUniqueName(), controlPanel.getSelectedIndex());
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

    protected void setModels(HashMap<ModelNames, Model> models)
    {
        modelManager.setModels(models);
    }

    protected void registerPopup(Model model, PopupMenu menu)
    {
        popupManager.registerPopup(model, menu);
    }

    protected Model getModel(ModelNames name)
    {
        return modelManager.getModel(name);
    }

    /**
     * Return a unique name for this view. No other view may have this
     * name. Note that only applies within built-in views or custom views
     * but a custom view can share the name of a built-in one or vice versa.
     * By default simply return the author concatenated with the
     * name if the author is not null or just the name if the author
     * is null.
     * @return
     */
    public String getUniqueName()
    {
        return polyhedralModelConfig.getUniqueName();
    }


    /**
     * Return the display name for this view (the name to be shown in the menu).
     * This name need not be unique among all views.
     * @return
     */
    public abstract String getDisplayName();

    public PolyhedralModelConfig getPolyhedralModelConfig()
    {
        return polyhedralModelConfig;
    }

    //
    //  Setup methods, to be defined by subclasses
    //

    protected abstract void setupModelManager();

    protected abstract void setupPopupManager();

    protected abstract void setupInfoPanelManager();

    protected abstract void setupSpectrumPanelManager();

    protected void setupRenderer()
    {
        //renderer = new Renderer(modelManager);
        setRenderer(new Renderer(getModelManager(), getStatusBar()));
    }

    protected abstract void setupPickManager();
}
