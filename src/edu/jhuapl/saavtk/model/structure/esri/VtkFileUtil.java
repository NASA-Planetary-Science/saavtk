package edu.jhuapl.saavtk.model.structure.esri;

import java.nio.file.Path;

import vtk.vtkAppendPolyData;
import vtk.vtkCellArray;
import vtk.vtkFloatArray;
import vtk.vtkIdTypeArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkVertex;
import java.util.List;

import org.ietf.jgss.Oid;

import edu.jhuapl.saavtk.util.ColorUtil;

public final class VtkFileUtil {

	public static void writeLineStructures(List<LineStructure> lss, Path vtkFile) {
		String filename = vtkFile.toString();
		if (!filename.endsWith(".vtk"))
			filename += ".vtk";

		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (int m = 0; m < lss.size(); m++) {
			LineStructure ls = lss.get(m);
			int structureId = m;
			float[] c = ColorUtil.getRGBColorComponents(ls.getStyle().getLineColor());

			vtkFloatArray colors = new vtkFloatArray();
			colors.SetNumberOfComponents(3);
			colors.SetName("colors");
			vtkIdTypeArray ids = new vtkIdTypeArray();
			ids.SetName("structure ids");

			vtkPoints points = new vtkPoints();
			vtkPolyData polyData = new vtkPolyData();
			polyData.SetPoints(points);
			polyData.GetCellData().AddArray(colors);
			polyData.GetCellData().AddArray(ids);
			vtkCellArray lines = new vtkCellArray();
			polyData.SetLines(lines);

			for (int i = 0; i < ls.getNumberOfSegments(); i++) {
				LineSegment s = ls.getSegment(i);
				int id1 = points.InsertNextPoint(s.start);
				int id2 = points.InsertNextPoint(s.end);
				vtkLine line = new vtkLine();
				line.GetPointIds().SetId(0, id1);
				line.GetPointIds().SetId(1, id2);
				lines.InsertNextCell(line);
				ids.InsertNextValue(structureId);
				colors.InsertNextTuple3(c[0], c[1], c[2]);
			}

			appendFilter.AddInputData(polyData);

		}

		appendFilter.Update();
		vtkPolyDataWriter writer = new vtkPolyDataWriter();
		writer.SetFileName(vtkFile.toString());
		writer.SetFileTypeToBinary();
		writer.SetInputData(appendFilter.GetOutput());
		writer.Write();

		appendFilter = new vtkAppendPolyData();
		for (int m = 0; m < lss.size(); m++) {
			LineStructure ls = lss.get(m);
			int structureId = m;
			float[] c = ColorUtil.getRGBColorComponents(ls.getStyle().getLineColor());

			vtkFloatArray colors = new vtkFloatArray();
			colors.SetNumberOfComponents(3);
			colors.SetName("colors");
			vtkIdTypeArray ids = new vtkIdTypeArray();
			ids.SetName("structure ids");

			vtkPoints points = new vtkPoints();
			vtkPolyData polyData = new vtkPolyData();
			polyData.SetPoints(points);
			polyData.GetCellData().AddArray(colors);
			polyData.GetCellData().AddArray(ids);
			vtkCellArray verts = new vtkCellArray();
			polyData.SetVerts(verts);

			for (int i = 0; i < ls.getNumberOfControlPoints(); i++) {
				int id = points.InsertNextPoint(ls.getControlPoint(i).toArray());
				vtkVertex vert = new vtkVertex();
				vert.GetPointIds().SetId(0, id);
				verts.InsertNextCell(vert);
				ids.InsertNextValue(structureId);
				colors.InsertNextTuple3(c[0], c[1], c[2]);
			}

			appendFilter.AddInputData(polyData);
		}

		appendFilter.Update();
		writer = new vtkPolyDataWriter();
		writer.SetFileName(filename.replace(".vtk", ".ctrlpts.vtk"));
		writer.SetFileTypeToBinary();
		writer.SetInputData(appendFilter.GetOutput());
		writer.Write();

	}

	public static void writePointStructures(List<PointStructure> lss, Path vtkFile) {
		String filename = vtkFile.toString();
		if (!filename.endsWith(".vtk"))
			filename += ".vtk";

		vtkPoints points = new vtkPoints();
		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		vtkFloatArray colors = new vtkFloatArray();
		colors.SetNumberOfComponents(3);
		colors.SetName("colors");
		polyData.GetCellData().AddArray(colors);
		vtkIdTypeArray ids = new vtkIdTypeArray();
		ids.SetName("structure ids");
		polyData.GetCellData().AddArray(ids);
		vtkCellArray verts = new vtkCellArray();
		polyData.SetVerts(verts);

		for (int m = 0; m < lss.size(); m++) {
			PointStructure ls = lss.get(m);
			int structureId = m;
			float[] c = ColorUtil.getRGBColorComponents(ls.getPointStyle().getColor());
			int id = points.InsertNextPoint(lss.get(m).location);
			vtkVertex vert = new vtkVertex();
			vert.GetPointIds().SetId(0, id);
			verts.InsertNextCell(vert);
			ids.InsertNextValue(structureId);
			colors.InsertNextTuple3(c[0], c[1], c[2]);
		}

		vtkPolyDataWriter writer = new vtkPolyDataWriter();
		writer.SetFileName(filename);
		writer.SetFileTypeToBinary();
		writer.SetInputData(polyData);
		writer.Write();

	}

}
