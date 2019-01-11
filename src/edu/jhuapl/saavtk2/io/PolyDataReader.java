package edu.jhuapl.saavtk2.io;

import vtk.vtkPolyData;

/**
 * Common interface for vtk file readers.
 * 
 * @author harpejw1
 *
 */
public interface PolyDataReader {
	
	/**
	 * Sets the file name and path of the file
	 * to read
	 * @param filename
	 */
	public void SetFileName(String filename);
	
	/**
	 * Call this to actually read the file
	 */
	public void Update();
	
	/**
	 * Returns the data read from the file 
	 * or null if {@link #Update()} has not been called.
	 * @return poly data read from the file
	 */
	public vtkPolyData GetOutput();
	
}
