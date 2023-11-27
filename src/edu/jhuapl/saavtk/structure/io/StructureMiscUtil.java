package edu.jhuapl.saavtk.structure.io;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ArrayListMultimap;

import edu.jhuapl.saavtk.model.PolyModel;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.ClosedShape;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Point;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.StructureType;
import edu.jhuapl.saavtk.structure.gui.load.InstallMode;
import edu.jhuapl.saavtk.structure.gui.misc.SpawnAttr;
import edu.jhuapl.saavtk.structure.util.ControlPointUtil;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.item.IdGenerator;
import glum.item.IncrIdGenerator;
import glum.task.Task;
import vtk.vtkTransform;
import vtk.vtkTriangle;

/**
 * Collection of utility methods for working with Structures / StructureManagers.
 * <p>
 * The functionality provided by this class can be categorized by:
 * <ul>
 * <li>Determining the ID that should be used for a new structure.
 * <li>Getting the specific structures from a list of general structures.
 * <li>Getting the status text for a specific structure.
 * </ul>
 *
 * @author lopeznr1
 */
public class StructureMiscUtil
{
	/**
	 * Utility method that returns the next id that should be used for the specified StructureManager.
	 */
	public static <G1 extends Structure> int calcNextId(StructureManager<G1> aManager)
	{
		var maxId = 0;
		for (var aItem : aManager.getAllItems())
		{
			int tmpId = aItem.getId();
			if (tmpId > maxId)
				maxId = tmpId;
		}

		return maxId + 1;
	}

	/**
	 * Utility method that will return a clone of the specified {@link Structure}.
	 *
	 * If a structure is not recognized an {@link UnsupportedOperationException} will be thrown.
	 */
	public static Structure cloneItem(Structure aItem)
	{
		// Delegate
		return cloneItemWithId(aItem, aItem.getId());
	}

	/**
	 * Utility method that will create a {@link Structure} for the specified parameters.
	 * <p>
	 * Any failure will result in an {@link Error} being thrown.
	 */
	public static Structure createStructureFor(PolyhedralModel aSmallBody, StructureType aType, int aId,
			List<Vector3D> aPointL, SpawnAttr aSpawnAttr)
	{
		// Instantiate a Point
		if (aType == StructureType.Point)
		{
			var center = aPointL.get(0);
			return new Point(aId, null, center, aSpawnAttr.color());
		}

		// Instantiate a Circle
		else if (aType == StructureType.Circle)
		{
			// Take the 3 points and compute a circle that passes through them.
			// To do this, first form a triangle from these 3 points.
			// Compute the normal and rotate the 3 points so that its normal
			// is facing the positive z direction. Then use VTK's Circumcircle
			// function which computes the center and radius of a circle that
			// passes through these points.
			var pt1 = aPointL.get(0).toArray();
			var pt2 = aPointL.get(1).toArray();
			var pt3 = aPointL.get(2).toArray();
			var normal = new double[3];

			var tri = new vtkTriangle();

			// Note Circumcircle ignores z component, so first need to rotate
			// triangle so normal points in z direction.
			tri.ComputeNormal(pt1, pt2, pt3, normal);

			// Bail if a Circle can not be created
			if (MathUtil.vnorm(normal) == 0.0)
				throw new Error("Could not fit circle to specified points!");

			double[] zaxis = { 0.0, 0.0, 1.0 };
			var cross = new double[3];
			MathUtil.vcrss(zaxis, normal, cross);
			// Compute angle between normal and zaxis
			var sepAngle = -MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

			var transform = new vtkTransform();
			transform.RotateWXYZ(sepAngle, cross);

			pt1 = transform.TransformDoublePoint(pt1);
			pt2 = transform.TransformDoublePoint(pt2);
			pt3 = transform.TransformDoublePoint(pt3);

			var centerArr = new double[3];
			var radius = Math.sqrt(tri.Circumcircle(pt1, pt2, pt3, centerArr));
			// Note Circumcircle ignores z component, so set it here.
			centerArr[2] = pt1[2];

			centerArr = transform.GetInverse().TransformDoublePoint(centerArr);
			var center = new Vector3D(centerArr);

			var angle = 0.0;
			var flattening = 1.0;
			var retItem = new Ellipse(aId, null, aType, center, radius, angle, flattening, aSpawnAttr.color());
			retItem.setShowInterior(aSpawnAttr.isIntShown());
			return retItem;
		}

		// Instantiate an Ellipse
		else if (aType == StructureType.Ellipse)
		{
			// Take the 3 points and compute an ellipse that passes through them.
			// To do this, assume that the first 2 points lie on the end-points of the major
			// axis and that the third point lies on one of the end-points of the minor axis.
			var pt1 = aPointL.get(0);
			var pt2 = aPointL.get(1);
			var pt3 = aPointL.get(2);
			var pt1Arr = pt1.toArray();
			var pt2Arr = pt2.toArray();

			var radius = 0.5 * MathUtil.distanceBetween(pt1Arr, pt2Arr);

			// Bail if an Ellipse can not be created
			if (radius == 0.0)
				throw new Error("Could not fit ellipse to specified points!");

			// First find the point on the asteroid that is midway between
			// the first 2 points. This is the center of the ellipse.
			var centerArr = new double[3];
			MathUtil.midpointBetween(pt1Arr, pt2Arr, centerArr);
			var center = new Vector3D(centerArr);

			var angle = EllipseUtil.computeAngle(aSmallBody, center, pt2);
			var flattening = EllipseUtil.computeFlattening(aSmallBody, center, radius, angle, pt3);

			var retItem = new Ellipse(aId, null, aType, center, radius, angle, flattening, aSpawnAttr.color());
			retItem.setShowInterior(aSpawnAttr.isIntShown());
			return retItem;
		}

		// Instantiate a Path
		else if (aType == StructureType.Path)
		{
			var tmpLatLonL = ControlPointUtil.convertToLatLonList(aPointL);
			var retItem = new PolyLine(aId, null, tmpLatLonL);
			retItem.setColor(aSpawnAttr.color());
			return retItem;
		}

		// Instantiate a Polygon
		else if (aType == StructureType.Polygon)
		{
			var tmpLatLonL = ControlPointUtil.convertToLatLonList(aPointL);
			var retItem = new Polygon(aId, null, tmpLatLonL);
			retItem.setColor(aSpawnAttr.color());
			retItem.setShowInterior(aSpawnAttr.isIntShown());
			return retItem;
		}

		throw new Error("No support for type: " + aType);
	}

	/**
	 * Returns a list of {@link Structures} that match the specified type.
	 * <p>
	 * If aType is null then all {@link Structure}s will be returned.
	 */
	public static List<Structure> getItemsOfType(Collection<Structure> aItemC, StructureType aType)
	{
		var retItemL = new ArrayList<Structure>();
		for (var aItem : aItemC)
		{
			if (aItem.getType() == aType || aType == null)
				retItemL.add(aItem);
		}

		return retItemL;
	}

	/**
	 * Returns the list of {@link PolyLine} structures that are not {@link Polygon}s.
	 */
	public static List<PolyLine> getPathsFrom(Collection<Structure> aItemC)
	{
		var retItemL = new ArrayList<PolyLine>();
		for (var aItem : aItemC)
		{
			if (aItem instanceof PolyLine && aItem instanceof Polygon == false)
				retItemL.add((PolyLine) aItem);
		}

		return retItemL;
	}

	/**
	 * Returns the list of {@link Polygon} structures.
	 */
	public static List<Polygon> getPolygonsFrom(Collection<Structure> aItemC)
	{
		var retItemL = new ArrayList<Polygon>();
		for (var aItem : aItemC)
		{
			if (aItem instanceof Polygon aPolygon)
				retItemL.add(aPolygon);
		}

		return retItemL;
	}

	/**
	 * Returns a short textual description of the specified structure.
	 * <p>
	 * The returned string will have the type, id, and relevant geometric attributes.
	 */
	public static String getStatusText(Structure aStructure, DecimalFormat aFormat)
	{
		var typeStr = "" + aStructure.getType();
		var retStr = typeStr + ", Id: " + aStructure.getId();

		var geoStr = getStatusTextGeo(aStructure, aFormat);
		if (geoStr.length() > 0)
			retStr += ", " + geoStr;

		return retStr;
	}

	/**
	 * Returns a short textual description of the specified structure.
	 * <p>
	 * The returned string is a comma separated string of relevant geometric attributes.
	 */
	public static String getStatusTextGeo(Structure aStructure, DecimalFormat aFormat)
	{
		var tmpState = aStructure.getRenderState();

		var retStr = "";
		if (aStructure instanceof Ellipse aEllipse)
			retStr += ", Diam: " + aFormat.format(aEllipse.getRadius() * 2) + " km";
		else if (Double.isNaN(tmpState.pathLength()) == false)
			retStr += ", Length: " + aFormat.format(tmpState.pathLength()) + " km";
		if (Double.isNaN(tmpState.surfaceArea()) == false)
			retStr += ", Area: " + aFormat.format(tmpState.surfaceArea()) + " km" + (char) 0x00B2;
		if (tmpState.controlPointL().size() > 1)
			retStr += ", # Pts: " + tmpState.controlPointL().size();

		if (retStr.length() > 2)
			retStr = retStr.substring(2);

		return retStr;
	}

	/**
	 * Helper method that will install the structures into the specified.
	 *
	 * @param aManager:
	 *    The {@link StructureManager} of interest.
	 * @param aItemC
	 *    The list of items that should be installed.
	 * @param aMode
	 *    Describes how the items should be installed.
	 *
	 * @see InstallMode
	 */
	public static void installStructures(Task aTask, AnyStructureManager aManager, Collection<Structure> aItemC,
			InstallMode aMode)
	{
		// Bail if aTask is aborted
		if (aTask.isActive() == false)
			return;

		// Gather the list of current installed items
		var origItemL = new ArrayList<Structure>();
		if (aMode != InstallMode.ReplaceAll)
			origItemL.addAll(aManager.getAllItems());

		// Form the appropriate ID generator
		IdGenerator tmpIG = null;
		if (aMode == InstallMode.AppendWithUniqueId)
			tmpIG = new IncrIdGenerator(StructureMiscUtil.calcNextId(aManager));

		// Perform the merge of the two lists
		var mergeMM = ArrayListMultimap.<Integer, Structure>create();

		for (var aItem : origItemL)
			mergeMM.put(aItem.getId(), aItem);

		for (var aItem : aItemC)
		{
			int tmpId = aItem.getId();
			if (aMode == InstallMode.AppendWithOriginalId)
				; // Nothing to do
			else if (aMode == InstallMode.AppendWithUniqueId)
				tmpId = tmpIG.getNextId();
			else if (aMode == InstallMode.ReplaceCollidingId)
				mergeMM.removeAll(tmpId);

			if (tmpId != aItem.getId())
				aItem = cloneItemWithId(aItem, tmpId);

			// Labels are always loaded with visibility turned off
			var tmpFA = aItem.getLabelFontAttr();
			if (tmpFA.getIsVisible() == true)
			{
				tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), tmpFA.getSize(), false);
				aItem.setLabelFontAttr(tmpFA);
			}

			mergeMM.put(tmpId, aItem);
		}

		// Install the items
		var fullItemL = new ArrayList<>(mergeMM.values());
		aManager.installItems(aTask, fullItemL);
	}

	/**
	 * Utility method that will project the control points associated with specified list of structures onto the provided
	 * shape model.
	 * <p>
	 * This method will only adjust control points for which the {@link Structure}'s shapeModelId does not match the
	 * passed in shapeModelId.
	 * <p>
	 * Throws a {@link RuntimeException} if the structure type is not recognized.
	 */
	public static void projectControlPointsToShapeModel(PolyModel aPolyModel, String aShapeModelId,
			List<? extends Structure> aItemL)
	{
		for (var aItem : aItemL)
		{
			var tmpShapeModelId = aItem.getShapeModelId();
			if (Objects.equals(aShapeModelId, tmpShapeModelId) == true)
				continue;

			// Update the structure's control points
			if (aItem instanceof PolyLine aPolyLine)
			{
				List<LatLon> tmpControlPointL = aPolyLine.getControlPoints();
				tmpControlPointL = ControlPointUtil.shiftControlPointsToNearestPointOnBody(aPolyModel, tmpControlPointL);
				aPolyLine.setControlPoints(tmpControlPointL);
			}
			else if (aItem instanceof Ellipse aEllipse)
			{
				var tmpPos = aEllipse.getCenter();
				tmpPos = aPolyModel.findClosestPoint(tmpPos);
				aEllipse.setCenter(tmpPos);
			}
			else if (aItem instanceof Point aPoint)
			{
				var tmpPos = aPoint.getCenter();
				tmpPos = aPolyModel.findClosestPoint(tmpPos);
				aPoint.setCenter(tmpPos);
			}
			else
			{
				throw new RuntimeException("Structure type is not recognized: " + aItem.getClass());
			}

			// Update the structure's associated shape model
			aItem.setShapeModelId(aShapeModelId);
		}
	}

	/**
	 * Utility method to set the change the center of the specified {@link Structure}.
	 */
	public static void setCenter(PolyModel aSmallBody, Structure aItem, Vector3D aCenter)
	{
		if (aItem instanceof Point aPoint)
		{
			aPoint.setCenter(aCenter);
			aPoint.setRenderState(aPoint.getRenderState().withCenterPt(aCenter));
			return;
		}
		else if (aItem instanceof Ellipse aEllipse)
		{
			aEllipse.setCenter(aCenter);
			aEllipse.setRenderState(aEllipse.getRenderState().withCenterPt(aCenter));
			return;
		}
		else if (aItem instanceof PolyLine || aItem instanceof Polygon)
		{
			// Unsupported functionality
			return;
		}

		throw new Error("Unsupported Structure: " + aItem.getClass() + " -> type: " + aItem.getType());
	}

	/**
	 * Utility method that will clone the specified {@link Structure} but with an id that matches the specified id.
	 *
	 * This method supports the following structures:
	 * <ul>
	 * <li>{@link Ellipse}.
	 * <li>{@link Point}.
	 * <li>{@link PolyLine}.
	 * <li>{@link Polygon}.
	 * <ul>
	 *
	 * If a structure is not recognized an {@link UnsupportedOperationException} will be thrown.
	 */
	private static Structure cloneItemWithId(Structure aItem, int aId)
	{
		var retItem = (Structure) null;
		if (aItem.getClass() == PolyLine.class)
		{
			var tmpItem = (PolyLine) aItem;
			retItem = new PolyLine(aId, tmpItem.getSource(), tmpItem.getControlPoints());
		}
		else if (aItem.getClass() == Polygon.class)
		{
			var tmpItem = (Polygon) aItem;
			retItem = new Polygon(aId, tmpItem.getSource(), tmpItem.getControlPoints());
		}
		else if (aItem instanceof Point aPoint)
		{
			retItem = new Point(aId, aPoint.getSource(), aPoint.getCenter(), aPoint.getColor());
		}
		else if (aItem instanceof Ellipse aEllipse)
		{
			retItem = new Ellipse(aId, aEllipse.getSource(), aEllipse.getType(), aEllipse.getCenter(),
					aEllipse.getRadius(), aEllipse.getAngle(), aEllipse.getFlattening(), aEllipse.getColor());
		}
		else
		{
			throw new UnsupportedOperationException("Unrecognized structure: " + aItem.getClass());
		}

		if (retItem instanceof ClosedShape aClosedShape)
			aClosedShape.setShowInterior(((ClosedShape) aItem).getShowInterior());

		retItem.setColor(aItem.getColor());
		retItem.setLabel(aItem.getLabel());
		retItem.setName(aItem.getName());
		retItem.setLabelFontAttr(aItem.getLabelFontAttr());
		retItem.setVisible(aItem.getVisible());
		retItem.setShapeModelId(aItem.getShapeModelId());
		retItem.setRenderState(aItem.getRenderState());
		return retItem;

	}

}
