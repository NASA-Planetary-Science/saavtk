package edu.jhuapl.saavtk2.polydata.clip;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.polydata.transform.PolyDataTranslate;
import vtk.vtkCubeSource;
import vtk.vtkImplicitFunction;
import vtk.vtkImplicitPolyDataDistance;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;

public class PolyDataClipWithPolyData extends PolyDataClip
{

	public PolyDataClipWithPolyData(vtkPolyData clipper)
	{
		super(generateClipFunction(clipper));
	}
	
	protected static vtkImplicitFunction generateClipFunction(vtkPolyData clipper)
	{
		vtkImplicitPolyDataDistance clipFunction=new vtkImplicitPolyDataDistance();
		clipFunction.SetInput(clipper);
		return clipFunction;
	}
	
	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		vtkSphereSource sphereSource=new vtkSphereSource();
		sphereSource.Update();
		vtkPolyData sphere=sphereSource.GetOutput();
		
		PolyDataTranslate translateModifier=new PolyDataTranslate(Vector3D.PLUS_I.scalarMultiply(0.25));
		sphere=translateModifier.apply(sphere);
		
		vtkCubeSource cubeSource=new vtkCubeSource();
		cubeSource.Update();
		vtkPolyData cube=cubeSource.GetOutput();
		
		PolyDataClipWithPolyData clipModifier=new PolyDataClipWithPolyData(cube);
		vtkPolyData result=clipModifier.apply(sphere);
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(result);
		writer.Write();
		
		writer.SetFileName("/Users/zimmemi1/Desktop/test1.vtk");
		writer.SetInputData(cube);
		writer.Write();

		writer.SetFileName("/Users/zimmemi1/Desktop/test2.vtk");
		writer.SetInputData(sphere);
		writer.Write();
	}

}
