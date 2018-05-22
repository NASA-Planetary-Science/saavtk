package edu.jhuapl.saavtk2.image.filters;

import vtk.vtkImageData;

public interface ImageDataFilter
{
	vtkImageData apply(vtkImageData source);
}
