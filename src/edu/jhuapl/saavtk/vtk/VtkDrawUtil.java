package edu.jhuapl.saavtk.vtk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkAbstractPointLocator;
import vtk.vtkAlgorithmOutput;
import vtk.vtkClipPolyData;
import vtk.vtkExtractPolyDataGeometry;
import vtk.vtkFeatureEdges;
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
	 * This basis of this method originated from (prior to 2018Jan)
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
	public static void drawEllipseOn(vtkPolyData aSurfacePD, vtkAbstractPointLocator aSurfacePL,
			Vector3D aCenter, double aMajorRadius, double aFlattening, double aAngle, int aNumSides,
			vtkPolyData aRetInteriorPD, vtkPolyData aRetExteriorPD)
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

}
