package edu.jhuapl.saavtk.colormaps;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import vtk.vtkColorTransferFunction;
import vtk.vtkLookupTable;

public abstract class RgbColormap implements Colormap
{

	vtkLookupTable lut;
	vtkColorTransferFunction ctf;
	double dataMin,dataMax;
	
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
		ctf=new vtkColorTransferFunction();
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
		ctf.SetRange(0,1);
		
		setNumberOfLevels(defaultNumberOfLevels);
		float[] comp=nanColor.getRGBColorComponents(null);
		lut.SetNanColor(comp[0],comp[1],comp[2],1);
	}

	@Override
	public void setNumberOfLevels(int n)
	{
		lut=new vtkLookupTable();
		lut.SetNumberOfTableValues(n);
		lut.SetRange(0,1);
		lut.Build();
		for (int i=0; i<n; i++)
		{
			double val=(double)i/(double)n;
			lut.SetTableValue(i, ctf.GetColor(val));
		}
	}
	
	@Override
	public int getNumberOfLevels()
	{
		return lut.GetNumberOfTableValues();
	}
	
	@Override
	public vtkLookupTable getLookupTable()
	{
		return lut;
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
	}

	@Override
	public void setRangeMax(double val)
	{
		this.dataMax=val;
	}
	
	@Override
	public void setLog(boolean flag)
	{
		if (flag)
			lut.SetScaleToLog10();
		else
			lut.SetScaleToLinear();
	} 
}
