package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class EllipsePicker extends Picker
{
	// Reference vars
	private ModelManager refModelManager;
	private PolyhedralModel refSmallBodyModel;
	private EllipseModel refStructureModel;
	private vtkJoglPanelComponent refRenWin;

	// VTK vars
	private vtkCellPicker smallBodyPicker;
	private vtkCellPicker structurePicker;

	// State vars
	private EditMode currEditMode;
	private int currVertexId;
	private boolean changeAngleKeyPressed;
	private boolean changeFlatteningKeyPressed;

	public EllipsePicker(Renderer renderer, ModelManager modelManager)
	{
		refModelManager = modelManager;
		refSmallBodyModel = (PolyhedralModel) modelManager.getPolyhedralModel();
		refStructureModel = (EllipseModel) modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);
		refRenWin = renderer.getRenderWindowPanel();

		smallBodyPicker = PickUtilEx.formSmallBodyPicker(refSmallBodyModel);
		structurePicker = PickUtilEx.formStructurePicker(refStructureModel.getBoundaryActor());

		currEditMode = EditMode.CLICKABLE;
		currVertexId = -1;
		changeAngleKeyPressed = false;
		changeFlatteningKeyPressed = false;
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

		// Bail if mouse button 1 is not pressed
		if (aEvent.getButton() != MouseEvent.BUTTON1)
		{
			refStructureModel.resetCircumferencePoints();
			return;
		}

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
//				refStructureModel.addNewStructure(pos);
				if (refStructureModel.addCircumferencePoint(pos) == false)
				{
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(refRenWin.getComponent()),
							"Could not fit ellipse to specified points.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// Assume nothing will be picked
		currVertexId = -1;

		// Bail if we are not ready to do a drag operation
		if (currEditMode != EditMode.DRAGGABLE)
			return;

		// Bail if mouse button 1 is not pressed
		if (aEvent.getButton() != MouseEvent.BUTTON1)
			return;

		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, structurePicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		// Determine what was picked
		vtkActor pickedActor = structurePicker.GetActor();
		if (pickedActor == refStructureModel.getBoundaryActor())
		{
			int cellId = structurePicker.GetCellId();
			int pointId = refStructureModel.getPolygonIdFromBoundaryCellId(cellId);
			currVertexId = pointId;
		}
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		currVertexId = -1;
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Bail if we are not in the proper edit mode or there is no vertex being edited
		if (currEditMode != EditMode.DRAGGABLE || currVertexId < 0)
			return;

		// Bail if the left button is not pressed
//		if (e.getButton() != MouseEvent.BUTTON1)
//			return;

		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, smallBodyPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);

		if (model == refSmallBodyModel)
		{
			double[] lastDragPosition = smallBodyPicker.GetPickPosition();

			if (aEvent.isControlDown() || aEvent.isShiftDown())
				refStructureModel.changeRadiusOfPolygon(currVertexId, lastDragPosition);
			else if (changeFlatteningKeyPressed)
				refStructureModel.changeFlatteningOfPolygon(currVertexId, lastDragPosition);
			else if (changeAngleKeyPressed)
				refStructureModel.changeAngleOfPolygon(currVertexId, lastDragPosition);
			else
				refStructureModel.movePolygon(currVertexId, lastDragPosition);
		}
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		int pickSucceeded = doPick(aEvent, structurePicker, refRenWin);
		int numActivePoints = refStructureModel.getNumberOfCircumferencePoints();

		// Only allow dragging if we are not in the middle of drawing a
		// new ellipse, i.e. if number of circumference points is zero.
		if (numActivePoints == 0 && pickSucceeded == 1
				&& structurePicker.GetActor() == refStructureModel.getBoundaryActor())
			currEditMode = EditMode.DRAGGABLE;
		else
			currEditMode = EditMode.CLICKABLE;

		GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
	}

	@Override
	public void keyPressed(KeyEvent aEvent)
	{
		int keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_SLASH)
			changeFlatteningKeyPressed = true;
		if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_PERIOD)
			changeAngleKeyPressed = true;

		if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
		{
			int[] selectedStructures = refStructureModel.getSelectedStructures();
			refStructureModel.removeStructures(selectedStructures);
		}
	}

	@Override
	public void keyReleased(KeyEvent aEvent)
	{
		int keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_SLASH)
			changeFlatteningKeyPressed = false;
		if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_PERIOD)
			changeAngleKeyPressed = false;
	}

}
