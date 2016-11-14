package edu.jhuapl.saavtk.io.writers;

import java.io.File;
import java.io.IOException;

import vtk.vtkPolyData;

public interface PolyDataWriter {

	public void setInputData(vtkPolyData data);
	
	public void setOutputFile(String fileName);
	
	public void setOutputFile(File file);
	
	public void write() throws IOException;
	
}
