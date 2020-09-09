package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.color.table.ColorTable;
import edu.jhuapl.saavtk.color.table.ColorTableUtil;
import edu.jhuapl.saavtk.util.LinearSpace;
import vtk.vtkLookupTable;

public class RgbColormap implements Colormap
{
	// Constants
//	private static final int DefaultNumberOfLevels = 32;
	private static final int DefaultNumberOfLabels = 5;

	// Listener vars
	private PropertyChangeSupport pcs;

	// Attributes
	private final ColorTable refColorTable;

	// State vars
	private double dataMin, dataMax;
	private boolean isLog;

	private int nLevels;

	// This field member does not belong in this class.
	// See notes on Colormap interface.
	private int nLabels;

	// VTK vars
	private vtkLookupTable lut = new vtkLookupTable();

	public RgbColormap(ColorTable aColorTable)
	{
		pcs = new PropertyChangeSupport(this);

		refColorTable = aColorTable;

		dataMin = Double.NaN;
		dataMax = Double.NaN;
		isLog = false;

		nLevels = DefaultNumberOfLabels;

		nLabels = DefaultNumberOfLabels;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener l)
	{
		pcs.addPropertyChangeListener(l);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l)
	{
		pcs.removePropertyChangeListener(l);
	}

	@Override
	public ColorMapAttr getColorMapAttr()
	{
		return new ColorMapAttr(refColorTable, getRangeMin(), getRangeMax(), getNumberOfLevels(), isLogScale());
	}

	@Override
	public int getNumberOfLevels()
	{
		return nLevels;
	}

	@Override
	public void setNumberOfLevels(int aNumLevels)
	{
		nLevels = aNumLevels;
		rebuildLookupTable();
	}

	@Override
	public vtkLookupTable getLookupTable()
	{
		return lut;
	}

	@Override
	public Color getColor(double val)
	{
		// Return the NaN color if NaN or infinite value
		if (Double.isNaN(val) || !Double.isFinite(val))
			return getNanColor();

//		if (isLog)
//		{
//			if (val > 0)
//				val = Math.log10(val);
//			else
//				return getNanColor();
//		}
		double[] c = lut.GetColor(val);
		return new Color((float) c[0], (float) c[1], (float) c[2]);
	}

	@Override
	public Color getNanColor()
	{
		double[] nanColor = lut.GetNanColor();
		return new Color((float) nanColor[0], (float) nanColor[1], (float) nanColor[2]);
	}

	@Override
	public double getRangeMin()
	{
		return dataMin;
	}

	@Override
	public double getRangeMax()
	{
		return dataMax;
	}

	@Override
	public void setRangeMin(double val)
	{
		this.dataMin = val;
//		this.logDataMin=Math.log10(val);
		rebuildLookupTable();
	}

	@Override
	public void setRangeMax(double val)
	{
		this.dataMax = val;
//		this.logDataMax=Math.log10(val);
		rebuildLookupTable();
	}

	@Override
	public String getName()
	{
		return refColorTable.getName();
	}

	@Override
	public boolean isLogScale()
	{
		return isLog;
	}

	@Override
	public void setLogScale(boolean aFlag)
	{
		// Bail if nothing has changed
		if (isLog == aFlag)
			return;
		isLog = aFlag;

		rebuildLookupTable();
	}

	@Override
	public double[] getLevels()
	{
		return LinearSpace.create(getRangeMin(), getRangeMax(), getNumberOfLevels());
	}

	@Override
	public int getNumberOfLabels()
	{
		return nLabels;
	}

	@Override
	public void setNumberOfLabels(int n)
	{
		nLabels = n;
	}

	/**
	 * Helper method to forms the VTK lookup table used to determine what color a
	 * value is assigned to.
	 */
	private void rebuildLookupTable()
	{
		if (dataMax <= dataMin)
			return;

		// Delegate
		ColorTableUtil.updateLookUpTable(lut, getColorMapAttr());

		pcs.firePropertyChange(colormapPropertyChanged, null, null);
	}

	/**
	 * Utility method to form a clone of this RgbColormap.
	 */
	public static RgbColormap copy(RgbColormap aColormap)
	{
		RgbColormap retColormap = new RgbColormap(aColormap.refColorTable);

		retColormap.isLog = aColormap.isLog;
		retColormap.dataMin = aColormap.dataMin;
		retColormap.dataMax = aColormap.dataMax;
		retColormap.nLevels = aColormap.nLevels;
		retColormap.rebuildLookupTable();

		return retColormap;
	}

}
