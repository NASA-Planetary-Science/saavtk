package edu.jhuapl.saavtk.structure;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.PickUtilEx;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.structure.gui.StructureGuiUtil;
import edu.jhuapl.saavtk.structure.gui.misc.SpawnAttr;
import edu.jhuapl.saavtk.structure.gui.misc.SpawnInfo;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.util.ControlPointUtil;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkControlPointPainter;
import edu.jhuapl.saavtk.structure.vtk.VtkStructureMultiPainter;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import glum.gui.panel.generic.MessagePanel;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Picker used to create, delete, modify or select round edge (circles, ellipses, points) structures.
 * <p>
 * The following structure mutations are supported:
 * <ul>
 * <li>Ability to delete selected structures
 * <li>Ability to move selected structures
 * <li>Ability to adjust radius of relevant selected structures
 * <li>Ability to change flattening of relevant selected structures
 * <li>Ability to adjust angle of relevant selected structures
 * <li>Ability to change control points of a single selected structure
 * </ul>
 * Note that structures that do not define a specific attribute will not be mutated.
 *
 * @author lopeznr1
 */
public class AnyStructurePicker extends Picker
{
	// Reference vars
	private final Renderer refRenderer;
	private final PolyhedralModel refSmallBody;
	private final PickManager refPickManager;
	private final AnyStructureManager refManager;
	private final vtkJoglPanelComponent refRenWin;

	// VTK vars
	private final ImmutableList<VtkStructureMultiPainter> vMainPainterL;
	private final VtkStructureMultiPainter vEditPainter;
	private final VtkControlPointPainter vControlPointPainter;
	private final vtkCellPicker vControlPointCP;
	private final vtkCellPicker vSmallBodyCP;
	private final vtkCellPicker vStructureCP;

	// State vars
	private ImmutableList<PickListener> pickListenerL;
	private Map<Structure, Structure> origItemM;
	private Set<Structure> editItemS;
	private Structure priEditItem;
	private boolean isKeyChangeAnglePressed;
	private boolean isKeyChangeFlatteningPressed;
	private boolean isKeyCtrlPressed;
	private boolean isMousePickedPoint;
	private boolean isMousePickedStruct;

	private SpawnAttr spawnAttr;
	private StructureType spawnType;

	private Structure hookItem;
	private boolean isDragControlPoint;
	private Mode workMode;

	/** Standard Constructor */
	public AnyStructurePicker(Renderer aRenderer, PolyhedralModel aSmallBody, PickManager aPickManager,
			AnyStructureManager aManager)
	{
		refRenderer = aRenderer;
		refSmallBody = aSmallBody;
		refPickManager = aPickManager;
		refManager = aManager;
		refRenWin = aRenderer.getRenderWindowPanel();

		vMainPainterL = refManager.getAllPainters();
		vEditPainter = formEditPainter(refRenderer, aSmallBody, refManager.getRenderAttr());
		vControlPointPainter = new VtkControlPointPainter();
		vControlPointPainter.setColorDraw(Color.BLUE);
		vControlPointCP = PickUtilEx.formPickerFor(vControlPointPainter.getActor());
		vSmallBodyCP = PickUtilEx.formSmallBodyPicker(refSmallBody);
		vStructureCP = PickUtilEx.formEmptyPicker();

		var fullHookL = new ArrayList<vtkActor>();
		for (var aPainter : vMainPainterL)
			fullHookL.addAll(aPainter.getHookActors());
		PickUtilEx.updatePickerProps(vStructureCP, fullHookL);

		refRenderer.addVtkPropProvider(vControlPointPainter);

		pickListenerL = ImmutableList.of();
		origItemM = new HashMap<>();
		editItemS = ImmutableSet.of();
		priEditItem = null;
		isKeyChangeAnglePressed = false;
		isKeyChangeFlatteningPressed = false;
		isKeyCtrlPressed = false;
		isMousePickedPoint = false;
		isMousePickedStruct = false;

		spawnAttr = SpawnAttr.Default;
		spawnType = StructureType.Point;

		hookItem = null;
		isDragControlPoint = false;
		workMode = Mode.None;
	}

	/**
	 * Registers a {@link PickListener} with this PickManager
	 */
	public synchronized void addListener(PickListener aListener)
	{
		var tmpListenerL = new ArrayList<>(pickListenerL);
		tmpListenerL.add(aListener);

		pickListenerL = ImmutableList.copyOf(tmpListenerL);
	}

	/**
	 * Deregisters a {@link PickListener} with this PickManager
	 */
	public synchronized void delListener(PickListener aListener)
	{
		var tmpListenerL = new ArrayList<>(pickListenerL);
		tmpListenerL.remove(aListener);

		pickListenerL = ImmutableList.copyOf(tmpListenerL);
	}

	/**
	 * Returns the hook item.
	 */
	public Structure getHookItem()
	{
		return hookItem;
	}

	/**
	 * Returns the configured {@link SpawnAttr}.
	 */
	public SpawnAttr getSpawnAttr()
	{
		return spawnAttr;
	}

	/**
	 * Returns the type of structure that will be created (when a sufficient number of control points have been
	 * specified).
	 */
	public StructureType getSpawnType()
	{
		return spawnType;
	}

	/**
	 * Returns the current step number.
	 * <p>
	 * The "step number" is defined as the integer corresponding to a set of steps needed for the creation of a
	 * structure.
	 * <p>
	 * The returned value if effectively the number of control points + 1.
	 */
	public int getStepNumber()
	{
		var controlPointL = vControlPointPainter.getPoints();
		return controlPointL.size() + 1;
	}

	/**
	 * Returns the {@link Mode} of the picker.
	 */
	public Mode getWorkMode()
	{
		return workMode;
	}

	/**
	 * Sets the specified {@link Structure} as the hookItem.
	 */
	public void setHookItem(Structure aItem)
	{
		updateHookItem(aItem, false);
		updateState();
	}

	/**
	 * Sets the configured {@link SpawnAttr}.
	 */
	public void setSpawnAttr(SpawnAttr aSpawnAttr)
	{
		spawnAttr = aSpawnAttr;

		if (hookItem == null)
			return;

		hookItem.setColor(spawnAttr.color());
		if (hookItem instanceof ClosedShape aClosedShape)
			aClosedShape.setShowInterior(aSpawnAttr.isIntShown());

		// Send out the update notifications
		refManager.notifyItemsMutated(ImmutableList.of(hookItem));
		notifyListeners(null);
	}

	/**
	 * Sets the type of {@link Structure} to be created.
	 */
	public void setSpawnType(StructureType aType)
	{
		spawnType = aType;

		hookItem = null;
		vControlPointPainter.delPointsAll();

		refRenderer.notifySceneChange();
		notifyListeners(null);
	}

	/**
	 * Sets the {@link Mode} of the picker.
	 */
	public void setWorkMode(Mode aMode)
	{
		// Clear the hook item whenever we switch to creating a structure
		if (aMode != workMode && aMode == Mode.Create)
			hookItem = null;

		workMode = aMode;

		// Keep the controlPoints synchronized to workMode
		updateHookItem(hookItem, true);
		updateState();
	}

	@Override
	public int getCursorType()
	{
		if (allowAddControlPoint() == true)
			return Cursor.CROSSHAIR_CURSOR;

		if (allowMouseDragAction() == true)
			return Cursor.HAND_CURSOR;

		return Cursor.DEFAULT_CURSOR;
	}

	@Override
	public boolean isExclusiveMode()
	{
		if (isDragControlPoint == true)
			return true;

		if (priEditItem != null)
			return true;

		return false;
	}

	@Override
	public void keyPressed(KeyEvent aEvent)
	{
		var keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_SLASH)
			isKeyChangeFlatteningPressed = true;
		if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_PERIOD)
			isKeyChangeAnglePressed = true;
		if (keyCode == KeyEvent.VK_CONTROL)
			isKeyCtrlPressed = true;
		if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
			doActionDelete();

		updateState();
		notifyListeners(aEvent);
	}

	@Override
	public void keyReleased(KeyEvent aEvent)
	{
		var keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_SLASH)
			isKeyChangeFlatteningPressed = false;
		if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_PERIOD)
			isKeyChangeAnglePressed = false;
		if (keyCode == KeyEvent.VK_CONTROL)
			isKeyCtrlPressed = false;

		updateState();
		notifyListeners(aEvent);
	}

	@Override
	public void mouseClicked(MouseEvent aEvent)
	{
		// Bail if ctrl is pressed
		if (isKeyCtrlPressed == true)
		{
			// Force the StatusNotifier to display the selection info
			refManager.setSelectedItems(refManager.getSelectedItems());
			updateState();
			return;
		}

		// Clear the hookItem if mouse button 1 is not pressed
		if (aEvent.getButton() != MouseEvent.BUTTON1)
		{
			updateHookItem(null, true);
			updateState();
			return;
		}

		// Allow an item to be selected and hooked if no control points
		if (workMode == Mode.Edit && vControlPointPainter.getPoints().size() == 0)
		{
			var isPicked = PickUtil.isPicked(vStructureCP, refRenWin, aEvent, getTolerance());
			if (isPicked == true)
			{
				var pickItem = getPickedStructure();
				var pickItemS = ImmutableSet.of(pickItem);
				refManager.setSelectedItems(pickItemS);

				updateHookItem(pickItem, true);
				updateState();
				return;
			}
		}

		// Bail if we are not allowed to add control points
		if (allowAddControlPoint() != true)
			return;

		// Bail if a valid point was not picked
		var isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		var pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Handle the action
		// TODO: Is this conditional really necessary?
		if (aEvent.getClickCount() == 1)
		{
			try
			{
				var tmpPt = new Vector3D(vSmallBodyCP.GetPickPosition());
				addControlPoint(tmpPt);
			}
			catch (Error aError)
			{
				var tmpPanel = new MessagePanel(refRenderer, "Failed to add control point", 300, 160);
				tmpPanel.setInfo(aError.getMessage());
				tmpPanel.setVisibleAsModal();
			}

			// Clear out the control points (if this is the final step)
			var spawnInfo = SpawnInfo.of(spawnType);
			var isDone = false;
			isDone |= aEvent.isControlDown() == true;
			isDone |= vControlPointPainter.getPoints().size() == spawnInfo.minSteps() && spawnInfo.hasMaxSteps() == true;
			if (isDone == true)
			{
				if (hookItem != null)
					refManager.setSelectedItems(ImmutableList.of(hookItem));

				vControlPointPainter.delPointsAll();
				hookItem = null;
			}
		}

		updateState();
		notifyListeners(aEvent);
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Bail if we are not allowed to do a drag operation
		if (allowMouseDragAction() != true)
			return;

		// Bail if we failed to pick something
		var isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Handle movement of the control points
		if (isDragControlPoint == true)
		{
			// Bail if the picked actor is not associated with refSmallBody
			var pickedActor = vSmallBodyCP.GetActor();
			if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
				return;

			// Update the control point
			var lastDragPosition = new Vector3D(vSmallBodyCP.GetPickPosition());
			var tmpIdx = vControlPointPainter.getHookIdx();
			vControlPointPainter.movePoint(tmpIdx, lastDragPosition);

			// Update the hookItem
			var controlPointL = vControlPointPainter.getPoints();
			if (hookItem != null)
				setControlPoints(hookItem, controlPointL);

			refRenderer.notifySceneChange();
			return;
		}

		// Bail if there is no item being edited
		if (priEditItem == null)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		var pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Adjust our painters
		var tmpColor = new Color(0, 0, 255);
		if (editItemS != refManager.getSelectedItems())
			tmpColor = null;
		vEditPainter.setDrawColor(tmpColor);
		for (var aPainter : vMainPainterL)
		{
			var fullItemS = aPainter.getWorkItems();
			var tmpItemS = Sets.intersection(fullItemS, editItemS);
			aPainter.setDimmedItems(tmpItemS);
		}

		// Delegate the actual mutation of the target items
		var lastDragPosition = new Vector3D(vSmallBodyCP.GetPickPosition());
		if (aEvent.isControlDown() || aEvent.isShiftDown())
			doChangeRadius(lastDragPosition);
		else if (isKeyChangeFlatteningPressed)
			doChangeFlattening(lastDragPosition);
		else if (isKeyChangeAnglePressed)
			doChangeAngle(lastDragPosition);
		else
			doChangePosition(lastDragPosition);

		// Send out the update notifications
		vEditPainter.updatePolyData();
		refRenderer.notifySceneChange();
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		// Determine if a control point is picked
		isMousePickedPoint = PickUtil.isPicked(vControlPointCP, refRenWin, aEvent, getTolerance());

		// Determine if a structure is picked
		isMousePickedStruct = PickUtil.isPicked(vStructureCP, refRenWin, aEvent, getTolerance());

		GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
		updateState();
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// Assume nothing will be picked
		priEditItem = null;
		isDragControlPoint = false;

		// Bail if we are not allowed to do a drag operation
		if (allowMouseDragAction() != true)
			return;

		// Bail if mouse button 1 is not pressed
		if (aEvent.getButton() != MouseEvent.BUTTON1)
			return;

		// Control points selection logic
		if (PickUtil.isPicked(vControlPointCP, refRenWin, aEvent, getTolerance()) == true)
		{
			// Button1 must be depressed
			if (aEvent.getButton() != MouseEvent.BUTTON1)
				return;

			// Update the hooked control point
			var hookControlPointIdx = (int) vControlPointCP.GetCellId();
			vControlPointPainter.setHookIdx(hookControlPointIdx);

			isDragControlPoint = true;
			return;
		}

		// Bail if we failed to pick something
		var isPicked = PickUtil.isPicked(vStructureCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Delegate the logic for starting an edit action
		doActionEditBeg();
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		// Notify the StrutureManager of the mutated items and restore the main painter
		if (priEditItem != null)
		{
			refManager.notifyItemsMutated(editItemS);
			for (var aPainter : vMainPainterL)
				aPainter.setDimmedItems(ImmutableList.of());
		}

		// Clear out the edit state
		priEditItem = null;
		editItemS = ImmutableSet.of();
		isDragControlPoint = false;

		// Reset the edit painter and remove it from the VTK scene
		vEditPainter.setWorkItems(ImmutableList.of());
		refRenderer.delVtkPropProvider(vEditPainter);

		updateState();
		notifyListeners(aEvent);
	}

	/**
	 * Helper method that adds the control point and if logical instantiates a new structure.
	 * <p>
	 * Any failure will result in an {@link Error} being thrown.
	 */
	private void addControlPoint(Vector3D aPoint)
	{
		// Determine the StructureType corresponding to the specified Structures.
		// Typically we just utilize the spawnType, unless there is a hookItem.
		var targType = spawnType;
		if (hookItem != null)
			targType = hookItem.getType();

		// Add the point to our ControlPointPainter. We add the point
		// at the position of the hookIdx unless there is no maxPoints.
		var hookIdx = vControlPointPainter.getHookIdx();
		var spawnInfo = SpawnInfo.of(targType);
		if (spawnInfo.hasMaxSteps() == true)
			hookIdx = vControlPointPainter.getPoints().size() - 1;
		vControlPointPainter.addPoint(hookIdx, aPoint);

		// Bail if there are insufficient number of control points
		var pointL = vControlPointPainter.getPoints();
		if (pointL.size() < spawnInfo.minSteps())
		{
			var radialOffset = refManager.getRenderAttr().radialOffset();
			vControlPointPainter.shiftRadialOffset(refSmallBody, radialOffset);
			refRenderer.notifySceneChange();
			hookItem = null;
			return;
		}

		// Clear out all points if no further control points can be added
		if (spawnInfo.hasMaxSteps() == true)
		{
			vControlPointPainter.delPointsAll();
			refRenderer.notifySceneChange();
		}

		// Instantiate a Structure if the minSteps has (just) been satisfied
		if (spawnInfo.minSteps() == pointL.size())
		{
			int tmpId = StructureMiscUtil.calcNextId(refManager);
			var tmpItem = StructureMiscUtil.createStructureFor(refSmallBody, spawnType, tmpId, pointL, spawnAttr);

			refManager.addItem(tmpItem);
			hookItem = tmpItem;
			return;
		}

		setControlPoints(hookItem, pointL);
	}

	/**
	 * Helper method to adjust all of the structure centers.
	 */
	private void adjustCenterOf(RealMatrix aRotMat, Vector3D aPickCenter)
	{
		// Update the centers of the target items
		for (var aItem : editItemS)
		{
			// Transform to the new center via the rotation matrix
			var origItem = origItemM.get(aItem);
			var origCenter = origItem.getRenderState().centerPt();
			var newCenter = new Vector3D(aRotMat.operate(origCenter.toArray()));

			// Adjust the center to the intercept of the small body
			var adjCenter = (Vector3D) null;
			var scaleFact = 1.0;
			var boundBoxDiag = refSmallBody.getBoundingBoxDiagonalLength();
			while (scaleFact < 4096)
			{
				var begPos = newCenter.scalarMultiply(0.01);
				var endPos = newCenter.scalarMultiply(boundBoxDiag * scaleFact);
				adjCenter = refSmallBody.calcInterceptBetween(begPos, endPos);
				if (adjCenter != null)
					break;

				scaleFact *= 2;
			}
			if (adjCenter == null)
				throw new Error("Failed to locate intercept... scaleFact:" + scaleFact);

			// Keep the priEditItem at the (selected) pick center
			newCenter = adjCenter;
			if (aItem == priEditItem)
				newCenter = aPickCenter;

			StructureMiscUtil.setCenter(refSmallBody, aItem, newCenter);
			markPainterStale(aItem);
		}

		// Get the updated and the original center of the priEditItem
		var priItemCenterNew = priEditItem.getRenderState().centerPt();

		var priEditItemOrig = origItemM.get(priEditItem);
		var priItemCenterOrig = priEditItemOrig.getRenderState().centerPt();

		// Correct the distance from the primary
		for (var aItem : editItemS)
		{
			// Do not adjust the primary item
			if (aItem == priEditItem)
				continue;

			// Compute the distance of the aItem's (original location) from the priEditItem's (original location)
			var origItem = origItemM.get(aItem);
			var origItemCenter = origItem.getRenderState().centerPt();
			var origDist = priItemCenterOrig.distance(origItemCenter);

			// Compute the vector from the center of the priEditItem to aItem
			var newItemCenter = aItem.getRenderState().centerPt();
			var dirVect = priItemCenterNew.subtract(newItemCenter).negate();

			// Compute the distance as currently configured
			var evalDist = priItemCenterNew.distance(newItemCenter);
			var scaleFact = origDist / evalDist;

			// Adjust the center so that the distance between the priEditItem and aItem is the same as the original
			dirVect = dirVect.scalarMultiply(scaleFact);
			var adjCenter = priItemCenterNew.add(dirVect);

			var adjCenterArr = refSmallBody.findClosestPoint(adjCenter.toArray());
			adjCenter = new Vector3D(adjCenterArr);
			StructureMiscUtil.setCenter(refSmallBody, aItem, adjCenter);
		}
	}

	/**
	 * Helper method that returns true if a control point can be placed.
	 */
	private boolean allowAddControlPoint()
	{
		// Never allow control points to be added when a control point is picked
		if (isMousePickedPoint == true)
			return false;

		// Never allow control points to be added when ctrl is pressed
		if (isKeyCtrlPressed == true)
			return false;

		// If the workMode is create, then allow control points
		if (workMode == Mode.Create)
			return true;

		// If the workMode == Edit, then allow if all of the below is true:
		// - hookItem's SpawnInfo allows for further control points
		// - there are already control points displayed
		var numControlPts = vControlPointPainter.getPoints().size();
		if (workMode == Mode.Edit && hookItem != null && SpawnInfo.of(hookItem).hasMaxSteps() == false
				&& numControlPts > 0)
			return true;

		return false;
	}

	/**
	 * Helper method that returns true if the mouse can perform a drag action.
	 */
	private boolean allowMouseDragAction()
	{
		if (isMousePickedPoint == true)
			return true;

		var numControlPts = vControlPointPainter.getPoints().size();
		if (workMode == Mode.Create && numControlPts == 0 && isKeyCtrlPressed == false)
			return false;

		if (isMousePickedStruct == true && numControlPts == 0)
			return true;

		return false;
	}

	/**
	 * Helper method to handle: the delete action.
	 */
	private void doActionDelete()
	{
		var tmpIdx = vControlPointPainter.getHookIdx();
		if (tmpIdx < 0)
			tmpIdx = vControlPointPainter.getPoints().size() - 1;
		if (tmpIdx >= 0)
		{
			// Determine if we should prompt the user for confirmation of deletion. This is needed
			// if removal of the control point would result in removal of the hookItem
			var isPromptNeeded = false;
			var pointL = vControlPointPainter.getPoints();
			if (hookItem != null && SpawnInfo.of(hookItem).minSteps() == pointL.size())
				isPromptNeeded = true;

			// Prompt the user user for confirmation
			if (isPromptNeeded == true)
			{
				// Bail if structure is not deleted
				var pickS = ImmutableSet.of(hookItem);
				if (StructureGuiUtil.promptAndDelete(refRenderer, refManager, pickS) == false)
					return;

				hookItem = null;
				vControlPointPainter.delPointsAll();
				refRenderer.notifySceneChange();
				return;
			}

			// Remove the control point
			vControlPointPainter.delPoint(tmpIdx);
			refRenderer.notifySceneChange();

			// Update the hookItem
			pointL = vControlPointPainter.getPoints();
			if (hookItem != null)
				setControlPoints(hookItem, pointL);
		}
		else
		{
			var pickS = refManager.getSelectedItems();
			StructureGuiUtil.promptAndDelete(refRenderer, refManager, pickS);
		}
	}

	/**
	 * Helper method that will set up internal state at the start of editing structures.
	 */
	private void doActionEditBeg()
	{
		// Add our edit painter to the VTK scene
		refRenderer.addVtkPropProvider(vEditPainter);

		// Retrieve the (primary) item that was picked
		priEditItem = getPickedStructure();

		// Determine the list of items to modify. The following logic is used:
		// - Mutate all selected items if the target item is part of the selected items
		// - otherwise just mutate the selected item
		editItemS = refManager.getSelectedItems();
		if (editItemS.contains(priEditItem) == false)
			editItemS = ImmutableSet.of(priEditItem);

		// Capture the initial state of structures at the start of the pick action
		origItemM.clear();
		var selectedS = refManager.getSelectedItems();
		for (var aItem : selectedS)
		{
			var origItem = StructureMiscUtil.cloneItem(aItem);
			origItemM.put(aItem, origItem);
		}

		if (priEditItem != null)
		{
			if (selectedS.contains(priEditItem) == false)
			{
				var origItem = StructureMiscUtil.cloneItem(priEditItem);
				origItemM.put(priEditItem, origItem);
			}
		}

		vEditPainter.setWorkItems(editItemS);
	}

	/**
	 * Helper method that will handle the mouse action: change angle
	 */
	private void doChangeAngle(Vector3D aDragPosition)
	{
		// Bail if the priEditItem is not an ellipse
		if (priEditItem.getType() != StructureType.Ellipse)
			return;
		var priEditItem2 = (Ellipse) origItemM.get(priEditItem);

		// Compute the deltaAngle
		var oldAngle = priEditItem2.getAngle();
		var newAngle = EllipseUtil.computeAngle(refSmallBody, priEditItem2.getCenter(), aDragPosition);
		var deltaAngle = newAngle - oldAngle;

		// Update the angle of all (relevant) items
		for (var aItem : editItemS)
		{
			// Only an ellipse has a defined attribute of angle
			if (aItem.getType() != StructureType.Ellipse)
				continue;

			var origItem = (Ellipse) origItemM.get(aItem);
			var tmpOldAngle = origItem.getAngle();
			var tmpNewAngle = tmpOldAngle + deltaAngle;

			var tmpItem = (Ellipse) aItem;
			tmpItem.setAngle(tmpNewAngle);
			markPainterStale(tmpItem);
		}
	}

	/**
	 * Helper method that will handle the mouse action: change flattening
	 */
	private void doChangeFlattening(Vector3D aDragPosition)
	{
		// Bail if the priEditItem is not an ellipse
		if (priEditItem.getType() != StructureType.Ellipse)
			return;
		var priEditItem2 = (Ellipse) origItemM.get(priEditItem);

		// Compute the deltaFlattening
		var oldFlattening = priEditItem2.getFlattening();
		var newFlattening = EllipseUtil.computeFlattening(refSmallBody, priEditItem2.getCenter(),
				priEditItem2.getRadius(), priEditItem2.getAngle(), aDragPosition);
		var deltaFlattening = newFlattening - oldFlattening;

		// Update the flattening of all (relevant) items
		for (var aItem : editItemS)
		{
			// Only an ellipse has a defined attribute of flattening
			if (aItem.getType() != StructureType.Ellipse)
				continue;

			var origItem = (Ellipse) origItemM.get(aItem);
			var tmpOldFlattening = origItem.getFlattening();
			var tmpNewFlattening = tmpOldFlattening + deltaFlattening;
			if (tmpNewFlattening < 0)
				tmpNewFlattening = 1.0 - tmpNewFlattening;
			else if (tmpNewFlattening > 1.0)
				tmpNewFlattening = 0 + tmpNewFlattening;

			var tmpItem = (Ellipse) aItem;
			tmpItem.setFlattening(tmpNewFlattening);
			markPainterStale(tmpItem);
		}
	}

	/**
	 * Helper method that will handle the mouse action: change position
	 */
	private void doChangePosition(Vector3D aDragPosition)
	{
		// Move all objects to their original position
		for (var aItem : editItemS)
		{
			if (aItem instanceof Point aPoint)
			{
				var origItem = (Point) origItemM.get(aPoint);
				aPoint.setCenter(origItem.getCenter());
				aPoint.setRenderState(origItem.getRenderState());
			}
			else if (aItem instanceof Ellipse aEllipse)
			{
				var origItem = (Ellipse) origItemM.get(aEllipse);
				aEllipse.setCenter(origItem.getCenter());
				aEllipse.setRenderState(origItem.getRenderState());
			}
			else if (aItem instanceof PolyLine aPolyLine)
			{
				var origItem = (PolyLine) origItemM.get(aPolyLine);
				aPolyLine.setControlPoints(origItem.getControlPoints());
				aPolyLine.setRenderState(origItem.getRenderState());
			}
			else
			{
				throw new Error("Unsupported type: " + aItem.getClass());
			}
		}

		// Get the rotation matrix between the priEditItem's original position and it's new position
		var priEditItemOrig = origItemM.get(priEditItem);
		var p0 = priEditItemOrig.getRenderState().centerPt();
		var p1 = aDragPosition;
		var rotaMat = calculateRotationMatrixBetweenPoints(p0, p1);

		// Adjust all of the selected items
		adjustCenterOf(rotaMat, aDragPosition);
	}

	/**
	 * Helper method that will handle the mouse action: change radius
	 */
	private void doChangeRadius(Vector3D aPointOnEdge)
	{
		// Bail if the priEditItem is not a circle or an ellipse
		if (priEditItem.getType() != StructureType.Circle && priEditItem.getType() != StructureType.Ellipse)
			return;
		var priEditItem2 = (Ellipse) origItemM.get(priEditItem);

		// Compute the deltaRadius
		var oldRadius = priEditItem2.getRadius();
		var newRadius = EllipseUtil.computeRadius(refSmallBody, priEditItem2.getCenter(), aPointOnEdge);
		var deltaRadius = newRadius - oldRadius;

		// Update the radius of all (relevant) items
		for (var aItem : editItemS)
		{
			// Only a circle or an ellipse has a defined attribute of radius
			if (aItem.getType() != StructureType.Circle && aItem.getType() != StructureType.Ellipse)
				continue;

			var origItem = (Ellipse) origItemM.get(aItem);
			var tmpOldRadius = origItem.getRadius();
			var tmpNewRadius = tmpOldRadius + deltaRadius;

			var tmpItem = (Ellipse) aItem;
			tmpItem.setRadius(tmpNewRadius);
			markPainterStale(tmpItem);
		}
	}

	/**
	 * Helper method that returns the picked structure.
	 * <p>
	 * This method should only be called if vStrcutureCP has been picked.
	 * <p>
	 * Throws an {@link Error} if no item was picked.
	 */
	private Structure getPickedStructure()
	{
		// Determine the item that was picked
		int cellId = (int) vStructureCP.GetCellId();
		var pickedActor = vStructureCP.GetActor();

		var retItem = (Structure) null;
		for (var aPainter : vMainPainterL)
		{
			retItem = aPainter.getItemFromCellId(pickedActor, cellId);
			if (retItem != null)
				return retItem;
		}

		throw new Error("Failed to select a structure.");
	}

	/**
	 * Helper method to mark the painter(s) associated with the specified item as stale.
	 */
	private void markPainterStale(Structure aItem)
	{
		var tmpPainter = vEditPainter.getVtkCompPainter(aItem);
		if (tmpPainter == null)
			return;

		tmpPainter.getMainPainter().vtkMarkStale();
		tmpPainter.getTextPainter().vtkMarkStale();
	}

	/**
	 * Helper method to send out notification to the listeners.
	 */
	private void notifyListeners(InputEvent aEvent)
	{
		// Bail if we have never been rendered
		if (refRenWin.getRenderWindow().GetNeverRendered() > 0)
			return;

		for (var aListener : pickListenerL)
			aListener.handlePickAction(aEvent, PickMode.Passive, PickTarget.Invalid, PickTarget.Invalid);
	}

	/**
	 * Helper method to modify the specified {@link Structure}'s control points.
	 */
	private void setControlPoints(Structure aItem, List<Vector3D> aPointL)
	{
		var tmpLatLonL = ControlPointUtil.convertToLatLonList(aPointL);

		if (aItem instanceof PolyLine aPolyLine)
		{
			aPolyLine.setControlPoints(tmpLatLonL);
			refManager.notifyItemsMutated(ImmutableList.of(aPolyLine));
		}
		else if (aItem instanceof Polygon aPolygon)
		{
			aPolygon.setControlPoints(tmpLatLonL);
			refManager.notifyItemsMutated(ImmutableList.of(aPolygon));
		}
		else
		{
			throw new Error("Logic Error: No control point support for: " + aItem.getClass());
		}
	}

	/**
	 * Helper method to update the hookItem.
	 */
	private void updateHookItem(Structure aItem, boolean aForceUpdate)
	{
		// Bail if nothing has changed
		if (hookItem == aItem && aForceUpdate == false)
			return;

		// Set the hookItem only if the corresponding SpawnInfo does
		// not have a max num steps
		hookItem = aItem;
		if (hookItem != null)
		{
			var tmpSpawnInfo = SpawnInfo.of(hookItem);
			if (tmpSpawnInfo.hasMaxSteps() == true)
				hookItem = null;
		}

		if (hookItem == null || workMode == Mode.None)
		{
			vControlPointPainter.delPointsAll();
		}
		else
		{
			var tmpPointL = hookItem.getRenderState().controlPointL();
			vControlPointPainter.setPoints(tmpPointL);
			vControlPointPainter.setHookIdx(tmpPointL.size() - 1);
		}

		refRenderer.notifySceneChange();
		notifyListeners(null);
	}

	/**
	 * Helper method to update various auxiliary state.
	 */
	private void updateState()
	{
		// Allow the refManager to handle pick actions if the ctrl key is pressed
		var isAllowPickAction = isKeyCtrlPressed == true;
		isAllowPickAction |= refPickManager.getActivePicker() != this;
		refManager.setAllowPickAction(isAllowPickAction);

		GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
	}

	/**
	 * Utility helper method to calculate a rotation matrix between 2 points.
	 * <p>
	 * Source of math equations:
	 * https://math.stackexchange.com/questions/114107/determine-the-rotation-necessary-to-transform-one-point-on-a-sphere-to-another
	 */
	private static RealMatrix calculateRotationMatrixBetweenPoints(Vector3D u, Vector3D v)
	{
		var uvCross = u.crossProduct(v);
		var uvNorm = uvCross.getNorm();

		var n = uvCross.scalarMultiply(1.0 / uvNorm);
		var t = n.crossProduct(u);

		var vuDot = v.dotProduct(u);
		var vtDot = v.dotProduct(t);
		var angA = Math.atan2(vtDot, vuDot);

		double[][] Tarr = { { u.getX(), t.getX(), n.getX() }, { u.getY(), t.getY(), n.getY() },
				{ u.getZ(), t.getZ(), n.getZ() } };
		var T = MatrixUtils.createRealMatrix(Tarr);

		var cosA = Math.cos(angA);
		var sinA = Math.sin(angA);
		double[][] RnArr = { { cosA, -sinA, 0 }, { sinA, cosA, 0 }, { 0, 0, 1 } };
		var Rn = MatrixUtils.createRealMatrix(RnArr);

		var invT = MatrixUtils.inverse(T);
		var rotMat = T.multiply(Rn).multiply(invT);
		return rotMat;
	}

	/**
	 * Utility helper method to for a {@link VtkStructureMultiPainter} used to display (edit) items.
	 */
	private static VtkStructureMultiPainter formEditPainter(SceneChangeNotifier aSceneChangeNotifier,
			PolyhedralModel aSmallBody, RenderAttr aRenderAttr)
	{
		var retPainter = new VtkStructureMultiPainter(aSceneChangeNotifier, aSmallBody, aRenderAttr);
		var drawColor = Color.MAGENTA;
		var interiorOpacity = 0.0;
		retPainter.setDrawColor(drawColor);
		retPainter.setInteriorOpacity(interiorOpacity);

		return retPainter;
	}

	/**
	 * Enum that defines the supported modes for the {@link AnyStructurePicker}.
	 *
	 * @author lopeznr1
	 */
	public enum Mode
	{
		/** Mode for when the picker is dormant. */
		None,

		/** Mode for when the picker can create an item. */
		Create,

		/** Mode for when the picker can edit an item. */
		Edit,
	}

}
