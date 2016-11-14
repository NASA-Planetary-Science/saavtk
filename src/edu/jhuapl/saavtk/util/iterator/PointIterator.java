package edu.jhuapl.saavtk.util.iterator;

import java.util.Iterator;

import edu.jhuapl.saavtk.util.Point3D;

public interface PointIterator extends Iterator<Point3D>, Iterable<Point3D> {

	/**
	 * Copies direct references to the underlying data
	 * but does not copy the data itself. i.e. lightweight 
	 * copy.
	 * @return
	 */
	public PointIterator shallowCopy();
	
	/**
	 * Causes the iterator to jump to the point
	 * at the given index and start iterating 
	 * from there. This is useful for multiple
	 * threads reading from the same data.
	 * @param index
	 */
	public void seekToPoint(int index);
	
	/**
	 * Sets the number of points for the 
	 * iterator to read. This can be
	 * less than or equal to the actual 
	 * number of points.
	 * @param size
	 */
	public void setNumberOfPoints(int size);
	
	/**
	 * Retruns the index of the current
	 * point.
	 * @return
	 */
	public int getCurrentIndex();
	
	/**
	 * Returns the number of points this 
	 * iterator will read.
	 */
	public int numberOfPoints();
}
