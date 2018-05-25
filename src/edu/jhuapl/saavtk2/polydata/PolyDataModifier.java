package edu.jhuapl.saavtk2.polydata;

import vtk.vtkPolyData;

public interface PolyDataModifier extends PolyDataOperator<vtkPolyData>
{
	public vtkPolyData apply(vtkPolyData polyData);
}
