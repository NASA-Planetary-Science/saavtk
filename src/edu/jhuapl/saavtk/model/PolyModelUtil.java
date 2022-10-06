package edu.jhuapl.saavtk.model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkFloatArray;

/**
 * Collection of utility methods useful for computing or determining various
 * attributes / parameters associated with {@link PolyModel}s.
 *
 * @author lopeznr1
 */
public class PolyModelUtil
{
	/**
	 * Utility method that returns the altitude for the specified {@link PolyModel}
	 * and a given (target) position.
	 * <P>
	 * The altitude is defined as the distance from the (target) position and the
	 * corresponding intercept position via {@link #calcInterceptPosition()}.
	 * <P>
	 * The returned value will be negative if the (target) position is within the
	 * {@link PolyModel}.
	 */
	public static double calcAltitudeFor(PolyModel aPolyModel, Vector3D aTargetPos)
	{
		Vector3D tmpInterceptPos = calcInterceptPosition(aPolyModel, aTargetPos);
		double retAltDist = aTargetPos.distance(tmpInterceptPos);

		boolean isInsidePolyModel = false;
		Vector3D geoCenterPos = aPolyModel.getGeometricCenterPoint();
		if (tmpInterceptPos.distance(geoCenterPos) > aTargetPos.distance(geoCenterPos))
			isInsidePolyModel = true;

		if (isInsidePolyModel == true)
			retAltDist = -retAltDist;

		return retAltDist;
	}

	/**
	 * Given the specified {@link PolyModel} compute and return the center point.
	 * <P>
	 * The center point is defined as a point that lies on the surface closest to
	 * the geometric center.
	 */
	public static Vector3D calcCenterPoint(GenericPolyhedralModel aPolyModel)
	{
		// Retrieve the geometric center from the PolyModel
		double[] tmpCenterArr = aPolyModel.getSmallBodyPolyData().GetCenter();
		Vector3D tmpCenterPt = new Vector3D(tmpCenterArr);

		// Locate a point on the surface closest to the geometric center
		Vector3D retCenterPt = aPolyModel.findClosestPoint(tmpCenterPt);
		return retCenterPt;
	}

	/**
	 * Utility method that returns the (surface) intercept position between the the
	 * {@link PolyModel}'s origin (geometric center) and the target position.
	 * <P>
	 * Note that if the {@link PolyModel} is a polygonal surface rather than a
	 * polyhedron then the target's intercept will be defined to be equivalent to
	 * the geometric center of the polygonal surface.
	 * <P>
	 * If a intercept could not be calculated then {@link Vector3D#NaN} is returned.
	 */
	public static Vector3D calcInterceptPosition(PolyModel aPolyModel, Vector3D aTargetPos)
	{
		// Retrieve the geometric center
		Vector3D geoCenterPos = aPolyModel.getGeometricCenterPoint();

		// Handle special case of a PolyModel that is really a polygonal surface.
		// Note for a polygonal model the assumption is made that the target intercept
		// point is defined as the geometric center of the (polygonal) surface.
		if (aPolyModel.isPolyhedron() == false)
			return geoCenterPos;

		// If the target is outside the (polyhedral) surface then this is a simple
		// intercept between 2 end points and the polyhedron that lies in between.
		Vector3D retInterceptPos = aPolyModel.calcInterceptBetween(aTargetPos, geoCenterPos);
		if (retInterceptPos != null)
			return retInterceptPos;

		// Otherwise the surface intercept lies (directionally) behind the target since
		// the target is within the (polyhedral) surface. The surface intercept must be
		// calculated such that a the target (direction) vector is extended to a
		// location where it lies outside of the polyhedron.
		//
		// The surface intercept is calculated between the (polyhedral) geometric origin
		// and the (extended) target directional vector.
		double extLen = aPolyModel.getBoundingBoxDiagonalLength() * 2;
		Vector3D unitDir = aTargetPos.subtract(geoCenterPos).normalize();

		Vector3D extPos = unitDir.scalarMultiply(extLen);
		retInterceptPos = aPolyModel.calcInterceptBetween(aTargetPos, extPos);
		if (retInterceptPos != null)
			return retInterceptPos;

		// Failed to calculate an intercept
		return Vector3D.NaN;
	}

	/**
	 * Utility method to determine if the specified {@link PolyModel} is a true
	 * polyhedron or just a polygonal surface.
	 */
	public static boolean calcIsPolyhedron(GenericPolyhedralModel aPolyModel)
	{
		// Determine if we have a model that is a polyhedral model or a polygonal
		// surface. We do this by evaluating the angle between the (individual)
		// normals and the "center" normal.
		Vector3D centerVect = calcCenterPoint(aPolyModel);
		vtkFloatArray tmpVFA = aPolyModel.getCellNormals();

		int numNorms = (int)tmpVFA.GetNumberOfTuples();
		int numNormsObtuse = 0;

		// Evaluate all of the normals. If performance is an issue then consider:
		// - Randomly evaluating only 10% or less of the normals
		// - Randomly evaluating only the first ~5K of normals
		for (int c1 = 0; c1 < numNorms; c1++)
		{
			double[] tmp = tmpVFA.GetTuple3(c1);
			Vector3D evalVect = new Vector3D(tmp[0], tmp[1], tmp[2]);

			// Evaluate whether the angle between the center normal and the individual
			// normal is acute or obtuse. Acute angles correspond to normals that are
			// on the same side.
			if (isAcuteAngle(centerVect, evalVect) == false)
				numNormsObtuse++;
		}

		// Heuristic: Assume the PolyModel is a polyhedron if the number of (obtuse)
		// normals away from the "center" normal exceed a ratio of 7%
		double ratio = (numNormsObtuse + 0.0) / numNorms;
		boolean retIsPolyhedron = ratio > 0.07;

		// Debug
		if (isDebug() == true)
			System.err.println(
					String.format("[PolyModel: %s] numNorms: %d numSameSides: %d numDiffSides: %d  Ratio diff: %1.2f\n",
							aPolyModel.hashCode(), numNorms, (numNorms - numNormsObtuse), numNormsObtuse, ratio));

		return retIsPolyhedron;
	}

	/**
	 * Given the specified {@link PolyhedralModel} compute and return the
	 * corresponding (average) surface normal.
	 * <P>
	 * The specified {@link PolyhedralModel} should be a surface rather than a
	 * closed body. Computation of the surface normal of a closed body is
	 * nonsensical.
	 * <P>
	 * The returned surface normal will be normalized.
	 */
	public static Vector3D calcSurfaceNormal(GenericPolyhedralModel aPolyModel)
	{
//		// Overly simplistic computation of normal
//		// Locate the normal at the located centerPt
//		Vector3D centerPt = calcCenterPoint(aPolyModel);
//
////		Vector3D normalVect = aPolyModel.getClosestNormal(centerPt);
//		Vector3D normalVect = aPolyModel.getNormalAtPoint(centerPt);
//
//		// Debug
//		if (isDebug() == true)
//			System.err.println("   centerPt: " + centerVect + "  norm: " + normalVect);

		// Calculate the average normal vector (composed of all cells)
		vtkFloatArray tmpVFA = aPolyModel.getCellNormals();
		int numNorms = (int)tmpVFA.GetNumberOfTuples();
		double sumX = 0.0, sumY = 0.0, sumZ = 0.0;
		for (int c1 = 0; c1 < numNorms; c1++)
		{
			double[] tmp = tmpVFA.GetTuple3(c1);
			sumX += tmp[0];
			sumY += tmp[1];
			sumZ += tmp[2];
		}
		Vector3D normalVect = new Vector3D(sumX / numNorms, sumY / numNorms, sumZ / numNorms);

		// Normalize the average normal vector
		if (normalVect.equals(Vector3D.ZERO) == true)
			normalVect = Vector3D.PLUS_K;
		else
			normalVect = normalVect.normalize();

		// Debug
		if (isDebug() == true)
			System.err.println("   SurfaceNormal: " + normalVect + " numNorms: " + numNorms);

		return normalVect;
	}

	/**
	 * Utility method that returns true if the angle between the 2 specified vectors
	 * is less than 90 degrees.
	 */
	public static boolean isAcuteAngle(Vector3D aVectA, Vector3D aVectB)
	{
		// The equation of the angle between 2 vectors is given by:
		// cos(theta) = (vectA dot vectB) / (||vectA|| * ||vectB||)
		//
		// Note however that we do not need to calculate the actual actual angle (theta)
		// just the sign of the expression cos(theta) to determine if we are looking at
		// acute or obtuse angles.
		double tmpVal = aVectA.dotProduct(aVectB);
		if (tmpVal > 0)
			return true;

		return false;
	}

	/**
	 * Helper method used for debugging
	 */
	private static boolean isDebug()
	{
		return false;
	}

}
