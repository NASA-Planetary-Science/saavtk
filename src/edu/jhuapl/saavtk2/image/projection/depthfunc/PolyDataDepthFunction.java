package edu.jhuapl.saavtk2.image.projection.depthfunc;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.image.projection.MapCoordinates;
import edu.jhuapl.saavtk2.image.projection.Projection;
import vtk.vtkIdList;
import vtk.vtkOBBTree;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class PolyDataDepthFunction implements DepthFunction {

	Projection projection;
	vtkPolyData polyData;
	vtkOBBTree tree;
	double rayLength;
	DepthFunction rayDepthFunction;
	
	public PolyDataDepthFunction(Projection projection, vtkPolyData polyData) {
		this.projection=projection;
		this.polyData=polyData;
		tree=new vtkOBBTree();
		tree.SetDataSet(polyData);
		tree.SetTolerance(1e-15);
		tree.BuildLocator();
		double cmag=new Vector3D(polyData.GetCenter()).subtract(projection.getRayOrigin()).getNorm();
		rayLength=2*(cmag+polyData.GetLength());
		rayDepthFunction=new ConstantDepthFunction(rayLength);
	}
	
	@Override
	public double value(MapCoordinates mapCoordinates) {
		Vector3D pos=projection.unproject(mapCoordinates, rayDepthFunction);
		vtkPoints pts=new vtkPoints();
		vtkIdList ids=new vtkIdList();
		tree.IntersectWithLine(projection.getRayOrigin().toArray(), pos.toArray(), pts, ids);
		if (ids.GetNumberOfIds()==0)
			return Double.POSITIVE_INFINITY;
		else
			return new Vector3D(pts.GetPoint(0)).subtract(projection.getRayOrigin()).getNorm();
	}

	@Override
	public double getInfinityDepth() {
		return Double.POSITIVE_INFINITY;
	}

}
