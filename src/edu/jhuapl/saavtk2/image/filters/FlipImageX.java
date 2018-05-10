package edu.jhuapl.saavtk2.image.filters;

import vtk.vtkImageData;
import vtk.vtkImageFlip;

public class FlipImageX implements ImageDataFilter
{

	@Override
	public vtkImageData apply(vtkImageData source)
	{
		vtkImageFlip filter=new vtkImageFlip();
		filter.SetFilteredAxes(0);
		filter.SetInputData(source);
		filter.Update();
		return filter.GetOutput();
	}
	
	

}
