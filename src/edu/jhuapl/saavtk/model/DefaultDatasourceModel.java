package edu.jhuapl.saavtk.model;

import java.util.List;

import edu.jhuapl.saavtk.util.BoundingBox;
import vtk.vtkPolyData;
import vtk.vtkProp;
import vtk.vtksbCellLocator;

public class DefaultDatasourceModel extends DatasourceModel
{
	@Override
	public boolean isBuiltIn()
	{
		return false;
	}

	public void updateScaleBarValue(double pixelSizeInKm)
	{}

	public void updateScaleBarPosition(int windowWidth, int windowHeight)
	{}

	@Override
	public List<vtkProp> getProps()
	{
		return null;
	}

	public vtksbCellLocator getCellLocator()
	{
		return null;
	}

	public BoundingBox getBoundingBox()
	{
		return null;
	}

	public void setShowScaleBar(boolean enabled)
	{}

	public boolean getShowScaleBar()
	{
		return false;
	}

	public boolean isEllipsoid()
	{
		return false;
	}

	public vtkPolyData getSmallBodyPolyData()
	{
		return null;
	}

	public List<vtkPolyData> getSmallBodyPolyDatas()
	{
		return null;
	}

	public String getCustomDataFolder()
	{
		return null;
	}

	public String getConfigFilename()
	{
		return null;
	}
}
