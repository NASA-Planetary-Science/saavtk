package edu.jhuapl.saavtk2.geom.euclidean;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Sphere {

	Vector3D center;
	double radius;
	
	public Sphere(double radius) {
		this(Vector3D.ZERO,radius);
	}
	
	public Sphere(Vector3D center, double radius) {
		this.center=center;
		this.radius=radius;
	}

	public Vector3D getCenter()
	{
		return center;
	}
	
	public double getRadius()
	{
		return radius;
	}
	
	public Vector3D getPosition(double azi, double pol)
	{
		double x=radius*Math.cos(azi)*Math.sin(pol);
		double y=radius*Math.sin(azi)*Math.sin(pol);
		double z=radius*Math.cos(pol);
		return new Vector3D(x,y,z);
	}

}
