package edu.jhuapl.saavtk.util.wireframe;

import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkPolyData;

public interface WireframeStrategy {

	public vtkPolyData convertPoint(Point3D center);
	
	public vtkPolyData convertLine(Point3D a, Point3D b);
	
}
