package edu.jhuapl.saavtk2.polydata;

import vtk.vtkPolyData;

public interface PolyDataOperator<R>
{
	R apply(vtkPolyData polyData);
}
