package edu.jhuapl.saavtk.util.wireframe;

import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkPolyData;

public class CylinderWireframeStrategy implements WireframeStrategy {

	private final int numberOfAngles;
	private final double radius;
	
	public CylinderWireframeStrategy() {
		this(WireframeUtil.DEFAULT_NUM_ANGLES, WireframeUtil.DEFAULT_RADIUS);
	}
	
	public CylinderWireframeStrategy(double radius) {
		this(WireframeUtil.DEFAULT_NUM_ANGLES, radius);
	}
	
	public CylinderWireframeStrategy(int numberOfAngles, double radius) {
		this.numberOfAngles = numberOfAngles;
		this.radius = radius;
	}
	
	@Override
	public vtkPolyData convertPoint(Point3D center) {
		return WireframeUtil.buildSphere(center, numberOfAngles, radius);
	}

	@Override
	public vtkPolyData convertLine(Point3D a, Point3D b) {
		return WireframeUtil.buildCylinderFromLine(a, b, numberOfAngles, radius);
	}
}
