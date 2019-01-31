package edu.jhuapl.saavtk.gui.render.camera;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Camera
{
	public void addListener(CameraListener listener);
	public void removeListener(CameraListener listener);
	
	public Vector3D getPosition();
	public Vector3D getFocalPoint();
	public Vector3D getLookUnit();
	public Vector3D getUpUnit();
	public Vector3D getRightUnit();

	public void setPosition(Vector3D point);
	public void setFocalPoint(Vector3D point);
	public void setUpUnit(Vector3D up);
	public void setLookUnit(Vector3D look);
	
	public void dolly(double distance);
	public void pan(double dx, double dy);
	public void zoom(double factor);
	public void roll(double angleDeg);
	public void pitch(double angleDeg);
	public void yaw(double angleDeg);

	/**
	 * Returns the coordinate system used by the camera.
	 */
	public CoordinateSystem getCoordinateSystem();

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
	 * Configures the camera's view to be aligned with the specified focalVect,
	 * positionalVect, and viewUpVect.
	 * 
	 * @param aFocalVect    The vector that defines the camera focus.
	 * @param aPositionVect The vector that defines where the camera will be
	 *                      positioned.
	 * @param aViewUpVect   The vector that defines what the up direction is.
	 */
	public void setView(Vector3D aFocalVect, Vector3D aPositionVect, Vector3D aViewUpVect);

}
