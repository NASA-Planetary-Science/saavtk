package edu.jhuapl.saavtk2.polydata.transform;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import vtk.vtkMatrix4x4;
import vtk.vtkTransform;

public class PolyDataRotate extends PolyDataTransform
{

	public PolyDataRotate(Rotation rotation)
	{
		super(createRotationTransform(rotation));
	}
	
	protected static vtkTransform createRotationTransform(Rotation rotation)
	{
		vtkTransform transform=new vtkTransform();
		vtkMatrix4x4 matrix=new vtkMatrix4x4();
		for (int i=0; i<3; i++)
			for (int j=0; j<3; j++)
				matrix.SetElement(i, j, rotation.getMatrix()[i][j]);
		matrix.SetElement(3, 3, 1);
		transform.SetMatrix(matrix);
		transform.Update();
		return transform;
	}
	
	
}
