package edu.jhuapl.saavtk2.image;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.hash.HashCode;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.LinearSpace;
import edu.jhuapl.saavtk2.image.filters.ImageDataFilter;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.CylindricalMapCoordinates;
import edu.jhuapl.saavtk2.image.projection.CylindricalProjection;
import edu.jhuapl.saavtk2.image.projection.PerspectiveMapCoordinates;
import edu.jhuapl.saavtk2.image.projection.Projection;
import edu.jhuapl.saavtk2.image.projection.depthfunc.ConstantDepthFunction;
import edu.jhuapl.saavtk2.image.projection.depthfunc.DepthFunction;
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
import vtk.vtkQuad;
import vtk.vtkSphereSource;

public class CylindricalImage extends AbstractImage
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
		projectionGeometry=createProjectionGeometry(targetSurface.GetLength()*projectionDepth, projection);
	}
	
	public static vtkPolyData createSurfaceGeometry(vtkPolyData targetSurface, Projection projection)
	{
		return projection.clipVisibleGeometry(targetSurface);
	}
	
	public static vtkPolyData createProjectionGeometry(double depth, Projection projection)
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
		
		int nDivLon=Math.max((int)(projection.getHorizontalMax()-projection.getHorizontalMin())/10,2);
		int nDivLat=Math.max((int)(projection.getVerticalMax()-projection.getVerticalMin())/10,2);
		double[] lonDegRange=LinearSpace.create(projection.getHorizontalMin(), projection.getHorizontalMax(), nDivLon);
		double[] latDegRange=LinearSpace.create(projection.getVerticalMin(), projection.getVerticalMax(), nDivLat);
		
		double latDeg,lonDeg;
		int id;
		DepthFunction func=new ConstantDepthFunction(depth);
		
		
		for (int i=0; i<nDivLat; i++)
		{
			vtkPolyLine lonLine=new vtkPolyLine();
			latDeg=latDegRange[i];
			for (int j=0; j<nDivLon; j++)
			{
				lonDeg=lonDegRange[j];
				id=points.InsertNextPoint(projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
				lonLine.GetPointIds().InsertNextId(id);
			}
			cells.InsertNextCell(lonLine);
		}
		
		for (int i=0; i<nDivLon; i++)
		{
			vtkPolyLine latLine=new vtkPolyLine();
			lonDeg=lonDegRange[i];
			for (int j=0; j<nDivLat; j++)
			{
				latDeg=latDegRange[j];
				id=points.InsertNextPoint(projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
				latLine.GetPointIds().InsertNextId(id);
			}
			cells.InsertNextCell(latLine);
		}

		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetLines(cells);
		return polyData;
	}

	public static int[] computeNumberOfDivisions(CylindricalProjection projection, double pixelsPerDegree)
	{
	     double degreesPerPixel=1./pixelsPerDegree;
	     int nDivLat=(int)((projection.getVerticalExtent())/degreesPerPixel);
	     int nDivLon=(int)((projection.getHorizontalExtent())/degreesPerPixel);
	     return new int[]{nDivLon,nDivLat};
	}
	
	public static double[] getLonDegRange(CylindricalProjection projection, double pixelsPerDegree)
	{
		int[] div=computeNumberOfDivisions(projection, pixelsPerDegree);
		return LinearSpace.create(projection.getHorizontalMin(), projection.getHorizontalMax(), div[0]);
	}
	
	public static double[] getLatDegRange(CylindricalProjection projection, double pixelsPerDegree)
	{
		int[] div=computeNumberOfDivisions(projection, pixelsPerDegree);
		return LinearSpace.create(projection.getVerticalMin(), projection.getVerticalMax(), div[1]);
	}
	
	public static vtkPolyData createProjectionGeometrySolid(double depth, Projection projection, double pixelsPerDegree)
	{   
		int[] dims=computeNumberOfDivisions((CylindricalProjection)projection, pixelsPerDegree);
		int nDivLon=dims[0];
		int nDivLat=dims[1];
		//int nDivLon=Math.max((int)(projection.getHorizontalMax()-projection.getHorizontalMin())/10,2);
		//int nDivLat=Math.max((int)(projection.getVerticalMax()-projection.getVerticalMin())/10,2);
		double[] lonDegRange=getLonDegRange((CylindricalProjection)projection, pixelsPerDegree);
		double[] latDegRange=getLatDegRange((CylindricalProjection)projection, pixelsPerDegree);
		DepthFunction func=new ConstantDepthFunction(depth);

		vtkPoints points=new vtkPoints();
		
		double latDeg,lonDeg;
			int[][] ids = new int[nDivLat][nDivLon];
			for (int i = 0; i < nDivLat; i++) {
				latDeg = latDegRange[i];
				for (int j = 0; j < nDivLon; j++) {
					lonDeg = lonDegRange[j];
					int id = points.InsertNextPoint(
							projection.unproject(new CylindricalMapCoordinates(lonDeg, latDeg), func).toArray());
					ids[i][j] = id;
				}
			}

			vtkCellArray polys = new vtkCellArray();
			for (int i = 0; i < nDivLat - 1; i++) {
				latDeg = latDegRange[i];
				for (int j = 0; j < nDivLon - 1; j++) {
					vtkQuad quad = new vtkQuad();
					int id0 = ids[i][j];
					int id1 = ids[i][j + 1];
					int id2 = ids[i + 1][j];
					int id3 = ids[i + 1][j + 1];
					quad.GetPointIds().SetId(0, id0);
					quad.GetPointIds().SetId(1, id1);
					quad.GetPointIds().SetId(2, id3);
					quad.GetPointIds().SetId(3, id2);
					polys.InsertNextCell(quad);
				}
			}

			vtkPolyData polyData=new vtkPolyData();
			polyData.SetPoints(points);
			polyData.SetPolys(polys);
		return polyData;
	}
	
	public static vtkPolyData createBoundaryGeometry(vtkPolyData surfaceGeometry)
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

	@Override
	public void importImageData(Importer<vtkImageData> importer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exportImageData(Exporter<vtkImageData> exporter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void importProjection(Importer<Projection> importer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exportProjection(Exporter<Projection> exporter) {
		// TODO Auto-generated method stub
		
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
