package edu.jhuapl.saavtk.example;

import java.util.HashMap;

import edu.jhuapl.near.pick.StructuresPickManager;
import edu.jhuapl.near.popupmenus.StructuresPopupManager;
import edu.jhuapl.saavtk.gui.PolyhedralModelControlPanel;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.StructuresControlPanel;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.model.CircleModel;
import edu.jhuapl.saavtk.model.CircleSelectionModel;
import edu.jhuapl.saavtk.model.EllipseModel;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.LineModel;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PointModel;
import edu.jhuapl.saavtk.model.PolygonModel;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.PolyhedralModelConfig;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;


/**
 * A view is a container which contains a control panel and renderer
 * as well as a collection of managers. A view is unique to a specific
 * body. This class is used to build all built-in and custom views.
 * All the configuration details of all the built-in and custom views
 * are contained in this class.
 */
public class ExampleView extends View
{
    /**
     * By default a view should be created empty. Only when the user
     * requests to show a particular View, should the View's contents
     * be created in order to reduce memory and startup time. Therefore,
     * this function should be called prior to first time the View is
     * shown in order to cause it
     */
    public ExampleView(StatusBar statusBar, PolyhedralModelConfig polyhedralModelConfig)
    {
        super(statusBar, polyhedralModelConfig);
    }

    public String getDisplayName()
    {
        if (getPolyhedralModelConfig().author == ShapeModelAuthor.CUSTOM)
            return getPolyhedralModelConfig().customName;
        else
        {
            String version = "";
            if (getPolyhedralModelConfig().version != null)
                version += " (" + getPolyhedralModelConfig().version + ")";
            return getPolyhedralModelConfig().author.toString() + version;
        }
    }

    protected void setupModelManager()
    {
        PolyhedralModel smallBodyModel = new ExamplePolyhedralModel((PolyhedralModelConfig)getPolyhedralModelConfig());
        setModelManager(new ExampleModelManager(smallBodyModel));
        Graticule graticule = new Graticule(smallBodyModel);

        HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.GRATICULE, graticule);

//        if (getPolyhedralModelConfig().hasLidarData)
//        {
//            allModels.putAll(ModelFactory.createLidarModels(smallBodyModel));
//        }

        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
        allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));

//        allModels.put(ModelNames.TRACKS, new LidarSearchDataCollection(smallBodyModel));

        setModels(allModels);
    }

    protected void setupPopupManager()
    {
        setPopupManager(new StructuresPopupManager(getModelManager(), getRenderer()));

//        if (getPolyhedralModelConfig().hasLidarData)
//        {
//            LidarSearchDataCollection lidarSearch = (LidarSearchDataCollection)getModel(ModelNames.LIDAR_SEARCH);
//            PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, getRenderer());
//            registerPopup(lidarSearch, popupMenu);
//        }
    }

    protected void setupTabs()
    {
        addTab(getPolyhedralModelConfig().getShapeModelName(), new PolyhedralModelControlPanel(getModelManager(), getPolyhedralModelConfig().getShapeModelName()));

//        if (getPolyhedralModelConfig().hasLidarData)
//        {
//            JComponent component = new LidarPanel(getPolyhedralModelConfig(), getModelManager(), getPickManager(), getRenderer());
//            addTab(getPolyhedralModelConfig().lidarInstrumentName.toString(), component);
//        }


        addTab("Structures", new StructuresControlPanel(getModelManager(), getPickManager()));

//        if (!getPolyhedralModelConfig().customTemporary)
//        {
//            ImagingInstrument instrument = null;
//            for (ImagingInstrument i : getPolyhedralModelConfig().imagingInstruments)
//            {
//                instrument = i;
//                break;
//            }
//
//            addTab("Images", new CustomImagesPanel(getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init());
//        }
//

//        addTab("Tracks", new TrackPanel(getPolyhedralModelConfig(), getModelManager(), getPickManager(), getRenderer()));

    }

    protected void setupPickManager()
    {
      setPickManager(new StructuresPickManager(getRenderer(), getStatusBar(), getModelManager(), getPopupManager()));
    }


    protected void setupInfoPanelManager()
    {
    }

    protected void setupSpectrumPanelManager()
    {
    }

}
