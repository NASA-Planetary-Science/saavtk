package edu.jhuapl.saavtk.gui.render.camera;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.camera.Camera;
import edu.jhuapl.saavtk.camera.CameraActionListener;

/**
 * Invalid Camera useful as a test harness / place holder.
 * <P>
 * Please note that calling any method on this object will result in an
 * {@link UnsupportedOperationException}.
 *
 * @author lopeznr1
 */
public class InvalidCamera implements Camera
{
	/** Singleton instance */
	public static final InvalidCamera Instance = new InvalidCamera();

	/** Private Constructor **/
	private InvalidCamera()
	{
		; // Nothing to do
	}

	@Override
	public void addCameraChangeListener(CameraActionListener aListener)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delCameraChangeListener(CameraActionListener aListener)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CoordinateSystem getCoordinateSystem()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector3D getFocalPoint()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector3D getPosition()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getRoll()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getViewAngle()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void getOrientationMatrix(double[] cxArr, double[] cyArr, double[] czArr)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCoordinateSystem(CoordinateSystem aCoordinateSystem)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFocalPoint(Vector3D aPosition)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPosition(Vector3D aPosition)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRoll(double aAngle)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setView(Vector3D aFocalVect, Vector3D aPosition, Vector3D aViewUpVect)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setViewAngle(double aAngle)
	{
		throw new UnsupportedOperationException();
	}

}
