package edu.jhuapl.saavtk.popup;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.PolygonModel;

public class PolygonsPopupMenu extends StructuresPopupMenu
{
    public PolygonsPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((PolygonModel)modelManager.getModel(ModelNames.POLYGON_STRUCTURES), modelManager.getPolyhedralModel(), renderer, false, true, true);
    }
}
