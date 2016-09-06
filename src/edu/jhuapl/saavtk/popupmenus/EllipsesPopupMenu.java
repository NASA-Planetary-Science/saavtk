package edu.jhuapl.saavtk.popupmenus;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.EllipseModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;

public class EllipsesPopupMenu extends StructuresPopupMenu
{
    public EllipsesPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((EllipseModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES), modelManager.getPolyhedralModel(), renderer, true, true, false);
    }
}
