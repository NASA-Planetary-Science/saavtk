package edu.jhuapl.saavtk.io.readers;

import vtk.vtkPolyData;
import vtk.vtkSTLReader;

public class StlReader implements PolyDataReader {

	private final vtkSTLReader reader;
	
	public StlReader() {
		this.reader = new vtkSTLReader();
	}
	
	@Override
	public void SetFileName(String filename) {
		reader.SetFileName(filename);
	}

	@Override
	public void Update() {
		reader.Update();
	}

	@Override
	public vtkPolyData GetOutput() {
		return reader.GetOutput();
	}
}
