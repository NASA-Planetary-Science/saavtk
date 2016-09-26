package edu.jhuapl.saavtk.color;

import vtk.vtkLookupTable;

public interface Colormap
{
	public vtkLookupTable getLookupTable();
	public double getRangeMin();
	public double getRangeMax();
	public void setRangeMin();
	public void setRangeMax();
	public double[] getColor(double value);
}
