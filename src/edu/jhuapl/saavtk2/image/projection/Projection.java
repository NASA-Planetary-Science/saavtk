package edu.jhuapl.saavtk2.image.projection;


import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.image.projection.depthfunc.DepthFunction;
import vtk.vtkPolyData;

public interface Projection
{
	public MapCoordinates project(Vector3D position);										// project a 3d vector into 2d map coordinates
	public Vector3D unproject(MapCoordinates mapCoordinates, DepthFunction depthFunction);								// project 2d map into 3d space, which requires a depth function for uniqueness  
	public vtkPolyData clipVisibleGeometry(vtkPolyData polyData);							// clip geometry to viewing volume, and keep only visible faces
	//
	public Vector3D getRayOrigin();
	public Vector3D getUpperRightUnit();
	public Vector3D getUpperLeftUnit();
	public Vector3D getLowerLeftUnit();
	public Vector3D getLowerRightUnit();
	public Vector3D getMidPointUnit();
	//
	public double getHorizontalMin();
	public double getHorizontalMax();
	public double getHorizontalExtent();
	public double getHorizontalMid();
	public double getVerticalMin();
	public double getVerticalMax();
	public double getVerticalExtent();
	public double getVerticalMid();
	public MapCoordinates createMapCoords(double x, double y);
}
