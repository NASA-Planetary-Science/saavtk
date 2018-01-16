package edu.jhuapl.saavtk.gui.renderer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Camera
{
	public void addCameraListener(CameraListener listener);
	public void removeCameraListener(CameraListener listener);
	public void fireCameraEvent();
	
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
	
		
/*    public enum CartesianAxis
    {
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Y,
        NEGATIVE_Y,
        POSITIVE_Z,
        NEGATIVE_Z
    }

    Vector3D position;
    Vector3D focalPoint;
    Vector3D upVector;
  	
    void setPosition(Vector3D position)
    {
    	this.position=position;
    }
    
    void setFocalPoint(Vector3D focalPoint)
    {
    	this.focalPoint=focalPoint;
    }
    
    void setUpVector(Vector3D up)
    {
    	this.upVector=up;
    }*/
	
}
