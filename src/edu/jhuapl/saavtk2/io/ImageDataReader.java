package edu.jhuapl.saavtk2.io;

import java.io.File;

import vtk.vtkImageData;

public interface ImageDataReader
{
	public vtkImageData read(File file);
}
