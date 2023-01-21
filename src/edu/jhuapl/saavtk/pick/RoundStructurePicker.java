package edu.jhuapl.saavtk.pick;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.EllipseManager;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.structure.vtk.VtkControlPointPainter;
import edu.jhuapl.saavtk.structure.vtk.VtkEllipseMultiPainter;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import vtk.vtkCellPicker;
import vtk.vtkTransform;
import vtk.vtkTriangle;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Picker used to create, delete, modify or select round edge (circles, ellipses, points) structures.
 * <p>
 * The following (multi) structure mutations are supported:
 * <ul>
 * <li>Ability to delete selected structures
 * <li>Ability to move selected structures
 * <li>Ability to adjust radius of selected structures
 * <li>Ability to change flattening of selected structures
 * <li>Ability to adjust angle of selected structures
 * </ul>
 * Note that structures that do not define a specific attribute will not be mutated.
 *
 * @author lopeznr1
 */
public class RoundStructurePicker extends Picker
{
	// Reference vars
	private final Renderer refRenderer;
	private final PolyhedralModel refSmallBody;
	private final EllipseManager refManager;
	private final vtkJoglPanelComponent refRenWin;
	private final Mode refMode;

	// VTK vars
	private final VtkEllipseMultiPainter vMainPainter;
	private final VtkEllipseMultiPainter vEditPainter;
	private final VtkControlPointPainter vControlPointPainter;
	private final vtkCellPicker vSmallBodyCP;
	private final vtkCellPicker vStructureCP;

	// State vars
	private Map<Ellipse, Ellipse> origItemM;
	private Set<Ellipse> editItemS;
	private Ellipse priEditItem;
	private EditMode currEditMode;
	private boolean changeAngleKeyPressed;
	private boolean changeFlatteningKeyPressed;

	/** Standard Constructor */
	public RoundStructurePicker(Renderer aRenderer, PolyhedralModel aSmallBody, EllipseManager aeManager, Mode aMode)
	{
		refRenderer = aRenderer;
		refSmallBody = aSmallBody;
		refManager = aeManager;
		refRenWin = aRenderer.getRenderWindowPanel();
		refMode = aMode;

		vMainPainter = refManager.getVtkMultiPainter();
		vEditPainter = formEditPainter(refRenderer, aSmallBody, refMode);
		vControlPointPainter = new VtkControlPointPainter();
		vControlPointPainter.setDrawColor(Color.BLUE);
		vSmallBodyCP = PickUtilEx.formSmallBodyPicker(refSmallBody);
		vStructureCP = PickUtilEx.formEmptyPicker();
		PickUtilEx.updatePickerProps(vStructureCP, vMainPainter.getHookActors());

		refRenderer.addVtkPropProvider(vControlPointPainter);

		origItemM = new HashMap<>();
		editItemS = ImmutableSet.of();
		priEditItem = null;
		currEditMode = EditMode.CLICKABLE;
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
		if (priEditItem != null)
			return true;

		return false;
	}

	@Override
	public void keyPressed(KeyEvent aEvent)
	{
		var keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_SLASH)
			changeFlatteningKeyPressed = true;
		if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_PERIOD)
			changeAngleKeyPressed = true;

		if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
		{
			var pickS = refManager.getSelectedItems();
			refManager.removeItems(pickS);
		}
	}

	@Override
	public void keyReleased(KeyEvent aEvent)
	{
		var keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_Z || keyCode == KeyEvent.VK_SLASH)
			changeFlatteningKeyPressed = false;
		if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_PERIOD)
			changeAngleKeyPressed = false;
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
			vControlPointPainter.setControlPoints(ImmutableList.of());
			return;
		}

		// Bail if a valid point was not picked
		var isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		var pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Handle the action
		var tmpPt = new Vector3D(vSmallBodyCP.GetPickPosition());

		// TODO: Is this conditional really necessary?
		if (aEvent.getClickCount() == 1)
		{
			if (addControlPoint(tmpPt) == false)
			{
				if (refMode != Mode.CIRCLE_MODE && refMode != Mode.ELLIPSE_MODE)
					throw new RuntimeException("Unsupported mode: " + refMode);

				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(refRenWin.getComponent()),
						"Could not fit " + refMode.getLabel().toLowerCase() + " to specified points.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// Assume nothing will be picked
		priEditItem = null;

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

		// Delegate the logic for starting an edit action
		doEditActionBeg();
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		// Notify the StrutureManager of the mutated items and restore the main painter
		if (priEditItem != null)
		{
			refManager.notifyItemsMutated(editItemS);
			vMainPainter.setDimmedItems(ImmutableList.of());
		}

		// Clear out the edit state
		priEditItem = null;
		editItemS = ImmutableSet.of();

		// Reset the edit painter and remove it from the VTK scene
		vEditPainter.setWorkItems(ImmutableList.of());
		refRenderer.delVtkPropProvider(vEditPainter);
	}

	private void doEditActionBeg()
	{
		// Add our edit painter to the VTK scene
		refRenderer.addVtkPropProvider(vEditPainter);

		// Determine the (primary) item that was picked
		int cellId = vStructureCP.GetCellId();
		var pickedActor = vStructureCP.GetActor();
		priEditItem = vMainPainter.getItemFromCellId(pickedActor, cellId);
		if (priEditItem == null)
			throw new Error("Failed to select priEditItem");

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
			var origItem = new Ellipse(aItem.getId(), aItem.getSource(), aItem.getMode(), aItem.getCenter(),
					aItem.getRadius(), aItem.getAngle(), aItem.getFlattening(), aItem.getColor(), aItem.getLabel());
			origItemM.put(aItem, origItem);
		}

		if (priEditItem != null)
		{
			if (selectedS.contains(priEditItem) == false)
			{
				var origItem = new Ellipse(priEditItem.getId(), priEditItem.getSource(), priEditItem.getMode(),
						priEditItem.getCenter(), priEditItem.getRadius(), priEditItem.getAngle(), priEditItem.getFlattening(),
						priEditItem.getColor(), priEditItem.getLabel());
				origItemM.put(priEditItem, origItem);
			}
		}

		vEditPainter.setWorkItems(editItemS);
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Bail if we are not in the proper edit mode or there is no item being edited
		if (currEditMode != EditMode.DRAGGABLE || priEditItem == null)
			return;

		// Bail if we failed to pick something
		var isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
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
		vMainPainter.setDimmedItems(editItemS);

		// Delegate the actual mutation of the target items
		var lastDragPosition = new Vector3D(vSmallBodyCP.GetPickPosition());
		if (aEvent.isControlDown() || aEvent.isShiftDown())
			doChangeRadius(lastDragPosition);
		else if (changeFlatteningKeyPressed)
			doChangeFlattening(lastDragPosition);
		else if (changeAngleKeyPressed)
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
		var isPicked = PickUtil.isPicked(vStructureCP, refRenWin, aEvent, getTolerance());
		var numControlPoints = vControlPointPainter.getControlPoints().size();

		// Only allow dragging if we are not in the middle of drawing a
		// new structure, i.e. if number of circumference points is zero.
		if (numControlPoints == 0 && isPicked == true && vMainPainter.getHookActors().contains(vStructureCP.GetActor()))
			currEditMode = EditMode.DRAGGABLE;
		else
			currEditMode = EditMode.CLICKABLE;

		GuiUtil.updateCursor(refRenWin.getComponent(), getCursorType());
	}

	/**
	 * Helper method that adds the control point and if logical instantiates a new structure.
	 * <p>
	 * Returns false if there is an issue adding the control point.
	 */
	private boolean addControlPoint(Vector3D aPoint)
	{
		// Retrieve the default color
		var defaultColor = new Color(0, 191, 255);
		if (refManager instanceof AbstractEllipsePolygonModel aManager)
			defaultColor = aManager.getDefaultColor();

		// Instantiate a "point" structure
		if (refMode == Mode.POINT_MODE)
		{
			// Retrieve the default radius
			var defaultRadius = EllipseUtil.getDefRadius(refSmallBody);
			if (refManager instanceof AbstractEllipsePolygonModel aManager)
				defaultRadius = aManager.getDefaultRadius();

			int tmpId = StructureMiscUtil.calcNextId(refManager);
			var center = aPoint;
			var angle = 0.0;
			var flattening = 1.0;
			var radius = defaultRadius;
			var retItem = new Ellipse(tmpId, null, refMode, center, radius, angle, flattening, defaultColor, "");

			refManager.addItem(retItem);
			return true;
		}

		// Round structures require 3 points
		var pointL = vControlPointPainter.getControlPoints();
		if (pointL.size() < 2)
		{
			vControlPointPainter.addPoint(aPoint);
			vControlPointPainter.shiftControlPoints(refSmallBody, refManager.getOffset());
			refRenderer.notifySceneChange();
			return true;
		}

		// Instantiate a "circle" structure
		if (refMode == Mode.CIRCLE_MODE)
		{
			// Take the 3 points and compute a circle that passes through them.
			// To do this, first form a triangle from these 3 points.
			// Compute the normal and rotate the 3 points so that its normal
			// is facing the positive z direction. Then use VTK's Circumcircle
			// function which computes the center and radius of a circle that
			// passes through these points.
			double[] pt1 = pointL.get(0).toArray();
			double[] pt2 = pointL.get(1).toArray();
			double[] pt3 = aPoint.toArray();
			double[] normal = new double[3];

			var tri = new vtkTriangle();

			// Note Circumcircle ignores z component, so first need to rotate
			// triangle so normal points in z direction.
			tri.ComputeNormal(pt1, pt2, pt3, normal);
			if (MathUtil.vnorm(normal) == 0.0)
			{
				// Remove all points if we cannot fit a circle
				vControlPointPainter.setControlPoints(ImmutableList.of());
				return false;
			}

			double[] zaxis = { 0.0, 0.0, 1.0 };
			double[] cross = new double[3];
			MathUtil.vcrss(zaxis, normal, cross);
			// Compute angle between normal and zaxis
			double sepAngle = -MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

			vtkTransform transform = new vtkTransform();
			transform.RotateWXYZ(sepAngle, cross);

			pt1 = transform.TransformDoublePoint(pt1);
			pt2 = transform.TransformDoublePoint(pt2);
			pt3 = transform.TransformDoublePoint(pt3);

			double[] centerArr = new double[3];
			double radius = Math.sqrt(tri.Circumcircle(pt1, pt2, pt3, centerArr));
			// Note Circumcircle ignores z component, so set it here.
			centerArr[2] = pt1[2];

			centerArr = transform.GetInverse().TransformDoublePoint(centerArr);
			Vector3D center = new Vector3D(centerArr);

			int tmpId = StructureMiscUtil.calcNextId(refManager);
			var angle = 0.0;
			var flattening = 1.0;
			var retItem = new Ellipse(tmpId, null, refMode, center, radius, angle, flattening, defaultColor, "");
			refManager.addItem(retItem);
		}

		// Instantiate a "ellipse" structure
		else if (refMode == Mode.ELLIPSE_MODE)
		{
			// Take the 3 points and compute an ellipse that passes through them.
			// To do this, assume that the first 2 points lie on the end-points of the major
			// axis and that the third point lies on one of the end-points of the minor axis.
			double[] pt1Arr = pointL.get(0).toArray();
			double[] pt2Arr = pointL.get(1).toArray();

			double radius = 0.5 * MathUtil.distanceBetween(pt1Arr, pt2Arr);
			if (radius == 0.0)
			{
				// Remove all points if we cannot fit an ellipse
				vControlPointPainter.setControlPoints(ImmutableList.of());
				return false;
			}

			// First find the point on the asteroid that is midway between
			// the first 2 points. This is the center of the ellipse.
			double[] centerArr = new double[3];
			MathUtil.midpointBetween(pt1Arr, pt2Arr, centerArr);
			Vector3D center = new Vector3D(centerArr);

			Vector3D pt2 = new Vector3D(pt2Arr);
			double angle = EllipseUtil.computeAngle(refSmallBody, center, pt2);

			Vector3D pt3 = aPoint;
			double flattening = EllipseUtil.computeFlattening(refSmallBody, center, radius, angle, pt3);

			int tmpId = StructureMiscUtil.calcNextId(refManager);
			var retItem = new Ellipse(tmpId, null, refMode, center, radius, angle, flattening, defaultColor, "");
			refManager.addItem(retItem);
		}

		// Clear out the control points
		vControlPointPainter.setControlPoints(ImmutableList.of());
		return true;
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
			var oldCenter = aItem.getCenter();
			var newCenter = new Vector3D(aRotMat.operate(oldCenter.toArray()));

			// Adjust the center to the intercept of the small body
			var boundBoxDiag = refSmallBody.getBoundingBoxDiagonalLength();
			var begPos = newCenter.scalarMultiply(0.01);
			var endPos = newCenter.scalarMultiply(boundBoxDiag * 2.1);
			newCenter = refSmallBody.calcInterceptBetween(begPos, endPos);
			if (newCenter == null)
				throw new Error("Failed to locate intercept...");

			// Keep the priEditItem at the (selected) pick center
			if (aItem == priEditItem)
				newCenter = aPickCenter;

			aItem.setCenter(newCenter);
			markPainterStale(aItem);
		}

		// Correct the distance from the primary
		var origPriItem = origItemM.get(priEditItem);
		for (var aItem : editItemS)
		{
			// Do not adjust the primary item
			if (aItem == priEditItem)
				continue;

			// Compute the distance of the aItem's (original location) from the priEditItem's (original location)
			var origItem = origItemM.get(aItem);
			var origDist = origPriItem.getCenter().distance(origItem.getCenter());

			// Compute the vector from the priEditItem to aItem
			var dirVect = priEditItem.getCenter().subtract(aItem.getCenter()).negate();

			// Compute the distance as currently configured
			var evalDist = priEditItem.getCenter().distance(aItem.getCenter());
			var scaleFact = origDist / evalDist;

			// Adjust the center so that the distance between the priEditItem and aItem is the same as the original
			dirVect = dirVect.scalarMultiply(scaleFact);
			var adjCenter = priEditItem.getCenter().add(dirVect);

			var adjCenterArr = refSmallBody.findClosestPoint(adjCenter.toArray());
			adjCenter = new Vector3D(adjCenterArr);
			aItem.setCenter(adjCenter);
		}
	}

	/**
	 * Helper method that will handle the mouse action: change angle
	 */
	private void doChangeAngle(Vector3D aDragPosition)
	{
		for (var aItem : editItemS)
		{
			var origItem = origItemM.get(aItem);
			aItem.setAngle(origItem.getAngle());
		}

		var oldAngle = priEditItem.getAngle();
		var newAngle = EllipseUtil.computeAngle(refSmallBody, priEditItem.getCenter(), aDragPosition);
		var deltaAngle = newAngle - oldAngle;

		// Update the angle of the target items
		for (var aItem : editItemS)
		{
			// Only an ellipse has a defined attribute of angle
			if (aItem.getMode() != Mode.ELLIPSE_MODE)
				continue;

			var tmpOldAngle = aItem.getAngle();
			var tmpNewAngle = tmpOldAngle + deltaAngle;

			aItem.setAngle(tmpNewAngle);
			markPainterStale(aItem);
		}
	}

	/**
	 * Helper method that will handle the mouse action: change flattening
	 */
	private void doChangeFlattening(Vector3D aDragPosition)
	{
		for (var aItem : editItemS)
		{
			var origItem = origItemM.get(aItem);
			aItem.setFlattening(origItem.getFlattening());
		}

		var oldFlattening = priEditItem.getFlattening();
		var newFlattening = EllipseUtil.computeFlattening(refSmallBody, priEditItem.getCenter(), priEditItem.getRadius(),
				priEditItem.getAngle(), aDragPosition);
		var deltaFlattening = newFlattening - oldFlattening;

		// Update the flattening of the target items
		for (var aItem : editItemS)
		{
			// Only an ellipse has a defined attribute of flattening
			if (aItem.getMode() != Mode.ELLIPSE_MODE)
				continue;

			var tmpOldFlattening = aItem.getFlattening();
			var tmpNewFlattening = tmpOldFlattening + deltaFlattening;
			if (tmpNewFlattening < 0)
				tmpNewFlattening = 1.0 - tmpNewFlattening;
			else if (tmpNewFlattening > 1.0)
				tmpNewFlattening = 0 + tmpNewFlattening;

			aItem.setFlattening(tmpNewFlattening);
			markPainterStale(aItem);
		}
	}

	/**
	 * Helper method that will handle the mouse action: change position
	 */
	private void doChangePosition(Vector3D aDragPosition)
	{
		for (var aItem : editItemS)
		{
			var origItem = origItemM.get(aItem);
			aItem.setCenter(origItem.getCenter());
		}

		var p0 = priEditItem.getCenter();
		var p1 = aDragPosition;
		var rotaMat = calculateRotationMatrixBetweenPoints(p0, p1);
		adjustCenterOf(rotaMat, aDragPosition);
	}

	/**
	 * Helper method that will handle the mouse action: change radius
	 */
	private void doChangeRadius(Vector3D aPointOnEdge)
	{
		for (var aItem : editItemS)
		{
			var origItem = origItemM.get(aItem);
			aItem.setRadius(origItem.getRadius());
		}

		var oldRadius = priEditItem.getRadius();
		var newRadius = EllipseUtil.computeRadius(refSmallBody, priEditItem.getCenter(), aPointOnEdge);
		var deltaRadius = newRadius - oldRadius;

		// Update the radius of the target items
		for (var aItem : editItemS)
		{
			var tmpOldRadius = aItem.getRadius();
			var tmpNewRadius = tmpOldRadius + deltaRadius;

			aItem.setRadius(tmpNewRadius);
			markPainterStale(aItem);
		}
	}

	/**
	 * Helper method to mark the painter(s) associated with the specified item as stale.
	 */
	private void markPainterStale(Ellipse aItem)
	{
		var tmpPainter = vEditPainter.getVtkCompPainter(aItem);
		if (tmpPainter == null)
			return;

		tmpPainter.getMainPainter().markStale();
		tmpPainter.getTextPainter().markStale();
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
	 * Utility helper method to for a {@link VtkEllipseMultiPainter} used to display (edit) items.
	 */
	private static VtkEllipseMultiPainter formEditPainter(SceneChangeNotifier aSceneChangeNotifier,
			PolyhedralModel aSmallBody, Mode aMode)
	{
		var interiorOpacity = 1.0;
		var tmpNumSides = 4;
		if (aMode != Mode.POINT_MODE)
		{
			interiorOpacity = 0.0;
			tmpNumSides = 20;
		}

		var retPainter = new VtkEllipseMultiPainter(aSceneChangeNotifier, aSmallBody, tmpNumSides);
		var drawColor = Color.MAGENTA;
		retPainter.setDrawColor(drawColor);
		retPainter.setInteriorOpacity(interiorOpacity);

		return retPainter;
	}

}
