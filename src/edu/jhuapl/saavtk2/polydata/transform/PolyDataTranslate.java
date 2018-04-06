package edu.jhuapl.saavtk2.polydata.transform;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkTransform;

public class PolyDataTranslate extends PolyDataTransform
{

	public PolyDataTranslate(Vector3D offset)
	{
		super(createTranslationTransform(offset));
	}
	
	protected static vtkTransform createTranslationTransform(Vector3D offset)
	{
		vtkTransform transform=new vtkTransform();
		transform.Translate(offset.toArray());
		transform.Update();
		return transform;
	}

}
