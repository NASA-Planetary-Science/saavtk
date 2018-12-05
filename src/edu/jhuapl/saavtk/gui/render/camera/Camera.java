package edu.jhuapl.saavtk.gui.render.camera;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;

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
	 * Returns the logical axis corresponding to aAxisType.
	 */
	public Vector3D getLogicalAxis(AxisType aAxisType);

	/**
	 * Resets the camera's orientation to a default configuration.
	 */
	public void reset();

	/**
	 * Configures the camera so that the camera is pointed down the logical axis
	 * corresponding to the specified AxisType.
	 * 
	 * @param aAxisType
	 * @param preserveCurrentDistance
	 */
	public void setOrientationInDirectionOfAxis(AxisType aAxisType, boolean aPreserveCurrentDistance);

}
