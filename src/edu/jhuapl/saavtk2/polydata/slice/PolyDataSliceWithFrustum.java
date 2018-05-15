package edu.jhuapl.saavtk2.polydata.slice;

import edu.jhuapl.saavtk2.polydata.PolyDataModifier;
import edu.jhuapl.saavtk2.util.Frustum;
import vtk.vtkPolyData;

public class PolyDataSliceWithFrustum implements PolyDataModifier
{

	Frustum frustum;
	
	public PolyDataSliceWithFrustum(Frustum frustum)
	{
		 this.frustum=frustum;
	}
	
	@Override
	public vtkPolyData apply(vtkPolyData polyData)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
