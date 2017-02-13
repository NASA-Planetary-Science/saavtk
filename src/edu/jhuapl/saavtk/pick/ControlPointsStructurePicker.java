package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

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
import edu.jhuapl.saavtk.model.structure.ControlPointsStructureModel;
import edu.jhuapl.saavtk.model.structure.Line;

/**
 * Picker for editing control point based structures such as Paths and Polygons.
 *
 * This picker supports something known as "Profile Mode". Profile mode is only
 * enabled for Paths and the mode simply enforces a maximum limit of 2 control
 * points per path (since saving out a profile are not supported for paths with
 * more than 2 control points).
 */
public class ControlPointsStructurePicker extends Picker
{
    private ModelManager modelManager;
    private vtksbmtJoglCanvas renWin;
    private PolyhedralModel smallBodyModel;
    private ControlPointsStructureModel structureModel;

    private vtkCellPicker smallBodyPicker;
    private vtkCellPicker structureActivationPicker;

    private int vertexIdBeingEdited = -1;

    private boolean profileMode = false;

    // There are 2 types of editing possible:
    //   1. Dragging an existing vertex to a new locations
    //   2. Extending a structure by adding new vertices
    public enum EditMode
    {
        VERTEX_DRAG_OR_DELETE,
        VERTEX_ADD
    }

    private EditMode currentEditMode = EditMode.VERTEX_ADD;

    private double[] lastDragPosition;

    public ControlPointsStructurePicker(
            Renderer renderer,
            ModelManager modelManager,
            ModelNames structureName
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.structureModel = (ControlPointsStructureModel)modelManager.getModel(structureName);

        profileMode = structureModel.hasProfileMode();

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

        structureActivationPicker = new vtkCellPicker();
        structureActivationPicker.PickFromListOn();
        structureActivationPicker.InitializePickList();
        vtkPropCollection structureActivationPickList = structureActivationPicker.GetPickList();
        structureActivationPickList.RemoveAllItems();
        structureActivationPicker.AddPickList(structureModel.getActivationActor());
    }

    public void mousePressed(MouseEvent e)
    {
        // If we pressed a vertex of an existing structure, begin dragging that vertex.
        // If we pressed a point on the body, begin adding a new control point.


        vertexIdBeingEdited = -1;
        lastDragPosition = null;

        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE)
        {
            if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3)
                return;

            int pickSucceeded = doPick(e, structureActivationPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = structureActivationPicker.GetActor();

                if (pickedActor == structureModel.getActivationActor())
                {
                    if (e.getButton() == MouseEvent.BUTTON1)
                    {
                        vertexIdBeingEdited = structureActivationPicker.GetCellId();

                        if (profileMode)
                        {
                            int lineId = structureModel.getStructureIdFromActivationCellId(vertexIdBeingEdited);
                            structureModel.activateStructure(lineId);
                        }

                        structureModel.selectCurrentStructureVertex(vertexIdBeingEdited);
                    }
                    else
                    {
                        vertexIdBeingEdited = -1;
                        if (profileMode)
                            structureModel.activateStructure(-1);
                    }
                }
            }
        }
        else if (this.currentEditMode == EditMode.VERTEX_ADD)
        {
            if (e.getButton() != MouseEvent.BUTTON1)
                return;

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
                        structureModel.insertVertexIntoActivatedStructure(pos);
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        if (this.currentEditMode == EditMode.VERTEX_DRAG_OR_DELETE &&
                vertexIdBeingEdited >= 0 &&
                lastDragPosition != null)
        {
            int vertexId = vertexIdBeingEdited;

            if (profileMode)
                vertexId = structureModel.getVertexIdFromActivationCellId(vertexIdBeingEdited);

            structureModel.updateActivatedStructureVertex(vertexId, lastDragPosition);
        }

        vertexIdBeingEdited = -1;
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

                    structureModel.moveActivationVertex(vertexIdBeingEdited, lastDragPosition);
                }
            }
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        int pickSucceeded = doPick(e, structureActivationPicker, renWin);

        // If we're in profile mode, then do not allow dragging of a vertex if we're
        // in the middle of creating a new profile. We can determine if we're in the
        // middle of creating one if the last line in the LineModel has fewer than 2
        // vertices.
        boolean profileModeOkToDrag = true;
        if (profileMode)
        {
            int lineId = structureModel.getNumberOfStructures() - 1;
            if (lineId >= 0)
            {
                Line line = (Line)structureModel.getStructure(lineId);
                if (line.controlPointIds.size() < 2)
                    profileModeOkToDrag = false;
            }
        }

        if (pickSucceeded == 1 &&
            structureActivationPicker.GetActor() == structureModel.getActivationActor() &&
            profileModeOkToDrag)
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

    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
        {
            structureModel.removeCurrentStructureVertex();
        }
    }
}
