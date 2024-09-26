package edu.jhuapl.saavtk2.polydata.clip;

import edu.jhuapl.saavtk2.polydata.PolyDataModifier;
import vtk.vtkClipPolyData;
import vtk.vtkImplicitFunction;
import vtk.vtkPolyData;

public class PolyDataClip implements PolyDataModifier
{

	vtkImplicitFunction clipFunction;
	vtkClipPolyData clipFilter=new vtkClipPolyData();
	
	public PolyDataClip(vtkImplicitFunction clipFunction)
	{
		this.clipFunction=clipFunction;
		clipFilter.SetClipFunction(clipFunction);
		clipFilter.GenerateClipScalarsOn();
		setInsideOut(false);
	}

	public void setInsideOut(boolean insideOut)
	{
		clipFilter.SetInsideOut(insideOut?1:0);
	}

	@Override
	public vtkPolyData apply(vtkPolyData polyData)
	{
		clipFilter.SetInputData(polyData);
		clipFilter.Update();
		vtkPolyData result=new vtkPolyData();
		result.DeepCopy(clipFilter.GetOutput());
		return result;
	}

}
