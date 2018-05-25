package edu.jhuapl.saavtk2.image;


import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk2.polydata.clip.PolyDataClipWithFrustum;
import edu.jhuapl.saavtk2.polydata.select.PolyDataRemoveSelectedCells;
import edu.jhuapl.saavtk2.polydata.select.VisibleNormalCellSelector;
import edu.jhuapl.saavtk2.util.Frustum;
import edu.jhuapl.saavtk2.image.filters.ImageDataFilter;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.PerspectiveProjection;
import vtk.vtkCellArray;
import vtk.vtkConnectivityFilter;
import vtk.vtkFeatureEdges;
import vtk.vtkGeometryFilter;
import vtk.vtkImageData;
import vtk.vtkLine;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class PerspectiveImage extends GenericImage
{
	
	
	public PerspectiveImage(PerspectiveProjection projection, GenericPolyhedralModel bodyModel, vtkImageData imageData)//, ImageKey key)
	{
		super(imageData, bodyModel.getSmallBodyPolyData(), projection);//, key);
	}
	
	public PerspectiveImage(PerspectiveProjection projection, GenericPolyhedralModel bodyModel, vtkImageData imageData, /*ImageKey key,*/ ImageDataFilter preFilter)
	{
		super(imageData, bodyModel.getSmallBodyPolyData(), projection);//, key);
		setRayCastDepth(projection.getFrustumDefinition().getOrigin().getNorm()+bodyModel.getSmallBodyPolyData().GetLength());
	}
		
	@Override
	protected void createGeometry(vtkPolyData targetSurface)
	{
		Frustum frustum=((PerspectiveProjection)projection).getFrustumDefinition();
		vtkPolyData clippedGeometry = new PolyDataClipWithFrustum(frustum).apply(targetSurface);
		surfaceGeometry = createSurfaceGeometry(clippedGeometry, frustum);
		projectionGeometry = createProjectionGeometry(frustum, targetSurface.GetLength() + frustum.getOrigin().getNorm());
	}
	
	@Override
	public PerspectiveProjection getProjection()
	{
		return (PerspectiveProjection)projection;
	}

	protected static vtkPolyData createSurfaceGeometry(vtkPolyData clippedGeometry, Frustum frustum)
	{
		VisibleNormalCellSelector selector = new VisibleNormalCellSelector(clippedGeometry);
		selector.setViewPoint(frustum.getOrigin());
		selector.apply();
		PolyDataRemoveSelectedCells remover = new PolyDataRemoveSelectedCells();
		remover.setIndicesToRemove(selector.getSelected());
		return remover.apply(clippedGeometry);
	}

	protected static vtkPolyData createBoundaryPolyData(vtkPolyData clippedGeometry, Frustum frustum)
	{
		vtkConnectivityFilter connectivityFilter = new vtkConnectivityFilter();
		connectivityFilter.SetInputData(clippedGeometry);
		connectivityFilter.SetExtractionModeToClosestPointRegion();
		connectivityFilter.SetClosestPoint(frustum.getOrigin().toArray());
		connectivityFilter.Update();
		vtkGeometryFilter geomFilter = new vtkGeometryFilter();
		geomFilter.SetInputData(connectivityFilter.GetOutput());
		geomFilter.Update();
		vtkFeatureEdges edgeFilter=new vtkFeatureEdges();
		edgeFilter.SetInputData(geomFilter.GetOutput());
		edgeFilter.Update();
		return edgeFilter.GetOutput();
	}

	protected static vtkPolyData createProjectionGeometry(Frustum frustum, double depth)
	{
		vtkPoints points = new vtkPoints();
		int oid = points.InsertNextPoint(frustum.getOrigin().toArray());
		int ulid = points.InsertNextPoint(frustum.getOrigin().add(frustum.getUpperLeftUnit().scalarMultiply(depth)).toArray());
		int urid = points.InsertNextPoint(frustum.getOrigin().add(frustum.getUpperRightUnit().scalarMultiply(depth)).toArray());
		int llid = points.InsertNextPoint(frustum.getOrigin().add(frustum.getLowerLeftUnit().scalarMultiply(depth)).toArray());
		int lrid = points.InsertNextPoint(frustum.getOrigin().add(frustum.getLowerRightUnit().scalarMultiply(depth)).toArray());
		//
		vtkLine ulline = new vtkLine();
		ulline.GetPointIds().SetId(0, oid);
		ulline.GetPointIds().SetId(1, ulid);
		vtkLine urline = new vtkLine();
		urline.GetPointIds().SetId(0, oid);
		urline.GetPointIds().SetId(1, urid);
		vtkLine llline = new vtkLine();
		llline.GetPointIds().SetId(0, oid);
		llline.GetPointIds().SetId(1, llid);
		vtkLine lrline = new vtkLine();
		lrline.GetPointIds().SetId(0, oid);
		lrline.GetPointIds().SetId(1, lrid);
		//
		vtkLine lboxline = new vtkLine();
		lboxline.GetPointIds().SetId(0, llid);
		lboxline.GetPointIds().SetId(1, ulid);
		vtkLine tboxline = new vtkLine();
		tboxline.GetPointIds().SetId(0, ulid);
		tboxline.GetPointIds().SetId(1, urid);
		vtkLine rboxline = new vtkLine();
		rboxline.GetPointIds().SetId(0, urid);
		rboxline.GetPointIds().SetId(1, lrid);
		vtkLine bboxline = new vtkLine();
		bboxline.GetPointIds().SetId(0, lrid);
		bboxline.GetPointIds().SetId(1, llid);
		//
		vtkCellArray cells = new vtkCellArray();
		cells.InsertNextCell(ulline);
		cells.InsertNextCell(urline);
		cells.InsertNextCell(llline);
		cells.InsertNextCell(lrline);
		cells.InsertNextCell(lboxline);
		cells.InsertNextCell(tboxline);
		cells.InsertNextCell(rboxline);
		cells.InsertNextCell(bboxline);
		//
		vtkPolyData polyData = new vtkPolyData();
		polyData.SetPoints(points);
		polyData.SetLines(cells);
		return polyData;
	}
	
	
/*	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		vtkSphereSource source=new vtkSphereSource();
		source.SetThetaResolution(36);
		source.SetPhiResolution(18);
		source.Update();
		vtkPolyData surface=source.GetOutput();
		
		Vector3D origin=Vector3D.PLUS_K.scalarMultiply(2);
		Vector3D lookAt=Vector3D.ZERO;
		Vector3D up=Vector3D.PLUS_J;
		double fov=10;
		Frustum frustum=new Frustum(origin, lookAt, up, fov, fov);
		
		vtkImageData imageData=AwtImageReader.read(new File("/Users/zimmemi1/Desktop/saavtk.png"));
		PerspectiveImage image=new PerspectiveImage(frustum, surface, imageData);
		
//		vtkAppendPolyData appendFilter=new vtkAppendPolyData();
//		appendFilter.AddInputData(image.getProjectionGeometry());
//		appendFilter.AddInputData(image.getSurfaceGeometry());
//		appendFilter.Update();
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(image.getSurfaceGeometry());
		writer.Write();

		//PerspectiveImage image=new PerspectiveImage(raster, frustum, surface);
	}*/


}
