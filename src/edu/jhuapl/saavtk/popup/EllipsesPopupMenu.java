package edu.jhuapl.saavtk.popup;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.EllipseModel;

public class EllipsesPopupMenu extends StructuresPopupMenu
{
    public EllipsesPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((EllipseModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES), modelManager.getPolyhedralModel(), renderer, false, true, false);
    }
}
