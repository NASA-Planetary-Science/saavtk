package edu.jhuapl.saavtk2.polydata.clip;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.geom.euclidean.Frustum;
import vtk.vtkDoubleArray;
import vtk.vtkImplicitFunction;
import vtk.vtkNativeLibrary;
import vtk.vtkPlanes;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;

public class PolyDataClipWithFrustum extends PolyDataClip
{

	public PolyDataClipWithFrustum(Frustum frustum)
	{
		super(generateClipFunction(frustum));
	}
	
	protected static vtkImplicitFunction generateClipFunction(Frustum frustum)
	{
		vtkPoints origins=new vtkPoints();
		origins.InsertNextPoint(frustum.getOrigin().toArray());
		origins.InsertNextPoint(frustum.getOrigin().toArray());
		origins.InsertNextPoint(frustum.getOrigin().toArray());
		origins.InsertNextPoint(frustum.getOrigin().toArray());
		//
		vtkDoubleArray normals=new vtkDoubleArray();
		normals.SetNumberOfComponents(3);
		Vector3D normalTop=frustum.getUpperRightUnit().crossProduct(frustum.getUpperLeftUnit());
		Vector3D normalRgt=frustum.getLowerRightUnit().crossProduct(frustum.getUpperRightUnit());
		Vector3D normalBot=frustum.getLowerLeftUnit().crossProduct(frustum.getLowerRightUnit());
		Vector3D normalLft=frustum.getUpperLeftUnit().crossProduct(frustum.getLowerLeftUnit());
		normals.InsertNextTuple3(normalTop.getX(), normalTop.getY(), normalTop.getZ());
		normals.InsertNextTuple3(normalRgt.getX(), normalRgt.getY(), normalRgt.getZ());
		normals.InsertNextTuple3(normalBot.getX(), normalBot.getY(), normalBot.getZ());
		normals.InsertNextTuple3(normalLft.getX(), normalLft.getY(), normalLft.getZ());
		//
		vtkPlanes planes=new vtkPlanes();
		planes.SetNormals(normals);
		planes.SetPoints(origins);
		return planes;
	}
	
	@Override
	public void setInsideOut(boolean insideOut)
	{
		clipFilter.SetInsideOut(insideOut?0:1); // invert value so default is to keep what's inside the frustum
	}

	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		vtkSphereSource source=new vtkSphereSource();
		source.Update();
		vtkPolyData polyData=source.GetOutput();
		
		Vector3D origin=Vector3D.PLUS_K;
		Vector3D lookAt=Vector3D.ZERO;
		Vector3D up=Vector3D.PLUS_J;
		double fov=10;
		Frustum frustum=new Frustum(origin, lookAt, up, fov, fov);
		
		PolyDataClipWithFrustum clipFunction=new PolyDataClipWithFrustum(frustum);
		vtkPolyData result=clipFunction.apply(polyData);
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(result);
		writer.Write();

	}
	
}
