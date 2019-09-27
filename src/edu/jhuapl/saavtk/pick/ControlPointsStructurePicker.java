package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.ControlPointsStructureModel;
import edu.jhuapl.saavtk.model.structure.Line;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Picker for editing control point based structures such as Paths and Polygons.
 *
 * This picker supports something known as "Profile Mode". Profile mode is only
 * enabled for Paths and the mode simply enforces a maximum limit of 2 control
 * points per path (since saving out a profile are not supported for paths with
 * more than 2 control points).
 */
public class ControlPointsStructurePicker<G1 extends Line> extends Picker
{
	// Reference vars
	private ModelManager refModelManager;
	private PolyhedralModel refSmallBodyModel;
	private ControlPointsStructureModel<G1> refStructureModel;
	private vtkJoglPanelComponent refRenWin;

	// VTK vars
	private vtkCellPicker smallBodyPicker;
	private vtkCellPicker structurePicker;

	// State vars
	private EditMode currEditMode;
	private int currVertexId;
	private boolean profileMode;

	private double[] lastDragPosition;

	public ControlPointsStructurePicker(Renderer renderer, ModelManager modelManager, ModelNames structureName)
	{
		refModelManager = modelManager;
		refSmallBodyModel = (PolyhedralModel) modelManager.getPolyhedralModel();
		refStructureModel = (ControlPointsStructureModel<G1>) modelManager.getModel(structureName);
		refRenWin = renderer.getRenderWindowPanel();

		smallBodyPicker = PickUtilEx.formSmallBodyPicker(refSmallBodyModel);
		structurePicker = PickUtilEx.formStructurePicker(refStructureModel.getActivationActor());

		currEditMode = EditMode.CLICKABLE;
		currVertexId = -1;
		profileMode = refStructureModel.hasProfileMode();
		lastDragPosition = null;
	}

	@Override
	public int getCursorType()
	{
		if (currEditMode == EditMode.DRAGGABLE)
			return Cursor.HAND_CURSOR;

		return Cursor.CROSSHAIR_CURSOR;
	}

	@Override
	public boolean isExclusiveMode()
	{
		if (currVertexId >= 0)
			return true;

		return false;
	}

	@Override
	public void mouseClicked(MouseEvent aEvent)
	{
		// We respond only if we are adding points
		if (currEditMode != EditMode.CLICKABLE)
			return;

		// Bail if the 1st mouse button is not pressed
		if (aEvent.getButton() != MouseEvent.BUTTON1)
			return;

		// Bail if a valid point was not picked
		int pickSucceeded = doPick(aEvent, smallBodyPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);
		if (model == refSmallBodyModel)
		{
			double[] pos = smallBodyPicker.GetPickPosition();
			// TODO: Is this conditional really necessary?
			if (aEvent.getClickCount() == 1)
			{
				refStructureModel.insertVertexIntoActivatedStructure(pos);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// If we pressed a vertex of an existing structure, begin dragging that vertex.
		// If we pressed a point on the body, begin adding a new control point.

		currVertexId = -1;
		lastDragPosition = null;

		// Bail if we are not ready to do a drag operation
		if (currEditMode != EditMode.DRAGGABLE)
			return;

		// Bail if either mouse button 1 or button 3 are not pressed
		if (aEvent.getButton() != MouseEvent.BUTTON1 && aEvent.getButton() != MouseEvent.BUTTON3)
			return;

		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, structurePicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		// Determine what was picked
		vtkActor pickedActor = structurePicker.GetActor();
		if (pickedActor == refStructureModel.getActivationActor())
		{
			if (aEvent.getButton() == MouseEvent.BUTTON1)
			{
				currVertexId = structurePicker.GetCellId();

				if (profileMode)
				{
					G1 tmpItem = refStructureModel.getStructureFromActivationCellId(currVertexId);
					refStructureModel.activateStructure(tmpItem);
				}

				refStructureModel.selectCurrentStructureVertex(currVertexId);
			}
			else
			{
				currVertexId = -1;
				if (profileMode)
					refStructureModel.activateStructure(null);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		if (this.currEditMode == EditMode.DRAGGABLE && currVertexId >= 0 && lastDragPosition != null)
		{
			int vertexId = currVertexId;
			if (profileMode)
				vertexId = refStructureModel.getVertexIdFromActivationCellId(currVertexId);

			refStructureModel.updateActivatedStructureVertex(vertexId, lastDragPosition);
		}

		currVertexId = -1;
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Bail if we are not in the proper edit mode or there is no vertex being edited
		if (currEditMode != EditMode.DRAGGABLE || currVertexId < 0)
			return;

		// Bail if the left button is not pressed
// 		if (e.getButton() != MouseEvent.BUTTON1)
// 			return;

		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, smallBodyPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);

		if (model == refSmallBodyModel)
		{
			lastDragPosition = smallBodyPicker.GetPickPosition();

			refStructureModel.moveActivationVertex(currVertexId, lastDragPosition);
		}
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		int pickSucceeded = doPick(aEvent, structurePicker, refRenWin);

		// If we're in profile mode, then do not allow dragging of a vertex if we're
		// in the middle of creating a new profile. We can determine if we're in the
		// middle of creating one if the last line in the LineModel has fewer than 2
		// vertices.
		boolean profileModeOkToDrag = true;
		if (profileMode)
		{
			int lineId = refStructureModel.getNumItems() - 1;
			if (lineId >= 0)
			{
				Line line = (Line) refStructureModel.getStructure(lineId);
				if (line.controlPointIds.size() < 2)
					profileModeOkToDrag = false;
			}
		}

		if (pickSucceeded == 1 && structurePicker.GetActor() == refStructureModel.getActivationActor()
				&& profileModeOkToDrag)
			currEditMode = EditMode.DRAGGABLE;
		else
			currEditMode = EditMode.CLICKABLE;

		GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
	}

	@Override
	public void keyPressed(KeyEvent aEvent)
	{
		int keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
		{
			refStructureModel.removeCurrentStructureVertex();
		}
	}
}
