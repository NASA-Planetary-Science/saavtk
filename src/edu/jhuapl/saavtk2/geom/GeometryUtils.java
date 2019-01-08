package edu.jhuapl.saavtk2.geom;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import vtk.vtkCubeSource;
import vtk.vtkOBBTree;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class GeometryUtils
{
	public static double[] compareBounds(double[] b1, double[] b2)
	{
		double[] bnew = new double[6];
		for (int i = 0; i < 6; i += 2)
			if (b1[i] < b2[i])
				bnew[i] = b1[i];
			else
				bnew[i] = b2[i];
		for (int i = 1; i < 6; i += 2)
			if (b1[i] > b2[i])
				bnew[i] = b1[i];
			else
				bnew[i] = b2[i];
		return bnew;
	}

	public static double[] createInitialBounds()
	{
		double[] bounds=new double[6];
		for (int i=0; i<6; i+=2)
		bounds[i]=Double.POSITIVE_INFINITY;
		for (int i=1; i<6; i+=2)
		bounds[i]=Double.NEGATIVE_INFINITY;
		return bounds;
	}

	public static double[] computeBounds(Collection<BasicGeometry> geometry)
	{
		double[] bounds=createInitialBounds();
		for (BasicGeometry geom : geometry)
			bounds=compareBounds(bounds, geom.getPolyData().GetBounds());	// TODO: this is just using polydata bounds, not actor bounds in a scene
		return bounds;
	}

	public static double getBoundsDiagonalLength(double[] bounds)
	{
		double dx=bounds[1]-bounds[0];
		double dy=bounds[3]-bounds[2];
		double dz=bounds[5]-bounds[4];
		return new Vector3D(dx,dy,dz).getNorm();
	}

	public static List<Vector3D> computeIntersectionsWithBounds(double[] bounds, Line line)
	{
		vtkCubeSource source=new vtkCubeSource();
		source.SetBounds(bounds);
		source.Update();

		vtkOBBTree tree=new vtkOBBTree();
		tree.SetDataSet(source.GetOutput());
		tree.SetTolerance(1e-15);
		tree.BuildLocator();

    	vtkPoints points=new vtkPoints();
    	tree.IntersectWithLine(line.pointAt(0).toArray(), line.pointAt(1).toArray(), points, null);
    	List<Vector3D> hits=Lists.newArrayList();
    	for (int i=0; i<points.GetNumberOfPoints(); i++)
    		hits.add(new Vector3D(points.GetPoint(i)));
    	return hits;

	}
	
	public static List<Vector3D> getPointsAsList(vtkPolyData polyData)
	{
		return getPointsAsList(polyData.GetPoints());
	}

	public static List<Vector3D> getPointsAsList(vtkPoints points)
	{
		List<Vector3D> pointList=Lists.newArrayList();
		for (int i=0; i<points.GetNumberOfPoints(); i++)
		{
			pointList.add(new Vector3D(points.GetPoint(i)));
		}
		return pointList;
	}
}
