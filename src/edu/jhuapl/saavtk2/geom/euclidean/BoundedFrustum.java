package edu.jhuapl.saavtk2.geom.euclidean;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkCellArray;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class BoundedFrustum extends Frustum {

	double depth;

	public BoundedFrustum(double[] origin, double[] ul, double[] ur, double[] ll, double[] lr, double depth) {
		super(origin, ul, ur, ll, lr);
		this.depth = depth;
	}

	public BoundedFrustum(Vector3D origin, Vector3D lookAt, Vector3D up, double fovxDeg, double fovyDeg, double depth) {
		super(origin, lookAt, up, fovxDeg, fovyDeg);
		this.depth = depth;
	}

	public BoundedFrustum(Vector3D origin, Vector3D ul, Vector3D ur, Vector3D ll, Vector3D lr, double depth) {
		super(origin, ul, ur, ll, lr);
		this.depth = depth;
	}
	
	public double getDepth()
	{
		return depth;
	}

	public static vtkPolyData createPolyDataRepresentation(BoundedFrustum frustum)
	{
		vtkPoints points = new vtkPoints();
		int oid = points.InsertNextPoint(frustum.getOrigin().toArray());
		int ulid = points.InsertNextPoint(frustum.getOrigin().add(frustum.getUpperLeftUnit().scalarMultiply(frustum.getDepth())).toArray());
		int urid = points.InsertNextPoint(frustum.getOrigin().add(frustum.getUpperRightUnit().scalarMultiply(frustum.getDepth())).toArray());
		int llid = points.InsertNextPoint(frustum.getOrigin().add(frustum.getLowerLeftUnit().scalarMultiply(frustum.getDepth())).toArray());
		int lrid = points.InsertNextPoint(frustum.getOrigin().add(frustum.getLowerRightUnit().scalarMultiply(frustum.getDepth())).toArray());
		//
		vtkLine ulline = new vtkLine();
		ulline.GetPointIds().SetId(0, oid);
		ulline.GetPointIds().SetId(1, ulid);
		vtkLine urline = new vtkLine();
		urline.GetPointIds().SetId(0, oid);
		urline.GetPointIds().SetId(1, urid);
		vtkLine llline = new vtkLine();
		llline.GetPointIds().SetId(0, oid);
		llline.GetPointIds().SetId(1, llid);
		vtkLine lrline = new vtkLine();
		lrline.GetPointIds().SetId(0, oid);
		lrline.GetPointIds().SetId(1, lrid);
		//
		vtkLine lboxline = new vtkLine();
		lboxline.GetPointIds().SetId(0, llid);
		lboxline.GetPointIds().SetId(1, ulid);
		vtkLine tboxline = new vtkLine();
		tboxline.GetPointIds().SetId(0, ulid);
		tboxline.GetPointIds().SetId(1, urid);
		vtkLine rboxline = new vtkLine();
		rboxline.GetPointIds().SetId(0, urid);
		rboxline.GetPointIds().SetId(1, lrid);
		vtkLine bboxline = new vtkLine();
		bboxline.GetPointIds().SetId(0, lrid);
		bboxline.GetPointIds().SetId(1, llid);
		//
		vtkCellArray cells = new vtkCellArray();
		cells.InsertNextCell(ulline);
		cells.InsertNextCell(urline);
		cells.InsertNextCell(llline);
		cells.InsertNextCell(lrline);
		cells.InsertNextCell(lboxline);
		cells.InsertNextCell(tboxline);
		cells.InsertNextCell(rboxline);
		cells.InsertNextCell(bboxline);
		//
		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetLines(cells);
		return polyData;
	}
}
