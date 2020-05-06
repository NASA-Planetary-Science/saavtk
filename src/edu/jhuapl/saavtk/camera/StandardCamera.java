package edu.jhuapl.saavtk.camera;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkCamera;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Vtk implementation of the {@link Camera} interface.
 * <P>
 * The installed {@link CoordinateSystem} defines the camera's frame of
 * reference.
 *
 * @author lopeznr1
 */
public class StandardCamera implements Camera
{
	// Ref vars
	private final vtkJoglPanelComponent refPanel;

	// State vars
	private List<CameraActionListener> cameraActionListenerL;
	private CoordinateSystem curCoordSystem;

	// Default vars
	private CoordinateSystem defCoordSystem;
	private double defDistance;

	/**
	 * Standard constructor
	 *
	 * @param aPanel            The panel associated with this camera.
	 * @param aCoordinateSystem The coordinate system associated with the camera.
	 *                          This coordinate system is utilized whenever the
	 *                          camera is reset.
	 * @param aDistance         The distance between the camera and the focus
	 *                          position. This distance is utilized whenever the
	 *                          camera is reset.
	 */
	public StandardCamera(vtkJoglPanelComponent aPanel, CoordinateSystem aCoordinateSystem, double aDistance)
	{
		refPanel = aPanel;

		cameraActionListenerL = new ArrayList<>();
		curCoordSystem = aCoordinateSystem;

		defCoordSystem = aCoordinateSystem;
		defDistance = aDistance;
	}

	/**
	 * Sets in the default that will be utilized whenever the camera is reset.
	 *
	 * @param aCoordSystem The coordinate system associated with the camera.
	 * @param aDefDistance The distance between the camera and the focus position.
	 */
	public void setDefaults(CoordinateSystem aCoordSystem, double aDefDistance)
	{
		defCoordSystem = aCoordSystem;
		defDistance = aDefDistance;
	}

	@Override
	public void addCameraChangeListener(CameraActionListener aListener)
	{
		cameraActionListenerL.add(aListener);
	}

	@Override
	public void delCameraChangeListener(CameraActionListener aListener)
	{
		cameraActionListenerL.remove(aListener);
	}

	@Override
	public Vector3D getFocalPoint()
	{
		vtkCamera vCamera = getVtkCamera();
		return new Vector3D(vCamera.GetFocalPoint());
	}

	@Override
	public Vector3D getPosition()
	{
		vtkCamera vCamera = getVtkCamera();
		return new Vector3D(vCamera.GetPosition());
	}

	@Override
	public double getRoll()
	{
		vtkCamera vCamera = getVtkCamera();
		return vCamera.GetRoll();
	}

	@Override
	public double getViewAngle()
	{
		vtkCamera vCamera = getVtkCamera();
		return vCamera.GetViewAngle();
	}

	@Override
	public CoordinateSystem getCoordinateSystem()
	{
		return curCoordSystem;
	}

	@Override
	public void getOrientationMatrix(double[] cxArr, double[] cyArr, double[] czArr)
	{
		vtkCamera cam = getVtkCamera();

		double[] pos = cam.GetPosition();

		double[] up = cam.GetViewUp();
		cxArr[0] = up[0];
		cxArr[1] = up[1];
		cxArr[2] = up[2];
		MathUtil.vhat(cxArr, cxArr);

		double[] fp = cam.GetFocalPoint();
		czArr[0] = fp[0] - pos[0];
		czArr[1] = fp[1] - pos[1];
		czArr[2] = fp[2] - pos[2];
		MathUtil.vhat(czArr, czArr);

		MathUtil.vcrss(czArr, cxArr, cyArr);
		MathUtil.vhat(cyArr, cyArr);
	}

	@Override
	public void reset()
	{
		// Reset the coordinate system to the default
		curCoordSystem = defCoordSystem;

		CameraUtil.setOrientationInDirectionOfAxis(this, AxisType.NEGATIVE_Z, defDistance);
	}

	@Override
	public void setCoordinateSystem(CoordinateSystem aCoordSystem)
	{
		curCoordSystem = aCoordSystem;

		// Send out event notification
		fireCameraEvent();
	}

	@Override
	public void setFocalPoint(Vector3D aPosition)
	{
		vtkCamera tmpVtkCamera = getVtkCamera();

		// Update the vtk camera
		refPanel.getVTKLock().lock();
		tmpVtkCamera.SetFocalPoint(aPosition.getX(), aPosition.getY(), aPosition.getZ());
		refPanel.getVTKLock().unlock();
		refPanel.resetCameraClippingRange();

		// Send out event notification
		fireCameraEvent();
	}

	@Override
	public void setPosition(Vector3D aPosition)
	{
		vtkCamera tmpVtkCamera = getVtkCamera();

		// Update the vtk camera
		refPanel.getVTKLock().lock();
		tmpVtkCamera.SetPosition(aPosition.getX(), aPosition.getY(), aPosition.getZ());
		refPanel.getVTKLock().unlock();
		refPanel.resetCameraClippingRange();

		// Send out event notification
		fireCameraEvent();
	}

	@Override
	public void setRoll(double aAngle)
	{
		vtkCamera tmpVtkCamera = getVtkCamera();

		// Update the vtk camera
		refPanel.getVTKLock().lock();
		tmpVtkCamera.SetRoll(aAngle);
		refPanel.getVTKLock().unlock();
		refPanel.resetCameraClippingRange();

		// Send out event notification
		fireCameraEvent();
	}

	@Override
	public void setView(Vector3D aFocalVect, Vector3D aPosition, Vector3D aViewUpVect)
	{
		vtkCamera tmpVtkCamera = getVtkCamera();

		// Update the vtk camera
		refPanel.getVTKLock().lock();
		tmpVtkCamera.SetFocalPoint(aFocalVect.toArray());
		tmpVtkCamera.SetPosition(aPosition.getX(), aPosition.getY(), aPosition.getZ());
		tmpVtkCamera.SetViewUp(aViewUpVect.toArray());
		refPanel.getVTKLock().unlock();
		refPanel.resetCameraClippingRange();

		// Send out event notification
		fireCameraEvent();
	}

	@Override
	public void setViewAngle(double aAngle)
	{
		vtkCamera tmpVtkCamera = getVtkCamera();

		// Update the vtk camera
		refPanel.getVTKLock().lock();
		tmpVtkCamera.SetViewAngle(aAngle);
		refPanel.getVTKLock().unlock();
		refPanel.resetCameraClippingRange();

		// Send out event notification
		fireCameraEvent();
	}

	/**
	 * Helper method to send out notification that the "camera" state has changed.
	 */
	protected void fireCameraEvent()
	{
		for (CameraActionListener aListener : cameraActionListenerL)
			aListener.handleCameraAction(this);
	}

	/**
	 * Helper method that returns the associated VTkCamera
	 */
	private vtkCamera getVtkCamera()
	{
		return refPanel.getActiveCamera();
	}

}
