package edu.jhuapl.saavtk2.polydata;

import vtk.vtkDoubleArray;
import vtk.vtkPolyData;
import vtk.vtkTriangle;

public class PolyDataAddCellCenters implements PolyDataModifier
{

	public static boolean hasCenters(vtkPolyData polyData)
	{
		return polyData.GetCellData().HasArray(centersArrayName)==1;
	}
	
	public static final String centersArrayName="centers";
	
	@Override
	public vtkPolyData apply(vtkPolyData polyData)
	{
		if (hasCenters(polyData))
			polyData.GetCellData().RemoveArray(centersArrayName);
		
		vtkDoubleArray centers=new vtkDoubleArray();
		centers.SetNumberOfComponents(3);
		centers.SetName(centersArrayName);
		centers.SetNumberOfTuples(polyData.GetNumberOfCells());
		for (int i=0; i<polyData.GetNumberOfCells(); i++)
		{
			vtkTriangle tri=(vtkTriangle)polyData.GetCell(i);
			double[] ctr=new double[3];
			tri.TriangleCenter(tri.GetPoints().GetPoint(0), tri.GetPoints().GetPoint(1), tri.GetPoints().GetPoint(2), ctr);
			centers.SetTuple3(i, ctr[0], ctr[1], ctr[2]);
		}
		polyData.GetCellData().AddArray(centers);
		
		return polyData;
	}

}
