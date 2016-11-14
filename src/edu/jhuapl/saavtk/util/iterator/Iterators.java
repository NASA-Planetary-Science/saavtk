package edu.jhuapl.saavtk.util.iterator;

import vtk.vtkDataArray;
import vtk.vtkFloatArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class Iterators {

	/**
	 * Returns the fastest point iterator for the given 
	 * object. 
	 * @param poly
	 * @param startIndex - Inclusive
	 * @param endIndex - Exclusive. 
	 * @return
	 */
	public static PointIterator get(vtkPolyData poly) {
		vtkPoints points = poly.GetPoints();
		if (points == null) {
			return new JavaArrayIterator(new float[0]);
		}
		vtkDataArray data = points.GetData();
		PointIterator iterator = null;
		if (data instanceof vtkFloatArray) {
			iterator = new JavaArrayIterator(poly);
		} else {
			iterator = new DirectAccessIterator(poly);
		}
		return iterator;
	}
	
	public static PointIterator get(vtkDataArray array) {
		if (array instanceof vtkFloatArray) {
			return new JavaArrayIterator(array);
		} else {
			return new TupleArrayIterator(array);
		}
	}
}
