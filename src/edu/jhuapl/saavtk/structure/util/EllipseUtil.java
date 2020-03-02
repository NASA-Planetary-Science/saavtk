package edu.jhuapl.saavtk.structure.util;

import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkCellArray;
import vtk.vtkIdTypeArray;
import vtk.vtkPoints;
import vtk.vtkTransform;

/**
 * Collection of miscellaneous utility methods for working with
 * {@link Ellipse}s.
 * <P>
 * A number of methods originated from the file (~2019Oct07):<BR>
 * edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.java<BR>
 * edu.jhuapl.saavtk.model.structure.AbstractEllipse.java<BR>
 *
 * @author lopeznr1
 */
public class EllipseUtil
{
	/**
	 * TODO: Add documentation
	 * <P>
	 * Source (~2019Oct07):
	 * edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.java
	 */
	public static double computeAngleOfPolygon(PolyhedralModel aSmallBody, Vector3D aCenter,
			Vector3D aNewPointOnPerimeter)
	{
		double[] centerArr = aCenter.toArray();
		double[] newPointOnPerimeterArr = aNewPointOnPerimeter.toArray();

		// The following math does this: we need to find the direction of
		// the semimajor axis of the ellipse. Then once we have that
		// we need to find the angular distance between the axis and the
		// vector from the ellipse center to the point the mouse
		// is hovering, where that vector is first projected onto the
		// tangent plane of the asteroid at the ellipse center.
		// This angular distance is what we rotate the ellipse by.

		// First compute cross product of normal and z axis
		double[] normal = aSmallBody.getNormalAtPoint(centerArr);
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		vtkTransform transform = new vtkTransform();
		transform.Translate(centerArr);
		transform.RotateWXYZ(sepAngle, cross);

		double[] xaxis = { 1.0, 0.0, 0.0 };
		xaxis = transform.TransformDoubleVector(xaxis);
		MathUtil.vhat(xaxis, xaxis);

		// Project newPoint onto the plane perpendicular to the
		// normal of the shape model.
		double[] projPoint = new double[3];
		MathUtil.vprjp(newPointOnPerimeterArr, normal, centerArr, projPoint);
		double[] projDir = new double[3];
		MathUtil.vsub(projPoint, centerArr, projDir);
		MathUtil.vhat(projDir, projDir);

		// Compute angular distance between projected direction and transformed x-axis
		double newAngle = MathUtil.vsep(projDir, xaxis) * 180.0 / Math.PI;

		// We need to negate this angle under certain conditions.
		if (newAngle != 0.0)
		{
			MathUtil.vcrss(xaxis, projDir, cross);
			double a = MathUtil.vsep(cross, normal) * 180.0 / Math.PI;
			if (a > 90.0)
				newAngle = -newAngle;
		}

		transform.Delete();

		return newAngle;
	}

	/**
	 * TODO: Add documentation
	 * <P>
	 * Source (~2019Oct07):
	 * edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.java
	 */
	public static double computeFlatteningOfPolygon(PolyhedralModel aSmallBody, Vector3D aCenter, double aRadius,
			double aAngle, Vector3D aNewPointOnPerimeter)
	{
		double[] centerArr = aCenter.toArray();
		double[] newPointOnPerimeterArr = aNewPointOnPerimeter.toArray();

		// The following math does this: we need to find the direction of
		// the semimajor axis of the ellipse. Then once we have that
		// we need to find the distance to that line from the point the mouse
		// is hovering, where that point is first projected onto the
		// tangent plane of the asteroid at the ellipse center.
		// This distance divided by the semimajor axis of the ellipse
		// is what we call the flattening.

		// First compute cross product of normal and z axis
		double[] normal = aSmallBody.getNormalAtPoint(centerArr);
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		vtkTransform transform = new vtkTransform();
		transform.Translate(centerArr);
		transform.RotateWXYZ(sepAngle, cross);
		transform.RotateZ(aAngle);

		double[] xaxis = { 1.0, 0.0, 0.0 };
		xaxis = transform.TransformDoubleVector(xaxis);
		MathUtil.vhat(xaxis, xaxis);

		// Project newPoint onto the plane perpendicular to the
		// normal of the shape model.
		double[] projPoint = new double[3];
		MathUtil.vprjp(newPointOnPerimeterArr, normal, centerArr, projPoint);
		double[] projDir = new double[3];
		MathUtil.vsub(projPoint, centerArr, projDir);

		double[] proj = new double[3];
		MathUtil.vproj(projDir, xaxis, proj);
		double[] distVec = new double[3];
		MathUtil.vsub(projDir, proj, distVec);
		double newRadius = MathUtil.vnorm(distVec);

		double newFlattening = 1.0;
		if (aRadius > 0.0)
			newFlattening = newRadius / aRadius;

		if (newFlattening < 0.001)
			newFlattening = 0.001;
		else if (newFlattening > 1.0)
			newFlattening = 1.0;

		transform.Delete();

		return newFlattening;
	}

	/**
	 * TODO: Add documentation
	 * <P>
	 * Source (~2019Oct07):
	 * edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.java
	 */
	public static double[] getStandardColoringValuesAtPolygon(AbstractEllipsePolygonModel aManager, Ellipse aEllipse,
			PolyhedralModel aSmallBody, Mode aMode) throws IOException
	{
		// Output array of 4 standard colorings (Slope, Elevation, GravAccel,
		// GravPotential).
		// Assume at the outset that none of the standard colorings are available.
		final double[] standardValues = new double[] { Double.NaN, Double.NaN, Double.NaN, Double.NaN };

		if (!aSmallBody.isColoringDataAvailable())
			return standardValues;

		int slopeIndex = -1;
		int elevationIndex = -1;
		int accelerationIndex = -1;
		int potentialIndex = -1;

		// Locate any of the 4 standard plate colorings in the list of all colorings
		// available for this resolution.
		// Usually the standard colorings are first in the list, so the loop could
		// terminate after all
		// 4 are >= 0, but omitting this check for brevity and readability.
		List<ColoringData> coloringDataList = aSmallBody.getAllColoringData();
		for (int index = 0; index < coloringDataList.size(); ++index)
		{
			String name = coloringDataList.get(index).getName();
			if (name.equalsIgnoreCase(PolyhedralModel.SlopeStr))
			{
				slopeIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.ElevStr))
			{
				elevationIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.GravAccStr))
			{
				accelerationIndex = index;
			}
			// This is a hack -- unfortunately, in at least OREx's case, this vector is
			// given a different name.
			else if (name.equalsIgnoreCase("Gravitational Magnitude"))
			{
				accelerationIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.GravPotStr))
			{
				potentialIndex = index;
			}
		}

		// Get all the coloring values interpolated at the center of the polygon.
		double[] allValues;

		try
		{
			allValues = aSmallBody.getAllColoringValues(aEllipse.getCenter().toArray());
			if (aMode != Mode.POINT_MODE)
			{
				// Replace slope and/or elevation central values with the average over the rim
				// of the circle.
				if (slopeIndex != -1 || elevationIndex != -1)
				{
					if (slopeIndex != -1)
						allValues[slopeIndex] = 0.; // Accumulate weighted sum in situ.
					if (elevationIndex != -1)
						allValues[elevationIndex] = 0.; // Accumulate weighted sum in situ.

					vtkCellArray lines = aManager.getVtkExteriorPolyDataFor(aEllipse).GetLines();
					vtkPoints points = aManager.getVtkExteriorPolyDataFor(aEllipse).GetPoints();

					vtkIdTypeArray idArray = lines.GetData();
					int size = idArray.GetNumberOfTuples();

					double totalLength = 0.0;
					double[] midpoint = new double[3];
					for (int i = 0; i < size; i += 3)
					{
						if (idArray.GetValue(i) != 2)
						{
							System.out.println("Big problem: polydata corrupted");
							return standardValues;
						}

						double[] pt1 = points.GetPoint(idArray.GetValue(i + 1));
						double[] pt2 = points.GetPoint(idArray.GetValue(i + 2));

						MathUtil.midpointBetween(pt1, pt2, midpoint);
						double dist = MathUtil.distanceBetween(pt1, pt2);
						totalLength += dist;

						double[] valuesAtMidpoint = aSmallBody.getAllColoringValues(midpoint);

						// Accumulate sums weighted by the length of this polygon segment.
						if (slopeIndex != -1)
							allValues[slopeIndex] += valuesAtMidpoint[slopeIndex] * dist;
						if (elevationIndex != -1)
							allValues[elevationIndex] += valuesAtMidpoint[elevationIndex] * dist;
					}

					// Normalize by the total (perimeter).
					if (slopeIndex != -1)
						allValues[slopeIndex] /= totalLength;
					if (elevationIndex != -1)
						allValues[elevationIndex] /= totalLength;
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Warning: plate coloring values were not available; omitting them from structures file.");
			System.err.println("Exception thrown was " + e.getMessage());

			allValues = new double[coloringDataList.size()];
			for (int index = 0; index < allValues.length; ++index)
			{
				allValues[index] = Double.NaN;
			}
		}

		// Use whichever standard coloring values are present to populate the output
		// array.
		if (slopeIndex != -1)
			standardValues[0] = allValues[slopeIndex];
		if (elevationIndex != -1)
			standardValues[1] = allValues[elevationIndex];
		if (accelerationIndex != -1)
			standardValues[2] = allValues[accelerationIndex];
		if (potentialIndex != -1)
			standardValues[3] = allValues[potentialIndex];

		return standardValues;
	}

	/**
	 * TODO: Add documentation
	 * <P>
	 * Source (~2019Oct07):
	 * edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.java
	 */
	public static Double getEllipseAngleRelativeToGravityVector(Ellipse aEllipse, PolyhedralModel aSmallBody)
	{
		double[] gravityVector = aSmallBody.getGravityVector(aEllipse.getCenter().toArray());
		if (gravityVector == null)
			return null;
		MathUtil.vhat(gravityVector, gravityVector);

		// First compute cross product of normal and z axis
		double[] normal = aSmallBody.getNormalAtPoint(aEllipse.getCenter().toArray());
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = -MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		// Rotate gravity vector and center of ellipse by amount
		// such that normal of ellipse faces positive z-axis
		vtkTransform transform = new vtkTransform();
		transform.RotateWXYZ(sepAngle, cross);

		gravityVector = transform.TransformDoubleVector(gravityVector);
		double[] center = transform.TransformDoublePoint(aEllipse.getCenter().toArray());

		// project gravity into xy plane
		double[] gravityPoint = { center[0] + gravityVector[0], center[1] + gravityVector[1],
				center[2] + gravityVector[2], };
		double[] projGravityPoint = new double[3];
		MathUtil.vprjp(gravityPoint, zaxis, center, projGravityPoint);
		double[] projGravityVector = new double[3];
		MathUtil.vsub(projGravityPoint, center, projGravityVector);
		MathUtil.vhat(projGravityVector, projGravityVector);

		// Compute direction of semimajor axis (both directions) in xy plane
		transform.Delete();
		transform = new vtkTransform();
		transform.RotateZ(aEllipse.getAngle());

		// Positive x direction
		double[] xaxis = { 1.0, 0.0, 0.0 };
		double[] semimajoraxis1 = transform.TransformDoubleVector(xaxis);

		// Negative x direction
		double[] mxaxis = { -1.0, 0.0, 0.0 };
		double[] semimajoraxis2 = transform.TransformDoubleVector(mxaxis);

		// Compute angular separation of projected gravity vector
		// with respect to x-axis using atan2
		double gravAngle = Math.atan2(projGravityVector[1], projGravityVector[0]) * 180.0 / Math.PI;
		if (gravAngle < 0.0)
			gravAngle += 360.0;

		// Compute angular separations of semimajor axes vectors (both directions)
		// with respect to x-axis using atan2
		double smaxisangle1 = Math.atan2(semimajoraxis1[1], semimajoraxis1[0]) * 180.0 / Math.PI;
		if (smaxisangle1 < 0.0)
			smaxisangle1 += 360.0;

		double smaxisangle2 = Math.atan2(semimajoraxis2[1], semimajoraxis2[0]) * 180.0 / Math.PI;
		if (smaxisangle2 < 0.0)
			smaxisangle2 += 360.0;

		// Compute angular separations between semimajor axes and gravity vector.
		// The smaller one is the one we want, which should be between 0 and 180
		// degrees.
		double sepAngle1 = smaxisangle1 - gravAngle;
		if (sepAngle1 < 0.0)
			sepAngle1 += 360.0;

		double sepAngle2 = smaxisangle2 - gravAngle;
		if (sepAngle2 < 0.0)
			sepAngle2 += 360.0;

		transform.Delete();

		return Math.min(sepAngle1, sepAngle2);
	}

}
