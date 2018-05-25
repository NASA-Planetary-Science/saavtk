package edu.jhuapl.saavtk2.image.boundary;

import java.awt.Color;

import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.Projection;
import vtk.vtkPolyData;

public interface ImageBoundary
{
	public vtkPolyData getPolyDataRepresentation();
	public double getOffset();
	public void setOffset(double offset);
	public Projection getProjection(); 
	public ImageKey getKey();
}
