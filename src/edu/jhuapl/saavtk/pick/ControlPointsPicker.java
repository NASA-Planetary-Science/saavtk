package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkControlPointPainter;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Picker that works with hard edge (lines, polygons) structures that provides
 * the following supported features:
 * <UL>
 * <LI>Support for (multiple) item deselection / selection
 * <LI>Ability to add or delete control points
 * <LI>Ability to drag control points
 * <LI>Capability to specify the "active" control point
 * </UL>
 * The "active" control point is the control point that was last selected.
 * Points will be added past the "active" control point.
 *
 * @author lopeznr1
 */
public class ControlPointsPicker<G1 extends PolyLine> extends Picker implements PickManagerListener
{
	// Reference vars
	private final ModelManager refModelManager;
	private final PickManager refPickManager;
	private final PolyhedralModel refSmallBody;
	private final LineModel<G1> refStructureManager;
	private final vtkJoglPanelComponent refRenWin;

	// VTK vars
	private final vtkCellPicker vSmallBodyPickerCP;
	private final vtkCellPicker vActivatePickerCP;
	private final vtkCellPicker vStructurePickerCP;

	// State vars
	private EditMode currEditMode;
	private G1 hookItem;
	private int hookControlPointIdx;
	private Vector3D lastDragPosition;

	private boolean isNewMode;
	private List<Vector3D> newPointL;

	/**
	 * Standard Constructor
	 */
	public ControlPointsPicker(Renderer aRenderer, PickManager aPickManager, ModelManager aModelManager,
			LineModel<G1> aStructureManager)
	{
		refModelManager = aModelManager;
		refPickManager = aPickManager;
		refSmallBody = refModelManager.getPolyhedralModel();
		refStructureManager = aStructureManager;
		refRenWin = aRenderer.getRenderWindowPanel();

		vSmallBodyPickerCP = PickUtilEx.formSmallBodyPicker(refSmallBody);
		vActivatePickerCP = PickUtilEx.formPickerFor(refStructureManager.getVtkControlPointActor());
		vStructurePickerCP = PickUtilEx.formPickerFor(refStructureManager.getVtkItemActor());

		currEditMode = EditMode.CLICKABLE;
		hookControlPointIdx = -1;
		hookItem = null;
		lastDragPosition = null;

		isNewMode = false;
		newPointL = new ArrayList<>();

		// Register for events of interest
		refPickManager.addListener(this);
	}

	/**
	 * Sends notification to this picker that a new item should be created
	 */
	public void startNewItem()
	{
		// Clear out any partially laid down points
		disableNewMode();

		// Switch into "new" mode
		isNewMode = true;
		newPointL.clear();
	}

	@Override
	public int getCursorType()
	{
		if (currEditMode == EditMode.DRAGGABLE)
			return Cursor.HAND_CURSOR;
		else if (currEditMode == EditMode.NONE)
			return Cursor.DEFAULT_CURSOR;

		return Cursor.CROSSHAIR_CURSOR;
	}

	@Override
	public boolean isExclusiveMode()
	{
		if (hookItem != null)
			return true;

		if (hookControlPointIdx >= 0)
			return true;

		return false;
	}

	@Override
	public void keyPressed(KeyEvent aEvent)
	{
		int keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
		{
			if (isNewMode == true)
			{
				int tmpIdx = refStructureManager.getControlPointPainter().getControlPoints().size() - 1;
				if (tmpIdx == -1)
					return;

				refStructureManager.getControlPointPainter().delPoint(tmpIdx);
				refStructureManager.notifyModelChanged();
				return;
			}

			int tmpIdx = refStructureManager.getActivatedControlPoint();
			refStructureManager.delControlPoint(tmpIdx);
		}

		hookControlPointIdx = -1;
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
		boolean isPicked = PickUtil.isPicked(vSmallBodyPickerCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Attempt to create a new control point
		vtkActor pickedActor = vSmallBodyPickerCP.GetActor();
		Model model = refModelManager.getModel(pickedActor);
		if (model == refSmallBody)
		{
			Vector3D tmpPos = new Vector3D(vSmallBodyPickerCP.GetPickPosition());
			if (aEvent.getClickCount() != 1)
				return;

			// Just add the new control point if we are creating a new item
			if (isNewMode == true)
			{
				VtkControlPointPainter tmpPainter = refStructureManager.getControlPointPainter();
				tmpPainter.addPoint(tmpPos);
				refStructureManager.notifyModelChanged();
				if (refStructureManager.getNumPointsNeededForNewItem() == tmpPainter.getControlPoints().size())
				{
					// Create the item
					int tmpId = StructureMiscUtil.calcNextId(refStructureManager);
					G1 tmpItem = refStructureManager.addItemWithControlPoints(tmpId, tmpPainter.getControlPoints());

					// Set it as the selected and activated item
					List<G1> tmpL = ImmutableList.of(tmpItem);
					refStructureManager.setSelectedItems(tmpL);
					refStructureManager.setActivatedItem(tmpItem);

					// Switch out of the new mode
					disableNewMode();
				}

				return;
			}

			int tmpControlPointIdx = refStructureManager.getActivatedControlPoint() + 1;
			refStructureManager.addControlPoint(tmpControlPointIdx, tmpPos);

			refStructureManager.setActivatedControlPoint(tmpControlPointIdx);
		}
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// If we pressed a vertex of an existing structure, begin dragging that vertex.
		// If we pressed a point on the body, begin adding a new control point.

		hookControlPointIdx = -1;
		hookItem = null;
		lastDragPosition = null;

		// Bail if we are not ready to do a drag operation
		if (currEditMode != EditMode.DRAGGABLE)
			return;

		// Bail if either mouse button 1 or button 3 are not pressed
		if (aEvent.getButton() != MouseEvent.BUTTON1 && aEvent.getButton() != MouseEvent.BUTTON3)
			return;

		// Control points selection logic
		if (PickUtil.isPicked(vActivatePickerCP, refRenWin, aEvent, getTolerance()) == true)
		{
			// Button1 must be depressed
			if (aEvent.getButton() != MouseEvent.BUTTON1)
				return;

			// Determine the selected item and the selected vertex
			int tmpCellId = vActivatePickerCP.GetCellId();
			hookControlPointIdx = refStructureManager.getControlPointIndexFromActivationCellId(tmpCellId);
			G1 hookItem = refStructureManager.getItemFromActivationCellId(tmpCellId);

			// Update the StructureManager of the activated items
			refStructureManager.setActivatedItem(hookItem);
			refStructureManager.setActivatedControlPoint(hookControlPointIdx);
		}

		// Bail if we are in profile mode since selection of structures is not supported
		if (refStructureManager.hasProfileMode() == true)
			return;

		// Structure item selection logic
		if (PickUtil.isPicked(vStructurePickerCP, refRenWin, aEvent, getTolerance()) == true)
		{
			vtkActor pickedActor = vStructurePickerCP.GetActor();
			int cellId = vStructurePickerCP.GetCellId();

			// Do not allow picking of the activated structure
			G1 tmpHookItem = refStructureManager.getItemFromCellId(cellId, pickedActor);
			if (tmpHookItem != refStructureManager.getActivatedItem())
				hookItem = tmpHookItem;

			// Ensure we are out of new mode
			disableNewMode();
		}
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		if (currEditMode == EditMode.DRAGGABLE && hookControlPointIdx >= 0 && lastDragPosition != null)
		{
			refStructureManager.moveControlPoint(hookControlPointIdx, lastDragPosition, true);
		}
		else if (hookItem != null)
		{
			// Update the selection
			HookUtil.updateSelection(refStructureManager, aEvent, hookItem);

			// Switch to the "none" EditMode
			currEditMode = EditMode.NONE;
			GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
		}

		hookItem = null;
		hookControlPointIdx = -1;
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		hookItem = null;

		// Bail if we are not in the proper edit mode or there is no vertex being edited
		if (currEditMode != EditMode.DRAGGABLE || hookControlPointIdx < 0)
			return;

		// Bail if we failed to pick something
		boolean isPicked = PickUtil.isPicked(vSmallBodyPickerCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Attempt to update the hooked ControlPoint's position
		vtkActor pickedActor = vSmallBodyPickerCP.GetActor();
		Model model = refModelManager.getModel(pickedActor);
		if (model == refSmallBody)
		{
			lastDragPosition = new Vector3D(vSmallBodyPickerCP.GetPickPosition());
			refStructureManager.moveControlPoint(hookControlPointIdx, lastDragPosition, false);
		}
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		vtkActor pickedActor = null;

		// Determine if the activatePicker has been triggered
		boolean isPickedA = PickUtil.isPicked(vActivatePickerCP, refRenWin, aEvent, getTolerance());
		if (isPickedA == true)
			pickedActor = vActivatePickerCP.GetActor();

		// Determine if the structurePicker has been triggered
		// Note if has a "profile mode" then ignore the structurePicker
		boolean isPickedB = PickUtil.isPicked(vStructurePickerCP, refRenWin, aEvent, getTolerance());
		if (isPickedB == true && pickedActor == null && refStructureManager.hasProfileMode() == false)
		{
			pickedActor = vStructurePickerCP.GetActor();
			int cellId = vStructurePickerCP.GetCellId();

			// Do not allow picking of the activated structure
			G1 tmpHookItem = refStructureManager.getItemFromCellId(cellId, pickedActor);
			if (tmpHookItem == refStructureManager.getActivatedItem())
				pickedActor = null;
		}

		// Switch to the proper editMode
		if (pickedActor != null)
			currEditMode = EditMode.DRAGGABLE;
		else
			currEditMode = EditMode.CLICKABLE;

		GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
	}

	@Override
	public void pickerChanged()
	{
		boolean isEnabled = refPickManager.getActivePicker() == this;
		if (isEnabled == false)
			disableNewMode();
	}

	/**
	 * Helper method that turn off this {@link ControlPointsPicker}'s "new" mode.
	 */
	private void disableNewMode()
	{
		// Bail if we are not in "new" mode
		if (isNewMode == false)
			return;

		// Switch out of "new" mode
		isNewMode = false;
		newPointL.clear();

		// Clear the painter
		VtkControlPointPainter tmpPainter = refStructureManager.getControlPointPainter();
		tmpPainter.setControlPoints(newPointL);

		refStructureManager.notifyModelChanged();
	}

}
