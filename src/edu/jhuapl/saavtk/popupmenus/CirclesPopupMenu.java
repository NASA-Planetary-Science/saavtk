package edu.jhuapl.saavtk.popupmenus;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.CircleModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;

public class CirclesPopupMenu extends StructuresPopupMenu
{
    public CirclesPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((CircleModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), modelManager.getPolyhedralModel(), renderer, true, true, false);
    }
}
