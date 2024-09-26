package edu.jhuapl.saavtk2.polydata.select;

import vtk.vtkPolyData;

public abstract class PolyDataCellSelector extends GenericPolyDataSelector
{

	public PolyDataCellSelector(vtkPolyData polyData)
	{
		super(polyData);
	}

	@Override
	public void apply()
	{
		selected.clear();
		unselected.clear();
		for (int i=0; i<polyData.GetNumberOfCells(); i++)
			if (select(i))
				selected.add(i);
			else
				unselected.add(i);
	}
	

}
