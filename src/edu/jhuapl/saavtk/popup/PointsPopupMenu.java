package edu.jhuapl.saavtk.popup;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.PointModel;

public class PointsPopupMenu extends StructuresPopupMenu
{
    public PointsPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((PointModel)modelManager.getModel(ModelNames.POINT_STRUCTURES), modelManager.getPolyhedralModel(), renderer, true, false, false);
    }
}
