package edu.jhuapl.saavtk2.io;

import java.io.File;
import java.io.IOException;

import vtk.vtkPolyData;
import vtk.vtkSTLWriter;

public class StlWriter implements PolyDataWriter {

	private final vtkSTLWriter writer;
	
	public StlWriter() {
		writer = new vtkSTLWriter();
	}

	@Override
	public void setInputData(vtkPolyData data) {
		writer.SetInputData(data);
	}

	@Override
	public void setOutputFile(String fileName) {
		writer.SetFileName(fileName);
	}

	@Override
	public void setOutputFile(File file) {
		setOutputFile(file.getAbsolutePath());
	}

	@Override
	public void write() throws IOException {
		writer.Update();
	}
}
