package edu.jhuapl.saavtk2.image.filters;

import vtk.vtkImageData;
import vtk.vtkImageMapToColors;
import vtk.vtkImageMapToRGBA;
import vtk.vtkLookupTable;

public class SingleComponentToGrayscaleFilter implements ImageDataFilter
{
	double min,max;
	int nlevels;
	
	public SingleComponentToGrayscaleFilter(double min, double max, int nlevels)
	{
		this.min=min;
		this.max=max;
		this.nlevels=nlevels;
	}

	@Override
	public vtkImageData apply(vtkImageData source)
	{
		vtkLookupTable table=new vtkLookupTable();
		table.SetNumberOfTableValues(nlevels);
		table.SetNumberOfColors(nlevels);
		table.SetBelowRangeColor(new double[]{0,0,0,1});
		for (int i=0; i<nlevels; i++)
		{
			double f=(double)i/(double)(nlevels-1);
			table.SetTableValue(i, new double[]{f,f,f,1});
		}
		table.SetAboveRangeColor(new double[]{1, 1, 1, 1});
		table.SetRange(min,max);
		table.Build();

		vtkImageMapToColors mapper=new vtkImageMapToColors();
		mapper.SetInputData(source);
		mapper.SetLookupTable(table);
		mapper.SetOutputFormatToLuminance();
		mapper.Update();
		
		return mapper.GetOutput();
	}

}
