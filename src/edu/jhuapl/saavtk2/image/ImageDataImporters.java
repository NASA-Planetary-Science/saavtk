package edu.jhuapl.saavtk2.image;

import java.nio.file.Path;

import vtk.vtkBMPReader;
import vtk.vtkGraphicsFactory;
import vtk.vtkImageData;
import vtk.vtkJPEGReader;
import vtk.vtkPNGReader;

public class ImageDataImporters {
	
	public static Importer<vtkImageData> fromJpeg(Path file)
	{
		return new Importer<vtkImageData>() {
			
			@Override
			public vtkImageData get() {
				vtkJPEGReader reader=new vtkJPEGReader();
				reader.SetFileName(file.toString());
				reader.Update();
				return reader.GetOutput();
			}
		};
	}
	
	public static Importer<vtkImageData> fromPng(Path file)
	{
		return new Importer<vtkImageData>() {
			
			@Override
			public vtkImageData get() {
				vtkPNGReader reader=new vtkPNGReader();
				reader.SetFileName(file.toString());
				reader.Update();
				return reader.GetOutput();
			}
		};
	}
	
	public static Importer<vtkImageData> fromBmp(Path file)
	{
		return new Importer<vtkImageData>() {
			
			@Override
			public vtkImageData get() {
				vtkBMPReader reader=new vtkBMPReader();
				reader.SetFileName(file.toString());
				reader.Update();
				return reader.GetOutput();
			}
		};
	}
	
		
	
}
