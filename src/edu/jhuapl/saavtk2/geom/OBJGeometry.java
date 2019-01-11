package edu.jhuapl.saavtk2.geom;

import java.nio.file.Path;

import vtk.vtkOBJReader;
import vtk.vtkPolyData;

public class OBJGeometry extends BasicGeometry
{
    public OBJGeometry(Path file)
    {
        super(generatePolyDataRepresentation(file));
    }
    
    public static vtkPolyData generatePolyDataRepresentation(Path objFile)
    {
    	vtkOBJReader reader=new vtkOBJReader();
    	reader.SetFileName(objFile.toString());
    	reader.Update();
    	return reader.GetOutput();
    }


}
