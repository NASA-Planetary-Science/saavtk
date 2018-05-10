package edu.jhuapl.saavtk2.image;


import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.image.filters.ImageDataFilter;
import edu.jhuapl.saavtk2.image.filters.PassThroughImageDataFilter;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.MapCoordinates;
import edu.jhuapl.saavtk2.image.projection.Projection;
import edu.jhuapl.saavtk2.image.projection.depthfunc.ConstantDepthFunction;
import edu.jhuapl.saavtk2.image.projection.depthfunc.DepthFunction;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkOBBTree;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public abstract class AbstractImage implements Image
{
	vtkImageData imageData;

	vtkPolyData surfaceGeometry = new vtkPolyData();
	vtkPolyData projectionGeometry = new vtkPolyData();
	vtkPolyData offLimbGeometry = new vtkPolyData();
	
	// stuff for getting pixel values via ray cast
	vtkOBBTree surfaceLocator;
	DepthFunction rayCastDepth;
	Projection projection;
	
//	ImageKey key;
	
	public AbstractImage(vtkImageData imageData, vtkPolyData targetSurface, Projection projection)//, ImageKey key)
	{
		this(imageData, targetSurface, projection, /*key,*/ new PassThroughImageDataFilter());
	}
	
	public AbstractImage(vtkImageData imageData, vtkPolyData targetSurface, Projection projection, /*ImageKey key,*/ ImageDataFilter preFilter)
	{
		this.imageData=preFilter.apply(imageData);
		this.projection=projection;
//		this.key=key;
		createGeometry(targetSurface);
		createLocator();
		setRayCastDepth(targetSurface.GetLength());
		generateTextureCoordinates();
	}

	protected abstract void createGeometry(vtkPolyData targetSurface);
	
	protected void createLocator()
	{
		surfaceLocator=new vtkOBBTree();
		surfaceLocator.SetDataSet(surfaceGeometry);
		surfaceLocator.SetTolerance(1e-12);
		surfaceLocator.BuildLocator();
	}
	
	protected void setRayCastDepth(double depth)
	{
		rayCastDepth=new ConstantDepthFunction(depth);

	}
	
	@Override
	public vtkPolyData getSurfaceGeometry()
	{
		return surfaceGeometry;
	}

	@Override
	public vtkPolyData getProjectionGeometry()
	{
		return projectionGeometry;
	}
	
	@Override
	public vtkPolyData getOffLimbGeometry()
	{
		return offLimbGeometry;
	}


	@Override
	public int getNumberOfBands()
	{
		return imageData.GetDimensions()[2];
	}

	@Override
	public vtkImageData getBand(int i)
	{
		int[] extents=imageData.GetExtent();
		vtkImageReslice sliceFilter=new vtkImageReslice();
		sliceFilter.SetInputData(imageData);
		sliceFilter.SetOutputExtent(extents[0], extents[1], extents[2], extents[3], i, i);
		sliceFilter.Update();
		return sliceFilter.GetOutput();
	}

	
	protected void generateTextureCoordinates()
	{
		vtkFloatArray surfaceTextureCoords=new vtkFloatArray();
		surfaceTextureCoords.SetNumberOfComponents(2);
		surfaceTextureCoords.SetNumberOfTuples(surfaceGeometry.GetNumberOfPoints());
		for (int p=0; p<surfaceGeometry.GetNumberOfPoints(); p++)
		{
			Vector3D pt=new Vector3D(surfaceGeometry.GetPoint(p));
			MapCoordinates mCoords=getProjection().project(pt);
			double tx=(mCoords.getX()-getProjection().getHorizontalMin())/getProjection().getHorizontalExtent();//(getProjection().getX()-getProjection().getHorizontalMin())/getProjection().getHorizontalExtent();
			double ty=(mCoords.getY()-getProjection().getVerticalMin())/getProjection().getVerticalExtent();//(getProjection().getY()-getProjection().getVerticalMin())/mCoords.getVerticalExtent();
			surfaceTextureCoords.SetTuple2(p, tx, ty);
		}
		surfaceGeometry.GetPointData().SetTCoords(surfaceTextureCoords);
	}

	@Override
	public int[] getPixelFromMapCoords(MapCoordinates mapCoords)
	{
		int[] dim=imageData.GetDimensions();
		int i=(int)((mapCoords.getX()-getProjection().getHorizontalMin())/getProjection().getHorizontalExtent()*(double)dim[0]);
		int j=(int)((mapCoords.getY()-getProjection().getVerticalMin())/getProjection().getVerticalExtent()*(double)dim[1]);
		if (i<0 || j<0 || i>=dim[0] || j>=dim[1])
			return null;
		else
			return new int[]{i,j};
	}

	@Override
	public double[] getTCoordsFromMapCoords(MapCoordinates mapCoords)
	{
		double tx=(mapCoords.getX()-getProjection().getHorizontalMin())/getProjection().getHorizontalExtent();
		double ty=(mapCoords.getY()-getProjection().getVerticalMin())/getProjection().getVerticalExtent();
		return new double[]{tx,ty};
	}
	
	
	@Override
	public MapCoordinates getMapCoordsFromPixel(int i, int j)
	{
		int[] dim=imageData.GetDimensions();
		double ival=(double)i/(double)dim[0]*getProjection().getHorizontalExtent()+getProjection().getHorizontalMin();
		double jval=(double)j/(double)dim[1]*getProjection().getVerticalExtent()+getProjection().getVerticalMin();
		return getProjection().createMapCoords(ival,jval);
	}

	@Override
	public MapCoordinates getMapCoordsFromTCoords(double tx, double ty)
	{
		double cx=tx*getProjection().getHorizontalExtent()+getProjection().getHorizontalMin();
		double cy=ty*getProjection().getVerticalExtent()+getProjection().getVerticalMin();
		return getProjection().createMapCoords(cx, cy);
	}
	
	@Override
	public Vector3D getPositionOnSurface(MapCoordinates mapCoords)
	{
		Vector3D rayOrigin=projection.getRayOrigin();
		Vector3D rayEndpoint=projection.unproject(mapCoords, rayCastDepth);
		vtkPoints hitPoints=new vtkPoints();
		surfaceLocator.IntersectWithLine(rayOrigin.toArray(), rayEndpoint.toArray(), hitPoints, null);
		if (hitPoints.GetNumberOfPoints()==0)
			return null;
		return new Vector3D(hitPoints.GetPoint(0));
	}

//	@Override
//	public ImageKey getKey()
//	{
//		return key;
//	}
	
}