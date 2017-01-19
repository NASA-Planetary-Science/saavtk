package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;

import edu.jhuapl.saavtk.util.LinearSpace;
import vtk.vtkColorTransferFunction;
import vtk.vtkLookupTable;
import vtk.vtkObject;
import vtk.vtkOutputWindow;

public class RgbColormap implements Colormap
{


    PropertyChangeSupport pcs=new PropertyChangeSupport(this);

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

	vtkLookupTable lut=new vtkLookupTable();
	vtkColorTransferFunction ctf;
	double dataMin,dataMax;
	boolean isLog=false;
	
	int nLabels=5;	// this should be placed in another class, cf. Colormap interface notes

	List<Double> interpLevels;
	List<Color> colors;
	int nLevels=defaultNumberOfLevels;
	Color nanColor;
	ColorSpace colorSpace;
	String name="";

	public enum ColorSpace
	{
		RGB,HSV,LAB,DIVERGING;
	}

	public static final int defaultNumberOfLevels=32;


	public RgbColormap(List<Double> interpLevels, List<Color> colors, int nLevels)	// the key in this Map must lie between 0 and 1
	{
		this(interpLevels, colors, nLevels, Color.white);
	}

	public RgbColormap(List<Double> interpLevels, List<Color> colors, int nLevels, Color nanColor)
	{
		this(interpLevels, colors, nLevels, nanColor, ColorSpace.RGB);
	}

	public RgbColormap(List<Double> interpLevels, List<Color> colors, int nLevels, Color nanColor, ColorSpace colorSpace)
	{
		this.interpLevels=interpLevels;
		this.colors=colors;
		this.nLevels=nLevels;
		this.nanColor=nanColor;
		this.colorSpace=colorSpace;
	}

	private void rebuildLookupTable()
	{
		if (dataMax<=dataMin)
			return;

		ctf=new vtkColorTransferFunction();
		ctf.SetAlpha(1);
        if (isLog)
            ctf.SetScaleToLog10();
        else
            ctf.SetScaleToLinear();
		switch (colorSpace)
		{
		case RGB:
			ctf.SetColorSpaceToRGB();
			break;
		case HSV:
			ctf.SetColorSpaceToHSV();
			break;
		case LAB:
			ctf.SetColorSpaceToLab();
			break;
		case DIVERGING:
			ctf.SetColorSpaceToDiverging();
			break;
		default:
			ctf.SetColorSpaceToRGB();
		}
		double rangeMin=Float.POSITIVE_INFINITY;
		double rangeMax=Float.NEGATIVE_INFINITY;
		for (int i=0; i<interpLevels.size(); i++)
		{
			if (interpLevels.get(i)<rangeMin)
				rangeMin=interpLevels.get(i);
			if (interpLevels.get(i)>rangeMax)
				rangeMax=interpLevels.get(i);
		}
		for (int i=0; i<interpLevels.size(); i++)
		{
			Color c=colors.get(i);
			float[] comp=c.getRGBComponents(null);
			ctf.AddRGBPoint((interpLevels.get(i)-rangeMin)/(rangeMax-rangeMin), comp[0], comp[1], comp[2]);
		}


        float[] comp=nanColor.getRGBColorComponents(null);
		lut.SetNanColor(comp[0],comp[1],comp[2],1);
		lut.SetNumberOfTableValues(nLevels);
		lut.ForceBuild();

		if (isLog)
		{
			double minValue=dataMin;
			double maxValue=dataMax;
			if (minValue<0)
				minValue=Double.MIN_VALUE;
			if (maxValue<0)
				maxValue=Double.MIN_VALUE;
		    lut.SetTableRange(minValue, maxValue);
		    lut.SetValueRange(minValue, maxValue);
		    lut.SetRange(minValue, maxValue);
		    System.out.println("Warning: negative values in range clipped to smallest possible value greater than zero ("+Double.MIN_VALUE+")");
		}
		else
		{
		    lut.SetTableRange(dataMin, dataMax);
		    lut.SetValueRange(dataMin, dataMax);
		    lut.SetRange(dataMin, dataMax);
		}

        if (isLog)
            lut.SetScaleToLog10();
        else
            lut.SetScaleToLinear();

        for (int i=0; i<getNumberOfLevels(); i++)
		{
			double val=(double)i/(double)getNumberOfLevels();//*(dataMax-dataMin)+dataMin;
			//lut.SetTableValue(i, ctf.GetColor(val));
			double[] col=ctf.GetColor(val);
			lut.SetTableValue(i, col[0], col[1], col[2], 1);
		}

		pcs.firePropertyChange(colormapPropertyChanged, null, null);
	}

	@Override
	public void setNumberOfLevels(int n)
	{
		nLevels=n;
		rebuildLookupTable();
	}

	@Override
	public int getNumberOfLevels()
	{
		return nLevels;
	}

	@Override
	public vtkLookupTable getLookupTable()
	{
		return lut;
	}

	@Override
	public Color getColor(double val)
	{
		if (Double.isNaN(val) || !Double.isFinite(val))
			return getNanColor();
		else
		{
/*		    if (isLog)
		    {
		        if (val>0)
		            val=Math.log10(val);
		        else
		            return getNanColor();
		    }*/
			double[] c=lut.GetColor(val);
			return new Color((float)c[0],(float)c[1],(float)c[2]);
		}
	}

	@Override
	public Color getNanColor()
	{
		double[] nanColor=lut.GetNanColor();
		return new Color((float)nanColor[0],(float)nanColor[1],(float)nanColor[2]);
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
		this.dataMin=val;
//		this.logDataMin=Math.log10(val);
		rebuildLookupTable();
	}

	@Override
	public void setRangeMax(double val)
	{
		this.dataMax=val;
//        this.logDataMax=Math.log10(val);
		rebuildLookupTable();
	}

/*	@Override
	public void setLog(boolean flag)
	{
	    isLog=flag;
		rebuildLookupTable();
	}

	@Override
	public boolean getLog()
	{
		return isLog;
	}*/

	public static RgbColormap copy(RgbColormap cmap)
	{
		RgbColormap newCmap=new RgbColormap(cmap.interpLevels,cmap.colors,cmap.nLevels,cmap.nanColor,cmap.colorSpace);
	//	newCmap.setLog(cmap.getLog());
		newCmap.setRangeMin(cmap.getRangeMin());
		newCmap.setRangeMax(cmap.getRangeMax());
		newCmap.setName(cmap.getName());
		return newCmap;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}

	@Override
	public void setLogScale(boolean flag)
	{
	    isLog=flag;
	}

	@Override
	public boolean isLogScale()
	{
	    return isLog;
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
		nLabels=n;
	}

}
