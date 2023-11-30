package edu.jhuapl.saavtk.example;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.panel.PolyhedralModelControlPanel;
import edu.jhuapl.saavtk.gui.render.ConfigurableSceneNotifier;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.gui.StructureMainPanel;

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
	public ExampleView(StatusNotifier aStatusNotifier, ViewConfig config)
	{
		super(aStatusNotifier, config);
	}
	
	
    public IBodyViewConfig getConfig()
    {
        return (IBodyViewConfig)config;
    }

	/**
	 * Returns model as a path. e.g. "Asteroid > Near-Earth > Eros > Image Based >
	 * Gaskell"
	 */
	@Override
	public String getPathRepresentation()
	{
		IBodyViewConfig config = getConfig();
		if (ShapeModelType.CUSTOM == config.getAuthor())
		{
			return ShapeModelType.CUSTOM + " > " + config.getModelLabel();
		}
		return "DefaultPath";
	}

	@Override
	public String getDisplayName()
	{
		if (getConfig().getAuthor() == ShapeModelType.CUSTOM)
			return getConfig().getModelLabel();
		else
		{
			String version = "";
			if (getConfig().getVersion() != null)
				version += " (" + getConfig().getVersion() + ")";
			return getConfig().getAuthor().toString() + version;
		}
	}

	@Override
	public String getModelDisplayName()
	{
		ShapeModelBody body = getConfig().getBody();
		return body != null ? body + " / " + getDisplayName() : getDisplayName();
	}

	@Override
	protected void setupModelManager()
	{
		PolyhedralModel smallBodyModel = new ExamplePolyhedralModel((ViewConfig)getConfig());
//		Graticule graticule = new Graticule(smallBodyModel);

		HashMap<ModelNames, List<Model>> allModels = new HashMap<>();
		allModels.put(ModelNames.SMALL_BODY, ImmutableList.of(smallBodyModel)); 
//		allModels.put(ModelNames.GRATICULE, ImmutableList.of(graticule));
		

		// if (getConfig().hasLidarData) 
		// {
		// allModels.putAll(ModelFactory.createLidarModels(smallBodyModel));
		// }

		var tmpSceneChangeNotifier = new ConfigurableSceneNotifier();
		var tmpStatusNotifier = getStatusNotifier();
		structureManager = new AnyStructureManager(tmpSceneChangeNotifier, tmpStatusNotifier, smallBodyModel);

		// allModels.put(ModelNames.TRACKS, new
		// LidarSearchDataCollection(smallBodyModel));

		setModelManager(new ModelManager(smallBodyModel, allModels));
		tmpSceneChangeNotifier.setTarget(getModelManager());
	}

	@Override
	protected void setupPopupManager()
	{
		ModelManager tmpModelManager = getModelManager();

		setPopupManager(new PopupManager(tmpModelManager));
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

		addTab("Structures", new StructureMainPanel(getRenderer(), getModelManager().getPolyhedralModel(),
				getStatusNotifier(), getPickManager(), structureManager));

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
		PickManager tmpPickManager = new PickManager(getRenderer(), getStatusNotifier(), getModelManager().getPolyhedralModel(), getModelManager());
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
	protected void setupPositionOrientationManager()
	{
		
	}
	
	@Override
	protected void initializeStateManager()
	{
		// TODO Auto-generated method stub

	}

}
