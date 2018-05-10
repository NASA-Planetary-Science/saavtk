package edu.jhuapl.saavtk2.geom;

import vtk.vtkImageData;
import vtk.vtkImageDataGeometryFilter;
import vtk.vtkPolyData;

public class ImageDataGeometry extends BasicGeometry  {

	vtkImageData imageData;
	
	public ImageDataGeometry(vtkImageData imageData) {
		super(createPolyDataRepresentation(imageData));
		this.imageData=imageData;
	}

	public static vtkPolyData createPolyDataRepresentation(vtkImageData imageData)
	{
		vtkImageDataGeometryFilter geomFilter=new vtkImageDataGeometryFilter();
		geomFilter.SetInputData(imageData);
		geomFilter.Update();
		return geomFilter.GetOutput();
	}
	
	public vtkImageData getImageData()
	{
		return imageData;
	}
	
	public void changeImageData(vtkImageData imageData)
	{
		this.polyData=createPolyDataRepresentation(imageData);
		update();
		this.imageData=imageData;
	}
}
