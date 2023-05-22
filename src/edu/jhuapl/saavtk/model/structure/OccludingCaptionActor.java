package edu.jhuapl.saavtk.model.structure;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkCaptionActor2D;
import vtk.vtkGenericCell;
import vtk.vtkTriangle;

public class OccludingCaptionActor extends vtkCaptionActor2D
{

	double[] normal;
	double[] rayStartPoint;
	boolean enabled=true;	// overrides visibility

	
	public OccludingCaptionActor(double[] polygonCenterPoint, String polygonName, PolyhedralModel smallBodyModel)
	{
		super();
        Vector3D polygonCenter=new Vector3D(polygonCenterPoint);
        normal=PolyDataUtil.getPolyDataNormalAtPoint(polygonCenterPoint, smallBodyModel.getSmallBodyPolyDataAtPosition(), smallBodyModel.getPointLocator());
    	double[] closestPoint=new double[3];
    	vtkGenericCell cell=new vtkGenericCell();
    	long[] cellId=new long[1];
    	int[] subId=new int[1];
    	double[] dist=new double[1];
    	smallBodyModel.getCellLocator().FindClosestPoint(polygonCenter.toArray(), closestPoint, cell, cellId, subId, dist);
    	try
    	{
    		if (cellId[0]==-1)
    			throw new Exception("Closest cell not identifiable for structure "+polygonName);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	vtkTriangle tri=(vtkTriangle)smallBodyModel.getSmallBodyPolyDataAtPosition().GetCell(cellId[0]);
    	double area=tri.ComputeArea();
    	double[] faceCenter=new double[3];
    	tri.TriangleCenter(tri.GetPoints().GetPoint(0), tri.GetPoints().GetPoint(1), tri.GetPoints().GetPoint(2), faceCenter);
    	double[] triBounds=tri.GetBounds();
    	double normInf=Math.max(triBounds[1]-triBounds[0],Math.max(triBounds[3]-triBounds[2], triBounds[5]-triBounds[4]));
    	// start occlusion rays at some distance above the closest face to the polygon center; this should work for hills as well as valleys, use the raw area as a distance
        rayStartPoint=new Vector3D(faceCenter).add(new Vector3D(normal).scalarMultiply(normInf)).toArray();
        //
	}
	
	public double[] getNormal()
	{
		return normal;
	}
	
	public double[] getRayStartPoint()
	{
		return rayStartPoint;
	}
	

	public void setEnabled(boolean flag)
	{
		enabled=flag;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
}
