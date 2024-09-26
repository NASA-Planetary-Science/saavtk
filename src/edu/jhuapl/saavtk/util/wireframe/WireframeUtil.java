package edu.jhuapl.saavtk.util.wireframe;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkCellArray;
import vtk.vtkCubeSource;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkSphereSource;
import vtk.vtkTransform;
import vtk.vtkTransformFilter;

public class WireframeUtil {

	public static final int DEFAULT_NUM_ANGLES = 100;
	public static final double DEFAULT_RADIUS = 2;//Why not?
	
	public static vtkPolyData buildCylinderFromLine(Point3D a, Point3D b) {
		return buildCylinderFromLine(a, b, DEFAULT_RADIUS);
	}
	
	public static vtkPolyData buildCylinderFromLine(Point3D a, Point3D b, double radius) {
		return buildCylinderFromLine(a, b, DEFAULT_NUM_ANGLES, radius);
	}
	
	public static vtkPolyData buildCylinderFromLine(Point3D a, Point3D b, int numberOfAngles, double radius) {
		vtkPolyData cylinder = new vtkPolyData();
		
		Vector3D aVec = new Vector3D(a.xyz);
		Vector3D bVec = new Vector3D(b.xyz);
		
		Vector3D zVec = bVec.subtract(aVec);
		double zLen = zVec.getNorm();
		if (zLen < 1e-5) {
			return cylinder;
		}
		
		zVec = zVec.normalize();
		
		//(x,y,z)*(xz, 0, -xz) = 0 
		Vector3D xVec = new Vector3D(zVec.getZ(), 0, -zVec.getX());
		if (xVec.getNorm() < 1e-5) {
			//If zVec is (0,1,0) we need to handle it differently.
			xVec = new Vector3D(0, zVec.getZ(), -zVec.getY());
		}
		xVec = xVec.normalize();
				
		Vector3D yVec = zVec.crossProduct(xVec).normalize();
		
		List<Point3D> aPoints = buildCircle3D(a, xVec, yVec, numberOfAngles, radius);
		List<Point3D> bPoints = buildCircle3D(b, xVec, yVec, numberOfAngles, radius);
		
		vtkPoints points = new vtkPoints();
		for (Point3D point3d : aPoints) {
			points.InsertNextPoint(point3d.xyz);
		}
		
		for (Point3D point3d : bPoints) {
			points.InsertNextPoint(point3d.xyz);
		}
		cylinder.SetPoints(points);
		
		vtkCellArray cells = new vtkCellArray();
		for (int i = 0; i < numberOfAngles-1; i++) {
			int p00 = i;
			int p10 = i+1;
			int p01 = i + numberOfAngles;
			int p11 = p01 + 1;
			
			cells.InsertNextCell(listOf(p00, p01, p11));
			cells.InsertNextCell(listOf(p11, p01, p00));
			
			cells.InsertNextCell(listOf(p00, p10, p11));
			cells.InsertNextCell(listOf(p11, p10, p00));
		}
		cylinder.SetPolys(cells);
		
		return cylinder;
	}
	
	public static List<Point3D> buildCircle3D(Point3D center, Vector3D xVec, Vector3D yVec, int numberOfAngles, double radius) {
		List<Point3D> points = new ArrayList<>(numberOfAngles);
		double dTheta = 2*Math.PI/(double)numberOfAngles;
		for (int angle = 0; angle < numberOfAngles; ++angle) {
			double theta = angle * dTheta;
			double x = radius * Math.cos(theta);
			double y = radius * Math.sin(theta);
			
			Vector3D pointVec = new Vector3D(center.xyz);
			pointVec = pointVec.add(xVec.scalarMultiply(x));
			pointVec = pointVec.add(yVec.scalarMultiply(y));
			points.add(new Point3D(pointVec.toArray()));
		}
		return points;
	}
	
	public static vtkPolyData buildSphere(Point3D center, int numberOfAngles, double radius) {
		vtkSphereSource source = new vtkSphereSource();
		source.SetCenter(center.xyz);
		source.SetRadius(radius);
		source.SetPhiResolution(numberOfAngles);
		source.SetThetaResolution(numberOfAngles);
		source.Update();
		return source.GetOutput();
	}
	
	public static vtkPolyData convertPointToCube(Point3D point, double radius) {
	    vtkCubeSource source=new vtkCubeSource();
	    source.SetCenter(point.xyz);
	    source.SetXLength(radius*2);
	    source.SetYLength(radius*2);
	    source.SetZLength(radius*2);
	    source.Update();

	    return source.GetOutput();
	}
	
	public static vtkPolyData convertLineToBox(Point3D point1, Point3D point2, double radius) {
	    Vector3D p1=new Vector3D(point1.xyz);
	    Vector3D p2=new Vector3D(point2.xyz);
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
	        transform.RotateWXYZ(Math.toDegrees(rotation.getAngle()), rotation.getAxis(RotationConvention.VECTOR_OPERATOR).toArray());
	    }
	    transform.Translate(center.toArray());
	    transform.Update();

	    vtkTransformFilter transformFilter=new vtkTransformFilter();
	    transformFilter.SetTransform(transform);
	    transformFilter.SetInputData(source.GetOutput());
	    transformFilter.Update();

	    return transformFilter.GetPolyDataOutput();
	}
	
	private static vtkIdList listOf(int... ids) {
		vtkIdList list = new vtkIdList();
		for (int id : ids) {
			list.InsertNextId(id);
		}
		return list;
	}
}
