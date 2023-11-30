package edu.jhuapl.saavtk.structure.vtk;

import java.util.ArrayList;
import java.util.Objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.vtk.VtkDrawUtil;
import vtk.vtkPoints;
import vtk.vtkPolyData;

/**
 * Object which consists of 2 control points and a collection of intermediate points. The control points are specified
 * as {@link LatLon}s.
 *
 * @author lopeznr1
 */
public class Segment
{
	// Attributes
	private final LatLon pointLL1;
	private final LatLon pointLL2;

	// State vars
	private boolean isStale;
	private double pathLen;
	private ImmutableList<Vector3D> intermediatePointL;

	/** Standard Constructor */
	public Segment(LatLon aPointA, LatLon aPointB)
	{
		pointLL1 = aPointA;
		pointLL2 = aPointB;

		isStale = true;
		pathLen = Double.NaN;
		intermediatePointL = ImmutableList.of();
	}

	/**
	 * Returns the (distance) length of this segment.
	 * <p>
	 * The length is the summation of the intermediate points.
	 */
	public double getPathLength()
	{
		return pathLen;
	}

	/**
	 * Returns the 1st end point as a {@link LatLon}.
	 */
	public LatLon getPointBegLL()
	{
		return pointLL1;
	}

	/**
	 * Returns the 2nd end point as a {@link LatLon}.
	 */
	public LatLon getPointEndLL()
	{
		return pointLL2;
	}

	/**
	 * Returns all of the 3D points that compose this segment
	 */
	public ImmutableList<Vector3D> getPoints3D()
	{
		if (isStale == true)
			throw new RuntimeException("Invalid state");

		return intermediatePointL;
	}

	/**
	 * Method that will update the intermediate points within this {@link Segment}.
	 */
	public void update(PolyhedralModel aSmallBody)
	{
		// Bail if we are not stale
		if (isStale == false)
			return;
		isStale = false;

		// Transform LLA to XYZ
		var pt1Arr = MathUtil.latrec(pointLL1);
		var pt2Arr = MathUtil.latrec(pointLL2);
		var pt1 = new Vector3D(pt1Arr);
		var pt2 = new Vector3D(pt2Arr);

		var vTmpPD = (vtkPolyData) null;
		var vTmpP = (vtkPoints) null;
		if (Math.abs(pointLL1.lat - pointLL2.lat) < 1e-8 && Math.abs(pointLL1.lon - pointLL2.lon) < 1e-8
				&& Math.abs(pointLL1.rad - pointLL2.rad) < 1e-8)
		{
			vTmpP = new vtkPoints();
			vTmpP.InsertNextPoint(pt1Arr);
			vTmpP.InsertNextPoint(pt2Arr);
		}
		else
		{
			var vSurfacePD = aSmallBody.getSmallBodyPolyData();
			var vSurfacePL = aSmallBody.getPointLocator();
			vTmpPD = VtkDrawUtil.drawPathPolyOn(vSurfacePD, vSurfacePL, pt1, pt2);
			if (vTmpPD == null)
				return;

			vTmpP = vTmpPD.GetPoints();
		}

		// Keep track of:
		// - The newly calculated 3D points
		// - The total distance between all points
		var tmpPointL = new ArrayList<Vector3D>();
		var numPts = vTmpP.GetNumberOfPoints();

		pathLen = 0.0;
		var prevPt = new Vector3D(vTmpP.GetPoint(0));
		for (var aIdx = 0; aIdx < numPts; aIdx++)
		{
			var currPt = new Vector3D(vTmpP.GetPoint(aIdx));
			tmpPointL.add(currPt);
			pathLen += prevPt.distance(currPt);

			prevPt = currPt;
		}
		intermediatePointL = ImmutableList.copyOf(tmpPointL);

		// Release VTK mem
		if (vTmpPD != null)
			vTmpPD.Delete();
		else
			vTmpP.Delete();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Segment other = (Segment) obj;
		return Objects.equals(pointLL1, other.pointLL1) && Objects.equals(pointLL2, other.pointLL2);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(pointLL1, pointLL2);
	}

}
