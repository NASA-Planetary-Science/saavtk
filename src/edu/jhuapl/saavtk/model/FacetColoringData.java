package edu.jhuapl.saavtk.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkFloatArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkTriangle;

public class FacetColoringData
{
	int cellId;
	double area;
	double[] center;
	LatLon llr;
	ImmutableList<ColoringData> allColoringData;

	public FacetColoringData(int cellId, ImmutableList<ColoringData> allColoringData)
	{
		this.cellId = cellId;
		this.allColoringData = allColoringData;
	}

	public String[] getAvailableColoringNames()
	{
		String[] names = new String[allColoringData.size()];
		int i = 0;
		for (ColoringData data : allColoringData)
		{
			names[i++] = data.getName();
		}

		return names;
	}

	public String[] getAvailableColoringNameUnits()
	{
		String[] names = new String[allColoringData.size()];
		int i = 0;
		for (ColoringData data : allColoringData)
		{
			names[i++] = data.getUnits();
		}

		return names;
	}

	public String[] getAvailable1DColoringNames()
	{
		String[] names = new String[get1DColorings().size()];
		int i = 0;
		for (ColoringData data : get1DColorings())
		{
			names[i++] = data.getName();
		}

		return names;
	}

	public String[] getAvailable1DColoringNameUnits()
	{
		String[] names = new String[get1DColorings().size()];
		int i = 0;
		for (ColoringData data : get1DColorings())
		{
			names[i++] = data.getUnits();
		}
		return names;
	}

	private Vector<ColoringData> get1DColorings()
	{
		Vector<ColoringData> oneDColorings = new Vector<ColoringData>();
		for (ColoringData data : allColoringData)
		{
			if (data.getElementNames().size() == 1)
				oneDColorings.add(data);
		}
		return oneDColorings;
	}

	public double[] getColoringValuesFor(String coloringName) throws IOException
	{
		ColoringData data = null;
		for (ColoringData coloring : allColoringData)
		{
			if (coloring.getName().equals(coloringName))
				data = coloring;
		}
		if (data == null)
			return null; //really should throw an exception here
		data.load();
		vtkFloatArray array = data.getData();
		int number = data.getElementNames().size();
		double[] vals = new double[number];
		switch (number)
		{
		case 1:
			vals = new double[] { array.GetTuple1(cellId) };
			break;
		case 2:
			vals = array.GetTuple2(cellId);
			break;
		case 3:
			vals = array.GetTuple3(cellId);
			break;
		case 4:
			vals = array.GetTuple4(cellId);
			break;
		case 6:
			vals = array.GetTuple6(cellId);
			break;
		case 9:
			vals = array.GetTuple9(cellId);
			break;
		default:
			throw new AssertionError();
		}
		return vals;
	}

	public void generateDataFromPolydata(vtkPolyData smallBodyPolyData)
	{
		vtkTriangle triangle = new vtkTriangle();

		vtkPoints points = smallBodyPolyData.GetPoints();
		int numberCells = smallBodyPolyData.GetNumberOfCells();
		smallBodyPolyData.BuildCells();
		vtkIdList idList = new vtkIdList();
		double[] pt0 = new double[3];
		double[] pt1 = new double[3];
		double[] pt2 = new double[3];
		center = new double[3];

		if (cellId < 0 || cellId > numberCells)
		{
			throw new IllegalArgumentException();
		}
		smallBodyPolyData.GetCellPoints(cellId, idList);
		int id0 = idList.GetId(0);
		int id1 = idList.GetId(1);
		int id2 = idList.GetId(2);
		points.GetPoint(id0, pt0);
		points.GetPoint(id1, pt1);
		points.GetPoint(id2, pt2);

		area = triangle.TriangleArea(pt0, pt1, pt2);
		triangle.TriangleCenter(pt0, pt1, pt2, center);
		llr = MathUtil.reclat(center);

		triangle.Delete();
		idList.Delete();
	}

	public void writeTo(BufferedWriter out) throws IOException
	{
		out.write(cellId + ",");
		out.write(area + ",");
		out.write(center[0] + ",");
		out.write(center[1] + ",");
		out.write(center[2] + ",");
		out.write((llr.lat * 180.0 / Math.PI) + ",");
		out.write((llr.lon * 180.0 / Math.PI) + ",");
		out.write(String.valueOf(llr.rad));
		for (ColoringData coloring : allColoringData)
		{
			double[] dArray = getColoringValuesFor(coloring.getName());
			for (double d : dArray)
			{
				out.write("," + d);
			}
		}
	}

}
