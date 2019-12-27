package edu.jhuapl.saavtk2.polydata.clip;

import vtk.vtkDoubleArray;
import vtk.vtkImplicitFunction;
import vtk.vtkNativeLibrary;
import vtk.vtkPlanes;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;

public class PolyDataClipWithBox extends PolyDataClip
{

    public PolyDataClipWithBox(double[] bounds)
    {
        super(generateClipFunction(bounds));
        setInsideOut(false);
    }

    @Override
    public void setInsideOut(boolean insideOut)
    {
        super.setInsideOut(!insideOut);// flip sense of user-specified inside-out to make sure polydata inside the
                                       // clipping surface is kept, if insideOut is false
    }

    protected static vtkImplicitFunction generateClipFunction(double[] bounds)
    {
        double xmin = bounds[0];
        double xmax = bounds[1];
        double ymin = bounds[2];
        double ymax = bounds[3];
        double zmin = bounds[4];
        double zmax = bounds[5];
        double xmid = (xmax + xmin) / 2;
        double ymid = (ymax + ymin) / 2;
        double zmid = (zmax + zmin) / 2;

        vtkPoints origins = new vtkPoints();
        origins.InsertNextPoint(xmin, ymid, zmid);
        origins.InsertNextPoint(xmax, ymid, zmid);
        origins.InsertNextPoint(xmid, ymin, zmid);
        origins.InsertNextPoint(xmid, ymax, zmid);
        origins.InsertNextPoint(xmid, ymid, zmin);
        origins.InsertNextPoint(xmid, ymid, zmax);
        //
        vtkDoubleArray normals = new vtkDoubleArray();
        normals.SetNumberOfComponents(3);
        normals.InsertNextTuple3(-1, 0, 0);
        normals.InsertNextTuple3(1, 0, 0);
        normals.InsertNextTuple3(0, -1, 0);
        normals.InsertNextTuple3(0, 1, 0);
        // normals.InsertNextTuple3(0,0,-1);
        // normals.InsertNextTuple3(0,0,1);
        //
        vtkPlanes planes = new vtkPlanes();
        planes.SetNormals(normals);
        planes.SetPoints(origins);
        return planes;
    }

    public static void main(String[] args)
    {
        vtkNativeLibrary.LoadAllNativeLibraries();

        vtkSphereSource source = new vtkSphereSource();
        source.Update();
        vtkPolyData polyData = source.GetOutput();

        double[] bounds = new double[] { -0.25, 0.25, -0.25, 0.25, -0.25, 0.25 };
        PolyDataClip clipFunction = new PolyDataClipWithBox(bounds);
        vtkPolyData result = clipFunction.apply(polyData);

        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
        writer.SetFileTypeToBinary();
        writer.SetInputData(result);
        writer.Write();

    }
}
