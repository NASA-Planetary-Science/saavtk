package edu.jhuapl.saavtk.vtk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkAbstractPointLocator;
import vtk.vtkAlgorithmOutput;
import vtk.vtkCellArray;
import vtk.vtkClipPolyData;
import vtk.vtkCutter;
import vtk.vtkExtractPolyDataGeometry;
import vtk.vtkFeatureEdges;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkObject;
import vtk.vtkPlane;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataConnectivityFilter;
import vtk.vtkRegularPolygonSource;
import vtk.vtkSphere;
import vtk.vtkTransform;
import vtk.vtkTransformPolyDataFilter;
import vtk.vtksbCellLocator;

/**
 * Collection of VTK based draw routines.
 *
 * @author lopeznr1
 */
public class VtkDrawUtil
{
	/**
	 * Utility method for drawing an ellipse onto a {@link vtkPolyData} surface.
	 * <P>
	 * The results of the rendered ellipse will be returned via the last two
	 * arguments (aRetInteriourPD, aRetExteriorPD).
	 * <P>
	 * The basis of this method originated from (prior to 2018Jan)
	 * edu.jhuapl.saavtk.util.PolyDataUtil.
	 *
	 * @param aSurfacePD     The {@link vtkPolyData} corresponding to the surface.
	 * @param aSurfacePL     The {@link vtkPointLocator} associated with the
	 *                       surface.
	 * @param aCenter        The ellipse's center.
	 * @param aMajorRadius   The ellipse's (semi-major axis) radius.
	 * @param aFlattening    The ellipse's flattening factor.
	 * @param aAngle         The angle between the semi-major axis and the line of
	 *                       longitude.
	 * @param aNumSides      The number of sides of the ellipse.
	 * @param aRetInteriorPD {@link vtkPolyData} that is used to store the computed
	 *                       interior. May be null.
	 * @param aRetExteriorPD {@link vtkPolyData} that is used to store the computed
	 *                       exterior. May be null.
	 */
	public static void drawEllipseOn(vtkPolyData aSurfacePD, vtkAbstractPointLocator aSurfacePL, Vector3D aCenter,
			double aMajorRadius, double aFlattening, double aAngle, int aNumSides, vtkPolyData aRetInteriorPD,
			vtkPolyData aRetExteriorPD)
	{
		// List holding vtk objects to delete at end of function
		List<vtkObject> deleteL = new ArrayList<>();

		// Retrieve the normal at the center of the ellipse
		Vector3D centerNormal = PolyDataUtil.getPolyDataNormalAtPoint(aCenter, aSurfacePD, aSurfacePL, 20);

		// If the number of points are too small, then vtkExtractPolyDataGeometry
		// as used here might fail, so skip this part (which is just an optimization
		// not really needed when the points are few) in this case.
		if (aSurfacePD.GetNumberOfPoints() >= 20000)
		{
			// Reduce the size of the polydata we need to process by only considering cells
			// within 1.2 times the radius. We make sure, however, that if the radius is
			// below a threshold to not go below it. The threshold is chosen to be 0.2 for
			// Eros, which is equal to the bounding box diagonal length divided by about
			// 193. For other bodies it will be different, depending on the diagonal length.
			BoundingBox boundingBox = new BoundingBox(aSurfacePD.GetBounds());
			double minRadius = boundingBox.getDiagonalLength() / 193.30280166816735;

			vtkSphere vSphereS = new vtkSphere();
			deleteL.add(vSphereS);
			vSphereS.SetCenter(aCenter.toArray());

			double tmpRadius = aMajorRadius;
			if (tmpRadius < minRadius)
				tmpRadius = minRadius;
			tmpRadius *= 1.2;

			vSphereS.SetRadius(tmpRadius);

			// Define the "sub" surface - intersection between input surface and the sphere
			vtkExtractPolyDataGeometry vExtractEPDG = new vtkExtractPolyDataGeometry();
			deleteL.add(vExtractEPDG);
			vExtractEPDG.SetImplicitFunction(vSphereS);
			vExtractEPDG.SetExtractInside(1);
			vExtractEPDG.SetExtractBoundaryCells(1);
			vExtractEPDG.SetInputData(aSurfacePD);
			vExtractEPDG.Update();
			vtkPolyData vSubSurfacePD = vExtractEPDG.GetOutput();
			deleteL.add(vSubSurfacePD);

			// Use the "sub" surface as the source surface
			aSurfacePD = vSubSurfacePD;
		}

		vtkRegularPolygonSource vPolygonRPS = new vtkRegularPolygonSource();
		deleteL.add(vPolygonRPS);
		// vPolygonRPS.SetCenter(centerArr);
		vPolygonRPS.SetRadius(aMajorRadius);
		// vPolygonRPS.SetNormal(centerNormal.toArray());
		vPolygonRPS.SetNumberOfSides(aNumSides);
		vPolygonRPS.SetGeneratePolygon(0);
		vPolygonRPS.SetGeneratePolyline(0);

		// Now transform the regular polygon to turn it into an ellipse
		// Apply the following transformations in this order
		// 1. Scale in xy plane to specified flattening
		// 2. Rotate around z axis by specified angle
		// 3. Rotate so normal is normal to surface at center
		// 4. Translate to center

		// First compute cross product of normal and z-axis
		Vector3D zAxis = Vector3D.PLUS_K;
		Vector3D cross = zAxis.crossProduct(centerNormal);

		// Compute angle between normal and z-axis
		double sepAngle = Vector3D.angle(centerNormal, zAxis);
		sepAngle = Math.toDegrees(sepAngle);

		vtkTransform vTransformT = new vtkTransform();
		deleteL.add(vTransformT);
		vTransformT.Translate(aCenter.toArray());
		vTransformT.RotateWXYZ(sepAngle, cross.toArray());
		vTransformT.RotateZ(aAngle);
		vTransformT.Scale(1.0, aFlattening, 1.0);

		// XXX: at this point in the code the specified center and the transformed
		// origin match up

		vtkTransformPolyDataFilter vTransformTPDF = new vtkTransformPolyDataFilter();
		deleteL.add(vTransformTPDF);
		vtkAlgorithmOutput vPolygonAO = vPolygonRPS.GetOutputPort();
		deleteL.add(vPolygonAO);
		vTransformTPDF.SetInputConnection(vPolygonAO);
		vTransformTPDF.SetTransform(vTransformT);
		vTransformTPDF.Update();

		vtkPolyData vTransformPD = vTransformTPDF.GetOutput();
		deleteL.add(vTransformPD);
		vtkPoints vPointsP = vTransformPD.GetPoints();
		deleteL.add(vPointsP);

		// List<vtkPlane> clipPlanes = new ArrayList<vtkPlane>();
		// List<vtkClipPolyData> clipFilters = new ArrayList<vtkClipPolyData>();
		// List<vtkPolyData> clipOutputs = new ArrayList<vtkPolyData>();

		// randomly shuffling the order of the sides we process can speed things up
		List<Integer> sideL = new ArrayList<>();
		for (int i = 0; i < aNumSides; ++i)
			sideL.add(i);
		Collections.shuffle(sideL);

		vtkAlgorithmOutput vNextInputAO = null;
		vtkClipPolyData vClipCPD = null;
		for (int i = 0; i < sideL.size(); ++i)
		{
			int side = sideL.get(i);

			// compute normal to plane formed by this side of polygon
			Vector3D currPt = new Vector3D(vPointsP.GetPoint(side));

			Vector3D nextPt;
			if (side < aNumSides - 1)
				nextPt = new Vector3D(vPointsP.GetPoint(side + 1));
			else
				nextPt = new Vector3D(vPointsP.GetPoint(0));

			Vector3D vec = nextPt.subtract(currPt);

			Vector3D tmpNormal = centerNormal;
			Vector3D planeNormal = tmpNormal.crossProduct(vec);
			planeNormal = planeNormal.normalize();

			// if (i > clipPlanes.size()-1)
			// clipPlanes.add(new vtkPlane());
			// vtkPlane vPlaneP = clipPlanes.get(i);
			vtkPlane vPlaneP = new vtkPlane();
			deleteL.add(vPlaneP);
			vPlaneP.SetOrigin(currPt.toArray());
			vPlaneP.SetNormal(planeNormal.toArray());

			// if (i > clipFilters.size()-1)
			// clipFilters.add(new vtkClipPolyData());
			// vClipCPD = clipFilters.get(i);
			vClipCPD = new vtkClipPolyData();
			deleteL.add(vClipCPD);
			if (i == 0)
				vClipCPD.SetInputData(aSurfacePD);
			else
				vClipCPD.SetInputConnection(vNextInputAO);
			vClipCPD.SetClipFunction(vPlaneP);
			vClipCPD.SetInsideOut(1);
			// vClipCPD.Update();

			vNextInputAO = vClipCPD.GetOutputPort();

			// if (i > clipOutputs.size()-1)
			// clipOutputs.add(nextInput);
			// clipOutputs.set(i, nextInput);
		}

		// Check if there is anything left after the clipping
		vClipCPD.Update();
		vtkPolyData vClipPD = vClipCPD.GetOutput();
		if (vClipPD.GetNumberOfPoints() > 0)
		{
			// Only do rest of processing if there is at least one point
			vtkPolyDataConnectivityFilter vConnectivityFilterPDCF = new vtkPolyDataConnectivityFilter();
			deleteL.add(vConnectivityFilterPDCF);
			vtkAlgorithmOutput vClipOutAO = vClipCPD.GetOutputPort();
			deleteL.add(vClipOutAO);

			vConnectivityFilterPDCF.SetInputConnection(vClipOutAO);
			vConnectivityFilterPDCF.SetExtractionModeToClosestPointRegion();
			vConnectivityFilterPDCF.SetClosestPoint(aCenter.toArray());
			vConnectivityFilterPDCF.Update();

			// polyData = new vtkPolyData();
			// if (outputPolyData == null)
			// outputPolyData = new vtkPolyData();

			if (aRetInteriorPD != null)
			{
				vtkPolyData vConnectivityFilterPD = vConnectivityFilterPDCF.GetOutput();
				deleteL.add(vConnectivityFilterPD);
				aRetInteriorPD.DeepCopy(vConnectivityFilterPD);
			}

			if (aRetExteriorPD != null)
			{
				// Compute the bounding edges of this surface
				vtkFeatureEdges vEdgeExtracterFE = new vtkFeatureEdges();
				deleteL.add(vEdgeExtracterFE);
				vtkAlgorithmOutput vConnectivityFilterAO = vConnectivityFilterPDCF.GetOutputPort();
				deleteL.add(vConnectivityFilterAO);
				vEdgeExtracterFE.SetInputConnection(vConnectivityFilterAO);
				vEdgeExtracterFE.BoundaryEdgesOn();
				vEdgeExtracterFE.FeatureEdgesOff();
				vEdgeExtracterFE.NonManifoldEdgesOff();
				vEdgeExtracterFE.ManifoldEdgesOff();
				vEdgeExtracterFE.Update();

				vtkPolyData vEdgeExtracterPD = vEdgeExtracterFE.GetOutput();
				deleteL.add(vEdgeExtracterPD);
				aRetExteriorPD.DeepCopy(vEdgeExtracterPD);
			}

		}

		// Release temporary VTK objects
		VtkUtil.deleteAll(deleteL);
	}

	/**
	 * Utility method for drawing a (multi-point) poly path on a {@link vtkPolyData}
	 * surface.
	 * <P>
	 * The results of the rendered poly path will be returned.
	 * <P>
	 * The basis of this method originated from (prior to 2018Jan)
	 * edu.jhuapl.saavtk.util.PolyDataUtil.
	 *
	 * @param aSurfacePD The {@link vtkPolyData} corresponding to the surface.
	 * @param aSurfacePL The {@link vtkPointLocator} associated with the surface.
	 * @param aPointBeg  The starting end point.
	 * @param aPointEnd  The ending end point.
	 * @return Returns a {@link vtkPolyData} that contains the contents of the
	 *         computed line.
	 */
	public static vtkPolyData drawPathPolyOn(vtkPolyData aSurfacePD, vtkAbstractPointLocator aSurfacePL,
			Vector3D aPointBeg, Vector3D aPointEnd)
	{
		// Get the average normal of the 2 end points
		Vector3D normalBeg = PolyDataUtil.getPolyDataNormalAtPoint(aPointBeg, aSurfacePD, aSurfacePL, 20);
		Vector3D normalEnd = PolyDataUtil.getPolyDataNormalAtPoint(aPointEnd, aSurfacePD, aSurfacePL, 20);
		Vector3D normalAvg = normalBeg.add(normalEnd).scalarMultiply(0.5);

		// Compute the normal for the cutting plane
		Vector3D diffVec = aPointBeg.subtract(aPointEnd);
		Vector3D normalCut = diffVec.crossProduct(normalAvg).normalize();

		vtkPlane vCutPlane = new vtkPlane();
		vCutPlane.SetOrigin(aPointBeg.toArray());
		vCutPlane.SetNormal(normalCut.toArray());

		vtkExtractPolyDataGeometry vExtract1EPDG = new vtkExtractPolyDataGeometry();
		vExtract1EPDG.SetImplicitFunction(vCutPlane);
		vExtract1EPDG.SetExtractInside(1);
		vExtract1EPDG.SetExtractBoundaryCells(1);
		vExtract1EPDG.SetInputData(aSurfacePD);
		vExtract1EPDG.Update();

		vtkExtractPolyDataGeometry vExtract2EPDG = new vtkExtractPolyDataGeometry();
		vExtract2EPDG.SetImplicitFunction(vCutPlane);
		vExtract2EPDG.SetExtractInside(0);
		vExtract2EPDG.SetExtractBoundaryCells(1);
		vExtract2EPDG.SetInputConnection(vExtract1EPDG.GetOutputPort());
		vExtract2EPDG.Update();

		vtkCutter vCutC = new vtkCutter();
		vCutC.SetInputConnection(vExtract2EPDG.GetOutputPort());
		vCutC.CreateDefaultLocator();
		vCutC.SetCutFunction(vCutPlane);
		vCutC.Update();

		vtkPolyData retLinePD = new vtkPolyData();
		retLinePD.DeepCopy(vCutC.GetOutput());

		// Take this line and put it into a cell locator so we can find the cells
		// closest to the end points
		vtksbCellLocator vCellCL = new vtksbCellLocator();
		vCellCL.SetDataSet(retLinePD);
		vCellCL.CacheCellBoundsOn();
		vCellCL.AutomaticOn();
		vCellCL.BuildLocator();

		// Bail if this is a degenerate case - no points in the polyline
		if (retLinePD.GetNumberOfPoints() == 0)
		{
			drawPathSimpleOn(aPointBeg, aPointEnd, retLinePD);
			return retLinePD;
		}

		// Search for the cells and points closest to the 2 end points
		double[] closestPt1Arr = new double[3];
		double[] closestPt2Arr = new double[3];
		vtkGenericCell vGeneric1GC = new vtkGenericCell();
		vtkGenericCell vGeneric2GC = new vtkGenericCell();
		long[] cellId1 = new long[1];
		long[] cellId2 = new long[1];
		int[] subId = new int[1];
		double[] dist2 = new double[1];

		vCellCL.FindClosestPoint(aPointBeg.toArray(), closestPt1Arr, vGeneric1GC, cellId1, subId, dist2);
		vCellCL.FindClosestPoint(aPointEnd.toArray(), closestPt2Arr, vGeneric2GC, cellId2, subId, dist2);

		// Bail if this is a degenerate case - both points are on the same cell
		if (cellId1[0] == cellId2[0])
		{
			Vector3D tmpPointBeg = new Vector3D(closestPt1Arr);
			Vector3D tmpPointEnd = new Vector3D(closestPt2Arr);
			drawPathSimpleOn(tmpPointBeg, tmpPointEnd, retLinePD);
			return retLinePD;
		}

		// Delegate to PolyDataUtil to finish the formation of poly path...
		boolean isPass = PolyDataUtil.convertPartOfLinesToPolyLineWithSplitting(retLinePD, closestPt1Arr, (int)cellId1[0],
				closestPt2Arr, (int)cellId2[0]);
		if (isPass == true)
			return retLinePD;

		return null;
	}

	/**
	 * Utility method that for drawing a simple path onto a {@link vtkPolyData}.
	 * <P>
	 * The results for the rendered path will be returned via the last argument
	 * (aRetPath).
	 *
	 * @param aPointBeg  The starting end point.
	 * @param aPointEnd  The ending end point.
	 * @param aRetPathPD {@link vtkPolyData} that is used to store the computed
	 *                   path.
	 */
	public static void drawPathSimpleOn(Vector3D aPointBeg, Vector3D aPointEnd, vtkPolyData aRetPathPD)
	{
		vtkPoints vPointP = aRetPathPD.GetPoints();
		vPointP.SetNumberOfPoints(2);
		vPointP.SetPoint(0, aPointBeg.toArray());
		vPointP.SetPoint(1, aPointEnd.toArray());

		vtkCellArray vLineCA = aRetPathPD.GetLines();
		vLineCA.Initialize();

		vtkIdList vTmpIL = new vtkIdList();
		vTmpIL.SetNumberOfIds(2);
		vTmpIL.SetId(0, 0);
		vTmpIL.SetId(1, 1);

		vLineCA.InsertNextCell(vTmpIL);
	}

}
