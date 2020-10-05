package edu.jhuapl.saavtk.example;

import java.awt.AWTException;
import java.util.HashMap;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.panel.PolyhedralModelControlPanel;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.CircleSelectionModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.popup.GraticulePopupMenu;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.structure.gui.StructureTabbedPane;

/**
 * A view is a container which contains a control panel and renderer as well as
 * a collection of managers. A view is unique to a specific body. This class is
 * used to build all built-in and custom views. All the configuration details of
 * all the built-in and custom views are contained in this class.
 */
public class ExampleView extends View
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * By default a view should be created empty. Only when the user requests to
	 * show a particular View, should the View's contents be created in order to
	 * reduce memory and startup time. Therefore, this function should be called
	 * prior to first time the View is shown in order to cause it
	 */
	public ExampleView(StatusBar statusBar, ViewConfig config)
	{
		super(statusBar, config);
	}

	/**
	 * Returns model as a path. e.g. "Asteroid > Near-Earth > Eros > Image Based >
	 * Gaskell"
	 */
	@Override
	public String getPathRepresentation()
	{
		ViewConfig config = getConfig();
		if (ShapeModelType.CUSTOM == config.author)
		{
			return ShapeModelType.CUSTOM + " > " + config.modelLabel;
		}
		return "DefaultPath";
	}

	@Override
	public String getDisplayName()
	{
		if (getConfig().author == ShapeModelType.CUSTOM)
			return getConfig().modelLabel;
		else
		{
			String version = "";
			if (getConfig().version != null)
				version += " (" + getConfig().version + ")";
			return getConfig().author.toString() + version;
		}
	}

	@Override
	public String getModelDisplayName()
	{
		ShapeModelBody body = getConfig().body;
		return body != null ? body + " / " + getDisplayName() : getDisplayName();
	}

	@Override
	protected void setupModelManager()
	{
		PolyhedralModel smallBodyModel = new ExamplePolyhedralModel(getConfig());
		Graticule graticule = new Graticule(smallBodyModel);

		HashMap<ModelNames, Model> allModels = new HashMap<>();
		allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
		allModels.put(ModelNames.GRATICULE, graticule);

		// if (getConfig().hasLidarData)
		// {
		// allModels.putAll(ModelFactory.createLidarModels(smallBodyModel));
		// }

		allModels.put(ModelNames.LINE_STRUCTURES, new LineModel<>(smallBodyModel));
		allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
		allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
		allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
		allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
		allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));

		// allModels.put(ModelNames.TRACKS, new
		// LidarSearchDataCollection(smallBodyModel));

		setModelManager(new ModelManager(smallBodyModel, allModels));
	}

	@Override
	protected void setupPopupManager()
	{
		ModelManager tmpModelManager = getModelManager();

		try
		{
			PopupMenu popupMenu = new GraticulePopupMenu(tmpModelManager, getRenderer());
			registerPopup(tmpModelManager.getModel(ModelNames.GRATICULE), popupMenu);
		}
		catch (AWTException e)
		{
			e.printStackTrace();
		}

		setPopupManager(new PopupManager(tmpModelManager));

		// if (getConfig().hasLidarData)
		// {
		// LidarSearchDataCollection lidarSearch =
		// (LidarSearchDataCollection)getModel(ModelNames.LIDAR_SEARCH);
		// PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, getRenderer());
		// registerPopup(lidarSearch, popupMenu);
		// }
	}

	@Override
	protected void setupTabs()
	{
		addTab(getConfig().getShapeModelName(),
				PolyhedralModelControlPanel.of(getRenderer(), getModelManager(), getConfig().getShapeModelName()));

		// if (getConfig().hasLidarData)
		// {
		// JComponent component = new LidarPanel(getConfig(), getModelManager(),
		// getPickManager(), getRenderer());
		// addTab(getConfig().lidarInstrumentName.toString(), component);
		// }

		addTab("Structures", new StructureTabbedPane(getPickManager(), getRenderer(), getModelManager()));

		// if (!getConfig().customTemporary)
		// {
		// ImagingInstrument instrument = null;
		// for (ImagingInstrument i : getConfig().imagingInstruments)
		// {
		// instrument = i;
		// break;
		// }
		//
		// addTab("Images", new CustomImagesPanel(getModelManager(),
		// getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(),
		// getRenderer(), instrument).init());
		// }
		//

		// addTab("Tracks", new TrackPanel(getConfig(), getModelManager(),
		// getPickManager(), getRenderer()));

	}

	@Override
	protected void setupPickManager()
	{
		PickManager tmpPickManager = new PickManager(getRenderer(), getModelManager());
		PickUtil.installDefaultPickHandler(getPickManager(), getStatusBar(), getRenderer(), getModelManager());
		setPickManager(tmpPickManager);

		// Manually register the PopupManager with the PickManager
		tmpPickManager.getDefaultPicker().addListener(getPopupManager());
	}

	@Override
	protected void setupInfoPanelManager()
	{
	}

	@Override
	protected void setupSpectrumPanelManager()
	{
	}

	@Override
	protected void initializeStateManager()
	{
		// TODO Auto-generated method stub

	}

}
