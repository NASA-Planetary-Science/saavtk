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
import edu.jhuapl.saavtk.gui.jogl.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.CircleModel;

public class CirclePicker extends Picker
{
    private ModelManager modelManager;
    private vtksbmtJoglCanvas renWin;
    private PolyhedralModel smallBodyModel;
    private CircleModel circleModel;

    private vtkCellPicker smallBodyPicker;
    private vtkCellPicker circlePicker;

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

    public CirclePicker(
            Renderer renderer,
            ModelManager modelManager
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.circleModel = (CircleModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES);

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

        circlePicker = new vtkCellPicker();
        circlePicker.PickFromListOn();
        circlePicker.InitializePickList();
        vtkPropCollection circlePickList = circlePicker.GetPickList();
        circlePickList.RemoveAllItems();
        circlePicker.AddPickList(circleModel.getBoundaryActor());
    }

    public void mousePressed(MouseEvent e)
    {
        // If we pressed a vertex of an existing circle, begin dragging that vertex.
        // If we pressed a point on the asteroid, begin drawing a new circle.


        vertexIdBeingEdited = -1;
        lastDragPosition = null;

        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE)
        {
            if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
                return;

            int pickSucceeded = doPick(e, circlePicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = circlePicker.GetActor();

                if (pickedActor == circleModel.getBoundaryActor())
                {
                    if (e.getButton() == MouseEvent.BUTTON1)
                    {
                        int cellId = circlePicker.GetCellId();
                        int pointId = circleModel.getPolygonIdFromBoundaryCellId(cellId);
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
                circleModel.resetCircumferencePoints();
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
                        if (!circleModel.addCircumferencePoint(pos))
                        {
                            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(renWin.getComponent()),
                                    "Could not fit circle to specified points.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    public void mouseDragged(MouseEvent e)
    {
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
                        circleModel.changeRadiusOfPolygon(vertexIdBeingEdited, lastDragPosition);
                    else
                        circleModel.movePolygon(vertexIdBeingEdited, lastDragPosition);
                }
            }
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        int pickSucceeded = doPick(e, circlePicker, renWin);

        // Only allow dragging if we are not in the middle of drawing a
        // a new circle, i.e. if number of circumference points is zero.
        if (circleModel.getNumberOfCircumferencePoints() == 0 &&
                pickSucceeded == 1 &&
                circlePicker.GetActor() == circleModel.getBoundaryActor())
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

    @Override
    public int getDefaultCursor()
    {
        return Cursor.CROSSHAIR_CURSOR;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
        {
            int[] selectedStructures = circleModel.getSelectedStructures();
            circleModel.removeStructures(selectedStructures);
        }
    }
}
