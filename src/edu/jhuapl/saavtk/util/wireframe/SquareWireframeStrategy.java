package edu.jhuapl.saavtk.util.wireframe;

import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkPolyData;

public class SquareWireframeStrategy implements WireframeStrategy {

	private final double radius;
	
	public SquareWireframeStrategy() {
		this(WireframeUtil.DEFAULT_RADIUS);
	}
	
	public SquareWireframeStrategy(double radius) {
		this.radius = radius;
	}
	
	@Override
	public vtkPolyData convertPoint(Point3D center) {
		return WireframeUtil.convertPointToCube(center, radius);
	}

	@Override
	public vtkPolyData convertLine(Point3D a, Point3D b) {
		return WireframeUtil.convertLineToBox(a, b, radius);
	}
}
