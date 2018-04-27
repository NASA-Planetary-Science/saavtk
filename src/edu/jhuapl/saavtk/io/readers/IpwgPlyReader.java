package edu.jhuapl.saavtk.io.readers;

import java.util.ArrayList;

import com.google.common.base.Stopwatch;

import vtk.vtkCellArray;
import vtk.vtkDoubleArray;
import vtk.vtkNativeLibrary;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkPolyVertex;
import vtk.vtkUnstructuredGrid;
import vtk.vtkUnstructuredGridWriter;
import vtk.vtkVertex;

public class IpwgPlyReader extends PlyReader {

	public vtkUnstructuredGrid getOutputAsUnstructuredGrid() {
		// tuples contain "x" "y" "z" "r" "g" "b" where rgb components are uchar
		// format (0-255)
		int npts=data.size();
		vtkPoints points = new vtkPoints();
		vtkUnstructuredGrid grid = new vtkUnstructuredGrid();
		vtkPolyVertex verts = new vtkPolyVertex();
		verts.GetPointIds().SetNumberOfIds(npts);
		points.SetNumberOfPoints(npts);
		vtkDoubleArray colors=new vtkDoubleArray();
		colors.SetNumberOfComponents(3);
		colors.SetNumberOfTuples(npts);
		colors.SetName("rgb");

//		Stopwatch sw=new Stopwatch();
//		sw.start();
		for (int i = 0; i < data.size(); i++) {
//			if (sw.elapsedMillis()>4000)
//			{
//				sw.reset();
//				sw.start();
//				System.out.println(i+"/"+data.size());
//			}
			double[] tuple = data.get(i);
			points.SetPoint(i, tuple[0], tuple[1], tuple[2]);
			verts.GetPointIds().SetId(i, i);
			colors.SetTuple3(i, tuple[3], tuple[4], tuple[5]);
		}
		grid.InsertNextCell(verts.GetCellType(), verts.GetPointIds());
		grid.SetPoints(points);
		grid.GetPointData().AddArray(colors);
		return grid;
	}

	@Deprecated
	@Override
	public vtkPolyData GetOutput() {
		throw new Error("This method is very, very slow for large IPWG PLY-format files. Use getOutputAsUnstructuredGrid method instead.");
/*		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
//		Stopwatch sw=new Stopwatch();
//		sw.start();
		for (int i = 0; i < data.size(); i++) {
//			if (sw.elapsedMillis()>4000)
//			{
//				sw.reset();
//				sw.start();
//				System.out.println(i+"/"+data.size());
//			}
			double[] tuple = data.get(i);
			double[] pos = new double[] { tuple[0], tuple[1], tuple[2] };
			int id = points.InsertNextPoint(pos);
			vtkVertex vert = new vtkVertex();
			vert.GetPointIds().SetId(0, id);
			cells.InsertNextCell(vert);
			pos = null;
		}
		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetVerts(cells);
		return polyData;*/
	}

	public static void main(String[] args) {
		vtkNativeLibrary.LoadAllNativeLibraries();
		IpwgPlyReader reader = new IpwgPlyReader();
		reader.SetFileName("/Users/steelrj1/Documents/PROJECTS/SBMT/ipwg/Eros161_B.ply");
		reader.Update();

		vtkUnstructuredGridWriter writer = new vtkUnstructuredGridWriter();
		vtkUnstructuredGrid grid = reader.getOutputAsUnstructuredGrid();
		writer.SetInputData(grid);
		writer.SetFileName("/Users/steelrj1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.Write();
	}

}
