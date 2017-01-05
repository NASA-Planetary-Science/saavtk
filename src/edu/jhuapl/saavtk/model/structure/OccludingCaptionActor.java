package edu.jhuapl.saavtk.model.structure;

import vtk.vtkCaptionActor2D;

public class OccludingCaptionActor extends vtkCaptionActor2D
{

	double[] normal;
	double[] rayStartPoint;
	
	public OccludingCaptionActor(double[] normal, double[] rayStartPoint)
	{
		super();
		this.normal=normal;
		this.rayStartPoint=rayStartPoint;
	}
	
	public double[] getNormal()
	{
		return normal;
	}
	
	public double[] getRayStartPoint()
	{
		return rayStartPoint;
	}
	
	
}
