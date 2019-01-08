package edu.jhuapl.saavtk2.geom.euclidean;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkCellArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class Line {
	Vector3D p1, p2;

	public Line(Vector3D p1, Vector3D p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	public Vector3D getP1() {
		return p1;
	}

	public Vector3D getP2() {
		return p2;
	}

	public Vector3D getUnit() {
		return p2.subtract(p1).normalize();
	}

	@Override
	public String toString() {
		return getClass().getName() + p1 + "-" + p2;
	}

	public static vtkPolyData createPolyDataRepresentation(Line line) {
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		int id0 = points.InsertNextPoint(line.getP1().toArray());
		int id1 = points.InsertNextPoint(line.getP2().toArray());
		vtkLine lineCell = new vtkLine();
		lineCell.GetPointIds().SetId(0, id0);
		lineCell.GetPointIds().SetId(1, id1);
		cells.InsertNextCell(lineCell);
		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetLines(cells);
		return polyData;
	}

	public static vtkPolyData createPolyDataRepresentation(Collection<Line> lines) {
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		for (Line l : lines) {
			int id0 = points.InsertNextPoint(l.getP1().toArray());
			int id1 = points.InsertNextPoint(l.getP2().toArray());
			vtkLine lineCell = new vtkLine();
			lineCell.GetPointIds().SetId(0, id0);
			lineCell.GetPointIds().SetId(1, id1);
			cells.InsertNextCell(lineCell);
		}
		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetLines(cells);
		return polyData;
	}
}
