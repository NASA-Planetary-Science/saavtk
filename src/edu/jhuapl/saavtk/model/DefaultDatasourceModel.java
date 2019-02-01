package edu.jhuapl.saavtk.model;

import java.util.List;

import edu.jhuapl.saavtk.util.BoundingBox;
import vtk.vtkPolyData;
import vtk.vtkProp;
import vtk.vtksbCellLocator;

public class DefaultDatasourceModel extends DatasourceModel
{
	private final CommonData commonData;

	protected DefaultDatasourceModel(CommonData commonData)
	{
		this.commonData = commonData;
	}

	@Override
	public boolean isBuiltIn()
	{
		return false;
	}

	public void updateScaleBarValue(@SuppressWarnings("unused") double pixelSizeInKm)
	{}

	public void updateScaleBarPosition(@SuppressWarnings("unused") int windowWidth, @SuppressWarnings("unused") int windowHeight)
	{}

	@Override
	public final void setCommonData(@SuppressWarnings("unused") CommonData commonData)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CommonData getCommonData()
	{
		return commonData;
	}

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

	public void setShowScaleBar(@SuppressWarnings("unused") boolean enabled)
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
