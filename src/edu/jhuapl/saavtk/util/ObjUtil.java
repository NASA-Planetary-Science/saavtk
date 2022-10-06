package edu.jhuapl.saavtk.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkAppendPolyData;
import vtk.vtkCell;
import vtk.vtkCubeSource;
import vtk.vtkDoubleArray;
import vtk.vtkFloatArray;
import vtk.vtkIdList;
import vtk.vtkImageData;
import vtk.vtkLine;
import vtk.vtkPNGWriter;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import vtk.vtkPolyLine;
import vtk.vtkTransform;
import vtk.vtkTransformFilter;
import vtk.vtkTriangleFilter;
import vtk.vtkUnsignedCharArray;

public class ObjUtil
{

    public static void writePolyDataToObj(vtkPolyData rawPolyData, Path objFile)
    {
        //System.out.print("Writing to "+objFile+"... ");
        writePolyDataToObj(rawPolyData, objFile, 0, 0, null, null, null);
        //System.out.println("Do1ne.");
    }
    
    public static void writePolyDataToObj(vtkPolyData rawPolyData, Path objFile, String header)
    {
        //System.out.print("Writing to "+objFile+"... ");
        writePolyDataToObj(rawPolyData, objFile, 0, 0, null, null, header);
        //System.out.println("Do1ne.");
    }

    public static void writePolyDataToObj(vtkPolyData rawPolyData, vtkImageData imageData, Path objFile, String header)
    {
    	String mtlFileName=objFile.toString().replace(".obj",".mtl");
    	String mtlName=objFile.getFileName().toString().replaceAll(".obj", "");
        writeImageToMtl(imageData,mtlFileName,mtlName);
        writePolyDataToObj(rawPolyData, objFile, 0, 0, mtlFileName, mtlName, header);
    }
    
    public static void writeImageToMtl(vtkImageData imageData, String mtlFileName, String mtlName)
    {
    	String imageFileName=mtlFileName+".png";
    	
    	vtkPNGWriter pngWriter=new vtkPNGWriter();
    	pngWriter.SetInputData(imageData);
    	pngWriter.SetFileName(imageFileName);
    	pngWriter.Write();
    	
		try
		{
			BufferedWriter writer=new BufferedWriter(new FileWriter(mtlFileName));
			writer.write("newmtl "+mtlName+"\n");
			writer.write("  d 1.000 \n");	// opacity
			writer.write("  Ka 1.000 1.000 1.000 \n");	// ambient color
			writer.write("  Kd 1.000 1.000 1.000 \n");	// diffuse color
			writer.write("  Ks 1.000 1.000 1.000 \n");	// diffuse color
			writer.write("  map_Ka "+imageFileName+"\n");	// specify ambient texture 
			writer.write("  map_Kd "+imageFileName+"\n");	// specify diffuse texture
			writer.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static String pointDataColorArrayName="PointColors";
    
	public static void writePolyDataToObj(vtkPolyData rawPolyData, Path objFile, double pointRadius, double lineRadius, String mtlFileName, String mtlName, String header)
	{
	    vtkTriangleFilter triangleFilter=new vtkTriangleFilter();
	    triangleFilter.SetInputData(rawPolyData);
	    triangleFilter.Update();
	    
	    vtkPolyDataNormals normalFilter=new vtkPolyDataNormals();
	    normalFilter.SetInputData(triangleFilter.GetOutput());
	    normalFilter.ComputePointNormalsOn();
	    normalFilter.ComputeCellNormalsOff();
	    normalFilter.Update();
	    
	    vtkPolyData polyData=normalFilter.GetOutput();
	    vtkUnsignedCharArray pointColors=(vtkUnsignedCharArray)polyData.GetPointData().GetArray(pointDataColorArrayName);
	    vtkFloatArray pointNormals=(vtkFloatArray)polyData.GetPointData().GetArray("Normals");
	    
	    try
		{
			BufferedWriter writer=new BufferedWriter(new FileWriter(objFile.toString()));
			
			// write header and material information
			if (header!=null)
				writer.write("# "+header+"\n");
			if (mtlFileName != null)
			{
				writer.write("mtllib "+mtlFileName+"\n");
				writer.write("usemtl "+mtlName+"\n");
			}
			// write vertices with optional color
			for (int i=0; i<polyData.GetNumberOfPoints(); i++)
			{
			    double[] pt=polyData.GetPoint(i);
			    if (pointColors==null)
			    	writer.write("v "+pt[0]+" "+pt[1]+" "+pt[2]+"\n");
			    else
			    {
			    	double[] rgb=pointColors.GetTuple3(i);
			    	double r=rgb[0]/255.;
			    	double g=rgb[1]/255.;
			    	double b=rgb[2]/255.;
			    	writer.write("v "+pt[0]+" "+pt[1]+" "+pt[2]+" "+r+" "+g+" "+b+"\n");
			    }
			}
			// write vertex normals
			if (pointNormals!=null)
			{
				for (int i=0; i<polyData.GetNumberOfPoints(); i++)
				{
					double[] nml=pointNormals.GetTuple3(i);
					writer.write("vn "+nml[0]+" "+nml[1]+" "+nml[2]+"\n");
				}
			}
			vtkFloatArray texCoords=(vtkFloatArray)polyData.GetPointData().GetTCoords();
			if (texCoords!=null)
			{
				for (int i=0; i<polyData.GetNumberOfPoints(); i++)
				{
					double[] uv=texCoords.GetTuple2(i);
					writer.write("vt "+uv[0]+" "+uv[1]+"\n");
				}
			}
			for (int i=0; i<polyData.GetNumberOfCells(); i++)
			{
				vtkCell cell=polyData.GetCell(i);
				if (cell.GetNumberOfPoints()==1)
					writer.write("p ");
				else if (cell.GetNumberOfPoints()==2)
					writer.write("l ");
				else 
					writer.write("f ");
				for (int j=0; j<cell.GetNumberOfPoints(); j++)
				{
					vtkIdList ids=polyData.GetCell(i).GetPointIds();
					int id=(int)ids.GetId(j)+1;
					if (cell.GetNumberOfPoints()<3)
						writer.write(id+" ");
					else
					{
						int nmlIdx=id;
						int texCoordIdx=id;
						writer.write(id+"/"+texCoordIdx+"/"+nmlIdx+" ");
					}
				}
				writer.write("\n");
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
	    	switch ((int)cell.GetNumberOfPoints())
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
