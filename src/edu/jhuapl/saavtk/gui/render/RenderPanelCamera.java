package edu.jhuapl.saavtk.gui.render;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.render.camera.Camera;
import edu.jhuapl.saavtk.gui.render.camera.CameraEvent;
import edu.jhuapl.saavtk.gui.render.camera.CameraListener;
import vtk.rendering.jogl.vtkJoglPanelComponent;


public class RenderPanelCamera implements Camera
{

	vtkJoglPanelComponent renderPanel;
	List<CameraListener> listeners=Lists.newArrayList();

	public RenderPanelCamera(vtkJoglPanelComponent renderView)
	{
		this.renderPanel=renderView;
	}
	
	@Override
	public Vector3D getPosition()
	{
		return new Vector3D(renderPanel.getActiveCamera().GetPosition()); 
	}

	@Override
	public Vector3D getFocalPoint()
	{
		return new Vector3D(renderPanel.getActiveCamera().GetFocalPoint());
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
		return new Vector3D(renderPanel.getActiveCamera().GetViewUp()).normalize();
	}
	
	@Override
	public void setPosition(Vector3D point)
	{
		renderPanel.getActiveCamera().SetPosition(point.toArray());
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void setFocalPoint(Vector3D point)
	{
		renderPanel.getActiveCamera().SetFocalPoint(point.toArray());
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
	}


	@Override
	public void dolly(double distance)
	{
		renderPanel.getActiveCamera().Dolly(distance);
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void pan(double dx, double dy)
	{
		Vector3D deltaVec=getRightUnit().scalarMultiply(dx).add(getUpUnit().scalarMultiply(dy));
		setPosition(getPosition().add(deltaVec));
		setFocalPoint(getFocalPoint().add(deltaVec));
		fireCameraEvent();
	}

	@Override
	public void zoom(double factor)
	{
		renderPanel.getActiveCamera().Zoom(factor);
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void roll(double angleDeg)
	{
		renderPanel.getActiveCamera().Roll(angleDeg);
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void pitch(double angleDeg)
	{
		renderPanel.getActiveCamera().Pitch(angleDeg);
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void yaw(double angleDeg)
	{
		renderPanel.getActiveCamera().Yaw(angleDeg);
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void setLookUnit(Vector3D look)
	{
		double dist=getFocalPoint().subtract(getPosition()).getNorm();
		setFocalPoint(getPosition().add(look.normalize().scalarMultiply(dist)));
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
		fireCameraEvent();
	}
	
	@Override
	public void setUpUnit(Vector3D up)
	{
		renderPanel.getActiveCamera().SetViewUp(up.toArray());
		renderPanel.getActiveCamera().OrthogonalizeViewUp();
		fireCameraEvent();
	}

	@Override
	public void addCameraListener(CameraListener listener)
	{
		listeners.add(listener);
	}
	
	@Override
	public void removeCameraListener(CameraListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public void fireCameraEvent()
	{
		for (CameraListener l : listeners)
			l.handle(new CameraEvent(this));
	}
	
}
