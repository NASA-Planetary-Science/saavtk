package edu.jhuapl.saavtk2.polydata.transform;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkTransform;

public class PolyDataScale extends PolyDataTransform
{

	public PolyDataScale(Vector3D origin, double sx, double sy, double sz)
	{
		super(createScalingTransform(origin, sx, sy, sz));
	}
	
	public PolyDataScale(Vector3D origin, double s)
	{
		super(createScalingTransform(origin, s, s, s));
	}
	
	public PolyDataScale(double s)
	{
		super(createScalingTransform(Vector3D.ZERO, s, s, s));
	}
	
	public PolyDataScale(double sx, double sy, double sz)
	{
		super(createScalingTransform(Vector3D.ZERO, sx, sy, sz));
	}

	protected static vtkTransform createScalingTransform(Vector3D origin, double sx, double sy, double sz)
	{
		vtkTransform transform=new vtkTransform();
		transform.Translate(origin.negate().toArray());
		transform.Scale(sx,sy,sz);
		transform.Translate(origin.toArray());
		transform.Update();
		return transform;
	}

}
