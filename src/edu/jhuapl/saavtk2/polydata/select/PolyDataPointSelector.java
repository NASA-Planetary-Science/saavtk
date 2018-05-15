package edu.jhuapl.saavtk2.polydata.select;

import vtk.vtkPolyData;

public abstract class PolyDataPointSelector extends GenericPolyDataSelector 
{

	public PolyDataPointSelector(vtkPolyData polyData)
	{
		super(polyData);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void apply()
	{
		selected.clear();
		unselected.clear();
		for (int i=0; i<polyData.GetNumberOfPoints(); i++)
			if (select(i))
				selected.add(i);
			else
				unselected.add(i);
	}

}
