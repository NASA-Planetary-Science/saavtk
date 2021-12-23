package edu.jhuapl.saavtk.camera;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Interface which defines a "camera" in a 3D scene.
 * <P>
 * This interface provides for the following:
 * <UL>
 * <LI>{@link CameraActionListener} mechanism
 * <LI>Access to the camera's position, focal point, roll, view angle
 * <LI>Configuration of the (reference frame) {@link CoordinateSystem}
 * </UL>
 *
 * @author lopeznr1
 */
public interface Camera
{
	public Vector3D getUpUnit();
	
	/**
	 * Registers a {@link CameraListener} with this camera.
	 */
	public void addCameraChangeListener(CameraActionListener aListener);

	/**
	 * Deregisters a {@link CameraListener} with this camera.
	 */
	public void delCameraChangeListener(CameraActionListener aListener);

	/**
	 * Returns the coordinate system used by the camera.
	 */
	public CoordinateSystem getCoordinateSystem();

	/**
	 * Returns the camera's focal point.
	 */
	public Vector3D getFocalPoint();

	/**
	 * Returns the 3x3 orientation matrix of the camera.
	 * <P>
	 * The matrix is returned via the (3) 3-element arrays.
	 * <P>
	 * TODO: More details on this method are needed;
	 */
	public void getOrientationMatrix(double[] cxArr, double[] cyArr, double[] czArr);

	/**
	 * Returns the camera's position.
	 */
	public Vector3D getPosition();

	/**
	 * Returns the camera's roll.
	 */
	public double getRoll();

	/**
	 * Returns the camera's view angle.
	 */
	public double getViewAngle();

	/**
	 * Resets the camera's orientation to a default configuration.
	 */
	public void reset();

	/**
	 * Sets in a new coordinate system used by the camera.
	 * <P>
	 * The new coordinate system will be with utilized with future view
	 * manipulations.
	 */
	public void setCoordinateSystem(CoordinateSystem aCoordinateSystem);

	/**
	 * Sets the camera's focal point.
	 */
	public void setFocalPoint(Vector3D aPosition);

	/**
	 * Sets the camera's position.
	 */
	public void setPosition(Vector3D aPosition);

	/**
	 * Sets the camera's roll.
	 */
	public void setRoll(double aAngle);

	/**
	 * Configures the camera to be aligned with the specified focalVect, position,
	 * and viewUpVect.
	 *
	 * @param aFocalVect  The vector that defines the camera focus.
	 * @param aPosition   The vector that defines where the camera will be
	 *                    positioned.
	 * @param aViewUpVect The vector that defines what the up direction is.
	 */
	public void setView(Vector3D aFocalVect, Vector3D aPosition, Vector3D aViewUpVect);

	/**
	 * Sets the camera's view angle.
	 */
	public void setViewAngle(double aAngle);

}
