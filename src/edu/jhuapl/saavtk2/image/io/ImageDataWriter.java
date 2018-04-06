package edu.jhuapl.saavtk2.image.io;

import java.io.File;

import vtk.vtkImageData;

public interface ImageDataWriter
{
	public void write(vtkImageData data, File file);
}
