package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class PointPicker extends Picker
{
	// Reference vars
	private ModelManager refModelManager;
	private PolyhedralModel refSmallBodyModel;
	private PointModel refStructureManager;
	private vtkJoglPanelComponent refRenWin;

	// VTK vars
	private vtkCellPicker smallBodyPicker;
	private vtkCellPicker structurePicker;

	// State vars
	private EditMode currEditMode;
	private int currVertexId;

	public PointPicker(Renderer aRenderer, ModelManager aModelManager)
	{
		refModelManager = aModelManager;
		refSmallBodyModel = aModelManager.getPolyhedralModel();
		refStructureManager = (PointModel) aModelManager.getModel(ModelNames.POINT_STRUCTURES);
		refRenWin = aRenderer.getRenderWindowPanel();

		smallBodyPicker = PickUtilEx.formSmallBodyPicker(refSmallBodyModel);
		structurePicker = PickUtilEx.formPickerFor(refStructureManager.getInteriorActor());

		currEditMode = EditMode.CLICKABLE;
		currVertexId = -1;
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
			return;

		// Bail if a valid point was not picked
		boolean isPicked = PickUtil.isPicked(smallBodyPicker, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);
		if (model == refSmallBodyModel)
		{
			double[] posArr = smallBodyPicker.GetPickPosition();
			// TODO: Is this conditional really necessary?
			if (aEvent.getClickCount() == 1)
			{
				refStructureManager.addNewStructure(new Vector3D(posArr));
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
		boolean isPicked = PickUtil.isPicked(structurePicker, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		vtkActor pickedActor = structurePicker.GetActor();

		if (pickedActor == refStructureManager.getInteriorActor())
		{
			int cellId = structurePicker.GetCellId();
			int pointId = refStructureManager.getPolygonIdFromInteriorCellId(cellId);
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
		Ellipse tmpItem = refStructureManager.getItem(currVertexId);

		// Bail if the left button is not pressed
//		if (e.getButton() != MouseEvent.BUTTON1)
//			return;

		// Bail if we failed to pick something
		boolean isPicked = PickUtil.isPicked(smallBodyPicker, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);

		if (model == refSmallBodyModel)
		{
			double[] lastDragPositionArr = smallBodyPicker.GetPickPosition();
			Vector3D lastDragPosition = new Vector3D(lastDragPositionArr);

			if (aEvent.isControlDown() || aEvent.isShiftDown())
				refStructureManager.changeRadiusOfPolygon(tmpItem, lastDragPosition);
			else
				refStructureManager.setCenter(tmpItem, lastDragPosition);
		}
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		boolean isPicked = PickUtil.isPicked(structurePicker, refRenWin, aEvent, getTolerance());

		if (isPicked == true && structurePicker.GetActor() == refStructureManager.getInteriorActor())
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
			Set<Ellipse> pickS = refStructureManager.getSelectedItems();
			refStructureManager.removeItems(pickS);
		}
	}

}
