package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JOptionPane;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.joglrendering.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.EllipseModel;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;

public class EllipsePicker extends Picker
{
    private ModelManager modelManager;
    private vtksbmtJoglCanvas renWin;
    private PolyhedralModel smallBodyModel;
    private EllipseModel ellipseModel;

    private vtkCellPicker smallBodyPicker;
    private vtkCellPicker ellipsePicker;

    private int vertexIdBeingEdited = -1;

    // There are 2 types of line editing possible:
    //   1. Dragging an existing vertex to a new locations
    //   2. Extending a line by adding new vertices
    public enum EditMode
    {
        VERTEX_DRAG_OR_DELETE,
        VERTEX_ADD
    }

    private EditMode currentEditMode = EditMode.VERTEX_ADD;

    private double[] lastDragPosition;
    private boolean changeFlatteningKeyPressed = false;
    private boolean changeAngleKeyPressed = false;

    public EllipsePicker(
            Renderer renderer,
            ModelManager modelManager
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.ellipseModel = (EllipseModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);

        smallBodyPicker = new vtkCellPicker();
        smallBodyPicker.PickFromListOn();
        smallBodyPicker.InitializePickList();
        smallBodyModel = (PolyhedralModel)modelManager.getPolyhedralModel();
        List<vtkProp> actors = smallBodyModel.getProps();
        vtkPropCollection smallBodyPickList = smallBodyPicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        for (vtkProp act : actors)
        {
            smallBodyPicker.AddPickList(act);
        }
        smallBodyPicker.AddLocator(smallBodyModel.getCellLocator());

        ellipsePicker = new vtkCellPicker();
        ellipsePicker.PickFromListOn();
        ellipsePicker.InitializePickList();
        vtkPropCollection ellipsePickList = ellipsePicker.GetPickList();
        ellipsePickList.RemoveAllItems();
        ellipsePicker.AddPickList(ellipseModel.getBoundaryActor());
    }

    public void mousePressed(MouseEvent e)
    {
        // If we pressed a vertex of an existing ellipse, begin dragging that vertex.
        // If we pressed a point on the asteroid, begin drawing a new ellipse.


        vertexIdBeingEdited = -1;
        lastDragPosition = null;

        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE)
        {
            if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
                return;

            int pickSucceeded = doPick(e, ellipsePicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = ellipsePicker.GetActor();

                if (pickedActor == ellipseModel.getBoundaryActor())
                {
                    if (e.getButton() == MouseEvent.BUTTON1)
                    {
                        int cellId = ellipsePicker.GetCellId();
                        int pointId = ellipseModel.getPolygonIdFromBoundaryCellId(cellId);
                        this.vertexIdBeingEdited = pointId;
                    }
                    else
                    {
                        vertexIdBeingEdited = -1;
                    }
                }
            }
        }
        else if (this.currentEditMode == EditMode.VERTEX_ADD)
        {
            if (e.getButton() != MouseEvent.BUTTON1)
            {
                ellipseModel.resetCircumferencePoints();
                return;
            }

            int pickSucceeded = doPick(e, smallBodyPicker, renWin);

            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    double[] pos = smallBodyPicker.GetPickPosition();
                    if (e.getClickCount() == 1)
                    {
                        if (!ellipseModel.addCircumferencePoint(pos))
                        {
                            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(renWin.getComponent()),
                                    "Could not fit ellipse to specified points.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseDragged(MouseEvent e)
    {
        //if (e.getButton() != MouseEvent.BUTTON1)
        //    return;


        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
            vertexIdBeingEdited >= 0)
        {
            int pickSucceeded = doPick(e, smallBodyPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    lastDragPosition = smallBodyPicker.GetPickPosition();

                    if (e.isControlDown() || e.isShiftDown())
                        ellipseModel.changeRadiusOfPolygon(vertexIdBeingEdited, lastDragPosition);
                    else if (changeFlatteningKeyPressed)
                        ellipseModel.changeFlatteningOfPolygon(vertexIdBeingEdited, lastDragPosition);
                    else if (changeAngleKeyPressed)
                        ellipseModel.changeAngleOfPolygon(vertexIdBeingEdited, lastDragPosition);
                    else
                        ellipseModel.movePolygon(vertexIdBeingEdited, lastDragPosition);
                }
            }
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        int pickSucceeded = doPick(e, ellipsePicker, renWin);

        // Only allow dragging if we are not in the middle of drawing a
        // new ellipse, i.e. if number of circumference points is zero.
        if (ellipseModel.getNumberOfCircumferencePoints() == 0 &&
                pickSucceeded == 1 &&
                ellipsePicker.GetActor() == ellipseModel.getBoundaryActor())
        {
            if (renWin.getComponent().getCursor().getType() != Cursor.HAND_CURSOR)
                renWin.getComponent().setCursor(new Cursor(Cursor.HAND_CURSOR));

            currentEditMode = EditMode.VERTEX_DRAG_OR_DELETE;
        }
        else
        {
            if (renWin.getComponent().getCursor().getType() != getDefaultCursor())
                renWin.getComponent().setCursor(new Cursor(getDefaultCursor()));

            currentEditMode = EditMode.VERTEX_ADD;
        }
    }

    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_SLASH)
            changeFlatteningKeyPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_X || e.getKeyCode() == KeyEvent.VK_PERIOD)
            changeAngleKeyPressed = true;

        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
        {
            int[] selectedStructures = ellipseModel.getSelectedStructures();
            ellipseModel.removeStructures(selectedStructures);
        }
    }

    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_SLASH)
            changeFlatteningKeyPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_X || e.getKeyCode() == KeyEvent.VK_PERIOD)
            changeAngleKeyPressed = false;
    }

    @Override
    public int getDefaultCursor()
    {
        return Cursor.CROSSHAIR_CURSOR;
    }
}
