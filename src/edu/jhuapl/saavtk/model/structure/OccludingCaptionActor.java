package edu.jhuapl.saavtk.model.structure;

import vtk.vtkCaptionActor2D;

public class OccludingCaptionActor extends vtkCaptionActor2D
{

	double[] normal;
	
	public OccludingCaptionActor(double[] normal)
	{
		super();
		this.normal=normal;
	}
	
	public double[] getNormal()
	{
		return normal;
	}

	
}
