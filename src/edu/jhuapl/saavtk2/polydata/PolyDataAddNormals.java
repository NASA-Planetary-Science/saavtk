package edu.jhuapl.saavtk2.polydata;

import vtk.vtkDoubleArray;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;

public class PolyDataAddNormals implements PolyDataModifier
{
	
	public static boolean hasCellNormals(vtkPolyData polyData)
	{
		return polyData.GetCellData().GetNormals()!=null;
	}
	
	public static boolean hasPointNormals(vtkPolyData polyData)
	{
		return polyData.GetPointData().GetNormals()!=null;
	}

	boolean computeCellNormals;
	boolean computePointNormals;
	
	public static final String normalsArrayName="normals";
	
	public PolyDataAddNormals()
	{
		this(true,true);
	}
	
	public PolyDataAddNormals(boolean computeCellNormals, boolean computePointNormals)
	{
		this.computeCellNormals=computeCellNormals;
		this.computePointNormals=computePointNormals;
	}

	@Override
	public vtkPolyData apply(vtkPolyData polyData)
	{
		vtkPolyDataNormals normals=new vtkPolyDataNormals();
		normals.SetInputData(polyData);
		if (computeCellNormals)
			normals.ComputeCellNormalsOn();
		if (computePointNormals)
			normals.ComputePointNormalsOn();
		normals.Update();
		
		if (computeCellNormals)
		{
			vtkDoubleArray cellNormals=new vtkDoubleArray();
			cellNormals.DeepCopy(normals.GetOutput().GetCellData().GetNormals());
			if (hasCellNormals(polyData))
				polyData.GetCellData().RemoveArray(normalsArrayName);
			polyData.GetCellData().SetNormals(cellNormals);
		}
		
		if (computePointNormals)
		{
			vtkDoubleArray pointNormals=new vtkDoubleArray();
			pointNormals.DeepCopy(normals.GetOutput().GetPointData().GetNormals());
			if (hasPointNormals(polyData))
				polyData.GetPointData().RemoveArray(normalsArrayName);
			polyData.GetPointData().SetNormals(pointNormals);
		}
		
		return polyData;
	}

}
