package edu.jhuapl.saavtk2.polydata.clip;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkImplicitFunction;
import vtk.vtkNativeLibrary;
import vtk.vtkPlane;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;

public class PolyDataClipWithPlane extends PolyDataClip
{

    public PolyDataClipWithPlane(Vector3D origin, Vector3D normal)
    {
        super(createClipFunction(new Plane(origin, normal)));
    }

    public PolyDataClipWithPlane(Plane plane)
    {
        super(createClipFunction(plane));
    }

    public static vtkImplicitFunction createClipFunction(Plane plane)
    {
        vtkPlane planeFunction = new vtkPlane();
        planeFunction.SetOrigin(plane.getOrigin().toArray());
        planeFunction.SetNormal(plane.getNormal().toArray());
        return planeFunction;
    }

    public static void main(String[] args)
    {
        vtkNativeLibrary.LoadAllNativeLibraries();

        vtkSphereSource source = new vtkSphereSource();
        source.Update();
        vtkPolyData polyData = source.GetOutput();

        PolyDataClipWithPlane clipModifier = new PolyDataClipWithPlane(Vector3D.ZERO, Vector3D.PLUS_K.add(Vector3D.PLUS_I));
        clipModifier.setInsideOut(true);
        vtkPolyData result = clipModifier.apply(polyData);

        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
        writer.SetFileTypeToBinary();
        writer.SetInputData(result);
        writer.Write();
    }

}
