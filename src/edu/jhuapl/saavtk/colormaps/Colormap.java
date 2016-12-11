package edu.jhuapl.saavtk.colormaps;

import vtk.vtkLookupTable;

public interface Colormap
{
	public vtkLookupTable getLookupTable();
	public double getRangeMin();
	public double getRangeMax();
	public void setRangeMin(double val);
	public void setRangeMax(double val);
	public void setLog(boolean flag);
	public void setNumberOfLevels(int n);
	public int getNumberOfLevels();
	public String getName();
}
