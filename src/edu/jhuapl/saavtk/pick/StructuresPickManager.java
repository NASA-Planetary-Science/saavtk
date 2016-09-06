package edu.jhuapl.saavtk.pick;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popupmenus.PopupManager;

public class StructuresPickManager extends PickManager
{
    public StructuresPickManager(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        super(renderer, statusBar, modelManager, popupManager);

        if (modelManager.getModel(ModelNames.LINE_STRUCTURES) != null)
            getNonDefaultPickers().put(PickMode.LINE_DRAW, new ControlPointsStructurePicker(renderer, modelManager, ModelNames.LINE_STRUCTURES));
        if (modelManager.getModel(ModelNames.POLYGON_STRUCTURES) != null)
            getNonDefaultPickers().put(PickMode.POLYGON_DRAW, new ControlPointsStructurePicker(renderer, modelManager, ModelNames.POLYGON_STRUCTURES));
        if (modelManager.getModel(ModelNames.CIRCLE_STRUCTURES) != null)
            getNonDefaultPickers().put(PickMode.CIRCLE_DRAW, new CirclePicker(renderer, modelManager));
        if (modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES) != null)
            getNonDefaultPickers().put(PickMode.ELLIPSE_DRAW, new EllipsePicker(renderer, modelManager));
        if (modelManager.getModel(ModelNames.POINT_STRUCTURES) != null)
            getNonDefaultPickers().put(PickMode.POINT_DRAW, new PointPicker(renderer, modelManager));
        if (modelManager.getModel(ModelNames.CIRCLE_STRUCTURES) != null)
            getNonDefaultPickers().put(PickMode.CIRCLE_SELECTION, new CircleSelectionPicker(renderer, modelManager));
    }
}