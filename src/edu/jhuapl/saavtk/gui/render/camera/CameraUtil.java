package edu.jhuapl.saavtk.gui.render.camera;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkCamera;
import vtk.vtkFloatArray;

/**
 * Collection of utility methods useful for camera operations.
 * <P>
 * A number of methods in this class deal with the retrieval / computation of
 * attributes associated with a PolyhedralModel. Eventually these utility
 * methods should be relocated to a more appropriate package.
 */
public class CameraUtil
{
	/**
	 * Given the specified PolyhedralModel compute and return the center point.
	 * <P>
	 * The center point is defined as a point that lies on the surface closest to
	 * the geometric center.
	 */
	public static Vector3D calcCenterPoint(PolyhedralModel aPolyModel)
	{
		// Retrieve the geometric center of the refPolyModel
		double[] centerPt = aPolyModel.getSmallBodyPolyData().GetCenter();

		// Locate a point on the surface closest to the geometric center of the model
		centerPt = aPolyModel.findClosestPoint(centerPt);

		Vector3D retCenterVect = new Vector3D(centerPt);
		return retCenterVect;
	}

	/**
	 * Given the specified PolyhedralModel compute and return the corresponding
	 * (average) surface normal.
	 * <P>
	 * The specified PolyhedralModel should be a surface rather than a closed body.
	 * Computation of the surface normal of a closed body is nonsensical.
	 * <P>
	 * The returned surface normal will be normalized.
	 * 
	 * @param aPolyModel
	 */
	public static Vector3D calcSurfaceNormal(PolyhedralModel aPolyModel)
	{
//		// Overly simplistic computation of normal
//		// Locate the normal at the located centerPt
//		Vector3D centerVect = calculateCenterPoint(aPolyModel);
//		double[] centerPt = centerVect.toArray();
//		
////		double[] normalPt = refPolyModel.getClosestNormal(centerPt);
//		double[] normalPt = aPolyModel.getNormalAtPoint(centerPt);
//		Vector3D normalVect = new Vector3D(normalPt);
//
//		// Debug
//		if (isDebug() == true)
//			System.err.println("   centerPt: " + centerVect + "  norm: " + normalVect);

		// Cast to a GenericPolyhedralModel
		// TODO: Should the method getCellNormals be part of PolyhedralModel interface?
		GenericPolyhedralModel tmpPolyModel = (GenericPolyhedralModel) aPolyModel;

		// Calculate the average normal vector (composed of all cells)
		vtkFloatArray tmpVFA = tmpPolyModel.getCellNormals();
		int numNorms = tmpVFA.GetNumberOfTuples();
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
	 * Utility method to determine if the specified PolyhedralModel is a true
	 * polyhedron or just a multipolygon surface.
	 */
	public static boolean isPolyhedron(GenericPolyhedralModel aPolyModel)
	{
		// Determine if we have a model that is a polyhedral model or a polygonal
		// surface. We do this by evaluating the angle between the (individual)
		// normals and the "center" normal.
		Vector3D centerVect = CameraUtil.calcCenterPoint(aPolyModel);
		vtkFloatArray tmpVFA = aPolyModel.getCellNormals();

		int numNorms = tmpVFA.GetNumberOfTuples();
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

		// Heuristic: Assume the shape model is a polyhedron if the number of (obtuse)
		// normals away from the "center" normal exceed a ratio of 7%
		double ratio = (numNormsObtuse + 0.0) / numNorms;
		boolean isPolyhedron = ratio > 0.07;

		// Debug
		if (isDebug() == true)
			System.err.println(String.format(
					"[CustomShapeModel: %s] numNorms: %d numSameSides: %d numDiffSides: %d  Ratio diff: %1.2f\n",
					aPolyModel.hashCode(), numNorms, (numNorms - numNormsObtuse), numNormsObtuse, ratio));

		return isPolyhedron;
	}

	/**
	 * Utility method that forms a CoordinateSystem from the specified normal and
	 * origin.
	 * 
	 * @param aNormal This Z-Axis will be aligned with the specified normal (and
	 *                normalized).
	 * @param aOrigin The origin of this coordinate system.
	 */
	public static CoordinateSystem formCoordinateSystem(Vector3D aNormal, Vector3D aOrigin)
	{
		CoordinateSystem retCoordinateSystem;
		Vector3D axisX, axisY, axisZ;

		// Let the Z-Axis equal the normal
		axisZ = aNormal;

		// Bail if z-axis is aligned with the standard unit z-axis
		if (axisZ.getX() == 0 && axisZ.getY() == 0)
		{
			axisX = Vector3D.PLUS_I;
			axisY = Vector3D.PLUS_J;
			axisZ = Vector3D.PLUS_K;

			retCoordinateSystem = new CoordinateSystem(axisX, axisY, axisZ, aOrigin);
			return retCoordinateSystem;
		}

		// Calculate the 2nd arbitrary (orthogonal) axis
		// Solve the equation: (a * d) + (b * e) + (c * f) = 0
		//
		// Due to our check that the normal is not alignment with
		// the (unit) z-axis we can be guaranteed that both getX()
		// and getY() will not equal zero (at the same time)!
		//
		// Source: https://en.wikipedia.org/wiki/Cross_product
		// Source: https://stackoverflow.com/questions/3049509
		double d, e, f;
		if (axisZ.getY() != 0)
		{
			// Let d = 0.50; f = 0.00; and solve for e
			d = 0.50;
			f = 0.00;
			e = -(axisZ.getX() * d) / axisZ.getY();
		}
		else
		{
			// Let e = 0.00; f = 0.50; and solve for d
			e = 0.00;
			f = 0.50;
			d = -(axisZ.getZ() * f) / axisZ.getX();
		}
		axisY = new Vector3D(d, e, f).normalize();

		// Calculate the 3rd (orthogonal) axis
		axisX = axisZ.crossProduct(axisY);
		axisX = axisX.normalize();

		retCoordinateSystem = new CoordinateSystem(axisX, axisY, axisZ, aOrigin);

		return retCoordinateSystem;
	}

	/**
	 * Utility method that computes distance between the camera's location and the
	 * focal point.
	 */
	public static double calcDistance(Camera aCamera)
	{
		Vector3D posVect = aCamera.getPosition();
		Vector3D focalVect = aCamera.getCoordinateSystem().getOrigin();

		double retDist = posVect.distance(focalVect);
		return retDist;
	}

	/**
	 * Utility method that configures the camera so that the camera is pointed down
	 * the logical axis corresponding to the specified AxisType.
	 * 
	 * @param aCamera   The camera to be manipulated.
	 * @param aAxisType The AxisType to look down.
	 * @param aDistance The distance between the camera and the focal point.
	 */
	public static void setOrientationInDirectionOfAxis(Camera aCamera, AxisType aAxisType, double aDistance)
	{
		CoordinateSystem curCoordSystem = aCamera.getCoordinateSystem();

		// Define local vars of interest
		Vector3D focalVect = curCoordSystem.getOrigin();
		Vector3D xAxisVect = curCoordSystem.getAxisX();
		Vector3D yAxisVect = curCoordSystem.getAxisY();
		Vector3D zAxisVect = curCoordSystem.getAxisZ();

		Vector3D viewVect;
		Vector3D targVect;
		if (aAxisType == AxisType.NEGATIVE_X)
		{
			targVect = xAxisVect;
			viewVect = zAxisVect;
		}
		else if (aAxisType == AxisType.POSITIVE_X)
		{
			targVect = xAxisVect;
			viewVect = zAxisVect;
		}
		else if (aAxisType == AxisType.NEGATIVE_Y)
		{
			targVect = yAxisVect;
			viewVect = zAxisVect;
		}
		else if (aAxisType == AxisType.POSITIVE_Y)
		{
			targVect = yAxisVect;
			viewVect = zAxisVect;
		}
		else if (aAxisType == AxisType.NEGATIVE_Z)
		{
			targVect = zAxisVect;
			viewVect = yAxisVect;
		}
		else if (aAxisType == AxisType.POSITIVE_Z)
		{
			targVect = zAxisVect;
			viewVect = yAxisVect;
		}
		else
		{
			throw new RuntimeException("Unsupported AxisType: " + aAxisType);
		}

		// Update the positional vector to account for the requested distance
		targVect = targVect.scalarMultiply(aDistance).add(focalVect);

		// Update the camera to reflect the new view configuration
		aCamera.setView(focalVect, targVect, viewVect);
	}

	/**
	 * Utility method that configures the camera so that the camera is pointed down
	 * the logical axis corresponding to the specified AxisType.
	 * <P>
	 * The distance between the Camera and the focal point will be maintained.
	 * 
	 * @param aCamera   The camera to be manipulated.
	 * @param aAxisType The AxisType to look down.
	 */
	public static void setOrientationInDirectionOfAxis(Camera aCamera, AxisType aAxisType)
	{
		double distance = CameraUtil.calcDistance(aCamera);
		if (aAxisType == AxisType.POSITIVE_X || aAxisType == AxisType.POSITIVE_Y || aAxisType == AxisType.POSITIVE_Z)
			distance = -distance;

		// Delegate
		CameraUtil.setOrientationInDirectionOfAxis(aCamera, aAxisType, distance);
	}

	/**
	 * Utility method to configure the camera to focus on the specified position,
	 * aFocalPos. The camera will be moved to a point directly above the focus
	 * position along the normal as specified by aFocalNorm.
	 * <P>
	 * TODO: Rework to make specific to {@link Camera} rather than {@link Renderer}
	 */
	public static void setFocalPosition(Renderer aRenderer, Vector3D aFocalPos, Vector3D aFocalNorm)
	{
		vtkCamera tmpCamera = aRenderer.getRenderWindowPanel().getActiveCamera();

		double flyToRange = aFocalPos.getNorm();

		Vector3D cPos = new Vector3D(tmpCamera.GetPosition());
		double cRange = cPos.getNorm();

		// Use law of cosines based on the current camera position and the fly-to
		// position to determine how far above the new spot to put the camera.
		double newAltitude = Math
				.sqrt(Math.abs(cRange * cRange + flyToRange * flyToRange - 2 * aFocalPos.dotProduct(cPos)));

		tmpCamera.SetFocalPoint(aFocalPos.toArray());
		tmpCamera.SetPosition(aFocalPos.add(aFocalNorm.scalarMultiply(newAltitude)).toArray());

		aRenderer.getRenderWindowPanel().Render();
	}

	/**
	 * Utility method that spins the view along the boresight of the current camera
	 * so that the Z axis of the body is up.
	 * <P>
	 * TODO: Rework to make specific to {@link Camera} rather than {@link Renderer}
	 */
	public static void spinBoresightForNormalAxisZ(Renderer aRenderer)
	{
		vtkCamera tmpCamera = aRenderer.getRenderWindowPanel().getActiveCamera();
//		Camera tmpCamera = refRenderer.getCamera();

		double[] position = tmpCamera.GetPosition();
		double[] focalPoint = tmpCamera.GetFocalPoint();
		double viewAngle = tmpCamera.GetViewAngle();

		double[] dir = { focalPoint[0] - position[0], focalPoint[1] - position[1], focalPoint[2] - position[2] };
		MathUtil.vhat(dir, dir);

		double[] zAxis = { 0.0, 0.0, 1.0 };
		double[] upVector = new double[3];
		MathUtil.vcrss(dir, zAxis, upVector);

		if (upVector[0] != 0.0 || upVector[1] != 0.0 || upVector[2] != 0.0)
		{
			MathUtil.vcrss(upVector, dir, upVector);
			aRenderer.setCameraOrientation(position, focalPoint, upVector, viewAngle);
		}
	}

	/**
	 * Helper method used for debugging
	 */
	private static boolean isDebug()
	{
		return false;
	}

}
