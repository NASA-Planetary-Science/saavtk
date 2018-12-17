package edu.jhuapl.saavtk.gui.render.camera;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;
import vtk.vtkCamera;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Standard camera that is associated with the specified panel.
 * <P>
 * By default the camera is assumed to be centered over the origin with a
 * standard coordinate system (X-axis: [1, 0, 0]; Y-axis: [0, 1, 0], Z-axis: [0,
 * 0, 1]). If this is not the case then the method {@link #setDefaults} should
 * be called so that the proper coordinate system can utilized.
 */
public class StandardCamera implements Camera
{
	// Ref vars
	private final vtkJoglPanelComponent refPanel;

	// State vars
	private List<CameraListener> myListenerL;
	private CoordinateSystem curCoordSystem;

	// Default vars
	private CoordinateSystem defCoordSystem;
	private double defDistance;

	/**
	 * Standard constructor
	 * 
	 * @param aPanel       The panel associated with this camera.
	 * @param aDefDistance The distance between the camera and the focus position.
	 *                     This distance is utilized whenever the camera is reset.
	 */
	public StandardCamera(vtkJoglPanelComponent aPanel, double aDefDistance)
	{
		refPanel = aPanel;

		myListenerL = new ArrayList<>();
		curCoordSystem = CoordinateSystem.Standard;

		defCoordSystem = CoordinateSystem.Standard;
		defDistance = aDefDistance;
	}

	/**
	 * Sets in the default that will be utilized whenever the camera is reset.
	 * <P>
	 * TODO: Consider passing the defaults at construction time.
	 * 
	 * @param aCoordSystem
	 * @param aDefDistance
	 */
	public void setDefaults(CoordinateSystem aCoordSystem, double aDefDistance)
	{
		defCoordSystem = aCoordSystem;
		defDistance = aDefDistance;
	}

	@Override
	public void addListener(CameraListener listener)
	{
		myListenerL.add(listener);
	}

	@Override
	public void removeListener(CameraListener listener)
	{
		myListenerL.remove(listener);
	}

	@Override
	public Vector3D getPosition()
	{
		vtkCamera vCamera = getVtkCamera();
		return new Vector3D(vCamera.GetPosition());
	}

	@Override
	public Vector3D getFocalPoint()
	{
		vtkCamera vCamera = getVtkCamera();
		return new Vector3D(vCamera.GetFocalPoint());
	}

	@Override
	public Vector3D getLookUnit()
	{
		return getFocalPoint().subtract(getPosition()).normalize();
	}

	@Override
	public Vector3D getRightUnit()
	{
		return getLookUnit().crossProduct(getUpUnit()).normalize();
	}

	@Override
	public Vector3D getUpUnit()
	{
		vtkCamera vCamera = getVtkCamera();
		return new Vector3D(vCamera.GetViewUp()).normalize();
	}

	@Override
	public void setPosition(Vector3D point)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.SetPosition(point.toArray());
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void setFocalPoint(Vector3D point)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.SetFocalPoint(point.toArray());
		vCamera.OrthogonalizeViewUp();
	}

	@Override
	public void setLookUnit(Vector3D look)
	{
		double dist = getFocalPoint().subtract(getPosition()).getNorm();
		setFocalPoint(getPosition().add(look.normalize().scalarMultiply(dist)));

		vtkCamera vCamera = getVtkCamera();
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void setUpUnit(Vector3D up)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.SetViewUp(up.toArray());
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void dolly(double distance)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Dolly(distance);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void pan(double dx, double dy)
	{
		Vector3D deltaVec = getRightUnit().scalarMultiply(dx).add(getUpUnit().scalarMultiply(dy));
		setPosition(getPosition().add(deltaVec));
		setFocalPoint(getFocalPoint().add(deltaVec));
		fireCameraEvent();
	}

	@Override
	public void zoom(double factor)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Zoom(factor);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void roll(double angleDeg)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Roll(angleDeg);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void pitch(double angleDeg)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Pitch(angleDeg);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void yaw(double angleDeg)
	{
		vtkCamera vCamera = getVtkCamera();
		vCamera.Yaw(angleDeg);
		vCamera.OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public CoordinateSystem getCoordinateSystem()
	{
		return curCoordSystem;
	}

	@Override
	public void reset()
	{
		// Reset the coordinate system to the default
		curCoordSystem = defCoordSystem;

		CameraUtil.setOrientationInDirectionOfAxis(this, AxisType.NEGATIVE_Z, defDistance);
//		refRenderPanel.resetCamera();
	}

	@Override
	public void setCoordinateSystem(CoordinateSystem aCoordSystem)
	{
		curCoordSystem = aCoordSystem;

		// Send out notification of the configuration change
		fireCameraEvent();
	}

	@Override
	public void setView(Vector3D aFocalVect, Vector3D aPositionVect, Vector3D aViewUpVect)
	{
		refPanel.getVTKLock().lock();

		// Orient the camera to reflect the new configuration
		vtkCamera tmpVtkCamera = getVtkCamera();
		tmpVtkCamera.SetFocalPoint(aFocalVect.toArray());
		tmpVtkCamera.SetPosition(aPositionVect.toArray());
		tmpVtkCamera.SetViewUp(aViewUpVect.toArray());

		refPanel.getVTKLock().unlock();

		refPanel.resetCameraClippingRange();

		// Send out notification of the configuration change
		fireCameraEvent();
	}

	/**
	 * Helper method to send out notification that the "camera" state has changed.
	 */
	protected void fireCameraEvent()
	{
		for (CameraListener aListener : myListenerL)
			aListener.handle(new CameraEvent(this));
	}

	/**
	 * Helper method that returns the associated VTkCamera
	 */
	private vtkCamera getVtkCamera()
	{
		return refPanel.getActiveCamera();
	}

}
