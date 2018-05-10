package edu.jhuapl.saavtk2.image.filters;

import java.util.Arrays;

import edu.jhuapl.saavtk.colormap.Colormap;
import vtk.vtkImageData;
import vtk.vtkImageMapToColors;
import vtk.vtkLookupTable;

public class ColormappingFilter implements ImageDataFilter
{

	Colormap cmap;

	public ColormappingFilter(Colormap cmap)
	{
		this.cmap = cmap;
	}

	public void setMinValue(double val)
	{
		cmap.setRangeMin(val);
	}

	public void setMaxValue(double val)
	{
		cmap.setRangeMax(val);
	}

	public double getMinValue()
	{
		return cmap.getRangeMin();
	}

	public double getMaxValue()
	{
		return cmap.getRangeMax();
	}

	@Override
	public vtkImageData apply(vtkImageData data)
	{
		vtkLookupTable lut = cmap.getLookupTable();
		vtkImageMapToColors mapToColors = new vtkImageMapToColors();
		mapToColors.SetInputData(data);
		//mapToColors.SetOutputFormatToLuminance();//.SetOutputFormatToRGBA();
		mapToColors.SetOutputFormatToRGBA();
		mapToColors.SetLookupTable(lut);
		mapToColors.Update();
		
		return mapToColors.GetOutput();
	}

}
