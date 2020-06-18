package edu.jhuapl.saavtk.camera;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;
import edu.jhuapl.saavtk.util.MathUtil;

/**
 * Collection of utility methods useful for working with {@link Camera}s.
 *
 * @author lopeznr1
 */
public class CameraUtil
{
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
		double distance = calcDistance(aCamera);
		if (aAxisType == AxisType.POSITIVE_X || aAxisType == AxisType.POSITIVE_Y || aAxisType == AxisType.POSITIVE_Z)
			distance = -distance;

		// Delegate
		setOrientationInDirectionOfAxis(aCamera, aAxisType, distance);
	}

	/**
	 * Utility method to configure the camera to focus on the specified position,
	 * aFocalPos. The camera will be moved to a point directly above the focus
	 * position along the normal as specified by aFocalNorm.
	 */
	public static void setFocalPosition(Camera aCamera, Vector3D aFocalPos, Vector3D aFocalNorm)
	{
		double flyToRange = aFocalPos.getNorm();

		Vector3D cPos = aCamera.getPosition();
		double cRange = cPos.getNorm();

		// Use law of cosines based on the current camera position and the fly-to
		// position to determine how far above the new spot to put the camera.
		double newAltitude = Math
				.sqrt(Math.abs(cRange * cRange + flyToRange * flyToRange - 2 * aFocalPos.dotProduct(cPos)));

		aCamera.setFocalPoint(aFocalPos);

		Vector3D tmpPosition = aFocalPos.add(aFocalNorm.scalarMultiply(newAltitude));
		aCamera.setPosition(tmpPosition);
	}

	/**
	 * Utility method that spins the view along the boresight of the current camera
	 * so that the Z axis of the body is up.
	 */
	public static void spinBoresightForNormalAxisZ(Camera aCamera)
	{
		Vector3D position = aCamera.getPosition();
		Vector3D focalPoint = aCamera.getFocalPoint();
		double[] posArr = position.toArray();
		double[] fpArr = focalPoint.toArray();

		double[] dir = { fpArr[0] - posArr[0], fpArr[1] - posArr[1], fpArr[2] - posArr[2] };
		MathUtil.vhat(dir, dir);

		double[] zAxis = { 0.0, 0.0, 1.0 };
		double[] upVectorArr = new double[3];
		MathUtil.vcrss(dir, zAxis, upVectorArr);

		if (upVectorArr[0] != 0.0 || upVectorArr[1] != 0.0 || upVectorArr[2] != 0.0)
		{
			MathUtil.vcrss(upVectorArr, dir, upVectorArr);
			aCamera.setView(focalPoint, position, new Vector3D(upVectorArr));
		}
	}

}
