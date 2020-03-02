package edu.jhuapl.saavtk.structure.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

/**
 * Collection of miscellaneous utility methods for working with (LatLon) control
 * points.
 *
 * @author lopeznr1
 */
public class ControlPointUtil
{
	/**
	 * Returns the centroid of the specified LatLon points as projected on the
	 * provided small body.
	 */
	public static Vector3D calcCentroidOnBody(PolyhedralModel aSmallBody, List<LatLon> aLatLonL)
	{
		double[] centroid = { 0.0, 0.0, 0.0 };
		for (LatLon aLatLon : aLatLonL)
		{
			double[] tmpPt = MathUtil.latrec(aLatLon);
			centroid[0] += tmpPt[0];
			centroid[1] += tmpPt[1];
			centroid[2] += tmpPt[2];
		}

		int numPts = aLatLonL.size();
		centroid[0] /= numPts;
		centroid[1] /= numPts;
		centroid[2] /= numPts;

		double[] closestPoint = aSmallBody.findClosestPoint(centroid);

		return new Vector3D(closestPoint);
	}

	/**
	 * Returns the "size" of the specified LatLon points as projected on the
	 * provided small body.
	 * <P>
	 * The size is defined as the longest distance between any control point and the
	 * associated centroid.
	 */
	public static double calcSizeOnBody(PolyhedralModel aSmallBody, List<LatLon> aLatLonL)
	{
		// Compute the centroid of the LatLon points
		Vector3D centroid = calcCentroidOnBody(aSmallBody, aLatLonL);

		// Determine the max distance between any point and the centroid
		double maxDist = 0.0;
		for (LatLon aLL : aLatLonL)
		{
			double[] p = MathUtil.latrec(aLL);
			double tmpDist = MathUtil.distanceBetween(centroid.toArray(), p);
			if (tmpDist > maxDist)
				maxDist = tmpDist;
		}

		return maxDist;
	}

	/**
	 * Utility method that takes a list of Vector3D points and returns a
	 * corresponding list of LatLon.
	 */
	public static List<LatLon> convertToLatLonList(List<Vector3D> aPointL)
	{
		List<LatLon> retLatLonL = new ArrayList<>();
		for (Vector3D aPoint : aPointL)
		{
			LatLon tmpLL = MathUtil.reclat(aPoint.toArray());
			retLatLonL.add(tmpLL);
		}

		return retLatLonL;
	}

	/**
	 * Moves each control point to the corresponding location nearest on the
	 * provided shape model.
	 * <P>
	 * There are a variety of situations where this may be used such as:
	 * <UL>
	 * <LI>Resolution changes (keep control points touching the shape model)
	 * <LI>Loading a structure to a different shape model
	 * </UL>
	 */
	public static List<LatLon> shiftControlPointsToNearestPointOnBody(PolyhedralModel aSmallBody,
			List<LatLon> aControlPointL)
	{
		List<LatLon> retL = new ArrayList<>();

		for (LatLon aLL : aControlPointL)
		{
			double ptArr[] = MathUtil.latrec(aLL);
			double[] closestPoint = aSmallBody.findClosestPoint(ptArr);

			LatLon tmpLL = MathUtil.reclat(closestPoint);
			retL.add(tmpLL);
		}

		return retL;
	}

}
