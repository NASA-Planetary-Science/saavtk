package edu.jhuapl.saavtk2.geom;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk2.geom.euclidean.Line;
import vtk.vtkActor;
import vtk.vtkCell;
import vtk.vtkCellLocator;
import vtk.vtkDataArray;
import vtk.vtkIdList;
import vtk.vtkOBBTree;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkTriangle;

public class BasicGeometry implements Geometry {
	protected vtkPolyDataMapper mapper=new vtkPolyDataMapper();
	protected vtkActor actor=new vtkActor();
	protected vtkOBBTree cellLocator=new vtkOBBTree();
	protected vtkPointLocator pointLocator=new vtkPointLocator();
	protected vtkPolyData polyData=null;

	public BasicGeometry(vtkPolyData polyData) {
		this.polyData=polyData;
		update();
	}

	public void update() {
		actor.SetMapper(mapper);
		mapper.SetInputData(polyData);
		mapper.Update();
		cellLocator.SetDataSet(polyData);
		cellLocator.SetTolerance(1e-15);
		cellLocator.BuildLocator();
		pointLocator.SetDataSet(polyData);
		pointLocator.SetTolerance(1e-15);
		pointLocator.BuildLocator();
	}

	@Override
	public vtkPolyData getPolyData() {
		return polyData;
	}

	@Override
	public vtkPolyDataMapper getMapper() {
		return mapper;
	}

	@Override
	public vtkActor getActor() {
		return actor;
	}

	@Override
	public vtkOBBTree getCellLocator() {
		return cellLocator;
	}

	@Override
	public vtkPointLocator getPointLocator() {
		return pointLocator;
	}

	@Override
	public CellIntersection computeFirstIntersectionWithLine(Line line) {
		List<CellIntersection> hits = computeOrderedIntersectionsWithLine(line);
		if (hits.size() > 0)
			return hits.get(0);
		else
			return null;
	}

	@Override
	public Collection<CellIntersection> computeAllIntersectionsWithLine(Line line) {
		vtkPoints points = new vtkPoints();
		vtkIdList ids=new vtkIdList();
		cellLocator.IntersectWithLine(line.getP1().toArray(), line.getP2().toArray(), points, ids);
		List<CellIntersection> hits = Lists.newArrayList();
		for (int i = 0; i < points.GetNumberOfPoints(); i++)
			hits.add(new CellIntersection(ids.GetId(i), new Vector3D(points.GetPoint(i))));
		return hits;
	}

	@Override
	public List<CellIntersection> computeOrderedIntersectionsWithLine(Line line) {
		List<CellIntersection> hits = Lists.newArrayList(computeAllIntersectionsWithLine(line));
		Collections.sort(hits, new CellIntersectionComparator(line));
		return hits;
	}
	
	public static double[] interpolatePointData(vtkPolyData polyData, vtkDataArray pointArray, CellIntersection hit)
	{
		if (true)
			throw new Error("not correctly implemented; needs to use MathUtil.interpolateWithin triangle, as vtkTriangle barycoords are valid only for triangle vertices in the xy plane");
		vtkTriangle tri=(vtkTriangle)polyData.GetCell(hit.getCellId());
		double[][] pts=new double[3][3];
		tri.GetPoints().GetPoint(0,pts[0]);
		tri.GetPoints().GetPoint(1,pts[1]);
		tri.GetPoints().GetPoint(2,pts[2]);
		double[] baryCoords=new double[3];
		tri.BarycentricCoords(pts[0], pts[1], pts[2], hit.getHitPosition().toArray(), baryCoords);
		int[] ids=new int[3];
		ids[0]=tri.GetPointId(0);
		ids[1]=tri.GetPointId(1);
		ids[2]=tri.GetPointId(2);
		double[] value=new double[pointArray.GetNumberOfComponents()];
		for (int i=0; i<pointArray.GetNumberOfComponents(); i++)
		{
			double c0=pointArray.GetComponent(ids[0], i);
			double c1=pointArray.GetComponent(ids[1], i);
			double c2=pointArray.GetComponent(ids[2], i);
			value[i]=c0*baryCoords[0]+c1*baryCoords[1]+c2*baryCoords[2];
		}
		return value;
	}
}
