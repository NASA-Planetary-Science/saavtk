package edu.jhuapl.saavtk2.image.filters;

import vtk.vtkImageData;

public class PassThroughImageDataFilter implements ImageDataFilter
{

	@Override
	public vtkImageData apply(vtkImageData source)
	{
		return source;
	}

}
