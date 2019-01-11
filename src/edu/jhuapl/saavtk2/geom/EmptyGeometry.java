package edu.jhuapl.saavtk2.geom;

import vtk.vtkPolyData;

public class EmptyGeometry extends BasicGeometry
{

	public EmptyGeometry()
	{
		super(new vtkPolyData());
	}

}
