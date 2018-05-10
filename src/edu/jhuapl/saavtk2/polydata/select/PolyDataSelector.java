package edu.jhuapl.saavtk2.polydata.select;

import java.util.List;

import vtk.vtkSelectionNode;

public interface PolyDataSelector
{
	public void apply();
	public boolean select(int i);
	public List<Integer> getSelected();
	public List<Integer> getUnselected();
}
