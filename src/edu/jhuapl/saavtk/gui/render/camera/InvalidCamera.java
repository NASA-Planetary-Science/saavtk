package edu.jhuapl.saavtk.gui.render.camera;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;

/**
 * Invalid Camera useful as a test harness / place holder.
 */
public class InvalidCamera implements Camera
{
	/** Singleton instance */
	public static final InvalidCamera Instance = new InvalidCamera();

	private InvalidCamera()
	{
		; // Nothing to do
	}

	@Override
	public void addListener(CameraListener listener)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeListener(CameraListener listener)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector3D getPosition()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector3D getFocalPoint()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector3D getLookUnit()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector3D getUpUnit()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector3D getRightUnit()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPosition(Vector3D point)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFocalPoint(Vector3D point)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUpUnit(Vector3D up)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLookUnit(Vector3D look)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void dolly(double distance)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void pan(double dx, double dy)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void zoom(double factor)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void roll(double angleDeg)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void pitch(double angleDeg)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void yaw(double angleDeg)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector3D getLogicalAxis(AxisType aAxisType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOrientationInDirectionOfAxis(AxisType aAxisType, boolean aPreserveCurrentDistance)
	{
		throw new UnsupportedOperationException();
	}

}
