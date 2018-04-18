package edu.jhuapl.saavtk.io.readers;

import com.google.common.base.Stopwatch;

import vtk.vtkCellArray;
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
		vtkPoints points = new vtkPoints();
		vtkUnstructuredGrid grid = new vtkUnstructuredGrid();
		vtkPolyVertex verts = new vtkPolyVertex();
		for (int i = 0; i < data.size(); i++) {
			double[] tuple = data.get(i);
			double[] pos = new double[] { tuple[0], tuple[1], tuple[2] };
			int id = points.InsertNextPoint(pos);
			verts.GetPointIds().InsertNextId(id);
			pos = null;
		}
		grid.InsertNextCell(verts.GetCellType(), verts.GetPointIds());
		grid.SetPoints(points);
		return grid;
	}

	@Override
	public vtkPolyData GetOutput() {
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		for (int i = 0; i < data.size(); i++) {
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
		return polyData;
	}

	public static void main(String[] args) {
		vtkNativeLibrary.LoadAllNativeLibraries();
		boolean usePolyData = true;
		Stopwatch sw = new Stopwatch();
		sw.start();
		IpwgPlyReader reader = new IpwgPlyReader();
		reader.SetFileName("/Users/zimmemi1/sbmt/spoc/ipwg/Eros161_B.ply");
		reader.Update();
		System.out.println(sw.elapsedMillis());

		if (usePolyData) {
			vtkPolyDataWriter writer = new vtkPolyDataWriter();
			writer.SetInputData(reader.GetOutput());
			writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
			writer.SetFileTypeToBinary();
			writer.Write();
		} else {
			vtkUnstructuredGridWriter writer = new vtkUnstructuredGridWriter();
			writer.SetInputData(reader.GetOutput());
			writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
			writer.SetFileTypeToBinary();
			writer.Write();
		}
	}

}
