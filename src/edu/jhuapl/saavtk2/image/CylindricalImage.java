package edu.jhuapl.saavtk2.image;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.hash.HashCode;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.LinearSpace;
import edu.jhuapl.saavtk2.image.filters.ImageDataFilter;
import edu.jhuapl.saavtk2.image.io.AwtImageReader;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.ConstantDepthFunction;
import edu.jhuapl.saavtk2.image.projection.CylindricalMapCoordinates;
import edu.jhuapl.saavtk2.image.projection.CylindricalProjection;
import edu.jhuapl.saavtk2.image.projection.DepthFunction;
import edu.jhuapl.saavtk2.image.projection.PerspectiveMapCoordinates;
import edu.jhuapl.saavtk2.image.projection.Projection;
import vtk.vtkAppendPolyData;
import vtk.vtkCellArray;
import vtk.vtkFeatureEdges;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkLine;
import vtk.vtkNativeLibrary;
import vtk.vtkOBBTree;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkPolyLine;
import vtk.vtkSphereSource;

public class CylindricalImage extends GenericImage
{
	protected static int projectionDegreesPerLineDivision=2;
	protected static double projectionDepth=1;
	
	public CylindricalImage(CylindricalProjection projection, GenericPolyhedralModel bodyModel, vtkImageData imageData)//, ImageKey key)
	{
		super(imageData, bodyModel.getSmallBodyPolyData(), projection);//, key);
	}
	
	public CylindricalImage(CylindricalProjection projection, GenericPolyhedralModel bodyModel, vtkImageData imageData, /*ImageKey key,*/ ImageDataFilter preFilter)
	{
		super(imageData, bodyModel.getSmallBodyPolyData(), projection, /*key,*/ preFilter);
	}


	@Override
	protected void createGeometry(vtkPolyData targetSurface)
	{
		surfaceGeometry=createSurfaceGeometry(targetSurface, projection);
		projectionGeometry=createProjectionGeometry(targetSurface.GetLength()*projectionDepth, surfaceGeometry, projection);
	}
	
	protected static vtkPolyData createSurfaceGeometry(vtkPolyData targetSurface, Projection projection)
	{
		return projection.clipVisibleGeometry(targetSurface);
	}
	
	protected static vtkPolyData createProjectionGeometry(double depth, vtkPolyData surfaceGeometry, Projection projection)
	{
	
		vtkPoints points=new vtkPoints();
		int oid=points.InsertNextPoint(Vector3D.ZERO.toArray());
		int ulid=points.InsertNextPoint(projection.getUpperLeftUnit().scalarMultiply(depth).add(projection.getRayOrigin()).toArray());
		int urid=points.InsertNextPoint(projection.getUpperRightUnit().scalarMultiply(depth).add(projection.getRayOrigin()).toArray());
		int llid=points.InsertNextPoint(projection.getLowerLeftUnit().scalarMultiply(depth).add(projection.getRayOrigin()).toArray());
		int lrid=points.InsertNextPoint(projection.getLowerRightUnit().scalarMultiply(depth).add(projection.getRayOrigin()).toArray());
		vtkLine ulline=new vtkLine();
		ulline.GetPointIds().SetId(0, oid);
		ulline.GetPointIds().SetId(1, ulid);
		vtkLine urline=new vtkLine();
		urline.GetPointIds().SetId(0, oid);
		urline.GetPointIds().SetId(1, urid);
		vtkLine llline=new vtkLine();
		llline.GetPointIds().SetId(0, oid);
		llline.GetPointIds().SetId(1, llid);
		vtkLine lrline=new vtkLine();
		lrline.GetPointIds().SetId(0, oid);
		lrline.GetPointIds().SetId(1, lrid);
		vtkCellArray cells=new vtkCellArray();
		cells.InsertNextCell(ulline);
		cells.InsertNextCell(urline);
		cells.InsertNextCell(llline);
		cells.InsertNextCell(lrline);
		
		int nDivLon=(int)(projection.getHorizontalMax()-projection.getHorizontalMin())/3;
		int nDivLat=(int)(projection.getHorizontalMax()-projection.getVerticalMin())/3;
		if (nDivLon==0)
			nDivLon=projectionDegreesPerLineDivision;
		if (nDivLat==0)
			nDivLat=projectionDegreesPerLineDivision;
		double[] lonDegRange=LinearSpace.create(projection.getHorizontalMin(), projection.getHorizontalMax(), nDivLon);
		double[] latDegRange=LinearSpace.create(projection.getVerticalMin(), projection.getVerticalMax(), nDivLat);
		
		vtkPolyLine minLatLine=new vtkPolyLine();
		vtkPolyLine maxLatLine=new vtkPolyLine();
		vtkPolyLine minLonLine=new vtkPolyLine();
		vtkPolyLine maxLonLine=new vtkPolyLine();
		double latDeg,lonDeg;
		int id;
		DepthFunction func=new ConstantDepthFunction(depth);
		for (int i=0; i<nDivLat; i++)
		{
			lonDeg=projection.getHorizontalMin();
			latDeg=latDegRange[i];
			id=points.InsertNextPoint(projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
			minLonLine.GetPointIds().InsertNextId(id);
			//
			lonDeg=projection.getHorizontalMax();
			latDeg=latDegRange[i];
			id=points.InsertNextPoint(projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
			maxLonLine.GetPointIds().InsertNextId(id);
		}
		for (int i=0; i<nDivLon; i++)
		{
			lonDeg=lonDegRange[i];
			latDeg=projection.getVerticalMin();
			id=points.InsertNextPoint(projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
			minLatLine.GetPointIds().InsertNextId(id);
			//
			//
			lonDeg=lonDegRange[i];
			latDeg=projection.getVerticalMax();
			id=points.InsertNextPoint(projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
			maxLatLine.GetPointIds().InsertNextId(id);
		}
		cells.InsertNextCell(minLatLine);
		cells.InsertNextCell(maxLatLine);
		cells.InsertNextCell(minLonLine);
		cells.InsertNextCell(maxLonLine);
		
		vtkPolyData polyData=new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetLines(cells);
		return polyData;
	}
	
	protected static vtkPolyData createBoundaryGeometry(vtkPolyData surfaceGeometry)
	{
		vtkFeatureEdges edgeFilter=new vtkFeatureEdges();
		edgeFilter.SetInputData(surfaceGeometry);
		edgeFilter.Update();
		return edgeFilter.GetOutput();
	}
	
	
	@Override
	public Projection getProjection()
	{
		return projection;
	}
	


/*	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		vtkSphereSource source=new vtkSphereSource();
		source.SetThetaResolution(360);
		source.SetPhiResolution(180);
		source.Update();
		vtkPolyData surface=source.GetOutput();

		CylindricalProjection projection=new CylindricalProjection(-5, 5, -10, 10);
		vtkImageData imageData=AwtImageReader.read(new File("/Users/zimmemi1/Desktop/saavtk.png"));
		CylindricalImage image=new CylindricalImage(projection, surface, imageData);
		
		vtkAppendPolyData appendFilter=new vtkAppendPolyData();
		appendFilter.AddInputData(image.getProjectionGeometry());
		appendFilter.AddInputData(image.getSurfaceGeometry());
		appendFilter.Update();
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(appendFilter.GetOutput());
		writer.Write();

		//PerspectiveImage image=new PerspectiveImage(raster, frustum, surface);
	
	}*/


}
