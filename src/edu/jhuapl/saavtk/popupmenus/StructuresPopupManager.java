package edu.jhuapl.saavtk.popupmenus;

import java.awt.event.MouseEvent;
import java.util.HashMap;

import vtk.vtkProp;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 */
public class StructuresPopupManager extends PopupManager
{
    private ModelManager modelManager;
    private HashMap<Model, PopupMenu> modelToPopupMap =
        new HashMap<Model, PopupMenu>();


    public StructuresPopupManager(ModelManager modelManager, Renderer renderer)
    {
        this.modelManager = modelManager;

        PopupMenu popupMenu = new LinesPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.LINE_STRUCTURES), popupMenu);

        popupMenu = new PolygonsPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.POLYGON_STRUCTURES), popupMenu);

        popupMenu = new CirclesPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), popupMenu);

        popupMenu = new EllipsesPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES), popupMenu);

        popupMenu = new PointsPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.POINT_STRUCTURES), popupMenu);

        popupMenu = new GraticulePopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.GRATICULE), popupMenu);
    }

    public PopupMenu getPopup(Model model)
    {
        return modelToPopupMap.get(model);
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
    {
        PopupMenu popup = modelToPopupMap.get(modelManager.getModel(pickedProp));
        if (popup != null)
            popup.showPopup(e, pickedProp, pickedCellId, pickedPosition);
    }

    public void showPopup(MouseEvent e, ModelNames name)
    {
        PopupMenu popup = modelToPopupMap.get(modelManager.getModel(name));
        if (popup != null)
            popup.show(e.getComponent(), e.getX(), e.getY());
    }

    protected HashMap<Model, PopupMenu> getModelToPopupMap()
    {
        return modelToPopupMap;
    }

    public void registerPopup(Model model, PopupMenu menu)
    {
        modelToPopupMap.put(model, menu);
    }
}
