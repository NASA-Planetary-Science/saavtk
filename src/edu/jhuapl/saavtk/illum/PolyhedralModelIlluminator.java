package edu.jhuapl.saavtk.illum;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import vtk.vtkIdList;
import vtk.vtkOBBTree;
import vtk.vtkPolyData;
import vtk.vtkTriangle;

public class PolyhedralModelIlluminator
{
	double[] illuminationFactor;
	vtkPolyData polyDataCopy;
	vtkOBBTree searchTree;
	double rayLength;
	
	public PolyhedralModelIlluminator(PolyhedralModel model)
	{
		polyDataCopy=new vtkPolyData();
		polyDataCopy.DeepCopy(model.getSmallBodyPolyData());
		searchTree=new vtkOBBTree();
		searchTree.SetDataSet(polyDataCopy);
		searchTree.SetTolerance(1e-12);
		searchTree.BuildLocator();
		double[] bounds=polyDataCopy.GetBounds();
		double dx=bounds[1]-bounds[0];
		double dy=bounds[3]-bounds[2];
		double dz=bounds[5]-bounds[4];
		rayLength=2*Math.sqrt(dx*dx+dy*dy+dz*dz);
	}
	
	public void illuminate(IlluminationField illumField)	// compute illumination just from viewpoint of triangle centers; a more sophisticated approach can be implemented later
	{
		List<Integer> faceIndices=Lists.newArrayList();
		for (int c=0; c<polyDataCopy.GetNumberOfCells(); c++)
			faceIndices.add(c);
		illuminationFactor=illuminate(illumField, faceIndices);
	}

	public double[] illuminate(IlluminationField illumField, List<Integer> faceIndices)	// get illumination just for a subset of the faces
	{
		double[] illumFac=new double[faceIndices.size()];
		for (int m=0; m<faceIndices.size(); m++)
		{
			int c=faceIndices.get(m);
			vtkTriangle tri=(vtkTriangle)polyDataCopy.GetCell(c);
			double[] pt0=tri.GetPoints().GetPoint(0);
			double[] pt1=tri.GetPoints().GetPoint(1);
			double[] pt2=tri.GetPoints().GetPoint(2);
			double[] normal=new double[3];
			double[] center=new double[3];
			tri.ComputeNormal(pt0,pt1,pt2,normal);
			tri.TriangleCenter(pt0,pt1,pt2,center);
			Vector3D normalVec=new Vector3D(normal);
			Vector3D centerVec=new Vector3D(center);
			Vector3D invIllumUnitVec=illumField.getUnobstructedFlux(centerVec).negate().normalize();
			if (invIllumUnitVec.dotProduct(normalVec)<0)
			{
				illumFac[m]=0;
				continue;
			}
			//
			Vector3D invIllumRay=invIllumUnitVec.scalarMultiply(rayLength);
			vtkIdList ids=new vtkIdList();
			double[] rayOrigin=centerVec.toArray();
			double[] rayEndpoint=centerVec.add(invIllumRay).toArray();
			searchTree.IntersectWithLine(rayOrigin, rayEndpoint, null, ids);
			boolean hit=false;
			for (int i=0; i<ids.GetNumberOfIds() && !hit; i++)
			{
				int id=ids.GetId(i);
				if (id==c)
					continue;	// ignore self-intersections
				else
					hit=true;	// but catch intersections with other faces
			}
			if (hit)
				illumFac[m]=0;
		}
		return illumFac;
	}

	public double getIlluminationFactor(int c)
	{
		return illuminationFactor[c];
	}
}
