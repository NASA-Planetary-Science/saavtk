package edu.jhuapl.saavtk.util.iterator;

import java.util.Iterator;

import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkDataArray;
import vtk.vtkFloatArray;
import vtk.vtkPolyData;

public class JavaArrayIterator implements PointIterator {

	private final float[] pointData;//x1,y1,z1, x2, y2, z2...
	private int currentIndex;
	private int size;
	
	public JavaArrayIterator(vtkDataArray array) {
		this (((vtkFloatArray)array).GetJavaArray(), 0, array.GetNumberOfTuples());
	}
	
	public JavaArrayIterator(vtkPolyData polyData) {
		this(((vtkFloatArray)polyData.GetPoints().GetData()).GetJavaArray(), 0, polyData.GetNumberOfPoints());
	}
	
	public JavaArrayIterator(float[] pointData) {
		this(pointData, 0, 0);
	}
	
	public JavaArrayIterator(float[] pointData, int startIndex, int size) {
		if (pointData == null) {
			pointData = new float[0];
		}
		
		if (pointData.length % 3 != 0) {
			throw new IllegalArgumentException("Array must have a multiple of 3 values");
		}
		
		this.pointData = pointData;
		this.currentIndex = startIndex;
		this.size = size;
	}
	
	@Override
	public Iterator<Point3D> iterator() {
		return shallowCopy();
	}
	
	@Override
	public boolean hasNext() {
//		System.out.println(size);
		return size > 0;
	}

	@Override
	public Point3D next() {
		float x = pointData[currentIndex++];
		float y = pointData[currentIndex++];
		float z = pointData[currentIndex++];
		--size;
		return new Point3D(new double[]{x, y, z});
	}

	@Override
	public void seekToPoint(int index) {
		this.currentIndex = index*3;
	}

	@Override
	public void setNumberOfPoints(int size) {
		this.size = Math.min(size, pointData.length/3);
	}
	
	@Override
	public JavaArrayIterator shallowCopy() {
		JavaArrayIterator copy = new JavaArrayIterator(pointData);
		copy.currentIndex = currentIndex;
		copy.size = size;
		return copy;
	}
	
	@Override
	public int getCurrentIndex() {
		return currentIndex / 3;
	}
	
	@Override
	public int numberOfPoints() {
		return size;
	}
}
