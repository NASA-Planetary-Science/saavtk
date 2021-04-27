package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import edu.jhuapl.saavtk.color.table.ColorMapAttr;
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
	//
	public int getNumberOfLabels();	// this really should be placed in some other class, since it is a property of the visual representation of a colormap, e.g. a colorbar, rather than the colormap itself
	public void setNumberOfLabels(int n);

	/**
	 * Returns the equivalent {@link ColorMapAttr} as specified by this Colormap.
	 */
	public ColorMapAttr getColorMapAttr();
}
