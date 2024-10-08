package edu.jhuapl.saavtk.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.file.IndexableTuple;
import edu.jhuapl.saavtk.vtk.VtkDrawUtil;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import vtk.vtkAbstractPointLocator;
import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkAppendPolyData;
import vtk.vtkCamera;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkCleanPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkDataArray;
import vtk.vtkDataSetMapper;
import vtk.vtkDecimatePro;
import vtk.vtkExtractSelectedFrustum;
import vtk.vtkFeatureEdges;
import vtk.vtkFloatArray;
import vtk.vtkFrustumSource;
import vtk.vtkGenericCell;
import vtk.vtkGeometryFilter;
import vtk.vtkIdList;
import vtk.vtkIdTypeArray;
import vtk.vtkNamedColors;
import vtk.vtkOBJReader;
import vtk.vtkObject;
import vtk.vtkPLYReader;
import vtk.vtkPlane;
import vtk.vtkPlaneSource;
import vtk.vtkPlanes;
import vtk.vtkPointData;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataConnectivityFilter;
import vtk.vtkPolyDataNormals;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkProperty;
import vtk.vtkRegularPolygonSource;
import vtk.vtkSTLReader;
import vtk.vtkSTLWriter;
import vtk.vtkTriangle;
import vtk.vtkUnstructuredGrid;
import vtk.vtksbCellLocator;

/**
 * This class contains various utility functions for operating on a vtkPolyData.
 */
public class PolyDataUtil
{
	//private static void printpt(double[] p, String s)
	//{
	//    System.out.println(s + " " + p[0] + " " + p[1] + " " + p[2]);
	//}

	// This variable should NEVER be modified
	private static vtkPolyData emptyPolyData;

	/**
	 * Clear a polydata by deep copying a freshly created empty polydata
	 *
	 * @param polydata
	 */
	public static void clearPolyData(vtkPolyData polydata)
	{
		if (emptyPolyData == null)
			emptyPolyData = new vtkPolyData();
		polydata.DeepCopy(emptyPolyData);
	}

	public static vtkPolyData computeMultipleFrustumIntersection(vtkPolyData polyData, vtksbCellLocator locator, vtkPointLocator pointLocator, List<Frustum> frustums)
	{
		Frustum f = frustums.get(0);
		polyData = computeFrustumIntersection(polyData, locator, pointLocator, f.origin, f.ul, f.ur, f.ll, f.lr);

		for (int i = 1; i < frustums.size(); ++i)
		{
			if (polyData == null || polyData.GetNumberOfPoints() == 0 || polyData.GetNumberOfCells() == 0)
				return null;

			locator = new vtksbCellLocator();
			pointLocator = new vtkPointLocator();

			locator.SetDataSet(polyData);
			locator.CacheCellBoundsOn();
			locator.AutomaticOn();
			//locator.SetMaxLevel(10);
			//locator.SetNumberOfCellsPerNode(5);
			locator.BuildLocator();

			pointLocator.SetDataSet(polyData);
			pointLocator.BuildLocator();

			f = frustums.get(i);
			polyData = computeFrustumIntersection(polyData, locator, pointLocator, f.origin, f.ul, f.ur, f.ll, f.lr);
		}

		return polyData;
	}
	
	public static vtkPlanes computeFrustumPlanes(/*vtkPolyData polyData,*/ double[] origin, double[] ul, double[] ur, double[] lr, double[] ll)
	{
//		Logger.getAnonymousLogger().log(Level.INFO, "!!!!!!!!!!!!!!1Computing Frustum Planes");

//		double[] top = new double[3];
//		double[] right = new double[3];
//		double[] bottom = new double[3];
//		double[] left = new double[3];
//
//		MathUtil.vcrss(ur, ul, top);
//		MathUtil.vcrss(lr, ur, right);
//		MathUtil.vcrss(ll, lr, bottom);
//		MathUtil.vcrss(ul, ll, left);
//		double dx = MathUtil.vnorm(origin);
////		System.out.println("PolyDataUtil: computeFrustumPlanes: origin " + new Vector3D(origin));
////		System.out.println("PolyDataUtil: computeFrustumPlanes: UL2 " + new Vector3D(ul));
////		System.out.println("PolyDataUtil: computeFrustumPlanes: dx is " + dx);
//		double[] UL2 = { origin[0] + ul[0] * dx, origin[1] + ul[1] * dx, origin[2] + ul[2] * dx };
//		System.out.println("PolyDataUtil: computeFrustumPlanes: UL2 " + UL2[0] + "," + UL2[1] + "," + UL2[2]);
//		double[] UR2 = { origin[0] + ur[0] * dx, origin[1] + ur[1] * dx, origin[2] + ur[2] * dx };
//		System.out.println("PolyDataUtil: computeFrustumPlanes: UR2 " + UR2[0] + "," + UR2[1] + "," + UR2[2]);
//		double[] LL2 = { origin[0] + ll[0] * dx, origin[1] + ll[1] * dx, origin[2] + ll[2] * dx };
//		System.out.println("PolyDataUtil: computeFrustumPlanes: LL2 " + LL2[0] + "," + LL2[1] + "," + LL2[2]);
//		double[] LR2 = { origin[0] + lr[0] * dx, origin[1] + lr[1] * dx, origin[2] + lr[2] * dx };
//		System.out.println("PolyDataUtil: computeFrustumPlanes: LR2 " + LR2[0] + "," + LR2[1] + "," + LR2[2]);
//		
////		System.out.println("PolyDataUtil: computeFrustumPlanes: top " + new Vector3D(top));
////		System.out.println("PolyDataUtil: computeFrustumPlanes: right " + new Vector3D(right));
////		System.out.println("PolyDataUtil: computeFrustumPlanes: bottom " + new Vector3D(bottom));
////		System.out.println("PolyDataUtil: computeFrustumPlanes: left " + new Vector3D(left));
//		
////		vtkPlane plane1 = new vtkPlane();
////		plane1.SetOrigin(UL2);
////		plane1.SetNormal(top);
////		vtkPlane plane2 = new vtkPlane();
////		plane2.SetOrigin(UR2);
////		plane2.SetNormal(right);
////		vtkPlane plane3 = new vtkPlane();
////		plane3.SetOrigin(LR2);
////		plane3.SetNormal(bottom);
////		vtkPlane plane4 = new vtkPlane();
////		plane4.SetOrigin(LL2);
////		plane4.SetNormal(left);
////		
////		vtkPlane plane5 = new vtkPlane();
////		plane5.SetOrigin(origin[0], origin[1], origin[2]);
////		plane5.SetNormal(-origin[0], -origin[1], -origin[2]);
////		
////		vtkPlane plane6 = new vtkPlane();
////		plane6.SetOrigin(0, 0, 0);
////		plane6.SetNormal(origin[0], origin[1], origin[2]);
//				
//		vtkPoints planePoints = new vtkPoints();
//		planePoints.SetNumberOfPoints(1);
//		planePoints.InsertPoint(0, UL2);
//		planePoints.InsertPoint(1, UR2);
//		planePoints.InsertPoint(2, LR2);
//		planePoints.InsertPoint(3, LL2);
//		planePoints.InsertPoint(4, new double[] {origin[0], origin[1], origin[2]});
////		System.out.println("PolyDataUtil: computeFrustumPlanes: origin " + origin[0] + "," + origin[1] + "," + origin[2]);
////
//		planePoints.InsertPoint(5, new double[] {0, 0, 0});
////		System.out.println("PolyDataUtil: computeFrustumPlanes: origin 0,0,0");
//
//		
//		vtkDoubleArray planeNormals = new vtkDoubleArray();
//		planeNormals.SetNumberOfComponents(3);
//		planeNormals.SetNumberOfTuples(1);
//		planeNormals.InsertTuple3(0, top[0], top[1], top[2]);
//		planeNormals.InsertTuple3(1, right[0], right[1], right[2]);
//		planeNormals.InsertTuple3(2, bottom[0], bottom[1], bottom[2]);
//		planeNormals.InsertTuple3(3, left[0], left[1], left[2]);
//		planeNormals.InsertTuple3(4, origin[0], origin[1], origin[2]);
//		planeNormals.InsertTuple3(5, -origin[0], -origin[1], -origin[2]);
//		System.out.println("PolyDataUtil: computeFrustumPlanes: normal " + top[0] + "," + top[1] + "," + top[2]);
////		System.out.println("PolyDataUtil: computeFrustumPlanes: normal " + right[0] + "," + right[1] + "," + right[2]);
////		System.out.println("PolyDataUtil: computeFrustumPlanes: normal " + bottom[0] + "," + bottom[1] + "," + bottom[2]);
////		System.out.println("PolyDataUtil: computeFrustumPlanes: normal " + left[0] + "," + left[1] + "," + left[2]);
////		System.out.println("PolyDataUtil: computeFrustumPlanes: normal " + origin[0] + "," + origin[1] + "," + origin[2]);
////		System.out.println("PolyDataUtil: computeFrustumPlanes: normal " + -origin[0] + "," + -origin[1] + "," + -origin[2]);
//
//		
//		vtkPlanes planes = new vtkPlanes();
//		planes.SetPoints(planePoints);
//		planes.SetNormals(planeNormals);
//		System.out.println("PolyDataUtil: computeFrustumPlanes: number of normals tuples " + planeNormals.GetNumberOfTuples());
//		
//		System.out.println("PolyDataUtil: computeVTKFrustumIntersection: planes " + planes.GetNumberOfPlanes());
//		return planes;
		
		edu.jhuapl.saavtk2.geom.euclidean.Frustum frustum = new edu.jhuapl.saavtk2.geom.euclidean.Frustum(origin, ul, ur, lr, ll);
		
		vtkCamera Camera = new vtkCamera();
//	    System.out.println("PolyDataUtil: renderFrustumPlanes: origin " + new Vector3D(origin));
	    Camera.SetPosition(origin[0], origin[1], origin[2]);
	    
//	    Camera.SetFocalPoint(0, 0, 0);
//	    System.out.println("PolyDataUtil: computeFrustumPlanes: frustum boresight " + frustum.getBoresightUnit());
	    Vector3D focalPoint = new Vector3D(origin).add(frustum.getBoresightUnit().scalarMultiply(10000));
//	    System.out.println("PolyDataUtil: computeFrustumPlanes: focal point " + focalPoint);
	    Camera.SetFocalPoint(focalPoint.toArray());
	    Camera.SetViewAngle(frustum.getFovXDeg());
	    Camera.SetViewUp(frustum.getR().toArray());
	    Camera.SetClippingRange(0.1,new Vector3D(origin).getNorm()*2);
	    double PlanesArray[] = new double[24];

	    Camera.GetFrustumPlanes(1.0, PlanesArray);

	    vtkPlanes Planes = new vtkPlanes();
	    Planes.SetFrustumPlanes(PlanesArray);
	    return Planes;
	}
	
	public static vtkActor renderFrustumPlanes(/*vtkPolyData polyData,*/ double[] origin, double[] ul, double[] ur, double[] lr, double[] ll)
	{		
	    vtkNamedColors Color = new vtkNamedColors();
	    //For Actor Color
	    double ActorColor[] = new double[4];
	    //Renderer Background Color
	    double BgColor[] = new double[4];
	    //BackFace color
	    double BackColor[] = new double[4];

	    //Change Color Name to Use your own Color for Change Actor Color
	    Color.GetColor("GreenYellow",ActorColor);
	    //Change Color Name to Use your own Color for Renderer Background
	    Color.GetColor("RoyalBlue",BgColor);
	    //Change Color Name to Use your own Color for BackFace Color
	    Color.GetColor("PeachPuff",BackColor);
	    
//	    vtkCamera Camera = new vtkCamera();
//	    System.out.println("PolyDataUtil: renderFrustumPlanes: origin " + new Vector3D(origin));
//	    Camera.SetPosition(origin[0], origin[1], origin[2]);
//	    Camera.SetFocalPoint(0, 0, 0);
//	    Camera.SetViewAngle(2);
//	    Camera.SetClippingRange(0.1,new Vector3D(origin).getNorm());
//	    double PlanesArray[] = new double[24];
//
//	    Camera.GetFrustumPlanes(1.0, PlanesArray);
//
//	    vtkPlanes Planes = new vtkPlanes();
//	    Planes.SetFrustumPlanes(PlanesArray);
	    
	    vtkPlanes Planes = computeFrustumPlanes(/*polyData,*/ origin, ul, ur, lr, ll);

	    //To create a frustum defined by a set of planes.
	    vtkFrustumSource FrustumSource = new vtkFrustumSource();
	    FrustumSource.ShowLinesOff();
	    FrustumSource.SetPlanes(Planes);

	    //Create a Mapper and Actor
	    vtkDataSetMapper Mapper = new vtkDataSetMapper();
	    Mapper.SetInputConnection(FrustumSource.GetOutputPort());

	    vtkProperty Back = new vtkProperty();
	    Back.SetColor(BackColor);

	    vtkActor Actor = new vtkActor();
	    Actor.SetMapper(Mapper);
	    Actor.GetProperty().EdgeVisibilityOn();
	    Actor.GetProperty().SetColor(ActorColor);
	    Actor.SetBackfaceProperty(Back);

		return Actor;
	}
	
	public static vtkActor renderFrustumPlane(double[] origin, double[] ul, double[] ur, double[] lr, double[] ll, int i)
	{		
		vtkPlanes planes = computeFrustumPlanes(origin, ul, ur, lr, ll);
		vtkPlane plane = planes.GetPlane(i);
//		System.out.println("PolyDataUtil: renderFrustumPlane: plane " + plane);
	    vtkNamedColors Color = new vtkNamedColors();
	    //For Actor Color
	    double ActorColor[] = new double[4];
	    //Renderer Background Color
	    double BgColor[] = new double[4];
	    //BackFace color
	    double BackColor[] = new double[4];

	    //Change Color Name to Use your own Color for Change Actor Color
	    Color.GetColor("GreenYellow",ActorColor);
	    //Change Color Name to Use your own Color for Renderer Background
	    Color.GetColor("RoyalBlue",BgColor);
	    //Change Color Name to Use your own Color for BackFace Color
	    Color.GetColor("PeachPuff",BackColor);
	    
	    vtkPlaneSource planeSource = new vtkPlaneSource();
	    planeSource.SetOrigin(plane.GetOrigin());
	    planeSource.SetNormal(plane.GetNormal());
	    planeSource.Update();
	    
	    //Create a Mapper and Actor
	    vtkDataSetMapper Mapper = new vtkDataSetMapper();
	    Mapper.SetInputConnection(planeSource.GetOutputPort());

	    vtkProperty Back = new vtkProperty();
	    Back.SetColor(BackColor);

	    vtkActor Actor = new vtkActor();
	    Actor.SetMapper(Mapper);
	    Actor.GetProperty().EdgeVisibilityOn();
	    Actor.GetProperty().SetColor(ActorColor);
	    Actor.SetBackfaceProperty(Back);

		return Actor;
	}
	
	public static vtkPolyData computeVTKFrustumIntersection(vtkPolyData polyData, vtksbCellLocator locator, vtkAbstractPointLocator pointLocator, double[] origin, double[] ul, double[] ur, double[] lr, double[] ll)
	{
//		System.out.println("PolyDataUtil: computeVTKFrustumIntersection: computing vtk frustum intersection");
//		Logger.getAnonymousLogger().log(Level.INFO, "!!!!!!!!!!!!!!1Computing VTK Frustum Intersection");

		vtkPlanes planes = computeFrustumPlanes(/*polyData,*/ origin, ul, ur, lr, ll);

		vtkExtractSelectedFrustum extractor = new vtkExtractSelectedFrustum();
		extractor.SetFrustum(planes);
//		System.out.println("PolyDataUtil: computeVTKFrustumIntersection: planes " + extractor.GetFrustum());
		extractor.PreserveTopologyOff();
		extractor.SetInputData(polyData);
		extractor.InsideOutOff();
		extractor.SetFieldType(0);
//		System.out.println("PolyDataUtil: computeVTKFrustumIntersection: extractor field type " + extractor.GetFieldType());
		extractor.Update();
//		Logger.getAnonymousLogger().log(Level.INFO, "extractor update");
//		vtkDataObject output = extractor.GetOutput();
		
		vtkUnstructuredGrid selectedGeometry = (vtkUnstructuredGrid) extractor.GetOutput();
		vtkGeometryFilter geometryFilter = new vtkGeometryFilter();
		geometryFilter.SetInputData(selectedGeometry);
		geometryFilter.Update();
//		Logger.getAnonymousLogger().log(Level.INFO, "Geofilter update");

		
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInputConnection(geometryFilter.GetOutputPort());
		normalsFilter.SetComputeCellNormals(1);
		normalsFilter.SetComputePointNormals(0);
		normalsFilter.SplittingOff();
		normalsFilter.Update();
		vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
//		Logger.getAnonymousLogger().log(Level.INFO, "Got normals filter");
		vtkPolyData tmpPolyData = new vtkPolyData();
		tmpPolyData.DeepCopy(normalsFilterOutput);
//		 Now remove from this clipped poly data all the cells that are facing away from the viewer.
		vtkDataArray cellNormals = tmpPolyData.GetCellData().GetNormals();
		vtkPoints points = tmpPolyData.GetPoints();

		int numCells = (int)cellNormals.GetNumberOfTuples();

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(0);
//		double[] viewDir = new double[3];
//		Logger.getAnonymousLogger().log(Level.INFO, "For loop starting");
		for (int i = 0; i < numCells; ++i)
		{
			double[] n = cellNormals.GetTuple3(i);
			MathUtil.vhat(n, n);

			// Compute the direction to the viewer from one of the point of the cell.
			tmpPolyData.GetCellPoints(i, idList);
//			double[] pt = points.GetPoint(idList.GetId(0));
//			MathUtil.vsub(origin, pt, viewDir);
//			viewDir[0] = origin[0] - pt[0];
//			viewDir[1] = origin[1] - pt[1];
//			viewDir[2] = origin[2] - pt[2];
//			MathUtil.vhat(viewDir, viewDir);

			double dot = MathUtil.vdot(n, origin);
			if (dot <= 0.0)
				tmpPolyData.DeleteCell(i);
		}
//		Logger.getAnonymousLogger().log(Level.INFO, "For loop done");
		tmpPolyData.RemoveDeletedCells();
		tmpPolyData.Modified();
		tmpPolyData.GetCellData().SetNormals(null);
//		Logger.getAnonymousLogger().log(Level.INFO, "Cleaning after normals");
		vtkCleanPolyData cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInputData(tmpPolyData);
		cleanPoly.Update();
		vtkPolyData cleanPolyOutput = cleanPoly.GetOutput();

		//polyData = new vtkPolyData();
		tmpPolyData.DeepCopy(cleanPolyOutput);
//		Logger.getAnonymousLogger().log(Level.INFO, "Cleaning done");
		
//		vtkGenericCell cell = new vtkGenericCell();

		points = tmpPolyData.GetPoints();
		int numPoints = (int)points.GetNumberOfPoints();

		int[] numberOfObscuredPointsPerCell = new int[(int)tmpPolyData.GetNumberOfCells()];
		Arrays.fill(numberOfObscuredPointsPerCell, 0);

//		double tol = 1e-6;
//		double[] t = new double[1];
//		double[] x = new double[3];
//		double[] pcoords = new double[3];
//		int[] subId = new int[1];
//		int[] cell_id = new int[1];
		
		final List<Future<Void>> resultList;
		List<Callable<Void>> taskList = new ArrayList<>();

		for (int i = 0; i < numPoints; ++i)
		{
			Callable<Void> task = new ObscuredTask(polyData, points, locator, pointLocator, origin, numberOfObscuredPointsPerCell, i);
			taskList.add(task);
		}
//		Logger.getAnonymousLogger().log(Level.INFO, "Waiting for tasks " + taskList.size());
//		System.out.println("PolyDataUtil: computeFrustumIntersection: waiting for tasks " + taskList.size());
		resultList = ThreadService.submitAll(taskList);
//		System.out.println("PolyDataUtil: computeFrustumIntersection: got results");
//		Logger.getAnonymousLogger().log(Level.INFO, "Got results");
//		Logger.getAnonymousLogger().log(Level.INFO, "After convex shape "  + tmpPolyData.GetNumberOfCells());
		tmpPolyData.RemoveDeletedCells();

		//cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInputData(tmpPolyData);
		cleanPoly.Update();
		cleanPolyOutput = cleanPoly.GetOutput();

		//polyData = new vtkPolyData();
		tmpPolyData.DeepCopy(cleanPolyOutput);
		return tmpPolyData;
		
		
//		vtkPolyData result = new vtkPolyData();
//		result.DeepCopy(normalsFilter.GetOutput());
//		System.out.println("PolyDataUtil: computeVTKFrustumIntersection: result num cells " + result.GetNumberOfCells());
//		System.out.println("PolyDataUtil: computeVTKFrustumIntersection: result num points " + result.GetNumberOfPoints());
//		System.out.println("PolyDataUtil: computeVTKFrustumIntersection: result num polys " + result.GetNumberOfPolys());
//		Logger.getAnonymousLogger().log(Level.INFO, "Returning poly");
//		vtkPolyData result = new vtkPolyData();
//		vtkPolyDataWriter imageWriter = new vtkPolyDataWriter();
//	        imageWriter.SetInputData(geometryFilter.GetOutput());
//	        imageWriter.SetFileName("/Users/steelrj1/Desktop/speedtest.vtk");
//	        imageWriter.SetFileTypeToBinary();
//	        imageWriter.Write();
//		result.DeepCopy(geometryFilter.GetOutput());
//		vtkPolyDataWriter imageWriter = new vtkPolyDataWriter();
//        imageWriter.SetInputData(tmpPolyData);
//        imageWriter.SetFileName("/Users/steelrj1/Desktop/speedtest.vtk");
//        imageWriter.SetFileTypeToBinary();
//        imageWriter.Write();
//        result.DeepCopy(tmpPolyData);
//		Logger.getAnonymousLogger().log(Level.INFO, "Polydata made");
//		return result;
//		return tmpPolyData;
//		System.out.println("PolyDataUtil: computeVTKFrustumIntersection: output " + output);
//		vtkUnstructuredGrid grid = new vtkUnstructuredGrid();
//		grid.ShallowCopy(output);
//		Logger.getAnonymousLogger().log(Level.INFO, "Number of cells filtered " + grid.GetNumberOfCells());
	}
	
	public static vtkPolyData computeFrustumIntersection(vtkPolyData polyData, vtksbCellLocator locator, vtkAbstractPointLocator pointLocator, double[] origin, double[] ul, double[] ur, double[] lr, double[] ll)
	{
//		vtkPlanes planes = computeFrustumPlanes(origin, ul, ur, lr, ll);
//		vtkPlane plane1 = planes.GetPlane(0); 
//		vtkPlane plane2 = planes.GetPlane(1); 
//		vtkPlane plane3 = planes.GetPlane(2); 
//		vtkPlane plane4 = planes.GetPlane(3); 
		//printpt(origin, "origin");
		//printpt(ul, "ul");
		//printpt(ur, "ur");
		//printpt(lr, "lr");
		//printpt(ll, "ll");
		// First compute the normals of the 6 planes.
		// Start with computing the normals of the 4 side planes of the frustum.
		double[] top = new double[3];
		double[] right = new double[3];
		double[] bottom = new double[3];
		double[] left = new double[3];

		MathUtil.vcrss(ur, ul, top);
		MathUtil.vcrss(lr, ur, right);
		MathUtil.vcrss(ll, lr, bottom);
		MathUtil.vcrss(ul, ll, left);
		double dx = MathUtil.vnorm(origin);
		double[] UL2 = { origin[0] + ul[0] * dx, origin[1] + ul[1] * dx, origin[2] + ul[2] * dx };
		double[] UR2 = { origin[0] + ur[0] * dx, origin[1] + ur[1] * dx, origin[2] + ur[2] * dx };
		double[] LL2 = { origin[0] + ll[0] * dx, origin[1] + ll[1] * dx, origin[2] + ll[2] * dx };
		double[] LR2 = { origin[0] + lr[0] * dx, origin[1] + lr[1] * dx, origin[2] + lr[2] * dx };

		vtkPlane plane1 = new vtkPlane();
		plane1.SetOrigin(UL2);
		plane1.SetNormal(top);
		vtkPlane plane2 = new vtkPlane();
		plane2.SetOrigin(UR2);
		plane2.SetNormal(right);
		vtkPlane plane3 = new vtkPlane();
		plane3.SetOrigin(LR2);
		plane3.SetNormal(bottom);
		vtkPlane plane4 = new vtkPlane();
		plane4.SetOrigin(LL2);
		plane4.SetNormal(left);
//		vtkPlane backPlane = new vtkPlane();
//		backPlane.SetOrigin(0, 0, 0);
//		backPlane.SetNormal(-origin[0], -origin[1], -origin[2]);
//		vtkClipPolyData backPlaneClip = new vtkClipPolyData();
//		backPlaneClip.SetInputData(polyData);
//		backPlaneClip.SetClipFunction(backPlane);
//		backPlaneClip.SetInsideOut(1);
//		backPlaneClip.Update();
//		System.out.println("PolyDataUtil: computeFrustumIntersection: polydata size " + polyData.GetNumberOfCells());
		// I found that the results are MUCH better when you use a separate vtkClipPolyData
		// for each plane of the frustum rather than trying to use a single vtkClipPolyData
		// with an vtkImplicitBoolean or vtkPlanes that combines all the planes together.
		vtkClipPolyData clipPolyData1 = new vtkClipPolyData();
		clipPolyData1.SetInputData(polyData);
		clipPolyData1.SetClipFunction(plane1);
		clipPolyData1.SetInsideOut(1);
		vtkAlgorithmOutput clipPolyData1OutputPort = clipPolyData1.GetOutputPort();
//		System.out.println("PolyDataUtil: computeFrustumIntersection: plane 1 " + plane1);
//		System.out.println("PolyDataUtil: computeFrustumIntersection: number after plane 1 " + clipPolyData1.GetOutput().GetNumberOfCells());
		
		vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
		clipPolyData2.SetInputConnection(clipPolyData1OutputPort);
		clipPolyData2.SetClipFunction(plane2);
		clipPolyData2.SetInsideOut(1);
		vtkAlgorithmOutput clipPolyData2OutputPort = clipPolyData2.GetOutputPort();
//		System.out.println("PolyDataUtil: computeFrustumIntersection: number after plane 2 " + clipPolyData2.GetOutput().GetNumberOfCells());


		vtkClipPolyData clipPolyData3 = new vtkClipPolyData();
		clipPolyData3.SetInputConnection(clipPolyData2OutputPort);
		clipPolyData3.SetClipFunction(plane3);
		clipPolyData3.SetInsideOut(1);
		vtkAlgorithmOutput clipPolyData3OutputPort = clipPolyData3.GetOutputPort();
//		System.out.println("PolyDataUtil: computeFrustumIntersection: number after plane 3 " + clipPolyData3.GetOutput().GetNumberOfCells());


		vtkClipPolyData clipPolyData4 = new vtkClipPolyData();
		clipPolyData4.SetInputConnection(clipPolyData3OutputPort);
		clipPolyData4.SetClipFunction(plane4);
		clipPolyData4.SetInsideOut(1);
//		Logger.getAnonymousLogger().log(Level.INFO, "Clipping data");
		clipPolyData4.Update();
//		Logger.getAnonymousLogger().log(Level.INFO, "Clipped Data");
		vtkAlgorithmOutput clipPolyData4OutputPort = clipPolyData4.GetOutputPort();
		if (clipPolyData4.GetOutput().GetNumberOfCells() == 0)
		{
//			System.out.println("PolyDataUtil: computeFrustumIntersection: no cells after clipping");
			return null;
		}
//		Logger.getAnonymousLogger().log(Level.INFO, "Setting up normals filter ");
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInputConnection(clipPolyData4OutputPort);
		normalsFilter.SetComputeCellNormals(1);
		normalsFilter.SetComputePointNormals(0);
		normalsFilter.SplittingOff();
		normalsFilter.Update();
		vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
//		Logger.getAnonymousLogger().log(Level.INFO, "Normals filter output size " + normalsFilterOutput.GetNumberOfCells());
		vtkPolyData tmpPolyData = new vtkPolyData();
		tmpPolyData.DeepCopy(normalsFilterOutput);
		// Now remove from this clipped poly data all the cells that are facing away from the viewer.
		vtkDataArray cellNormals = tmpPolyData.GetCellData().GetNormals();
		vtkPoints points = tmpPolyData.GetPoints();

		int numCells = (int)cellNormals.GetNumberOfTuples();

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(0);
		double[] viewDir = new double[3];
//		Logger.getAnonymousLogger().log(Level.INFO, "Looping through cells " + tmpPolyData.GetNumberOfCells());
		double[] pt;
		double[] n;
		double dot;
		for (int i = 0; i < numCells; ++i)
		{
			n = cellNormals.GetTuple3(i);
			MathUtil.vhat(n, n);

			// Compute the direction to the viewer from one of the point of the cell.
			tmpPolyData.GetCellPoints(i, idList);
			pt = points.GetPoint(idList.GetId(0));

//			viewDir[0] = origin[0] - pt[0];
//			viewDir[1] = origin[1] - pt[1];
//			viewDir[2] = origin[2] - pt[2];
			MathUtil.vsub(origin, pt, viewDir);
			MathUtil.vhat(viewDir, viewDir);

			dot = MathUtil.vdot(n, viewDir);
			if (dot <= 0.0)
				tmpPolyData.DeleteCell(i);
		}
//		Logger.getAnonymousLogger().log(Level.INFO, "Removing deleted cells "  + tmpPolyData.GetNumberOfCells());
		tmpPolyData.RemoveDeletedCells();
		tmpPolyData.Modified();
		tmpPolyData.GetCellData().SetNormals(null);
//		Logger.getAnonymousLogger().log(Level.INFO, "After normals before cleaning " + tmpPolyData.GetNumberOfCells());
		vtkCleanPolyData cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInputData(tmpPolyData);
		cleanPoly.Update();
		vtkPolyData cleanPolyOutput = cleanPoly.GetOutput();

		//polyData = new vtkPolyData();
		tmpPolyData.DeepCopy(cleanPolyOutput);
//		Logger.getAnonymousLogger().log(Level.INFO, "Cleaned after normals " + tmpPolyData.GetNumberOfCells());
		// If the body was a convex shape we would be done now.
		// Unfortunately, since it's not, it's possible for the polydata to have multiple connected
		// pieces in view of the camera and some of these pieces are obscured by other pieces.
		// Thus first check how many connected pieces there are in the clipped polydata.
		// If there's only one, we're done. If there's more than one, we need to remove the
		// obscured cells. To remove
		// cells that are obscured by other cells we do the following: Go through every point in the
		// polydata and form a line segment connecting it to the origin (i.e. the camera
		// location). Remove all cells for which all three of its points are obscured.
		// Now you may argue that it's possible that such
		// cells are only partially obscured (if say only one of its points are obscured),
		// not fully obscured and therefore we should split
		// these cells into pieces and only throw out the pieces that are fully obscured.
		// However, doing this would require a lot more computation and I don't think
		// going this far is really necessary. You might also argue that it's possible
		// for there to be cells whos points are not obscured though the interior of the cell
		// is obscured and thus this cell should have been removed. However, I think
		// this is highly unlikely given that the cells are all very small, and it's probably
		// not worth the trouble.

		// So now, first count the number of connected pieces.
		//            if (connectivityFilter == null) connectivityFilter = new vtkPolyDataConnectivityFilter();
		//            connectivityFilter.SetInputConnection(cleanPoly.GetOutputPort());
		//            connectivityFilter.SetExtractionModeToAllRegions();
		//            connectivityFilter.Update();
		//            int numRegions = connectivityFilter.GetNumberOfExtractedRegions();
		//            System.out.println("numRegions: " + numRegions);
		//            if (numRegions == 1)
		//            {
		//                return tmpPolyData;
		//            }

		vtkGenericCell cell = new vtkGenericCell();

		points = tmpPolyData.GetPoints();
		int numPoints = (int)points.GetNumberOfPoints();

		int[] numberOfObscuredPointsPerCell = new int[(int)tmpPolyData.GetNumberOfCells()];
		Arrays.fill(numberOfObscuredPointsPerCell, 0);

		double tol = 1e-6;
		double[] t = new double[1];
		double[] x = new double[3];
		double[] pcoords = new double[3];
		int[] subId = new int[1];
		long[] cell_id = new long[1];
//		Logger.getAnonymousLogger().log(Level.INFO, "Checking obscured cells "  + tmpPolyData.GetNumberOfCells());
//		final List<Future<List<Integer>>> resultList;
//		List<Callable<List<Integer>>> taskList = new ArrayList<>();
//		
//		for (int i = 0; i < numPoints; ++i)
//		{
//			Callable<List<Integer>> task = new FilterTask(tmpPolyData, locator, origin, i);
//			taskList.add(task);
//			
//		}
//		System.out.println("PolyDataUtil: computeFrustumIntersection: getting results");
//		resultList = ThreadService.submitAll(taskList);
//		Logger.getAnonymousLogger().log(Level.INFO, "Got result list "  + tmpPolyData.GetNumberOfCells());
//		Set<Integer> allBlockedIndices = new HashSet<Integer>();
//		for (int i = 0; i < resultList.size(); i++)
//		{
//			int index = i;
//			Future<List<Integer>> future = resultList.get(i);
//			List<Integer> indices;
//			try
//			{
//				indices = future.get();
//				allBlockedIndices.addAll(indices);
//			}
//			catch (InterruptedException | ExecutionException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
//		System.out.println("PolyDataUtil: computeFrustumIntersection: all blocked indices " + allBlockedIndices.size());
//		for (Integer cellId : allBlockedIndices)
//		{
//			++numberOfObscuredPointsPerCell[cellId];
//			if (numberOfObscuredPointsPerCell[cellId] == 3)
//			{
//				System.out.println("PolyDataUtil: computeFrustumIntersection: deleting " + cellId);
//				tmpPolyData.DeleteCell(cellId);
//			}
//		}
		//SERIAL WAY
		for (int i = 0; i < numPoints; ++i)
		{
			double[] sourcePnt = points.GetPoint(i);

//			Logger.getAnonymousLogger().log(Level.INFO, "Getting result ");
			int result = locator.IntersectWithLine(origin, sourcePnt, tol, t, x, pcoords, subId, cell_id, cell);
//			Logger.getAnonymousLogger().log(Level.INFO, "Got result ");
			if (result == 1)
			{
				int ptid = (int)pointLocator.FindClosestPoint(sourcePnt);
				polyData.GetPointCells(ptid, idList);

				// The following check makes sure we don't delete any cells
				// if the intersection point happens to coincides with sourcePnt.
				// To do this we test to see of the intersected cell
				// is one of the cells which share a point with sourcePnt.
				// If it is we skip to the next point.
				if (idList.IsId(cell_id[0]) >= 0)
				{
					//System.out.println("Too close  " + i);
					continue;
				}

				tmpPolyData.GetPointCells(i, idList);
				int numPtCells = (int)idList.GetNumberOfIds();
				for (int j = 0; j < numPtCells; ++j)
				{
					// The following makes sure that only cells for which ALL three of its
					// points are obscured get deleted
					int cellId = (int)idList.GetId(j);
					++numberOfObscuredPointsPerCell[cellId];
					if (numberOfObscuredPointsPerCell[cellId] == 3)
						tmpPolyData.DeleteCell(cellId);
				}
//				Logger.getAnonymousLogger().log(Level.INFO, "After successful loop");
			}
		}
		
		//Parallel way
//		ThreadService.initialize(40);
//		final List<Future<Void>> resultList;
//		List<Callable<Void>> taskList = new ArrayList<>();
//
//		for (int i = 0; i < numPoints; ++i)
//		{
//			Callable<Void> task = new ObscuredTask(polyData, points, locator, pointLocator, origin, numberOfObscuredPointsPerCell, i);
//			taskList.add(task);
//		}
//		resultList = ThreadService.submitAll(taskList);
		
		
		tmpPolyData.RemoveDeletedCells();

		//cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInputData(tmpPolyData);
		cleanPoly.Update();
		cleanPolyOutput = cleanPoly.GetOutput();

		//polyData = new vtkPolyData();
		tmpPolyData.DeepCopy(cleanPolyOutput);
//		Logger.getAnonymousLogger().log(Level.INFO, "After convex shape cleaning "  + tmpPolyData.GetNumberOfCells());
		return tmpPolyData;
	}
	
	
	
//	private void filterObscuredFaces(vtkPolyData tmpPolyData, vtksbCellLocator locator, vtkAbstractPointLocator pointLocator, double[] origin, int i)
//	{
//		
//		
//		++numberOfObscuredPointsPerCell[cellId];
//		if (numberOfObscuredPointsPerCell[cellId] == 3)
//			tmpPolyData.DeleteCell(cellId);
//	}
	
	public static double approximateOverlapFraction(vtkTriangle surfaceTriangle, Frustum frustum) // estimate how much of a triangle is inside the frustum?
	{
		double overlap = 0;
		int nPoints = 3; // this can be replaced with some other number if face subdivision is used
		for (int i = 0; i < nPoints; i++)
		{
			double[] pt = surfaceTriangle.GetPoints().GetPoint(i);
			double[] uv = new double[2];
			frustum.computeTextureCoordinatesFromPoint(pt, 1, 1, uv, false);
			if (uv[0] >= 0 && uv[0] <= 1 && uv[1] >= 0 && uv[1] <= 1)
				overlap += 1;
		}
		return overlap / nPoints;
	}

	public static double[] computeFrustumAxisVector(double[] origin, double[] ul, double[] ur, double[] lr, double[] ll)
	{
		double[] ctr = new double[3];
		for (int i = 0; i < 3; i++)
			ctr[i] = origin[i] + (ul[i] + ur[i] + lr[i] + ll[i]) / 4.;
		return ctr;
	}

	public static double[] computePolydataPointsBarycenter(vtkPolyData polyData)
	{
		Vector3D center = Vector3D.ZERO;
		for (int i = 0; i < polyData.GetNumberOfPoints(); i++)
			center = center.add(new Vector3D(polyData.GetPoint(i)));
		return center.scalarMultiply(1. / polyData.GetNumberOfPoints()).toArray();
	}

	public static double computeFarthestFrustumPlaneDepth(vtkPolyData polyData, double[] origin, double[] ul, double[] ur, double[] lr, double[] ll)
	{
		Vector3D originVec = new Vector3D(origin);
		Vector3D farPlaneCenterVec = new Vector3D(computeFrustumAxisVector(origin, ul, ur, lr, ll));
		Vector3D axialVec = farPlaneCenterVec.subtract(originVec).normalize();
		double maxDepth = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < polyData.GetNumberOfPoints(); i++)
		{
			Vector3D dVec = new Vector3D(polyData.GetPoint(i)).subtract(originVec);
			double depth = dVec.dotProduct(axialVec);
			if (depth > maxDepth)
				maxDepth = depth;
		}
		return maxDepth;
	}

	public static vtkPolyData generateFrustumPlane(double[] origin, double[] ul, double[] ur, double[] lr, double[] ll, double depth, int[] resolution)
	{
		vtkPoints points = new vtkPoints();
		int[][] ids = new int[resolution[0]][resolution[1]];

		// set up points for plane
		Vector3D originVec = new Vector3D(origin);
		Vector3D farPlaneCenterVec = new Vector3D(computeFrustumAxisVector(origin, ul, ur, lr, ll));
		double depthNorm = depth / farPlaneCenterVec.subtract(originVec).getNorm();
		Vector3D llCrn = new Vector3D(ll).scalarMultiply(depthNorm).add(originVec);
		Vector3D lrCrn = new Vector3D(lr).scalarMultiply(depthNorm).add(originVec);
		Vector3D ulCrn = new Vector3D(ul).scalarMultiply(depthNorm).add(originVec);
		Vector3D lxVec = lrCrn.subtract(llCrn);
		Vector3D lyVec = ulCrn.subtract(llCrn);
		for (int i = 0; i < resolution[0]; i++)
			for (int j = 0; j < resolution[1]; j++)
			{
				double dx = i / ((double) resolution[0] - 1);
				double dy = j / ((double) resolution[1] - 1);
				ids[i][j] = (int)points.InsertNextPoint(llCrn.add(lxVec.scalarMultiply(dx)).add(lyVec.scalarMultiply(dy)).toArray());
			}

		// set up triangles for plane
		vtkCellArray cells = new vtkCellArray();
		for (int i = 0; i < resolution[0] - 1; i++)
			for (int j = 0; j < resolution[1] - 1; j++)
			{
				vtkTriangle tri1 = new vtkTriangle();
				tri1.GetPointIds().SetId(0, ids[i][j]);
				tri1.GetPointIds().SetId(1, ids[i + 1][j]);
				tri1.GetPointIds().SetId(2, ids[i + 1][j + 1]);
				cells.InsertNextCell(tri1);
				//
				vtkTriangle tri2 = new vtkTriangle();
				tri2.GetPointIds().SetId(0, ids[i + 1][j + 1]);
				tri2.GetPointIds().SetId(1, ids[i][j + 1]);
				tri2.GetPointIds().SetId(2, ids[i][j]);
				cells.InsertNextCell(tri2);
			}

		// init plane polydata
		vtkPolyData plane = new vtkPolyData();
		plane.SetPoints(points);
		plane.SetPolys(cells);

		return plane;
	}

	/*
	 * public static vtkPolyData cutFrustumPlaneWithPolyData( vtkPolyData polyData,
	 * double[] origin, double[] ul, double[] ur, double[] lr, double[] ll, double
	 * depth, int[] resolution) { vtkPoints points=new vtkPoints(); int[][] ids=new
	 * int[resolution[0]][resolution[1]];
	 *
	 * // set up points for plane Vector3D originVec=new Vector3D(origin); double[]
	 * ctr=new double[3]; for (int i=0; i<3; i++)
	 * ctr[i]=(ul[i]+ur[i]+lr[i]+ll[i])/4.; Vector3D farPlaneCenterVec=new
	 * Vector3D(ctr); double
	 * depthNorm=depth/farPlaneCenterVec.subtract(originVec).getNorm(); Vector3D
	 * llCrn=new
	 * Vector3D(ll).subtract(originVec).scalarMultiply(depthNorm).add(originVec);
	 * Vector3D lrCrn=new
	 * Vector3D(lr).subtract(originVec).scalarMultiply(depthNorm).add(originVec);
	 * Vector3D ulCrn=new
	 * Vector3D(ul).subtract(originVec).scalarMultiply(depthNorm).add(originVec);
	 * Vector3D lxVec=lrCrn.subtract(llCrn); Vector3D lyVec=ulCrn.subtract(llCrn);
	 * for (int i=0; i<resolution[0]; i++) for (int j=0; j<resolution[1]; j++) {
	 * double dx=(double)i/(double)resolution[0]; double
	 * dy=(double)j/(double)resolution[1];
	 * ids[i][j]=points.InsertNextPoint(llCrn.add(lxVec.scalarMultiply(dx)).add(
	 * lyVec.scalarMultiply(dy)).toArray()); }
	 *
	 * // set up triangles for plane vtkCellArray cells=new vtkCellArray(); for (int
	 * i=0; i<resolution[0]-1; i++) for (int j=0; j<resolution[1]-1; j++) {
	 * vtkTriangle tri1=new vtkTriangle(); tri1.GetPointIds().SetId(0, ids[i][j]);
	 * tri1.GetPointIds().SetId(1, ids[i+1][j]); tri1.GetPointIds().SetId(2,
	 * ids[i+1][j+1]); cells.InsertNextCell(tri1); // vtkTriangle tri2=new
	 * vtkTriangle(); tri2.GetPointIds().SetId(0, ids[i+1][j+1]);
	 * tri2.GetPointIds().SetId(1, ids[i][j+1]); tri2.GetPointIds().SetId(2,
	 * ids[i][j]); cells.InsertNextCell(tri2); }
	 *
	 * // init plane polydata vtkPolyData plane=new vtkPolyData();
	 * plane.SetPoints(points); plane.SetPolys(cells);
	 *
	 * // subtract body polydata from plane polydata
	 * vtkBooleanOperationPolyDataFilter booleanFilter=new
	 * vtkBooleanOperationPolyDataFilter(); booleanFilter.SetInputData(0, polyData);
	 * booleanFilter.SetInputData(1, plane); booleanFilter.SetTolerance(1e-12);
	 * booleanFilter.SetOperationToDifference(); booleanFilter.Update();
	 *
	 * // return result vtkPolyData result=new vtkPolyData();
	 * result.DeepCopy(booleanFilter.GetOutput()); return result; }
	 */

	/*
	 *
	 * // Old version. Doesn't work so well.
	 *
	 * private static boolean determineIfPolygonIsClockwise( vtkPolyData polyData,
	 * vtkAbstractPointLocator pointLocator, List<LatLon> controlPoints) { // To
	 * determine if a polygon is clockwise or counterclockwise we do the following:
	 * // 1. First compute the mean normal of the polygon by averaging the shape
	 * model // normals at all the control points // 2. Then compute the mean normal
	 * by summing the cross products of all adjacent // edges. // 3. If the dot
	 * product of the normals computed in steps 1 and 2 are negative, // then the
	 * polygon is counterclockwise, otherwise it's clockwise.
	 *
	 * int numPoints = controlPoints.size();
	 *
	 * // Step 1 double[] normal = {0.0, 0.0, 0.0}; for (LatLon llr : controlPoints)
	 * { double[] pt = MathUtil.latrec(llr); double[] normalAtPt =
	 * getPolyDataNormalAtPoint(pt, polyData, pointLocator); normal[0] +=
	 * normalAtPt[0]; normal[1] += normalAtPt[1]; normal[2] += normalAtPt[2]; }
	 * MathUtil.vhat(normal, normal);
	 *
	 * // Step 2 double[] normal2 = {0.0, 0.0, 0.0}; for (int i=0; i<numPoints; ++i)
	 * { double[] pt1 = MathUtil.latrec(controlPoints.get(i)); double[] pt0 = null;
	 * double[] pt2 = null; if (i == 0) { pt0 =
	 * MathUtil.latrec(controlPoints.get(numPoints-1)); pt2 =
	 * MathUtil.latrec(controlPoints.get(i+1)); } else if (i == numPoints-1) { pt0 =
	 * MathUtil.latrec(controlPoints.get(numPoints-2)); pt2 =
	 * MathUtil.latrec(controlPoints.get(0)); } else { pt0 =
	 * MathUtil.latrec(controlPoints.get(i-1)); pt2 =
	 * MathUtil.latrec(controlPoints.get(i+1)); }
	 *
	 * double[] edge0 = {pt0[0]-pt1[0], pt0[1]-pt1[1], pt0[2]-pt1[2]}; double[]
	 * edge1 = {pt2[0]-pt1[0], pt2[1]-pt1[1], pt2[2]-pt1[2]}; double[] cross = new
	 * double[3]; MathUtil.vcrss(edge0, edge1, cross);
	 *
	 * normal2[0] += cross[0]; normal2[1] += cross[1]; normal2[2] += cross[2]; }
	 * MathUtil.vhat(normal2, normal2);
	 *
	 * // Step 3 return MathUtil.vdot(normal, normal2) > 0.0; }
	 */

	private static boolean determineIfPolygonIsClockwise(vtkPolyData polyData, vtkAbstractPointLocator pointLocator, List<LatLon> controlPoints)
	{
		// To determine if a polygon is clockwise or counterclockwise we do the following:
		// 1. First compute the centroid and mean normal of the polygon by averaging the shape model
		//    normals at all the control points.
		// 2. Then project each point onto the plane formed using the centroid and normal computed from step 1
		//    and also find the projected point that is farthest from centroid.
		// 3. This farthest point is assumed to lie on the convex hull of the polygon. Therefore we can use
		//    the two edges that share this point to determine if the polygon is clockwise.
		//    (See https://en.wikipedia.org/wiki/Curve_orientation)

		int numPoints = controlPoints.size();

		// Step 1
		double[] normal = { 0.0, 0.0, 0.0 };
		double[] centroid = { 0.0, 0.0, 0.0 };
		for (LatLon llr : controlPoints)
		{
			double[] pt = MathUtil.latrec(llr);
			centroid[0] += pt[0];
			centroid[1] += pt[1];
			centroid[2] += pt[2];
			double[] normalAtPt = getPolyDataNormalAtPoint(pt, polyData, pointLocator);
			normal[0] += normalAtPt[0];
			normal[1] += normalAtPt[1];
			normal[2] += normalAtPt[2];
		}
		MathUtil.vhat(normal, normal);
		centroid[0] /= numPoints;
		centroid[1] /= numPoints;
		centroid[2] /= numPoints;

		// Step 2
		double dist = -Double.MAX_VALUE;
		int farthestProjectedPointIdx = 0;
		List<Object> projectedPoints = new ArrayList<Object>();
		for (int i = 0; i < numPoints; ++i)
		{
			double[] pt1 = MathUtil.latrec(controlPoints.get(i));
			double[] projectedPoint = new double[3];
			MathUtil.vprjp(pt1, normal, centroid, projectedPoint);
			double d = MathUtil.distance2Between(centroid, projectedPoint);
			if (d > dist)
			{
				dist = d;
				farthestProjectedPointIdx = i;
			}
			projectedPoints.add(projectedPoint);
		}

		// Step 3
		double[] pt1 = (double[]) projectedPoints.get(farthestProjectedPointIdx);
		double[] pt0 = null;
		double[] pt2 = null;
		if (farthestProjectedPointIdx == 0)
		{
			pt0 = (double[]) projectedPoints.get(numPoints - 1);
			pt2 = (double[]) projectedPoints.get(1);
		}
		else if (farthestProjectedPointIdx == numPoints - 1)
		{
			pt0 = (double[]) projectedPoints.get(numPoints - 2);
			pt2 = (double[]) projectedPoints.get(0);
		}
		else
		{
			pt0 = (double[]) projectedPoints.get(farthestProjectedPointIdx - 1);
			pt2 = (double[]) projectedPoints.get(farthestProjectedPointIdx + 1);
		}

		double[] edge0 = { pt1[0] - pt0[0], pt1[1] - pt0[1], pt1[2] - pt0[2] };
		double[] edge1 = { pt2[0] - pt1[0], pt2[1] - pt1[1], pt2[2] - pt1[2] };
		double[] cross = new double[3];
		MathUtil.vcrss(edge0, edge1, cross);
		MathUtil.vhat(cross, cross);

		return MathUtil.vdot(normal, cross) < 0.0;
	}

	/**
	 * Determine if a triangle formed from 3 consecutive vertices of a polygon
	 * contain a reflex vertex. A triangle with reflex vertex is one whose interior
	 * is actually outside of the polygon rather than inside. In order to determine
	 * if a triangle contains a reflex vertex, one must know if the polygon is
	 * clockwise or counterclockwise (which is provided as an argument to this
	 * function).
	 *
	 * @param polyData
	 * @param pointLocator
	 * @param controlPoints
	 * @param isClockwise
	 * @return
	 */
	private static boolean determineIfTriangleContainsReflexVertex(vtkPolyData polyData, vtkAbstractPointLocator pointLocator, List<LatLon> controlPoints, boolean isClockwise)
	{
		// First compute mean normal to shape model at vertices
		double[] pt1 = MathUtil.latrec(controlPoints.get(0));
		double[] pt2 = MathUtil.latrec(controlPoints.get(1));
		double[] pt3 = MathUtil.latrec(controlPoints.get(2));

		double[] normal1 = getPolyDataNormalAtPoint(pt1, polyData, pointLocator);
		double[] normal2 = getPolyDataNormalAtPoint(pt2, polyData, pointLocator);
		double[] normal3 = getPolyDataNormalAtPoint(pt3, polyData, pointLocator);
		double[] normalOfShapeModel = { (normal1[0] + normal2[0] + normal3[0]) / 3.0, (normal1[1] + normal2[1] + normal3[1]) / 3.0, (normal1[2] + normal2[2] + normal3[2]) / 3.0
		};

		// Now compute the normal of the triangle
		double[] normalOfTriangle = new double[3];
		MathUtil.triangleNormal(pt1, pt2, pt3, normalOfTriangle);

		if (isClockwise)
			return MathUtil.vdot(normalOfShapeModel, normalOfTriangle) > 0.0;
		else
			return MathUtil.vdot(normalOfShapeModel, normalOfTriangle) <= 0.0;
	}

	private static boolean determineIfNeedToReverseTriangleVertices(vtkPolyData polyData, vtkAbstractPointLocator pointLocator, List<LatLon> controlPoints)
	{
		// Determine if we need to reverse the ordering of the triangle vertices.
		// Do this as follows: Compute the mean normal to the shape model at the 3 vertices.
		// Then compute the normal to the triangle. If the two vectors face opposite direction
		// then return false, otherwise return true.

		double[] pt1 = MathUtil.latrec(controlPoints.get(0));
		double[] pt2 = MathUtil.latrec(controlPoints.get(1));
		double[] pt3 = MathUtil.latrec(controlPoints.get(2));

		double[] normal1 = getPolyDataNormalAtPoint(pt1, polyData, pointLocator);
		double[] normal2 = getPolyDataNormalAtPoint(pt2, polyData, pointLocator);
		double[] normal3 = getPolyDataNormalAtPoint(pt3, polyData, pointLocator);
		double[] normalOfShapeModel = { (normal1[0] + normal2[0] + normal3[0]) / 3.0, (normal1[1] + normal2[1] + normal3[1]) / 3.0, (normal1[2] + normal2[2] + normal3[2]) / 3.0
		};

		// Now compute the normal of the triangle
		double[] normalOfTriangle = new double[3];
		MathUtil.triangleNormal(pt1, pt2, pt3, normalOfTriangle);

		return MathUtil.vdot(normalOfShapeModel, normalOfTriangle) > 0.0;
	}

	public static void drawTriangleOnPolyData(vtkPolyData polyData, vtkAbstractPointLocator pointLocator, List<LatLon> controlPoints, vtkPolyData outputInterior, vtkPolyData outputBoundary)
	{
		if (controlPoints.size() != 3)
		{
			System.err.println("Must have exactly 3 vertices for triangle");
			return;
		}

		// Determine if we need to reverse the ordering of the vertices.
		if (determineIfNeedToReverseTriangleVertices(polyData, pointLocator, controlPoints))
		{
			// First clone the control points so we don't modify array passed into function
			controlPoints = new ArrayList<>(controlPoints);
			Collections.reverse(controlPoints);
		}

		// List holding vtk objects to delete at end of function
		List<vtkObject> d = new ArrayList<vtkObject>();

		int numberOfSides = controlPoints.size();

		vtkAlgorithmOutput nextInput = null;
		vtkClipPolyData clipPolyData = null;
		for (int i = 0; i < numberOfSides; ++i)
		{
			double[] pt1 = MathUtil.latrec(controlPoints.get(i));
			double[] pt2 = null;
			if (i < numberOfSides - 1)
				pt2 = MathUtil.latrec(controlPoints.get(i + 1));
			else
				pt2 = MathUtil.latrec(controlPoints.get(0));

			double[] normal1 = getPolyDataNormalAtPoint(pt1, polyData, pointLocator);
			double[] normal2 = getPolyDataNormalAtPoint(pt2, polyData, pointLocator);

			double[] avgNormal = new double[3];
			avgNormal[0] = (normal1[0] + normal2[0]) / 2.0;
			avgNormal[1] = (normal1[1] + normal2[1]) / 2.0;
			avgNormal[2] = (normal1[2] + normal2[2]) / 2.0;

			double[] vec1 = { pt1[0] - pt2[0], pt1[1] - pt2[1], pt1[2] - pt2[2] };

			double[] normal = new double[3];
			MathUtil.vcrss(vec1, avgNormal, normal);
			MathUtil.vhat(normal, normal);

			vtkPlane plane = new vtkPlane();
			plane.SetOrigin(pt1);
			plane.SetNormal(normal);

			//if (i > clipFilters.size()-1)
			//    clipFilters.add(new vtkClipPolyData());
			//clipPolyData = clipFilters.get(i);
			clipPolyData = new vtkClipPolyData();
			d.add(clipPolyData);
			if (i == 0)
				clipPolyData.SetInputData(polyData);
			else
				clipPolyData.SetInputConnection(nextInput);
			clipPolyData.SetClipFunction(plane);
			clipPolyData.SetInsideOut(1);
			//clipPolyData.Update();

			nextInput = clipPolyData.GetOutputPort();

			//if (i > clipOutputs.size()-1)
			//    clipOutputs.add(nextInput);
			//clipOutputs.set(i, nextInput);
		}

		clipPolyData.Update();

		vtkPolyDataConnectivityFilter connectivityFilter = new vtkPolyDataConnectivityFilter();
		d.add(connectivityFilter);
		vtkAlgorithmOutput clipPolyDataOutput = clipPolyData.GetOutputPort();
		d.add(clipPolyDataOutput);
		connectivityFilter.SetInputConnection(clipPolyDataOutput);
		connectivityFilter.SetExtractionModeToClosestPointRegion();
		connectivityFilter.SetClosestPoint(MathUtil.latrec(controlPoints.get(0)));
		connectivityFilter.Update();

		//        polyData = new vtkPolyData();
		//if (outputPolyData == null)
		//    outputPolyData = new vtkPolyData();

		if (outputInterior != null)
		{
			vtkPolyData connectivityFilterOutput = connectivityFilter.GetOutput();
			d.add(connectivityFilterOutput);
			outputInterior.DeepCopy(connectivityFilterOutput);
		}

		if (outputBoundary != null)
		{
			// Compute the bounding edges of this surface
			vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
			d.add(edgeExtracter);
			vtkAlgorithmOutput connectivityFilterOutput = connectivityFilter.GetOutputPort();
			d.add(connectivityFilterOutput);
			edgeExtracter.SetInputConnection(connectivityFilterOutput);
			edgeExtracter.BoundaryEdgesOn();
			edgeExtracter.FeatureEdgesOff();
			edgeExtracter.NonManifoldEdgesOff();
			edgeExtracter.ManifoldEdgesOff();
			edgeExtracter.Update();

			vtkPolyData edgeExtracterOutput = edgeExtracter.GetOutput();
			d.add(edgeExtracterOutput);
			outputBoundary.DeepCopy(edgeExtracterOutput);
		}

		for (vtkObject o : d)
			o.Delete();
	}

	public static void drawPolygonOnPolyData(vtkPolyData polyData, vtkAbstractPointLocator pointLocator, List<LatLon> controlPoints, vtkPolyData outputInterior, vtkPolyData outputBoundary)
	{
		if (outputInterior == null && outputBoundary == null)
		{
			return;
		}

		// If interior polydata is null, then only return boundary
		if (outputInterior == null)
		{
			drawClosedLoopOnPolyData(polyData, pointLocator, controlPoints, outputBoundary);
			return;
		}

		List<LatLon> originalControlPoints = controlPoints;
		controlPoints = new ArrayList<>(originalControlPoints);

		// Do a sort of ear-clipping algorithm to break up the polygon into triangles.
		int numTriangles = controlPoints.size() - 2;
		if (numTriangles < 1)
		{
			vtkPolyData empty = new vtkPolyData();
			outputInterior.DeepCopy(empty);
			if (outputBoundary != null)
				outputBoundary.DeepCopy(empty);
			empty.Delete();
			return;
		}

		vtkGenericCell genericCell = new vtkGenericCell();

		int[] ids = new int[3];
		List<LatLon> cp = new ArrayList<LatLon>();
		List<vtkPolyData> triangles = new ArrayList<vtkPolyData>();

		// Preallocate these arrays
		for (int i = 0; i < 3; ++i)
			cp.add(null);
		for (int i = 0; i < numTriangles; ++i)
			triangles.add(new vtkPolyData());

		boolean isClockwise = determineIfPolygonIsClockwise(polyData, pointLocator, originalControlPoints);

		for (int i = 0; i < numTriangles; ++i)
		{
			int numPoints = controlPoints.size();
			for (int j = 0; j < numPoints; ++j)
			{
				// Go through consecutive triplets of vertices and check if it is an ear.
				if (j == 0)
				{
					ids[0] = numPoints - 1;
					ids[1] = 0;
					ids[2] = 1;
				}
				else if (j == numPoints - 1)
				{
					ids[0] = numPoints - 2;
					ids[1] = numPoints - 1;
					ids[2] = 0;
				}
				else
				{
					ids[0] = j - 1;
					ids[1] = j;
					ids[2] = j + 1;
				}
				cp.set(0, controlPoints.get(ids[0]));
				cp.set(1, controlPoints.get(ids[1]));
				cp.set(2, controlPoints.get(ids[2]));

				// First check to see if it's a reflex vertex, and, if so, continue
				// to next triplet
				if (determineIfTriangleContainsReflexVertex(polyData, pointLocator, cp, isClockwise))
				{
					continue;
				}

				drawTriangleOnPolyData(polyData, pointLocator, cp, triangles.get(i), null);

				// Test if the other vertices intersect this triangle. If not, then this is a valid ear.
				vtksbCellLocator cellLocator = new vtksbCellLocator();
				cellLocator.SetDataSet(triangles.get(i));
				cellLocator.CacheCellBoundsOn();
				cellLocator.AutomaticOn();
				cellLocator.BuildLocator();

				boolean intersects = false;
				for (int k = 0; k < numPoints; ++k)
				{
					if (k != ids[0] && k != ids[1] && k != ids[2])
					{
						// See if the other points touch this triangle by intersecting a ray from the origin
						// in the direction of the point.
						double[] origin = { 0.0, 0.0, 0.0 };
						double tol = 1e-6;
						double[] t = new double[1];
						double[] x = new double[3];
						double[] pcoords = new double[3];
						int[] subId = new int[1];
						long[] cellId = new long[1];
						double[] lookPt = MathUtil.latrec(controlPoints.get(k));
						// Scale the control point
						MathUtil.vscl(2.0, lookPt, lookPt);

						int result = cellLocator.IntersectWithLine(origin, lookPt, tol, t, x, pcoords, subId, cellId, genericCell);
						if (result > 0)
						{
							intersects = true;
							break;
						}
					}
				}

				cellLocator.Delete();

				if (!intersects)
				{
					// Remove the ear point from the list of control points and break out of
					// the inner loop.
					controlPoints.remove(j);
					break;
				}
			}
		}

		// Now combine all the triangles into a single mesh.
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		appendFilter.UserManagedInputsOn();
		appendFilter.SetNumberOfInputs(triangles.size());
		for (int i = 0; i < numTriangles; ++i)
		{
			vtkPolyData poly = triangles.get(i);
			if (poly != null)
				appendFilter.SetInputDataByNumber(i, poly);
		}
		appendFilter.Update();
		vtkAlgorithmOutput appendFilterOutput = appendFilter.GetOutputPort();

		vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
		cleanFilter.PointMergingOff();
		cleanFilter.ConvertLinesToPointsOff();
		cleanFilter.ConvertPolysToLinesOff();
		cleanFilter.ConvertStripsToPolysOff();
		cleanFilter.SetInputConnection(appendFilterOutput);
		cleanFilter.Update();

		vtkPolyData cleanFilterOutput = cleanFilter.GetOutput();
		outputInterior.DeepCopy(cleanFilterOutput);

		// Note we cannot use vtkFeatureEdges since the polygon really consists of
		// multiple triangles concatenated together and we would end up having edges
		// that cut through the polygon.
		if (outputBoundary != null)
		{
			drawClosedLoopOnPolyData(polyData, pointLocator, originalControlPoints, outputBoundary);
		}

		/*
		 * vtkFeatureEdges edgeExtracter = new vtkFeatureEdges(); vtkAlgorithmOutput
		 * cleanFilterOutput = cleanFilter.GetOutputPort();
		 * edgeExtracter.SetInputConnection(cleanFilterOutput);
		 * edgeExtracter.BoundaryEdgesOn(); edgeExtracter.FeatureEdgesOff();
		 * edgeExtracter.NonManifoldEdgesOff(); edgeExtracter.ManifoldEdgesOff();
		 * edgeExtracter.Update();
		 *
		 * vtkPolyData edgeExtracterOutput = edgeExtracter.GetOutput();
		 * outputBoundary.DeepCopy(edgeExtracterOutput);
		 */
	}

	public static void drawClosedLoopOnPolyData(vtkPolyData polyData, vtkAbstractPointLocator pointLocator, List<LatLon> controlPoints, vtkPolyData outputBoundary)
	{
		int numPoints = controlPoints.size();
		if (numPoints < 2)
		{
			vtkPolyData empty = new vtkPolyData();
			if (outputBoundary != null)
				outputBoundary.DeepCopy(empty);
			empty.Delete();
			return;
		}

		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		appendFilter.UserManagedInputsOff();

		double[] pt1 = null;
		double[] pt2 = null;
		for (int i = 0; i < numPoints; ++i)
		{
			pt1 = MathUtil.latrec(controlPoints.get(i));
			if (i < numPoints - 1)
				pt2 = MathUtil.latrec(controlPoints.get(i + 1));
			else
				pt2 = MathUtil.latrec(controlPoints.get(0));

			vtkPolyData poly = VtkDrawUtil.drawPathPolyOn(polyData, pointLocator, new Vector3D(pt1), new Vector3D(pt2));

			// Remove normals (which we don't need) as this causes an error
			// in the Append filter.
			poly.GetPointData().SetNormals(null);
			appendFilter.AddInputData(poly);
		}
		appendFilter.Update();
		vtkAlgorithmOutput appendFilterOutput = appendFilter.GetOutputPort();

		vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
		cleanFilter.PointMergingOn();
		cleanFilter.SetTolerance(0.0);
		cleanFilter.SetInputConnection(appendFilterOutput);
		cleanFilter.Update();
		vtkPolyData cleanFilterOutput = cleanFilter.GetOutput();

		outputBoundary.ShallowCopy(cleanFilterOutput);
	}

	public static void drawConeOnPolyData(vtkPolyData polyData, vtkAbstractPointLocator pointLocator, double[] vertex, double[] axis, // must be unit vector
			double angle, int numberOfSides, vtkPolyData outputInterior, vtkPolyData outputBoundary)
	{
		/*
		 * double[] normal = getPolyDataNormalAtPoint(center, polyData, pointLocator);
		 *
		 *
		 * // Reduce the size of the polydata we need to process by only // considering
		 * cells within twice radius of center. //vtkSphere sphere = new vtkSphere(); if
		 * (sphere == null) sphere = new vtkSphere(); sphere.SetCenter(center);
		 * sphere.SetRadius(radius >= 0.2 ? 1.2*radius : 1.2*0.2);
		 *
		 * //vtkExtractPolyDataGeometry extract = new vtkExtractPolyDataGeometry(); if
		 * (extract == null) extract = new vtkExtractPolyDataGeometry();
		 * extract.SetImplicitFunction(sphere); extract.SetExtractInside(1);
		 * extract.SetExtractBoundaryCells(1); extract.SetInput(polyData);
		 * extract.Update(); polyData = extract.GetOutput();
		 */

		double radius = Math.tan(angle);
		double[] polygonCenter = { vertex[0] + axis[0], vertex[1] + axis[1], vertex[2] + axis[2]
		};

		vtkRegularPolygonSource polygonSource = new vtkRegularPolygonSource();
		polygonSource.SetCenter(polygonCenter);
		polygonSource.SetRadius(radius);
		polygonSource.SetNormal(axis);
		polygonSource.SetNumberOfSides(numberOfSides);
		polygonSource.SetGeneratePolygon(0);
		polygonSource.SetGeneratePolyline(0);
		polygonSource.Update();

		vtkPoints points = polygonSource.GetOutput().GetPoints();

		List<vtkClipPolyData> clipFilters = new ArrayList<vtkClipPolyData>();
		List<vtkPlane> clipPlanes = new ArrayList<vtkPlane>();
		List<vtkAlgorithmOutput> clipOutputs = new ArrayList<vtkAlgorithmOutput>(); // not sure is this one is really needed

		// randomly shuffling the order of the sides we process can speed things up
		List<Integer> sides = new ArrayList<Integer>();
		for (int i = 0; i < numberOfSides; ++i)
			sides.add(i);
		Collections.shuffle(sides);

		vtkAlgorithmOutput nextInput = null;
		vtkClipPolyData clipPolyData = null;
		for (int i = 0; i < sides.size(); ++i)
		{
			int side = sides.get(i);

			// compute normal to plane formed by this side of polygon
			double[] currentPoint = points.GetPoint(side);

			double[] nextPoint = null;
			if (side < numberOfSides - 1)
				nextPoint = points.GetPoint(side + 1);
			else
				nextPoint = points.GetPoint(0);

			double[] vec = { nextPoint[0] - currentPoint[0], nextPoint[1] - currentPoint[1], nextPoint[2] - currentPoint[2] };

			double[] vec2 = { currentPoint[0] - vertex[0], currentPoint[1] - vertex[1], currentPoint[2] - vertex[2]
			};
			double[] planeNormal = new double[3];
			MathUtil.vcrss(vec2, vec, planeNormal);
			MathUtil.vhat(planeNormal, planeNormal);

			if (i > clipPlanes.size() - 1)
				clipPlanes.add(new vtkPlane());
			vtkPlane plane = clipPlanes.get(i);
			//            vtkPlane plane = new vtkPlane();
			plane.SetOrigin(currentPoint);
			plane.SetNormal(planeNormal);

			if (i > clipFilters.size() - 1)
				clipFilters.add(new vtkClipPolyData());
			clipPolyData = clipFilters.get(i);
			//            clipPolyData = new vtkClipPolyData();
			if (i == 0)
				clipPolyData.SetInputData(polyData);
			else
				clipPolyData.SetInputConnection(nextInput);
			clipPolyData.SetClipFunction(plane);
			clipPolyData.SetInsideOut(1);
			//clipPolyData.Update();

			nextInput = clipPolyData.GetOutputPort();

			if (i > clipOutputs.size() - 1)
				clipOutputs.add(nextInput);
			clipOutputs.set(i, nextInput);
		}

		vtkPolyDataConnectivityFilter connectivityFilter = new vtkPolyDataConnectivityFilter();
		connectivityFilter.SetInputConnection(clipPolyData.GetOutputPort());
		connectivityFilter.SetExtractionModeToClosestPointRegion();
		connectivityFilter.SetClosestPoint(polygonCenter);
		connectivityFilter.Update();

		//        polyData = new vtkPolyData();
		//if (outputPolyData == null)
		//    outputPolyData = new vtkPolyData();

		if (outputInterior != null)
		{
			//            polyData.DeepCopy(connectivityFilter.GetOutput());
			outputInterior.DeepCopy(connectivityFilter.GetOutput());
		}

		if (outputBoundary != null)
		{
			// Compute the bounding edges of this surface
			vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
			edgeExtracter.SetInputConnection(connectivityFilter.GetOutputPort());
			edgeExtracter.BoundaryEdgesOn();
			edgeExtracter.FeatureEdgesOff();
			edgeExtracter.NonManifoldEdgesOff();
			edgeExtracter.ManifoldEdgesOff();
			edgeExtracter.Update();

			//polyData.DeepCopy(edgeExtracter.GetOutput());
			outputBoundary.DeepCopy(edgeExtracter.GetOutput());
		}

		//vtkPolyDataWriter writer = new vtkPolyDataWriter();
		//writer.SetInput(polygonSource.GetOutput());
		//writer.SetFileName("/tmp/coneeros.vtk");
		//writer.SetFileTypeToBinary();
		//writer.Write();

		//return polyData;
		//return outputPolyData;
	}

	public static void shiftPolyDataInNormalDirection(vtkPolyData polyData, double shiftAmount)
	{
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInputData(polyData);
		normalsFilter.SetComputeCellNormals(0);
		normalsFilter.SetComputePointNormals(1);
		normalsFilter.SplittingOff();
		normalsFilter.Update();

		vtkDataArray pointNormals = normalsFilter.GetOutput().GetPointData().GetNormals();
		vtkPoints points = polyData.GetPoints();

		int numPoints = (int)points.GetNumberOfPoints();

		for (int i = 0; i < numPoints; ++i)
		{
			double[] point = points.GetPoint(i);
			double[] normal = pointNormals.GetTuple3(i);

			point[0] += normal[0] * shiftAmount;
			point[1] += normal[1] * shiftAmount;
			point[2] += normal[2] * shiftAmount;

			points.SetPoint(i, point);
		}

		polyData.Modified();
	}

	public static void shiftPolyDataInMeanNormalDirection(vtkPolyData polyData, double shiftAmount)
	{
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInputData(polyData);
		normalsFilter.SetComputeCellNormals(0);
		normalsFilter.SetComputePointNormals(1);
		normalsFilter.SplittingOff();
		normalsFilter.Update();

		Vector3D meanNormal = new Vector3D(computePolyDataNormal(normalsFilter.GetOutput()));
		shiftPolyData(polyData, meanNormal.scalarMultiply(shiftAmount));
	}

	public static void shiftPolyData(vtkPolyData polyData, Vector3D shift)
	{
		vtkPoints points = polyData.GetPoints();
		for (int i = 0; i < points.GetNumberOfPoints(); ++i)
		{
			Vector3D newPoint = new Vector3D(points.GetPoint(i)).add(shift);
			points.SetPoint(i, newPoint.toArray());
		}
		polyData.Modified();
	}

	/**
	 * Unlike the next function, this one takes a point locator to search for
	 * closest points. This version is more useful for shifting lines and polylines
	 * while the other version is more useful for shifting individual points.
	 *
	 * @param polyLine
	 * @param polyData
	 * @param pointLocator
	 * @param shiftAmount
	 */
	public static void shiftPolyLineInNormalDirectionOfPolyData(vtkPolyData polyLine, vtkPolyData polyData, vtkAbstractPointLocator pointLocator, double shiftAmount)
	{
		vtkPointData pointData = polyData.GetPointData();
		vtkDataArray pointNormals = pointData.GetNormals();
		vtkPoints points = polyLine.GetPoints();

		int numPoints = (int)points.GetNumberOfPoints();

		for (int i = 0; i < numPoints; ++i)
		{
			double[] point = points.GetPoint(i);
			int idx = (int)pointLocator.FindClosestPoint(point);

			if (idx < 0)
				continue;

			double[] normal = pointNormals.GetTuple3(idx);

			point[0] += normal[0] * shiftAmount;
			point[1] += normal[1] * shiftAmount;
			point[2] += normal[2] * shiftAmount;

			points.SetPoint(i, point);
		}

		polyLine.Modified();

		pointData.Delete();
		pointNormals.Delete();
		points.Delete();
	}

	/**
	 * Unlike the previous function, this one takes a cell locator to look for
	 * closest points. The cell normals must also be provided as a separate input
	 * (not included the polydata). This version is more useful for shifting points
	 * while the other version is more useful for shifting lines and polylines.
	 *
	 *
	 * @param polyLine
	 * @param polyData
	 * @param cellLocator
	 * @param shiftAmount
	 */
	public static void shiftPolyLineInNormalDirectionOfPolyData(vtkPolyData polyLine, vtkPolyData polyData, vtkFloatArray polyDataCellNormals, vtksbCellLocator cellLocator, double shiftAmount)
	{
		vtkPoints points = polyLine.GetPoints();
		int numPoints = (int)points.GetNumberOfPoints();

		double[] closestPoint = new double[3];
		long[] cellId = new long[1];
		int[] subId = new int[1];
		double[] dist2 = new double[1];
		vtkGenericCell genericCell = new vtkGenericCell();

		for (int i = 0; i < numPoints; ++i)
		{
			double[] point = points.GetPoint(i);

			cellLocator.FindClosestPoint(point, closestPoint, genericCell, cellId, subId, dist2);

			if (cellId[0] < 0)
				continue;

			double[] normal = polyDataCellNormals.GetTuple3(cellId[0]);

			point[0] += normal[0] * shiftAmount;
			point[1] += normal[1] * shiftAmount;
			point[2] += normal[2] * shiftAmount;

			points.SetPoint(i, point);
		}

		polyLine.Modified();

		points.Delete();
		genericCell.Delete();
	}

	/**
	 * Compute and return the surface area of a polydata. This function assumes the
	 * cells of the polydata are all triangles.
	 *
	 * @param polydata
	 * @return
	 */
	public static double computeSurfaceArea(vtkPolyData polydata)
	{
		int numberOfCells = (int)polydata.GetNumberOfCells();

		double totalArea = 0.0;
		for (int i = 0; i < numberOfCells; ++i)
		{
			totalArea += ((vtkTriangle) polydata.GetCell(i)).ComputeArea();
		}

		return totalArea;
	}

	/**
	 * Compute and return the length of a polyline. This function assumes the cells
	 * of the polydata are all lines.
	 *
	 * @param polyline
	 * @return
	 */
	public static double computeLength(vtkPolyData polyline)
	{
		vtkPoints points = polyline.GetPoints();
		vtkCellArray lines = polyline.GetLines();
		vtkIdTypeArray idArray = lines.GetData();

		int size = (int)idArray.GetNumberOfTuples();
		double totalLength = 0.0;
		int index = 0;
		while (index < size)
		{
			int numPointsPerLine = (int)idArray.GetValue(index++);
			for (int i = 0; i < numPointsPerLine - 1; ++i)
			{
				double[] pt0 = points.GetPoint(idArray.GetValue(index));
				double[] pt1 = points.GetPoint(idArray.GetValue(++index));
				totalLength += MathUtil.distanceBetween(pt0, pt1);
			}
			++index;
		}

		return totalLength;
	}

	/**
	 * The boundary generated in getImageBorder is great, unfortunately the border
	 * consists of many lines of 2 vertices each. We, however, need a single
	 * polyline consisting of all the points. I was not able to find something in
	 * vtk that can convert this, so we will have to implement it here. Fortunately,
	 * the algorithm is pretty simple (assuming the list of lines has no
	 * intersections or other anomalies): Start with the first 2-vertex line
	 * segment. These 2 points will be the first 2 points of our new polyline we're
	 * creating. Choose the second point. Now in addition to this line segment,
	 * there is only one other line segment that contains the second point. Search
	 * for that line segment and let the other point in that line segment be the 3rd
	 * point of our polyline. Repeat this till we've formed the polyline.
	 *
	 * @param polyline
	 * @param startPoint
	 * @return
	 */

	/**
	 * This function takes a polyline
	 *
	 * @param polyline
	 * @param pt1
	 * @param id1
	 * @param pt2
	 * @param id2
	 * @return
	 */
	public static boolean convertPartOfLinesToPolyLineWithSplitting(vtkPolyData polyline, double[] pt1, int id1, double[] pt2, int id2)
	{
		vtkCellArray lines_orig = polyline.GetLines();
		vtkPoints points_orig = polyline.GetPoints();

		vtkIdTypeArray idArray = lines_orig.GetData();
		int size = (int)idArray.GetNumberOfTuples();
		//System.out.println(size);
		//System.out.println(idArray.GetNumberOfComponents());

		if (points_orig.GetNumberOfPoints() < 2)
			return true;

		if (size < 3)
		{
			System.out.println("Error: polydata corrupted");
			return false;
		}

		List<IdPair> lines = new ArrayList<IdPair>();
		for (int i = 0; i < size; i += 3)
		{
			//System.out.println(idArray.GetValue(i));
			if (idArray.GetValue(i) != 2)
			{
				System.out.println("Big problem: polydata corrupted");
				return false;
			}
			lines.add(new IdPair((int)idArray.GetValue(i + 1), (int)idArray.GetValue(i + 2)));
		}

		int newPointId1 = -1;
		int newPointId2 = -1;

		{
			newPointId1 = (int)points_orig.InsertNextPoint(pt1);
			IdPair line1 = lines.get(id1);
			int tmp = line1.id2;
			line1.id2 = newPointId1;
			IdPair line = new IdPair(newPointId1, tmp);
			lines.add(line);
		}

		{
			newPointId2 = (int)points_orig.InsertNextPoint(pt2);
			IdPair line2 = lines.get(id2);
			int tmp = line2.id2;
			line2.id2 = newPointId2;
			IdPair line = new IdPair(newPointId2, tmp);
			lines.add(line);
		}

		int startIdx = newPointId1;
		int numPoints = (int)points_orig.GetNumberOfPoints();

		// Find which line segment contains the startIdx, and move this line segment first.
		// Also make sure startIdx is the first id of the pair
		for (int i = 0; i < lines.size(); ++i)
		{
			IdPair line = lines.get(i);

			if (line.id1 == startIdx || line.id2 == startIdx)
			{
				if (line.id2 == startIdx)
				{
					// swap the pair
					line.id2 = line.id1;
					line.id1 = startIdx;
				}

				lines.remove(i);
				lines.add(0, line);
				break;
			}
		}

		// First do first direction ("left")
		IdPair line = lines.get(0);
		List<Integer> idListLeft = new ArrayList<Integer>();
		idListLeft.add(line.id1);
		idListLeft.add(line.id2);
		boolean leftDirectionSuccess = true;

		for (int i = 2; i < numPoints; ++i)
		{
			int id = line.id2;

			if (newPointId2 == idListLeft.get(idListLeft.size() - 1))
				break;
			// Find the other line segment that contains id
			for (int j = 1; j < lines.size(); ++j)
			{
				IdPair nextLine = lines.get(j);
				if (id == nextLine.id1)
				{
					idListLeft.add(nextLine.id2);

					line = nextLine;
					break;
				}
				else if (id == nextLine.id2 && line.id1 != nextLine.id1)
				{
					idListLeft.add(nextLine.id1);

					// swap the ids
					int tmp = nextLine.id1;
					nextLine.id1 = nextLine.id2;
					nextLine.id2 = tmp;

					line = nextLine;
					break;
				}

				if (j == lines.size() - 1)
				{
					/*
					 * System.out.println("Error: Could not find other line segment");
					 * System.out.println("i, j = " + i + " " + j);
					 * System.out.println("numPoints = " + numPoints);
					 * System.out.println("lines.size() = " + lines.size());
					 * System.out.println("startIdx = " + startIdx); for (int k=0; k<lines.size();
					 * ++k) { System.out.println("line " + k + " - " + lines.get(k).id1 + " " +
					 * lines.get(k).id2); }
					 */

					leftDirectionSuccess = false;
					break;
				}
			}

			if (!leftDirectionSuccess)
				break;
		}

		// Then do second direction ("right")
		line = lines.get(0);
		List<Integer> idListRight = new ArrayList<Integer>();
		idListRight.add(line.id1);
		boolean rightDirectionSuccess = true;

		for (int i = 1; i < numPoints; ++i)
		{
			int id = line.id1;

			if (newPointId2 == idListRight.get(idListRight.size() - 1))
				break;
			// Find the other line segment that contains id
			for (int j = 1; j < lines.size(); ++j)
			{
				IdPair nextLine = lines.get(j);
				if (id == nextLine.id2)
				{
					idListRight.add(nextLine.id1);

					line = nextLine;
					break;
				}
				else if (id == nextLine.id1 && line.id2 != nextLine.id2)
				{
					idListRight.add(nextLine.id2);

					// swap the ids
					int tmp = nextLine.id2;
					nextLine.id2 = nextLine.id1;
					nextLine.id1 = tmp;

					line = nextLine;
					break;
				}

				if (j == lines.size() - 1)
				{
					/*
					 * System.out.println("Error: Could not find other line segment");
					 * System.out.println("i, j = " + i + " " + j);
					 * System.out.println("numPoints = " + numPoints);
					 * System.out.println("lines.size() = " + lines.size());
					 * System.out.println("startIdx = " + startIdx); for (int k=0; k<lines.size();
					 * ++k) { System.out.println("line " + k + " - " + lines.get(k).id1 + " " +
					 * lines.get(k).id2); }
					 */

					rightDirectionSuccess = false;
					break;
				}
			}

			if (!rightDirectionSuccess)
				break;
		}

		//System.out.println("id left  " + idListLeft);
		//System.out.println("id right " + idListRight);
		//if (idListLeft.size() < idListRight.size())
		//    idList = idListLeft;
		List<Integer> idList = idListRight;
		if (leftDirectionSuccess && rightDirectionSuccess)
		{
			if (computePathLength(points_orig, idListLeft) < computePathLength(points_orig, idListRight))
				idList = idListLeft;
		}
		else if (leftDirectionSuccess && !rightDirectionSuccess)
		{
			idList = idListLeft;
		}
		else if (!leftDirectionSuccess && !rightDirectionSuccess)
		{
			System.out.println("Error: Could not find other line segment");
			System.out.println("There is likely something wrong with the shape model that is preventing");
			System.out.println("VTK from finding a connected path between the two vertices along the model.");
			System.out.println("Make sure faces have correct orientation and are connected without holes.");
			return false;
		}

		// It would be nice if the points were in the order they are drawn rather
		// than some other arbitrary order. Therefore reorder the points so that
		// the id list will just be increasing numbers in order
		int numIds = idList.size();
		vtkIdList idList2 = new vtkIdList();
		vtkPoints points = new vtkPoints();
		points.SetNumberOfPoints(numIds);
		idList2.SetNumberOfIds(numIds);
		for (int i = 0; i < numIds; ++i)
		{
			int id = idList.get(i);
			points.SetPoint(i, points_orig.GetPoint(id));
			idList2.SetId(i, i);
		}

		//System.out.println("num points: " + numPoints);
		//System.out.println("num ids: " + idList.GetNumberOfIds());
		//System.out.println(idList.size());

		polyline.SetPoints(null);
		polyline.SetPoints(points);

		polyline.SetLines(null);
		vtkCellArray new_lines = new vtkCellArray();
		new_lines.InsertNextCell(idList2);
		polyline.SetLines(new_lines);

		return true;
	}

	private static double computePathLength(vtkPoints points, List<Integer> ids)
	{
		int size = ids.size();
		double length = 0.0;

		double[] pt1 = points.GetPoint(ids.get(0));
		for (int i = 1; i < size; ++i)
		{
			double[] pt2 = points.GetPoint(ids.get(i));
			double dist = MathUtil.distanceBetween(pt1, pt2);
			length += dist;
			pt1 = pt2;
		}

		return length;
	}

	/**
	 * Utility method that computes a normal of a (arbitrary) point on the specified
	 * surface.
	 * <P>
	 * The normal is calculated by a (simple) averaging of the nearest N
	 * (aNumNearPts) number of actual points on the surface.
	 * <P>
	 * If there are less (actual) points in the surface than requested then the
	 * normal will be computed with the limited number of nearby points.
	 * <P>
	 * This method will return null if there are no nearby points.
	 *
	 * @param aPoint      The point for which the normal will be calculated.
	 * @param aSurfacePD  The {@link vtkPolyData} surface.
	 * @param aSurfacePL  The {@link vtkPointLocator} associated with the surface.
	 * @param aNumNearPts The number of (nearest) points to use in the computation.
	 */
	public static Vector3D getPolyDataNormalAtPoint(Vector3D aPoint, vtkPolyData aSurfacePD,
			vtkAbstractPointLocator aSurfacePL, int aNumNearPts)
	{
		vtkIdList vNearIL = new vtkIdList();

		aSurfacePL.FindClosestNPoints(aNumNearPts, aPoint.toArray(), vNearIL);

		// Average the normals
		double[] normArr = { 0.0, 0.0, 0.0 };

		int N = (int)vNearIL.GetNumberOfIds();
		if (N < 1)
			return null;

		vtkDataArray normals = aSurfacePD.GetPointData().GetNormals();
		for (int i = 0; i < N; ++i)
		{
			double[] tmp = normals.GetTuple3(vNearIL.GetId(i));
			normArr[0] += tmp[0];
			normArr[1] += tmp[1];
			normArr[2] += tmp[2];
		}

		normArr[0] /= N;
		normArr[1] /= N;
		normArr[2] /= N;

		vNearIL.Delete();

		return new Vector3D(normArr);
	}

	/**
	 * Utility method that computes a normal of a (arbitrary) point on the specified
	 * surface.
	 * <P>
	 * The number of (actual) points used in the computation is 20.
	 * <P>
	 * See
	 * {@link #getPolyDataNormalAtPoint(Vector3D, vtkPolyData, vtkAbstractPointLocator, int)}
	 */
	public static double[] getPolyDataNormalAtPoint(double[] aPointArr, vtkPolyData aSurfacePD,
			vtkAbstractPointLocator aSurfacePL)
	{
		// Delegate
		return getPolyDataNormalAtPoint(new Vector3D(aPointArr), aSurfacePD, aSurfacePL, 20).toArray();
	}

	/**
	 * Compute the mean normal vector over the entire vtkPolyData by averaging all
	 * the normal vectors.
	 */
	public static double[] computePolyDataNormal(vtkPolyData aSurfacePD)
	{
		// Average the normals
		double[] normArr = { 0.0, 0.0, 0.0 };

		int N = (int)aSurfacePD.GetNumberOfPoints();
		vtkDataArray normals = aSurfacePD.GetPointData().GetNormals();
		for (int i = 0; i < N; ++i)
		{
			double[] tmp = normals.GetTuple3(i);
			normArr[0] += tmp[0];
			normArr[1] += tmp[1];
			normArr[2] += tmp[2];
		}

		normArr[0] /= N;
		normArr[1] /= N;
		normArr[2] /= N;

		return normArr;
	}

	/**
	 * Get the area of a given cell. Assumes cells are triangles.
	 */
	/*
	 * // The idList parameter is needed only to avoid repeated memory // allocation
	 * when this function is called within a loop. public static double
	 * getCellArea(vtkPolyData polydata, int cellId, vtkIdList idList) {
	 * polydata.GetCellPoints(cellId, idList);
	 *
	 * int numberOfCells = idList.GetNumberOfIds(); if (numberOfCells != 3) {
	 * System.err.println("Error: Cells must have exactly 3 vertices!"); return 0.0;
	 * }
	 *
	 * double[] pt0 = polydata.GetPoint(idList.GetId(0)); double[] pt1 =
	 * polydata.GetPoint(idList.GetId(1)); double[] pt2 =
	 * polydata.GetPoint(idList.GetId(2));
	 *
	 * return MathUtil.triangleArea(pt0, pt1, pt2); }
	 */

	/**
	 *
	 * @param polydata
	 * @param pointdata
	 * @param cellId
	 * @param pt
	 * @param idList this parameter is needed only to avoid repeated memory
	 *            allocation when this function is called within a loop.
	 * @return interpolated scalar value (from a vtk 1-tuple).
	 */
	public static double interpolateWithinCell(vtkPolyData polydata, vtkDataArray pointdata, int cellId, double[] pt, vtkIdList idList)
	{
		return interpolateWithinCell(polydata, pointdata, cellId, pt, idList, 1)[0];
	}

	/**
	 *
	 * @param polydata
	 * @param pointdata
	 * @param cellId
	 * @param pt
	 * @param idList this parameter is needed only to avoid repeated memory
	 *            allocation when this function is called within a loop.
	 * @return interpolated 3-vector value (from a vtk 3-tuple).
	 */
	public static double[] interpolate3VectorWithinCell(vtkPolyData polydata, vtkDataArray pointdata, int cellId, double[] pt, vtkIdList idList)
	{
		return interpolateWithinCell(polydata, pointdata, cellId, pt, idList, 3);
	}

	/**
	 *
	 * @param polydata
	 * @param pointdata
	 * @param cellId
	 * @param pt
	 * @param idList this parameter is needed only to avoid repeated memory
	 *            allocation when this function is called within a loop.
	 * @param tupleDegree the degree of tuple returned (size of output array). Must
	 *            be 1, 2, or 3.
	 * @return interpolated vector value from a vtk N-tuple (N = 1, 2, or 3)
	 */
	public static double[] interpolateWithinCell(vtkPolyData polydata, vtkDataArray pointdata, int cellId, double[] pt, vtkIdList idList, int tupleDegree)
	{
		polydata.GetCellPoints(cellId, idList);

		int numberOfCells = (int)idList.GetNumberOfIds();
		if (numberOfCells != 3)
		{
			throw new AssertionError("Error: Cells must have exactly 3 vertices!");
		}

		double[] p1 = new double[3];
		double[] p2 = new double[3];
		double[] p3 = new double[3];
		polydata.GetPoint(idList.GetId(0), p1);
		polydata.GetPoint(idList.GetId(1), p2);
		polydata.GetPoint(idList.GetId(2), p3);

		if (tupleDegree == 1)
		{
			double v1 = pointdata.GetTuple1(idList.GetId(0));
			double v2 = pointdata.GetTuple1(idList.GetId(1));
			double v3 = pointdata.GetTuple1(idList.GetId(2));

			double result0 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1, v2, v3);

			return new double[] { result0 };
		}
		else if (tupleDegree == 2)
		{
			double[] v1 = pointdata.GetTuple2(idList.GetId(0));
			double[] v2 = pointdata.GetTuple2(idList.GetId(1));
			double[] v3 = pointdata.GetTuple2(idList.GetId(2));

			double result0 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[0], v2[0], v3[0]);
			double result1 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[1], v2[1], v3[1]);

			return new double[] { result0, result1 };
		}
		else if (tupleDegree == 3)
		{
			double[] v1 = pointdata.GetTuple3(idList.GetId(0));
			double[] v2 = pointdata.GetTuple3(idList.GetId(1));
			double[] v3 = pointdata.GetTuple3(idList.GetId(2));

			double result0 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[0], v2[0], v3[0]);
			double result1 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[1], v2[1], v3[1]);
			double result2 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[2], v2[2], v3[2]);

			return new double[] { result0, result1, result2 };
		}

		throw new IllegalArgumentException("Cannot interpolate a tuple of degree " + tupleDegree);
	}

    /**
     *
     * @param polydata
     * @param pointData
     * @param cellId
     * @param pt
     * @param idList this parameter is needed only to avoid repeated memory
     *            allocation when this function is called within a loop.
     * @param tupleDegree the degree of tuple returned (size of output array). Must
     *            be 1, 2, or 3.
     * @return interpolated vector value from a vtk N-tuple (N = 1, 2, or 3)
     */
    public static double[] interpolateWithinCell(vtkPolyData polydata, IndexableTuple pointData, int cellId, double[] pt, vtkIdList idList, int tupleDegree)
    {
        polydata.GetCellPoints(cellId, idList);

        int numberOfCells = (int)idList.GetNumberOfIds();
        if (numberOfCells != 3)
        {
            throw new AssertionError("Error: Cells must have exactly 3 vertices!");
        }

        double[] p1 = new double[3];
        double[] p2 = new double[3];
        double[] p3 = new double[3];
        polydata.GetPoint(idList.GetId(0), p1);
        polydata.GetPoint(idList.GetId(1), p2);
        polydata.GetPoint(idList.GetId(2), p3);

        double[] v1 = pointData.get((int)idList.GetId(0)).get();
        double[] v2 = pointData.get((int)idList.GetId(1)).get();
        double[] v3 = pointData.get((int)idList.GetId(2)).get();

        if (tupleDegree == 1)
        {
            double result0 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[0], v2[0], v3[0]);

            return new double[] { result0 };
        }
        else if (tupleDegree == 2)
        {
            double result0 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[0], v2[0], v3[0]);
            double result1 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[1], v2[1], v3[1]);

            return new double[] { result0, result1 };
        }
        else if (tupleDegree == 3)
        {
            double result0 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[0], v2[0], v3[0]);
            double result1 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[1], v2[1], v3[1]);
            double result2 = MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1[2], v2[2], v3[2]);

            return new double[] { result0, result1, result2 };
        }

        throw new IllegalArgumentException("Cannot interpolate a tuple of degree " + tupleDegree);
    }

    /**
	 * This function takes cell data and computes point data from it by computing an
	 * average over all cells that share that point. Cells that are large carry more
	 * weight than those that are smaller.
	 *
	 * @param polydata
	 * @param cellScalars
	 * @param pointScalars
	 */
	public static void generatePointScalarsFromCellScalars(vtkPolyData polydata, vtkFloatArray cellScalars, vtkFloatArray pointScalars)
	{
		polydata.BuildLinks(0);
		int numberOfPoints = (int)polydata.GetNumberOfPoints();

		vtkIdList idList = new vtkIdList();

		pointScalars.SetNumberOfComponents(1);
		pointScalars.SetNumberOfTuples(numberOfPoints);

		for (int i = 0; i < numberOfPoints; ++i)
		{
			polydata.GetPointCells(i, idList);
			int numberOfCells = (int)idList.GetNumberOfIds();

			/*
			 * // After writing the following, wasn't sure if it was mathematically correct.
			 * double totalArea = 0.0; double[] areas = new double[numberOfCells];
			 *
			 * for (int j=0; j<numberOfCells; ++j) { areas[j] = getCellArea(polydata,
			 * idList.GetId(j)); totalArea += areas[j]; }
			 *
			 * double pointValue = 0.0; if (totalArea > 0.0) { for (int j=0;
			 * j<numberOfCells; ++j) pointValue += (areas[j]/totalArea) *
			 * cellScalars.GetTuple1(idList.GetId(j)); } else { for (int j=0;
			 * j<numberOfCells; ++j) pointValue += cellScalars.GetTuple1(idList.GetId(j));
			 *
			 * pointValue /= (double)numberOfCells; }
			 */

			double pointValue = 0.0;

			for (int j = 0; j < numberOfCells; ++j)
				pointValue += cellScalars.GetTuple1(idList.GetId(j));

			pointValue /= numberOfCells;

			pointScalars.SetTuple1(i, pointValue);
		}
	}
	
	/**
	 * Given a frustum and a polydata footprint, generate texture coordinates for
	 * all points in the polydata assuming an image acquired with that frustum is
	 * texture mapped to it.
	 *
	 * @param frustum
	 * @param polyData
	 */
	public static void generateTextureCoordinates(Frustum frustum, int width, int height, vtkPolyData footprint)
	{
		generateTextureCoordinates(frustum.origin, frustum.ul, frustum.lr, frustum.ur, width, height, footprint);
	}

	/**
	 * Given a frustum and a polydata footprint, generate texture coordinates for
	 * all points in the polydata assuming an image acquired with that frustum is
	 * texture mapped to it.
	 *
	 * @param frustum
	 * @param polyData
	 */
	public static void generateTextureCoordinates(double[] spacecraftPosition, double[] frustum1, double[] frustum2, double[] frustum3, 
													int width, int height, vtkPolyData footprint)
	{
		int numberOfPoints = (int)footprint.GetNumberOfPoints();

		vtkPointData pointData = footprint.GetPointData();
		vtkDataArray textureCoordinates = pointData.GetTCoords();
		vtkFloatArray textureCoords = null;

		if (textureCoordinates != null && textureCoordinates instanceof vtkFloatArray)
		{
			textureCoords = (vtkFloatArray) textureCoordinates;
		}
		else
		{
			textureCoords = new vtkFloatArray();
			pointData.SetTCoords(textureCoords);
		}

		textureCoords.SetNumberOfComponents(2);
		textureCoords.SetNumberOfTuples(numberOfPoints);

		vtkPoints points = footprint.GetPoints();

		double a = MathUtil.vsep(frustum1, frustum3);
		double b = MathUtil.vsep(frustum1, frustum2);

		final double umin = 1.0 / (2.0 * height);
		final double umax = 1.0 - umin;
		final double vmin = 1.0 / (2.0 * width);
		final double vmax = 1.0 - vmin;

		double[] vec = new double[3];

		for (int i = 0; i < numberOfPoints; ++i)
		{
			double[] pt = points.GetPoint(i);

			vec[0] = pt[0] - spacecraftPosition[0];
			vec[1] = pt[1] - spacecraftPosition[1];
			vec[2] = pt[2] - spacecraftPosition[2];
			MathUtil.vhat(vec, vec);

			double d1 = MathUtil.vsep(vec, frustum1);
			double d2 = MathUtil.vsep(vec, frustum2);

			double v = (d1 * d1 + b * b - d2 * d2) / (2.0 * b);
			double u = d1 * d1 - v * v;
			if (u <= 0.0)
				u = 0.0;
			else
				u = Math.sqrt(u);

			//System.out.println(v/b + " " + u/a + " " + d1 + " " + d2);

			v = v / b;
			u = u / a;

			if (v < 0.0)
				v = 0.0;
			else if (v > 1.0)
				v = 1.0;

			if (u < 0.0)
				u = 0.0;
			else if (u > 1.0)
				u = 1.0;

			// We need to map the [0, 1] intervals into the [umin, umax] and [vmin, vmax] intervals.
			// See the comments to the function adjustTextureCoordinates in Frustum.java for
			// an explanation as to why this is necessary.
			u = (umax - umin) * u + umin;
			v = (vmax - vmin) * v + vmin;

			textureCoords.SetTuple2(i, v, u);
		}
	}

	/**
	 * Compute surface area of polydata. Unlike vtkMassProperties which can compute
	 * surface area, this one works even for non-closed surface. It simply adds the
	 * areas of all the triangles.
	 *
	 * @param polydata
	 * @return
	 */
	public static double getSurfaceArea(vtkPolyData polydata)
	{
		double area = 0.0;

		int numberOfCells = (int)polydata.GetNumberOfCells();

		for (int i = 0; i < numberOfCells; ++i)
		{
			vtkCell cell = polydata.GetCell(i);
			vtkPoints points = cell.GetPoints();
			double[] pt0 = points.GetPoint(0);
			double[] pt1 = points.GetPoint(1);
			double[] pt2 = points.GetPoint(2);

			area += MathUtil.triangleArea(pt0, pt1, pt2);
			points.Delete();
			cell.Delete();
		}

		return area;
	}

	public static void getBoundary(vtkPolyData polydata, vtkPolyData boundary)
	{
		// Compute the bounding edges of this surface
		vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
		edgeExtracter.SetInputData(polydata);
		edgeExtracter.BoundaryEdgesOn();
		edgeExtracter.FeatureEdgesOff();
		edgeExtracter.NonManifoldEdgesOff();
		edgeExtracter.ManifoldEdgesOff();
		edgeExtracter.Update();

		vtkPolyData edgeExtracterOutput = edgeExtracter.GetOutput();
		boundary.DeepCopy(edgeExtracterOutput);

		edgeExtracter.Delete();
	}

	/**
	 * Get the number of facets in a PolyData file.
	 *
	 * @param fileName the name of the file
	 * @return the number of facets.
	 */
	public static int getVTKPolyDataSize(String fileName)
	{
		vtkPolyDataReader smallBodyReader = null;
		try
		{
			smallBodyReader = new vtkPolyDataReader();
			smallBodyReader.SetFileName(fileName);
			smallBodyReader.Update();
			vtkPolyData output = smallBodyReader.GetOutput();
			return (int)output.GetNumberOfPolys();
		}
		finally
		{
			if (smallBodyReader != null)
			{
				smallBodyReader.Delete();
			}
		}
	}

	/**
	 * Read in PDS vertex file format. There are 2 variants of this file. In one the
	 * first line contains the number of points and the number of cells and then
	 * follows the points and vertices. In the other variant the first line only
	 * contains the number of points, then follows the points, then follows a line
	 * listing the number of cells followed by the cells. Support both variants
	 * here.
	 *
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static vtkPolyData loadPDSShapeModel(String filename) throws Exception
	{
		vtkPolyData polydata = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		polydata.SetPoints(points);
		polydata.SetPolys(cells);

		InputStream fs = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		// Read in the first line which list the number of points and plates
		String val = in.readLine().trim();
		String[] vals = val.split("\\s+");
		int numPoints = -1;
		int numCells = -1;
		if (vals.length == 1)
		{
			numPoints = Integer.parseInt(vals[0]);
		}
		else if (vals.length == 2)
		{
			numPoints = Integer.parseInt(vals[0]);
			numCells = Integer.parseInt(vals[1]);
		}
		else
		{
			in.close();
			throw new IOException("Format not valid");
		}

		for (int j = 0; j < numPoints; ++j)
		{
			vals = in.readLine().trim().split("\\s+");
			double x = Double.parseDouble(vals[1]);
			double y = Double.parseDouble(vals[2]);
			double z = Double.parseDouble(vals[3]);
			points.InsertNextPoint(x, y, z);
		}

		if (numCells == -1)
		{
			val = in.readLine().trim();
			numCells = Integer.parseInt(val);
		}

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(3);
		for (int j = 0; j < numCells; ++j)
		{
			vals = in.readLine().trim().split("\\s+");
			int idx1 = Integer.parseInt(vals[1]) - 1;
			int idx2 = Integer.parseInt(vals[2]) - 1;
			int idx3 = Integer.parseInt(vals[3]) - 1;
			idList.SetId(0, idx1);
			idList.SetId(1, idx2);
			idList.SetId(2, idx3);
			cells.InsertNextCell(idList);
		}

		idList.Delete();

		in.close();

		addPointNormalsToShapeModel(polydata);

		return polydata;
	}

	/**
	 * Several PDS shape models are in special format similar to standard Gaskell
	 * vertex shape models but are zero based and don't have a first column listing
	 * the id.
	 *
	 * @param filename
	 * @param inMeters If true, vertices are assumed to be in meters. If false,
	 *            assumed to be kilometers.
	 * @return
	 * @throws IOException
	 */
	public static vtkPolyData loadTempel1AndWild2ShapeModel(String filename, boolean inMeters) throws Exception
	{
		vtkPolyData polydata = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		polydata.SetPoints(points);
		polydata.SetPolys(cells);

		InputStream fs = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		// Read in the first line which lists the number of points and plates
		String val = in.readLine().trim();
		String[] vals = val.split("\\s+");
		int numPoints = -1;
		int numCells = -1;
		if (vals.length == 2)
		{
			numPoints = Integer.parseInt(vals[0]);
			numCells = Integer.parseInt(vals[1]);
		}
		else
		{
			in.close();
			throw new IOException("Format not valid");
		}

		for (int j = 0; j < numPoints; ++j)
		{
			vals = in.readLine().trim().split("\\s+");
			double x = Double.parseDouble(vals[0]);
			double y = Double.parseDouble(vals[1]);
			double z = Double.parseDouble(vals[2]);

			if (inMeters)
			{
				x /= 1000.0;
				y /= 1000.0;
				z /= 1000.0;
			}

			points.InsertNextPoint(x, y, z);
		}

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(3);
		for (int j = 0; j < numCells; ++j)
		{
			vals = in.readLine().trim().split("\\s+");
			int idx1 = Integer.parseInt(vals[0]);
			int idx2 = Integer.parseInt(vals[1]);
			int idx3 = Integer.parseInt(vals[2]);
			idList.SetId(0, idx1);
			idList.SetId(1, idx2);
			idList.SetId(2, idx3);
			cells.InsertNextCell(idList);
		}

		idList.Delete();

		in.close();

		addPointNormalsToShapeModel(polydata);

		return polydata;
	}

	/**
	 * Read in a shape model with format where each line in file consists of lat,
	 * lon, and radius, or lon, lat, and radius. Note that most of the shape models
	 * of Thomas and Stooke in this format use west longtude. The only exception is
	 * Thomas's Ida model which uses east longitude.
	 *
	 * @param filename
	 * @param westLongitude if true, assume longitude is west, if false assume east
	 * @return
	 * @throws Exception
	 */
	public static vtkPolyData loadLLRShapeModel(String filename, boolean westLongitude) throws Exception
	{

		// We need to load the file in 2 passes. In the first pass
		// we figure out the latitude/longitude spacing (both assumed same),
		// which column is latitude, and which column is longitude.
		//
		// It is assumed the following:
		// If 0 is the first field of the first column,
		// then longitude is the first column.
		// If -90 is the first field of the first column,
		// then latitude is the first column.
		// If 90 is the first field of the first column,
		// then latitude is the first column.
		//
		// These assumptions ensure that the shape models of Thomas, Stooke, and Hudson
		// are loaded in correctly. However, other shape models in some other lat, lon
		// scheme may not be loaded correctly with this function.
		//
		// In the second pass, we load the file using the values
		// determined in the first pass.

		// First pass
		double latLonSpacing = 0.0;
		int latIndex = 0;
		int lonIndex = 1;

		InputStream fs = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		{
			// We only need to look at the first 2 lines of the file
			// in the first pass to determine everything we need.
			String[] vals = in.readLine().trim().split("\\s+");
			double a1 = Double.parseDouble(vals[0]);
			double b1 = Double.parseDouble(vals[1]);
			vals = in.readLine().trim().split("\\s+");
			double a2 = Double.parseDouble(vals[0]);
			double b2 = Double.parseDouble(vals[1]);

			if (a1 == 0.0)
			{
				latIndex = 1;
				lonIndex = 0;
			}
			else if (a1 == -90.0 || a1 == 90.0)
			{
				latIndex = 0;
				lonIndex = 1;
			}
			else
			{
				System.out.println("Error occurred");
			}

			if (a1 != a2)
				latLonSpacing = Math.abs(a2 - a1);
			else if (b1 != b2)
				latLonSpacing = Math.abs(b2 - b1);
			else
				System.out.println("Error occurred");

			in.close();
		}

		// Second pass
		fs = new FileInputStream(filename);
		isr = new InputStreamReader(fs);
		in = new BufferedReader(isr);

		vtkPolyData body = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray polys = new vtkCellArray();
		body.SetPoints(points);
		body.SetPolys(polys);

		int numRows = (int) Math.round(180.0 / latLonSpacing) + 1;
		int numCols = (int) Math.round(360.0 / latLonSpacing) + 1;

		int count = 0;
		int[][] indices = new int[numRows][numCols];
		String line;
		while ((line = in.readLine()) != null)
		{
			String[] vals = line.trim().split("\\s+");
			double lat = Double.parseDouble(vals[latIndex]);
			double lon = Double.parseDouble(vals[lonIndex]);
			double rad = Double.parseDouble(vals[2]);

			int row = (int) Math.round((lat + 90.0) / latLonSpacing);
			int col = (int) Math.round(lon / latLonSpacing);

			// Only include 1 point at each pole and don't include any points
			// at longitude 360 since it's the same as longitude 0
			if ((lat == -90.0 && lon > 0.0) || (lat == 90.0 && lon > 0.0) || lon == 360.0)
			{
				indices[row][col] = -1;
			}
			else
			{
				if (westLongitude)
					lon = -lon;

				indices[row][col] = count++;
				LatLon ll = new LatLon(lat * Math.PI / 180.0, lon * Math.PI / 180.0, rad);
				double[] pt = MathUtil.latrec(ll);
				points.InsertNextPoint(pt);
			}
		}

		in.close();

		// Now add connectivity information
		int i0, i1, i2, i3;
		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(3);
		for (int m = 0; m <= numRows - 2; ++m)
			for (int n = 0; n <= numCols - 2; ++n)
			{
				// Add triangles touching south pole
				if (m == 0)
				{
					i0 = indices[m][0]; // index of south pole point
					i1 = indices[m + 1][n];
					if (n == numCols - 2)
						i2 = indices[m + 1][0];
					else
						i2 = indices[m + 1][n + 1];

					if (i0 >= 0 && i1 >= 0 && i2 >= 0)
					{
						idList.SetId(0, i2);
						idList.SetId(1, i1);
						idList.SetId(2, i0);
						polys.InsertNextCell(idList);
					}
					else
					{
						System.out.println("Error occurred");
					}

				}
				// Add triangles touching north pole
				else if (m == numRows - 2)
				{
					i0 = indices[m + 1][0]; // index of north pole point
					i1 = indices[m][n];
					if (n == numCols - 2)
						i2 = indices[m][0];
					else
						i2 = indices[m][n + 1];

					if (i0 >= 0 && i1 >= 0 && i2 >= 0)
					{
						idList.SetId(0, i2);
						idList.SetId(1, i1);
						idList.SetId(2, i0);
						polys.InsertNextCell(idList);
					}
					else
					{
						System.out.println("Error occurred");
					}
				}
				// Add middle triangles that do not touch either pole
				else
				{
					// Get the indices of the 4 corners of the rectangle to the upper right
					i0 = indices[m][n];
					i1 = indices[m + 1][n];
					if (n == numCols - 2)
					{
						i2 = indices[m][0];
						i3 = indices[m + 1][0];
					}
					else
					{
						i2 = indices[m][n + 1];
						i3 = indices[m + 1][n + 1];
					}

					// Add upper left triangle
					if (i0 >= 0 && i1 >= 0 && i2 >= 0)
					{
						idList.SetId(0, i2);
						idList.SetId(1, i1);
						idList.SetId(2, i0);
						polys.InsertNextCell(idList);
					}
					else
					{
						System.out.println("Error occurred");
					}

					// Add bottom right triangle
					if (i2 >= 0 && i1 >= 0 && i3 >= 0)
					{
						idList.SetId(0, i3);
						idList.SetId(1, i1);
						idList.SetId(2, i2);
						polys.InsertNextCell(idList);
					}
					else
					{
						System.out.println("Error occurred");
					}
				}
			}

		//vtkPolyDataWriter writer = new vtkPolyDataWriter();
		//writer.SetInput(body);
		//writer.SetFileName("/tmp/coneeros.vtk");
		////writer.SetFileTypeToBinary();
		//writer.Write();

		addPointNormalsToShapeModel(body);

		return body;
	}

	/**
	 * This function is used to load the Eros model based on NLR data available from
	 * http://sbn.psi.edu/pds/resource/nearbrowse.html. It is very similar to the
	 * previous function but with several subtle differences.
	 *
	 * @param filename
	 * @param westLongitude
	 * @return
	 * @throws Exception
	 */
	public static vtkPolyData loadLLR2ShapeModel(String filename, boolean westLongitude) throws Exception
	{

		double latLonSpacing = 1.0;
		int latIndex = 1;
		int lonIndex = 0;

		InputStream fs = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		vtkPolyData body = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray polys = new vtkCellArray();
		body.SetPoints(points);
		body.SetPolys(polys);

		int numRows = (int) Math.round(180.0 / latLonSpacing) + 2;
		int numCols = (int) Math.round(360.0 / latLonSpacing);

		int count = 0;
		int[][] indices = new int[numRows][numCols];
		String line;
		double[] northPole = { 0.0, 0.0, 0.0 };
		double[] southPole = { 0.0, 0.0, 0.0 };

		indices[0][0] = count++;
		points.InsertNextPoint(southPole); // placeholder for south pole

		while ((line = in.readLine()) != null)
		{
			String[] vals = line.trim().split("\\s+");
			double lat = Double.parseDouble(vals[latIndex]);
			double lon = Double.parseDouble(vals[lonIndex]);
			double rad = Double.parseDouble(vals[2]) / 1000.0;

			int row = (int) Math.round((lat + 89.5) / latLonSpacing) + 1;
			int col = (int) Math.round((lon - 0.5) / latLonSpacing);

			if (westLongitude)
				lon = -lon;

			indices[row][col] = count++;
			LatLon ll = new LatLon(lat * Math.PI / 180.0, lon * Math.PI / 180.0, rad);
			double[] pt = MathUtil.latrec(ll);
			points.InsertNextPoint(pt);

			// We need to compute the pole points (not included in the file)
			// by avereging the points at latitudes 89.5 and -89.5
			if (lat == -89.5)
			{
				southPole[0] += pt[0];
				southPole[1] += pt[1];
				southPole[2] += pt[2];
			}
			else if (lat == 89.5)
			{
				northPole[0] += pt[0];
				northPole[1] += pt[1];
				northPole[2] += pt[2];
			}
		}

		in.close();

		for (int i = 0; i < 3; ++i)
		{
			southPole[i] /= 360.0;
			northPole[i] /= 360.0;
		}

		points.SetPoint(0, southPole);

		indices[numRows - 1][0] = count++;
		points.InsertNextPoint(northPole); // north pole

		// Now add connectivity information
		int i0, i1, i2, i3;
		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(3);
		for (int m = 0; m <= numRows - 2; ++m)
			for (int n = 0; n <= numCols - 1; ++n)
			{
				// Add triangles touching south pole
				if (m == 0)
				{
					i0 = indices[m][0]; // index of south pole point
					i1 = indices[m + 1][n];
					if (n == numCols - 1)
						i2 = indices[m + 1][0];
					else
						i2 = indices[m + 1][n + 1];

					if (i0 >= 0 && i1 >= 0 && i2 >= 0)
					{
						idList.SetId(0, i2);
						idList.SetId(1, i1);
						idList.SetId(2, i0);
						polys.InsertNextCell(idList);
					}
					else
					{
						System.out.println("Error occurred");
					}

				}
				// Add triangles touching north pole
				else if (m == numRows - 2)
				{
					i0 = indices[m + 1][0]; // index of north pole point
					i1 = indices[m][n];
					if (n == numCols - 1)
						i2 = indices[m][0];
					else
						i2 = indices[m][n + 1];

					if (i0 >= 0 && i1 >= 0 && i2 >= 0)
					{
						idList.SetId(0, i2);
						idList.SetId(1, i1);
						idList.SetId(2, i0);
						polys.InsertNextCell(idList);
					}
					else
					{
						System.out.println("Error occurred");
					}
				}
				// Add middle triangles that do not touch either pole
				else
				{
					// Get the indices of the 4 corners of the rectangle to the upper right
					i0 = indices[m][n];
					i1 = indices[m + 1][n];
					if (n == numCols - 1)
					{
						i2 = indices[m][0];
						i3 = indices[m + 1][0];
					}
					else
					{
						i2 = indices[m][n + 1];
						i3 = indices[m + 1][n + 1];
					}

					// Add upper left triangle
					if (i0 >= 0 && i1 >= 0 && i2 >= 0)
					{
						idList.SetId(0, i2);
						idList.SetId(1, i1);
						idList.SetId(2, i0);
						polys.InsertNextCell(idList);
					}
					else
					{
						System.out.println("Error occurred");
					}

					// Add bottom right triangle
					if (i2 >= 0 && i1 >= 0 && i3 >= 0)
					{
						idList.SetId(0, i3);
						idList.SetId(1, i1);
						idList.SetId(2, i2);
						polys.InsertNextCell(idList);
					}
					else
					{
						System.out.println("Error occurred");
					}
				}
			}

		addPointNormalsToShapeModel(body);

		return body;
	}

	public static vtkPolyData loadVTKShapeModel(String filename) throws Exception
	{
		vtkPolyDataReader smallBodyReader = new vtkPolyDataReader();
		smallBodyReader.SetFileName(filename);
		smallBodyReader.Update();

		vtkPolyData output = smallBodyReader.GetOutput();

		vtkPolyData shapeModel = new vtkPolyData();
		shapeModel.ShallowCopy(output);

		smallBodyReader.Delete();

		addPointNormalsToShapeModel(shapeModel);

		return shapeModel;
	}

	public static vtkPolyData loadOBJShapeModel(String filename) throws Exception
	{
		vtkOBJReader smallBodyReader = new vtkOBJReader();
		smallBodyReader.SetFileName(filename);
		smallBodyReader.Update();

		vtkPolyData output = smallBodyReader.GetOutput();

		vtkPolyData shapeModel = new vtkPolyData();
		shapeModel.ShallowCopy(output);

		smallBodyReader.Delete();

		addPointNormalsToShapeModel(shapeModel);
		return shapeModel;
	}

	public static vtkPolyData loadPLYShapeModel(String filename) throws Exception
	{
		vtkPLYReader smallBodyReader = new vtkPLYReader();
		smallBodyReader.SetFileName(filename);
		smallBodyReader.Update();

		vtkPolyData output = smallBodyReader.GetOutput();

		vtkPolyData shapeModel = new vtkPolyData();
		shapeModel.ShallowCopy(output);

		smallBodyReader.Delete();

		addPointNormalsToShapeModel(shapeModel);

		return shapeModel;
	}

	public static vtkPolyData loadSTLShapeModel(String filename) throws Exception
	{
		vtkSTLReader smallBodyReader = new vtkSTLReader();
		smallBodyReader.SetFileName(filename);
		smallBodyReader.Update();

		vtkPolyData output = smallBodyReader.GetOutput();

		vtkPolyData shapeModel = new vtkPolyData();
		shapeModel.ShallowCopy(output);

		smallBodyReader.Delete();

		addPointNormalsToShapeModel(shapeModel);

		return shapeModel;
	}

	public static vtkPolyData loadFITShapeModel(String filename) throws Exception
	{
		vtkPoints points = new vtkPoints();
		vtkCellArray polys = new vtkCellArray();
		vtkPolyData shapeModel = new vtkPolyData();
		vtkIdList idList = new vtkIdList();
		shapeModel.SetPoints(points);
		shapeModel.SetPolys(polys);

		Fits f = new Fits(filename);
		BasicHDU hdu = f.getHDU(0);

		// First pass, figure out number of planes and grab size and scale information
		Header header = hdu.getHeader();
		HeaderCard headerCard;
		int xIdx = -1;
		int yIdx = -1;
		int zIdx = -1;
		int planeCount = 0;
		while ((headerCard = header.nextCard()) != null)
		{
			String headerKey = headerCard.getKey();
			String headerValue = headerCard.getValue();

			if (headerKey.startsWith("PLANE"))
			{
				// Determine if we are looking at a coordinate or a backplane
				if (headerValue.startsWith("X"))
				{
					// This plane is the X coordinate, save the index
					xIdx = planeCount;
				}
				else if (headerValue.startsWith("Y"))
				{
					// This plane is the Y coordinate, save the index
					yIdx = planeCount;
				}
				else if (headerValue.startsWith("Z"))
				{
					// This plane is the Z coordinate, save the index
					zIdx = planeCount;
				}

				// Increment plane count
				planeCount++;
			}
		}

		// Check to see if x,y,z planes were all defined
		if (xIdx < 0)
		{
			f.close();
			throw new IOException("FITS file does not contain plane for X coordinate");
		}
		else if (yIdx < 0)
		{
			f.close();
			throw new IOException("FITS file does not contain plane for Y coordinate");
		}
		else if (zIdx < 0)
		{
			f.close();
			throw new IOException("FITS file does not contain plane for Z coordinate");
		}

		// Check dimensions of actual data
		int[] axes = hdu.getAxes();
//		if (axes.length != 3 || axes[1] != axes[2])
//		{
//			throw new IOException("FITS file has incorrect dimensions");
//		}

		int liveSize = axes[1];
		int liveSize2 = axes[2];

		float[][][] data = (float[][][]) hdu.getData().getData();
		f.getStream().close();

		int[][] indices = new int[liveSize][liveSize2];
		int c = 0;
		float x, y, z;
		float INVALID_VALUE = -1.0e38f;

		// First add points to the vtkPoints array
		for (int m = 0; m < liveSize; ++m)
			for (int n = 0; n < liveSize2; ++n)
			{
				indices[m][n] = -1;

				// A pixel value of -1.0e38 means that pixel is invalid and should be skipped
				x = data[xIdx][m][n];
				y = data[yIdx][m][n];
				z = data[zIdx][m][n];

				// Check to see if x,y,z values are all valid
				boolean valid = x != INVALID_VALUE && y != INVALID_VALUE && z != INVALID_VALUE;

				// Only add point if everything is valid
				if (valid)
				{
					points.InsertNextPoint(x, y, z);
					indices[m][n] = c;
					++c;
				}
			}

		idList.SetNumberOfIds(3);

		// Now add connectivity information
		int i0, i1, i2, i3;
		for (int m = 1; m < liveSize; ++m)
			for (int n = 1; n < liveSize2; ++n)
			{
				// Get the indices of the 4 corners of the rectangle to the upper left
				i0 = indices[m - 1][n - 1];
				i1 = indices[m][n - 1];
				i2 = indices[m - 1][n];
				i3 = indices[m][n];

				// Add upper left triangle
				if (i0 >= 0 && i1 >= 0 && i2 >= 0)
				{
					idList.SetId(0, i1);
					idList.SetId(1, i2);
					idList.SetId(2, i0);
					polys.InsertNextCell(idList);
				}
				// Add bottom right triangle
				if (i2 >= 0 && i1 >= 0 && i3 >= 0)
				{
					idList.SetId(0, i1);
					idList.SetId(1, i2);
					idList.SetId(2, i3);
					polys.InsertNextCell(idList);
				}
			}

		addPointNormalsToShapeModel(shapeModel);
		f.close();
		return shapeModel;
	}

	/**
	 * This function loads a shape model in a variety of formats. It looks at its
	 * file extension to determine it format. It supports these formats: 1. VTK
	 * (.vtk extension) 2. OBJ (.obj extension) 3. PDS vertex style shape models
	 * (.pds, .plt, or .tab extension) 4. Lat, lon, radius format also used in PDS
	 * shape models (.llr extension)
	 *
	 * This function also adds normal vectors to the returned polydata, if not
	 * available in the file.
	 *
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static vtkPolyData loadShapeModel(String filename) throws Exception
	{
		vtkPolyData shapeModel = new vtkPolyData();
		if (filename.toLowerCase().endsWith(".vtk"))
		{
			shapeModel = loadVTKShapeModel(filename);
		}
		else if (filename.toLowerCase().endsWith(".obj") || filename.toLowerCase().endsWith(".wf"))
		{
			shapeModel = loadOBJShapeModel(filename);
		}
		else if (filename.toLowerCase().endsWith(".pds") || filename.toLowerCase().endsWith(".plt") || filename.toLowerCase().endsWith(".tab"))
		{
			shapeModel = loadPDSShapeModel(filename);
		}
		else if (filename.toLowerCase().endsWith(".llr"))
		{
			boolean westLongitude = true;
			// Thomas's Ida shape model uses east longitude. All the others use west longitude.
			// TODO rather than hard coding this check in, need better way to decide if model
			// uses west or east longitude.
			if (filename.toLowerCase().contains("thomas") && filename.toLowerCase().contains("243ida"))
				westLongitude = false;
			shapeModel = loadLLRShapeModel(filename, westLongitude);
		}
		else if (filename.toLowerCase().endsWith(".llr2"))
		{
			shapeModel = loadLLR2ShapeModel(filename, false);
		}
		else if (filename.toLowerCase().endsWith(".t1"))
		{
			shapeModel = loadTempel1AndWild2ShapeModel(filename, false);
		}
		else if (filename.toLowerCase().endsWith(".w2"))
		{
			shapeModel = loadTempel1AndWild2ShapeModel(filename, true);
		}
		else if (filename.toLowerCase().endsWith(".ply"))
		{
			shapeModel = loadPLYShapeModel(filename);
		}
		else if (filename.toLowerCase().endsWith(".stl"))
		{
			shapeModel = loadSTLShapeModel(filename);
		}
		else
		{
			throw new RuntimeException("Error: Unrecognized extension in file name " + filename);
		}

//		addPointNormalsToShapeModel(shapeModel, true);

		return shapeModel;
	}

	public static void addPointNormalsToShapeModel(vtkPolyData polydata)
	{
		if (polydata.GetPointData().GetNormals() == null)
		{
			// Add normal vectors
			vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
			normalsFilter.SetInputData(polydata);
			normalsFilter.SetComputeCellNormals(0);
			normalsFilter.SetComputePointNormals(1);
			normalsFilter.SplittingOff();
			normalsFilter.FlipNormalsOff();
			normalsFilter.Update();

			vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
			polydata.DeepCopy(normalsFilterOutput);
			normalsFilter.Delete();
		}
	}

	public static void saveShapeModelAsPLT(vtkPolyData polydata, File aFile) throws IOException
	{
		// This saves it out in exactly the same format as Bob Gaskell's shape
		// models including precision and field width. That's why there's
		// extra space padded at the end to make all lines the same length.

		FileWriter fstream = new FileWriter(aFile);
		BufferedWriter out = new BufferedWriter(fstream);

		vtkPoints points = polydata.GetPoints();

		int numberPoints = (int)polydata.GetNumberOfPoints();
		int numberCells = (int)polydata.GetNumberOfCells();
		out.write(String.format("%12d %12d                              \r\n", numberPoints, numberCells));

		double[] p = new double[3];
		for (int i = 0; i < numberPoints; ++i)
		{
			points.GetPoint(i, p);
			out.write(String.format("%10d%15.5f%15.5f%15.5f\r\n", (i + 1), p[0], p[1], p[2]));
		}

		polydata.BuildCells();
		vtkIdList idList = new vtkIdList();
		for (int i = 0; i < numberCells; ++i)
		{
			polydata.GetCellPoints(i, idList);
			int id0 = (int)idList.GetId(0);
			int id1 = (int)idList.GetId(1);
			int id2 = (int)idList.GetId(2);
			out.write(String.format("%10d%10d%10d%10d               \r\n", (i + 1), (id0 + 1), (id1 + 1), (id2 + 1)));
		}

		idList.Delete();
		out.close();
	}

	public static void saveShapeModelAsOBJ(vtkPolyData polydata, File aFile) throws IOException
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(aFile);
			saveShapeModelAsOBJ(polydata, fos);
		}
		catch (Throwable th)
		{
			th.printStackTrace();
		}
		finally
		{
			if (fos != null)
			{
				fos.close();
			}
		}
	}

	public static void saveShapeModelAsOBJ(vtkPolyData polydata, OutputStream stream) throws IOException
	{
		// This saves it out in OBJ format

		OutputStreamWriter fstream = new OutputStreamWriter(stream);
		BufferedWriter out = new BufferedWriter(fstream);

		vtkPoints points = polydata.GetPoints();

		int numberPoints = (int)polydata.GetNumberOfPoints();
		int numberCells = (int)polydata.GetNumberOfCells();

		double[] p = new double[3];
		for (int i = 0; i < numberPoints; ++i)
		{
			points.GetPoint(i, p);
			out.write("v " + (float) p[0] + " " + (float) p[1] + " " + (float) p[2] + "\r\n");
		}

		polydata.BuildCells();
		vtkIdList idList = new vtkIdList();
		for (int i = 0; i < numberCells; ++i)
		{
			polydata.GetCellPoints(i, idList);
			int id0 = (int)idList.GetId(0);
			int id1 = (int)idList.GetId(1);
			int id2 = (int)idList.GetId(2);
			out.write("f " + (id0 + 1) + " " + (id1 + 1) + " " + (id2 + 1) + "\r\n");
		}

		idList.Delete();
		out.close();
	}

	public static void saveShapeModelAsVTK(vtkPolyData polydata, File aFile) throws IOException
	{
		// First make a copy of polydata and remove all cell and point data since we don't want to save that out
		vtkPolyData newpolydata = new vtkPolyData();
		newpolydata.DeepCopy(polydata);
		newpolydata.GetPointData().Reset();
		newpolydata.GetCellData().Reset();

		// regenerate point normals
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInputData(newpolydata);
		normalsFilter.SetComputeCellNormals(0);
		normalsFilter.SetComputePointNormals(1);
		normalsFilter.AutoOrientNormalsOn();
		normalsFilter.SplittingOff();
		normalsFilter.Update();

		vtkPolyDataWriter writer = new vtkPolyDataWriter();
		writer.SetInputConnection(normalsFilter.GetOutputPort());
		writer.SetFileName(aFile.getAbsolutePath());
		writer.SetFileTypeToBinary();
		writer.Write();
	}

	public static void saveShapeModelAsSTL(vtkPolyData polydata, File aFile) throws IOException
	{
		// First make a copy of polydata and remove all cell and point data since we don't want to save that out
		vtkPolyData newpolydata = new vtkPolyData();
		newpolydata.DeepCopy(polydata);
		newpolydata.GetPointData().Reset();
		newpolydata.GetCellData().Reset();

		vtkSTLWriter writer = new vtkSTLWriter();
		writer.SetInputData(newpolydata);
		writer.SetFileName(aFile.getAbsolutePath());
		writer.SetFileTypeToBinary();
		writer.Write();
	}

	public static void removeDuplicatePoints(vtkPolyData polydata) throws Exception
	{
		vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
		cleanFilter.PointMergingOn();
		cleanFilter.SetTolerance(0.0);
		cleanFilter.ConvertLinesToPointsOff();
		cleanFilter.ConvertPolysToLinesOff();
		cleanFilter.ConvertStripsToPolysOff();
		cleanFilter.SetInputData(polydata);
		cleanFilter.Update();
		vtkPolyData cleanOutput = cleanFilter.GetOutput();

		polydata.DeepCopy(cleanOutput);
	}

	public static void decimatePolyData(vtkPolyData polydata, double targetReduction)
	{
		vtkDecimatePro dec = new vtkDecimatePro();
		dec.SetInputData(polydata);
		dec.SetTargetReduction(targetReduction);
		dec.PreserveTopologyOn();
		dec.SplittingOff();
		dec.BoundaryVertexDeletionOff();
		dec.SetMaximumError(Double.MAX_VALUE);
		dec.AccumulateErrorOn();
		dec.PreSplitMeshOn();
		dec.Update();
		vtkPolyData decOutput = dec.GetOutput();

		polydata.DeepCopy(decOutput);

		dec.Delete();
	}
}
