package edu.jhuapl.saavtk2.image;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.image.filters.ColormappingFilter;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.MapCoordinates;
import edu.jhuapl.saavtk2.image.projection.Projection;
import vtk.vtkImageData;
import vtk.vtkPolyData;

public interface Image
{
	public Projection getProjection();
	public vtkPolyData getSurfaceGeometry();
	public vtkPolyData getProjectionGeometry();
	public vtkPolyData getOffLimbGeometry();
	public int getNumberOfBands();
	public vtkImageData getBand(int i);
	public MapCoordinates getMapCoordsFromPixel(int i, int j);
	public int[] getPixelFromMapCoords(MapCoordinates mapCoords);
	public MapCoordinates getMapCoordsFromTCoords(double tx, double ty);
	public double[] getTCoordsFromMapCoords(MapCoordinates mapCoords);
	public Vector3D getPositionOnSurface(MapCoordinates mapCoords);
//	public ImageKey getKey();
}