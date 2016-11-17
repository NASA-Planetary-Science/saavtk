package edu.jhuapl.saavtk.util.iterator;

import java.util.Iterator;

import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkDataArray;

public class TupleArrayIterator implements PointIterator {

	private final vtkDataArray array;
	private int currentIndex;
	private int size;
	
	public TupleArrayIterator(vtkDataArray array) {
		this.array  = array;
	}
	
	public TupleArrayIterator(TupleArrayIterator toCopy) {
		this.array = toCopy.array;
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
		double[] point = array.GetTuple3(currentIndex++);
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
	public TupleArrayIterator shallowCopy() {
		return new TupleArrayIterator(this);
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
