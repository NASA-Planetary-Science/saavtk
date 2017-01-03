package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import vtk.vtkLookupTable;

public interface Colormap
{
    public static final String colormapPropertyChanged="Colormap property change";
    //
	public void setRangeMin(double val);
	public void setRangeMax(double val);
	public void setNumberOfLevels(int n);
	public void setLogScale(boolean flag);
	//
	public String getName();
	public Color getColor(double val);
	public Color getNanColor();
    public double getRangeMin();
    public double getRangeMax();
    public int getNumberOfLevels();
    public boolean isLogScale();
    public vtkLookupTable getLookupTable();
    public double[] getLevels();
    //
    public void addPropertyChangeListener(PropertyChangeListener l);
	public void removePropertyChangeListener(PropertyChangeListener l);
}
