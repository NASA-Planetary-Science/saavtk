package edu.jhuapl.saavtk2.polydata.select;

import java.util.List;

import com.google.common.collect.Lists;

import vtk.vtkPolyData;

public abstract class GenericPolyDataSelector implements PolyDataSelector
{
	vtkPolyData polyData;
	List<Integer> selected=Lists.newArrayList();
	List<Integer> unselected=Lists.newArrayList();
	
	public GenericPolyDataSelector(vtkPolyData polyData)
	{
		this.polyData=polyData;
	}
	
	@Override
	public List<Integer> getSelected()
	{
		return selected;
	}

	@Override
	public List<Integer> getUnselected()
	{
		return unselected;
	}
		
}
