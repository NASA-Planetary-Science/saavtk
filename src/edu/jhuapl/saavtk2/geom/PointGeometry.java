package edu.jhuapl.saavtk2.geom;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkVertex;

public class PointGeometry extends BasicGeometry
{

	public PointGeometry(Vector3D pt)
	{
		super(createPolyDataRepresentation(pt));
	}
	
	public PointGeometry(Collection<Vector3D> pts)
	{
		super(createPolyDataRepresentation(pts));
	}

	public static vtkPolyData createPolyDataRepresentation(Vector3D pt)
	{
		vtkPoints points=new vtkPoints();
		vtkCellArray cells=new vtkCellArray();
		int id=(int)points.InsertNextPoint(pt.toArray());
		vtkVertex vert=new vtkVertex();
		vert.GetPointIds().SetId(0, id);
		cells.InsertNextCell(vert);
		vtkPolyData polyData=new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetVerts(cells);
		return polyData;
	}

	public static vtkPolyData createPolyDataRepresentation(Collection<Vector3D> pts)
	{
		vtkPoints points=new vtkPoints();
		vtkCellArray cells=new vtkCellArray();
		for (Vector3D v : pts)
		{
			int id0=(int)points.InsertNextPoint(v.toArray());
			vtkVertex vert=new vtkVertex();
			vert.GetPointIds().SetId(0, id0);
			cells.InsertNextCell(vert);
			
		}
		vtkPolyData polyData=new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetVerts(cells);
		return polyData;
	}

}
