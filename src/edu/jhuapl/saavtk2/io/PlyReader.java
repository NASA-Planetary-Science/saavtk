package edu.jhuapl.saavtk2.io;

import vtk.vtkPLYReader;
import vtk.vtkPolyData;

public class PlyReader implements PolyDataReader {

	private final vtkPLYReader reader;
	
	public PlyReader() {
		this.reader = new vtkPLYReader();
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
