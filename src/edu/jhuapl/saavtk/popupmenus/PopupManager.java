package edu.jhuapl.saavtk.popupmenus;

import java.awt.event.MouseEvent;
import java.util.HashMap;

import vtk.vtkProp;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 */
public abstract class PopupManager
{
    public abstract PopupMenu getPopup(Model model);

    public abstract void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition);

    public abstract void showPopup(MouseEvent e, ModelNames name);

    protected abstract HashMap<Model, PopupMenu> getModelToPopupMap();

    public abstract void registerPopup(Model model, PopupMenu menu);
}
