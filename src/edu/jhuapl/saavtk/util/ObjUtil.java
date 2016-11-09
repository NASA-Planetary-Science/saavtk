package edu.jhuapl.saavtk.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkAppendPolyData;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkCubeSource;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkTransform;
import vtk.vtkTransformFilter;
import vtk.vtkTriangle;
import vtk.vtkTriangleFilter;

public class ObjUtil
{
    public static void writePolyDataToObj(vtkPolyData rawPolyData, Path objFile)
    {
        //System.out.print("Writing to "+objFile+"... ");
        writePolyDataToObj(rawPolyData, objFile, 0, 0);
        //System.out.println("Done.");
    }
    
	public static void writePolyDataToObj(vtkPolyData rawPolyData, Path objFile, double pointRadius, double lineRadius)
	{
	    vtkTriangleFilter triangleFilter=new vtkTriangleFilter();
	    triangleFilter.SetInputData(rawPolyData);
	    triangleFilter.Update();
	    vtkPolyData polyData=triangleFilter.GetOutput();
        
	    try
		{
			BufferedWriter writer=new BufferedWriter(new FileWriter(objFile.toString()));
			for (int i=0; i<polyData.GetNumberOfPoints(); i++)
			{
			    double[] pt=polyData.GetPoint(i);
			    writer.write("v "+pt[0]+" "+pt[1]+" "+pt[2]+"\n");
			}
			for (int i=0; i<polyData.GetNumberOfCells(); i++)
			{
			    vtkIdList ids=polyData.GetCell(i).GetPointIds();
                int id0=ids.GetId(0)+1;
                int id1=ids.GetId(1)+1;
                int id2=ids.GetId(2)+1;
                writer.write("f "+id0+" "+id1+" "+id2+"\n");
			}
			writer.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static vtkPolyData convertToWireframe(vtkPolyData rawPolyData, double wireSize)
	{
	    vtkAppendPolyData appendFilter=new vtkAppendPolyData();

	    for (int i=0; i<rawPolyData.GetNumberOfCells(); i++)
	    {
	    	vtkCell cell=rawPolyData.GetCell(i);
	    	switch (cell.GetNumberOfPoints())
	    	{
	    	case 1:
                appendFilter.AddInputData(convertPointToCube(cell.GetPoints().GetPoint(0), wireSize));
	    	    break;
	    	case 2:
                appendFilter.AddInputData(convertLineToBox(cell.GetPoints().GetPoint(0), cell.GetPoints().GetPoint(1), wireSize));
	    	    break;
	    	default:
	    	    for (int j=0; j<cell.GetNumberOfPoints(); j++)
	    	    {
	    	        int jp=j+1;
	    	        if (jp==cell.GetNumberOfPoints())
	    	            jp=0;
                    appendFilter.AddInputData(convertLineToBox(cell.GetPoints().GetPoint(j), cell.GetPoints().GetPoint(jp), wireSize));
	    	    }
	    	    break;
	    	}
	    }
	    appendFilter.Update();
	    
	    return appendFilter.GetOutput();
	}
	
	private static vtkPolyData convertPointToCube(double[] point, double radius)
	{
	    vtkCubeSource source=new vtkCubeSource();
	    source.SetCenter(point);
	    source.SetXLength(radius*2);
	    source.SetYLength(radius*2);
	    source.SetZLength(radius*2);
	    source.Update();

	    return source.GetOutput();
	}
	
	private static vtkPolyData convertLineToBox(double[] point1, double[] point2, double radius)
	{
	    Vector3D p1=new Vector3D(point1);
	    Vector3D p2=new Vector3D(point2);
	    Vector3D line=p2.subtract(p1);
	    Vector3D center=p2.add(p1).scalarMultiply(1./2.);
	    vtkCubeSource source=new vtkCubeSource();
	    source.SetXLength(line.getNorm());
	    source.SetYLength(radius*2);
	    source.SetZLength(radius*2);
	    source.Update();
	    
        vtkTransform transform=new vtkTransform();
        transform.PostMultiply();
	    if (line.getNorm()!=0)
	    {
	        Rotation rotation=new Rotation(Vector3D.PLUS_I, line.normalize());
	        transform.RotateWXYZ(Math.toDegrees(rotation.getAngle()), rotation.getAxis().toArray());
	    }
	    transform.Translate(center.toArray());
	    transform.Update();

	    vtkTransformFilter transformFilter=new vtkTransformFilter();
	    transformFilter.SetTransform(transform);
	    transformFilter.SetInputData(source.GetOutput());
	    transformFilter.Update();

	    return transformFilter.GetPolyDataOutput();
	}
	
    
}
