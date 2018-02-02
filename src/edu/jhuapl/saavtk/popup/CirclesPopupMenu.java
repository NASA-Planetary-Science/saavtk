package edu.jhuapl.saavtk.popup;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.CircleModel;

public class CirclesPopupMenu extends StructuresPopupMenu
{
    public CirclesPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((CircleModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), modelManager.getPolyhedralModel(), renderer, true, true, false);
    }
}
