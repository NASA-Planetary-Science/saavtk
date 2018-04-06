package edu.jhuapl.saavtk2.polydata.select;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.polydata.PolyDataAddCellCenters;
import edu.jhuapl.saavtk2.polydata.PolyDataAddNormals;
import vtk.vtkDoubleArray;
import vtk.vtkPolyData;

// This is not a full geometric (ray-traced) occlusion test; it just looks to see if each face normal is pointed away from the observer.
public class VisibleNormalCellSelector extends PolyDataCellSelector
{

	Vector3D viewpoint=null;
	vtkDoubleArray centers,normals;
	
	public VisibleNormalCellSelector(vtkPolyData scenePolyData)
	{
		super(scenePolyData);
		if (!PolyDataAddNormals.hasCellNormals(scenePolyData))
			scenePolyData=new PolyDataAddNormals().apply(scenePolyData);
		if (!PolyDataAddCellCenters.hasCenters(scenePolyData))
			scenePolyData=new PolyDataAddCellCenters().apply(scenePolyData);
		centers=(vtkDoubleArray)scenePolyData.GetCellData().GetNormals();
		normals=(vtkDoubleArray)scenePolyData.GetCellData().GetNormals();
	}
	
	public void setViewPoint(Vector3D v)
	{
		viewpoint=v;
	}

	@Override
	public boolean select(int i)
	{
		if (viewpoint==null)
			throw new NullPointerException("viewpoint is null");
		Vector3D ctr=new Vector3D(centers.GetTuple3(i));
		Vector3D nml=new Vector3D(normals.GetTuple3(i));
		if (ctr.subtract(viewpoint).dotProduct(nml)<0)
			return true;
		return false;
	}

	
}
