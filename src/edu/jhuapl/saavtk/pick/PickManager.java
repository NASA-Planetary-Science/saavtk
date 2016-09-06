package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.util.HashMap;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.joglrendering.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popupmenus.PopupManager;
import edu.jhuapl.saavtk.util.Preferences;

public class PickManager extends Picker
{
    public static final double DEFAULT_PICK_TOLERANCE = 0.002;

    public enum PickMode
    {
        DEFAULT,
        CIRCLE_SELECTION,
        POLYGON_DRAW,
        LINE_DRAW,
        CIRCLE_DRAW,
        ELLIPSE_DRAW,
        POINT_DRAW,
        LIDAR_SHIFT
    }

    private PickMode pickMode = PickMode.DEFAULT;
    private Renderer renderer;
    private vtksbmtJoglCanvas renWin;

    private DefaultPicker defaultPicker;

    private HashMap<PickMode, Picker> nondefaultPickers = new HashMap<PickMode, Picker>();
    public HashMap<PickMode, Picker> getNonDefaultPickers() { return nondefaultPickers; }

    private PopupManager popupManager;
    private StatusBar statusBar;

    public StatusBar getStatusBar() { return statusBar; }

    public PickManager(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        this.renderer = renderer;
        this.renWin = renderer.getRenderWindowPanel();
        this.popupManager = popupManager;

        if (modelManager.getModel(ModelNames.LINE_STRUCTURES) != null)
            nondefaultPickers.put(PickMode.LINE_DRAW, new ControlPointsStructurePicker(renderer, modelManager, ModelNames.LINE_STRUCTURES));
        if (modelManager.getModel(ModelNames.POLYGON_STRUCTURES) != null)
            nondefaultPickers.put(PickMode.POLYGON_DRAW, new ControlPointsStructurePicker(renderer, modelManager, ModelNames.POLYGON_STRUCTURES));
        if (modelManager.getModel(ModelNames.CIRCLE_STRUCTURES) != null)
            nondefaultPickers.put(PickMode.CIRCLE_DRAW, new CirclePicker(renderer, modelManager));
        if (modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES) != null)
            nondefaultPickers.put(PickMode.ELLIPSE_DRAW, new EllipsePicker(renderer, modelManager));
        if (modelManager.getModel(ModelNames.POINT_STRUCTURES) != null)
            nondefaultPickers.put(PickMode.POINT_DRAW, new PointPicker(renderer, modelManager));
        if (modelManager.getModel(ModelNames.CIRCLE_STRUCTURES) != null)
            nondefaultPickers.put(PickMode.CIRCLE_SELECTION, new CircleSelectionPicker(renderer, modelManager));

        setDefaultPicker(new DefaultPicker(renderer, statusBar, modelManager, popupManager));

        setPickTolerance(Preferences.getInstance().getAsDouble(
                Preferences.PICK_TOLERANCE, Picker.DEFAULT_PICK_TOLERANCE));

        addPicker(defaultPicker);
    }

    public void setPickMode(PickMode mode)
    {
        if (this.pickMode == mode)
            return;

        this.pickMode = mode;

        if (this.pickMode == PickMode.DEFAULT)
        {
            renderer.setInteractorStyleToDefault();
            for (PickMode pm : nondefaultPickers.keySet())
            {
                removePicker(nondefaultPickers.get(pm));
            }
            defaultPicker.setSuppressPopups(false);
            renWin.getComponent().setCursor(new Cursor(defaultPicker.getDefaultCursor()));
        }
        else
        {
            renderer.setInteractorStyleToNone();
            for (PickMode pm : nondefaultPickers.keySet())
            {
                if (pm != this.pickMode)
                {
                    removePicker(nondefaultPickers.get(pm));
                }
            }
            Picker picker = nondefaultPickers.get(this.pickMode);
            addPicker(picker);
            defaultPicker.setSuppressPopups(true);
            renWin.getComponent().setCursor(new Cursor(picker.getDefaultCursor()));
        }
    }

    public DefaultPicker getDefaultPicker()
    {
        return defaultPicker;
    }

    protected void setDefaultPicker(DefaultPicker defaultPicker)
    {
        this.defaultPicker = defaultPicker;
    }

    protected void addPicker(Picker picker)
    {
        renWin.getComponent().addMouseListener(picker);
        renWin.getComponent().addMouseMotionListener(picker);
        renWin.getComponent().addMouseWheelListener(picker);
        renWin.getComponent().addKeyListener(picker);
    }

    private void removePicker(Picker picker)
    {
        renWin.getComponent().removeMouseListener(picker);
        renWin.getComponent().removeMouseMotionListener(picker);
        renWin.getComponent().removeMouseWheelListener(picker);
        renWin.getComponent().removeKeyListener(picker);
    }

    public double getPickTolerance()
    {
        // All the pickers managed by this class should have the same
        // tolerance so just return tolerance of the default picker.
        return defaultPicker.getPickTolerance();
    }

    public void setPickTolerance(double pickTolerance)
    {
        defaultPicker.setPickTolerance(pickTolerance);
        for (PickMode pm : nondefaultPickers.keySet())
            nondefaultPickers.get(pm).setPickTolerance(pickTolerance);
    }

    public PopupManager getPopupManager()
    {
        return popupManager;
    }

}