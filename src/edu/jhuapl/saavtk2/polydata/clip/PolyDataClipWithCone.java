package edu.jhuapl.saavtk2.polydata.clip;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkCone;
import vtk.vtkImplicitBoolean;
import vtk.vtkImplicitFunction;
import vtk.vtkNativeLibrary;
import vtk.vtkPlane;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;
import vtk.vtkTransform;

public class PolyDataClipWithCone extends PolyDataClip
{

	public PolyDataClipWithCone(Vector3D apex, Vector3D axis, double fovDeg)
	{
		super(generateClipFunction(apex, axis, fovDeg));
	}

	protected static vtkImplicitFunction generateClipFunction(Vector3D apex, Vector3D axis, double fovDeg)
	{		
		vtkPlane plane=new vtkPlane();
		plane.SetOrigin(0,0,0);
		plane.SetNormal(-1,0,0);

		vtkCone coneFunction=new vtkCone();
		coneFunction.SetAngle(fovDeg);
			
		Rotation rot=new Rotation(Vector3D.PLUS_I, axis);
		vtkTransform transform=new vtkTransform();
		transform.RotateWXYZ(Math.toDegrees(rot.getAngle()), rot.getAxis().negate().toArray());
		transform.Update();
		
		vtkImplicitBoolean booleanFunction=new vtkImplicitBoolean();
		booleanFunction.SetOperationTypeToIntersection();
		booleanFunction.AddFunction(plane);
		booleanFunction.AddFunction(coneFunction);
		booleanFunction.SetTransform(transform);
		return booleanFunction;
	}
	
	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		vtkSphereSource source=new vtkSphereSource();
		source.SetThetaResolution(360);
		source.SetPhiResolution(180);
		source.Update();
		vtkPolyData polyData=source.GetOutput();
		
		Vector3D apex=Vector3D.ZERO;
		Vector3D axis=Vector3D.MINUS_I.add(Vector3D.MINUS_K).add(Vector3D.PLUS_J).normalize();//Vector3D.PLUS_K.add(Vector3D.PLUS_I);
		double fovDeg=60;
		PolyDataClipWithCone clipModifier=new PolyDataClipWithCone(apex, axis, fovDeg);
		vtkPolyData result=clipModifier.apply(polyData);
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(result);
		writer.Write();

		writer.SetFileName("/Users/zimmemi1/Desktop/sphere.vtk");
		writer.SetInputData(polyData);
		writer.Write();

		
	}
}
