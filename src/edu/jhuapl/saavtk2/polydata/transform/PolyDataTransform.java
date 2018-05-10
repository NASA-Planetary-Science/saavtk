package edu.jhuapl.saavtk2.polydata.transform;

import edu.jhuapl.saavtk2.polydata.PolyDataModifier;
import vtk.vtkPolyData;
import vtk.vtkTransform;
import vtk.vtkTransformFilter;

public class PolyDataTransform implements PolyDataModifier
{
	protected vtkTransform transform;
	protected vtkTransformFilter transformFilter;

	public PolyDataTransform(vtkTransform transform)
	{
		this.transform=transform;
		this.transformFilter=new vtkTransformFilter();
		transformFilter.SetTransform(transform);
		transform.Update();
	}
	
	@Override
	public vtkPolyData apply(vtkPolyData polyData)
	{
		transformFilter.SetInputData(polyData);
		transformFilter.Update();
		vtkPolyData result=new vtkPolyData();
		result.DeepCopy(transformFilter.GetOutput());
		return result;
	}

}
