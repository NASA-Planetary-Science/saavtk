package edu.jhuapl.saavtk.io.readers;

import vtk.vtkOBJReader;
import vtk.vtkPolyData;

public class ObjReader implements PolyDataReader {

	private final vtkOBJReader reader;
	
	public ObjReader() {
		this.reader = new vtkOBJReader();
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
