package edu.jhuapl.saavtk2.polydata.transform;

import vtk.vtkTransform;

public class PolyDataCompositeTransform extends PolyDataTransform
{
	public PolyDataCompositeTransform(PolyDataTransform... transforms)	// carry out transforms from left to right
	{
		super(createTransform(transforms));
	}
	
	protected static vtkTransform createTransform(PolyDataTransform... transforms)
	{
		vtkTransform transform=new vtkTransform();
		transform.PostMultiply();
		for (int i=0; i<transforms.length; i++)
			transform.Concatenate(transforms[i].transform);
		transform.Update();
		return transform;
	}
}
