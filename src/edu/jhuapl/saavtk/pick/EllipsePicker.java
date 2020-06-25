package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class EllipsePicker extends Picker
{
	// Reference vars
	private final PolyhedralModel refSmallBody;
	private final EllipseModel refStructureManager;
	private final vtkJoglPanelComponent refRenWin;

	// VTK vars
	private final vtkCellPicker vSmallBodyCP;
	private final vtkCellPicker vStructureCP;

	// State vars
	private EditMode currEditMode;
	private int currVertexId;
	private boolean changeAngleKeyPressed;
	private boolean changeFlatteningKeyPressed;

	public EllipsePicker(Renderer aRenderer, PolyhedralModel aSmallBody, StructureManager<?> aStructureManager)
	{
		refSmallBody = aSmallBody;
		refStructureManager = (EllipseModel) aStructureManager;
		refRenWin = aRenderer.getRenderWindowPanel();

		vSmallBodyCP = PickUtilEx.formSmallBodyPicker(refSmallBody);
		vStructureCP = PickUtilEx.formPickerFor(refStructureManager.getBoundaryActor());

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
			refStructureManager.resetCircumferencePoints();
			return;
		}

		// Bail if a valid point was not picked
		boolean isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		vtkActor pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Handle the action
		double[] posArr = vSmallBodyCP.GetPickPosition();

		// TODO: Is this conditional really necessary?
		if (aEvent.getClickCount() == 1)
		{
//			refStructureModel.addNewStructure(pos);
			if (refStructureManager.addCircumferencePoint(posArr) == false)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(refRenWin.getComponent()),
						"Could not fit ellipse to specified points.", "Error", JOptionPane.ERROR_MESSAGE);
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
		boolean isPicked = PickUtil.isPicked(vStructureCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Determine what was picked
		vtkActor pickedActor = vStructureCP.GetActor();
		if (pickedActor == refStructureManager.getBoundaryActor())
		{
			int cellId = vStructureCP.GetCellId();
			int pointId = refStructureManager.getPolygonIdFromBoundaryCellId(cellId);
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
		boolean isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		vtkActor pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Handle the action
		double[] lastDragPositionArr = vSmallBodyCP.GetPickPosition();
		Vector3D lastDragPosition = new Vector3D(lastDragPositionArr);

		if (aEvent.isControlDown() || aEvent.isShiftDown())
			EllipseUtil.changeRadius(refStructureManager, tmpItem, refSmallBody, lastDragPosition);
		else if (changeFlatteningKeyPressed)
			doMouseChangeFlattening(tmpItem, lastDragPosition);
		else if (changeAngleKeyPressed)
			doMouseChangeAngle(tmpItem, lastDragPosition);
		else
			refStructureManager.setCenter(tmpItem, lastDragPosition);
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		boolean isPicked = PickUtil.isPicked(vStructureCP, refRenWin, aEvent, getTolerance());
		int numActivePoints = refStructureManager.getNumberOfCircumferencePoints();

		// Only allow dragging if we are not in the middle of drawing a
		// new ellipse, i.e. if number of circumference points is zero.
		if (numActivePoints == 0 && isPicked == true && vStructureCP.GetActor() == refStructureManager.getBoundaryActor())
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
			Set<Ellipse> pickS = refStructureManager.getSelectedItems();
			refStructureManager.removeItems(pickS);
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

	/**
	 * Helper method that will handle the mouse action: change angle
	 */
	private void doMouseChangeAngle(Ellipse aItem, Vector3D aDragPosition)
	{
		// Update the item's angle
		double tmpAngle = EllipseUtil.computeAngle(refSmallBody, aItem.getCenter(), aDragPosition);
		if (tmpAngle < 0)
			tmpAngle += 180;
		aItem.setAngle(tmpAngle);

		// Send out notification of the mutation
		List<Ellipse> tmpItemL = ImmutableList.of(aItem);
		refStructureManager.notifyItemsMutated(tmpItemL);
	}

	/**
	 * Helper method that will handle the mouse action: change flattening
	 */
	private void doMouseChangeFlattening(Ellipse aItem, Vector3D aDragPosition)
	{
		// Update the item's angle
		double tmpFlattening = EllipseUtil.computeFlattening(refSmallBody, aItem.getCenter(), aItem.getRadius(),
				aItem.getAngle(), aDragPosition);
		aItem.setFlattening(tmpFlattening);

		// Send out notification of the mutation
		List<Ellipse> tmpItemL = ImmutableList.of(aItem);
		refStructureManager.notifyItemsMutated(tmpItemL);
	}

}
