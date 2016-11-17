package edu.jhuapl.saavtk.util.iterator;

import java.util.Iterator;

import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class DirectAccessIterator implements PointIterator {

	private final vtkPoints points;
	private int currentIndex;
	private int size;
	
	public DirectAccessIterator(vtkPolyData poly) {
		this.points = poly.GetPoints();
		this.currentIndex = 0;
		this.size = 0;
	}
	
	private DirectAccessIterator(DirectAccessIterator toCopy) {
		this.points = toCopy.points;
		this.currentIndex = toCopy.currentIndex;
		this.size = toCopy.size;
	}
	
	@Override
	public Iterator<Point3D> iterator() {
		return shallowCopy();
	}
	
	@Override
	public boolean hasNext() {
		return size > 0;
	}

	@Override
	public Point3D next() {
		double[] point = points.GetPoint(currentIndex++);
		--size;
		return new Point3D(point);
	}

	@Override
	public void seekToPoint(int index) {
		this.currentIndex = index;
	}

	@Override
	public void setNumberOfPoints(int size) {
		this.size = size;
	}
	
	@Override
	public DirectAccessIterator shallowCopy() {
		return new DirectAccessIterator(this);
	}
	
	@Override
	public int getCurrentIndex() {
		return currentIndex;
	}
	
	@Override
	public int numberOfPoints() {
		return size;
	}
}
