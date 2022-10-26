package edu.jhuapl.saavtk.grid.painter;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;

import Jama.Matrix;
import crucible.core.math.vectorspace.MatrixIJK;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import vtk.vtkAppendPolyData;
import vtk.vtkCaptionActor2D;
import vtk.vtkCone;
import vtk.vtkConeSource;
import vtk.vtkCutter;
import vtk.vtkImplicitFunction;
import vtk.vtkMatrix4x4;
import vtk.vtkPlane;
import vtk.vtkPolyData;
import vtk.vtkTransform;
import vtk.vtkTransformFilter;

/**
 * Collection of VTK based draw routines associated with the generation of
 * grids.
 * <p>
 * Currently the only supported grid is that of a LatLon grid.
 *
 * @author lopeznr1
 */
public class VtkGridUtil
{
	/**
	 * Utility method that generates a LatLon grid and stores the result into the
	 * provided VTK arguments.
	 * <p>
	 * This logic originated from the edu.jhuapl.saavtk.model.Graticule class.
	 *
	 * @param aSmallBody      The small body for which a lat lon grid will be
	 *                        generated.
	 * @param aVtkPolyData    The {@link vtkPolyData} used to draw the lat lon grid.
	 *
	 * @param aVtkLabelActorL The list where VTK text labels will be stored. Note
	 *                        that this list will be first cleared before new VTK
	 *                        text actors are created and added to this list.
	 * @param aLatSpacing     The spacing between the lines of latitude.
	 * @param aLonSpacing     The spacing beltween the lines of longitude.
	 */
	public static void generateLatLonGrid(PolyhedralModel aSmallBody, vtkPolyData aVtkPolyData,
			List<vtkCaptionActor2D> aVtkLabelActorL, double aLatSpacing, double aLonSpacing)
	{
		// Allocate (temporal) VTK vars
		var vAppendFilter = new vtkAppendPolyData();
		var vPlane = new vtkPlane();
		var vCone = new vtkCone();
		var vCutPolyData = new vtkCutter();
		var vTransform = new vtkTransform();
		var deleteL = ImmutableList.of(vAppendFilter, vPlane, vCone, vCutPolyData, vTransform);

		// Bail if invalid configuration
		if (Double.isNaN(aLatSpacing) == true || Double.isNaN(aLonSpacing) == true)
			return;

		// Ensure latSpacing and lonSpacing is within bounds
		aLatSpacing = Doubles.constrainToRange(aLatSpacing, 0.1, 90.0);
		aLonSpacing = Doubles.constrainToRange(aLonSpacing, 0.1, 180.0);

		aVtkLabelActorL.clear();

		int numberLonCircles = (int) Math.ceil(180.0 / aLonSpacing);
		int numberLatCircles = (int) Math.ceil(90.0 / aLatSpacing);

		double[] origin = { 0.0, 0.0, 0.0 };
		double[] zaxis = { 0.0, 0.0, 1.0 };

		vAppendFilter.UserManagedInputsOn();
		vAppendFilter.SetNumberOfInputs(numberLatCircles + numberLonCircles);
		vtkPolyData[] tmps = new vtkPolyData[numberLatCircles + numberLonCircles];

		var smallBodyPolyData = aSmallBody.getSmallBodyPolyData();
		vCutPolyData.SetInputData(smallBodyPolyData);

		// First do the longitudes.
		for (int i = 0; i < numberLonCircles; ++i)
		{
			double lon = aLonSpacing * i * Math.PI / 180.0;
			double[] vec = MathUtil.latrec(new LatLon(0.0, lon, 1.0));
			double[] normal = new double[3];
			MathUtil.vcrss(vec, zaxis, normal);

			vPlane.SetOrigin(origin);
			vPlane.SetNormal(normal);

			vCutPolyData.SetCutFunction(vPlane);
			vCutPolyData.Update();

			tmps[i] = new vtkPolyData();
			tmps[i].DeepCopy(vCutPolyData.GetOutput());
			vAppendFilter.SetInputDataByNumber(i, tmps[i]);
		}

		String fmt = "%.0f";
		String deg = String.valueOf(Character.toChars(0x00B0));
		if (Configuration.isWindows())
			deg = " ";

		for (int i = 0; i <= numberLonCircles; i++)
		{
			double lat = 0;
			double lon = aLonSpacing * i * Math.PI / 180.0;
			// create caption
			double[] intersectPoint = new double[3];
			aSmallBody.getPointAndCellIdFromLatLon(lat, lon, intersectPoint);
			double[] translatedPoint = aSmallBody.getCurrentTransform().TransformDoublePoint(intersectPoint);
			String captionLabel = String.format(fmt, lon / Math.PI * 180) + deg;
			if (i != 0 && i != numberLonCircles)
				captionLabel += "E";
			var caption = formCaption(aSmallBody, translatedPoint, captionLabel);
			aVtkLabelActorL.add(caption);
		}

		for (int i = 1; i < numberLonCircles; i++) // don't do 0
		{
			double lat = 0;
			double lon = aLonSpacing * i * Math.PI / 180.0;
			// create caption
			double[] intersectPoint = new double[3];
			aSmallBody.getPointAndCellIdFromLatLon(lat, 2 * Math.PI - lon, intersectPoint);
			double[] translatedPoint = aSmallBody.getCurrentTransform().TransformDoublePoint(intersectPoint);
			String captionLabel = String.format(fmt, lon / Math.PI * 180) + deg + "W";
			var caption = formCaption(aSmallBody, translatedPoint, captionLabel);
			aVtkLabelActorL.add(caption);
		}

		double[] yaxis = { 0.0, 1.0, 0.0 };
		vTransform.Identity();
		vTransform.RotateWXYZ(90.0, yaxis);
		for (int i = 0; i < numberLatCircles; ++i)
		{
			vtkImplicitFunction cutFunction = null;
			if (i == 0)
			{
				vPlane.SetOrigin(origin);
				vPlane.SetNormal(zaxis);
				cutFunction = vPlane;
			}
			else
			{
				vCone.SetTransform(vTransform);
				vCone.SetAngle(aLatSpacing * i);
				cutFunction = vCone;
			}

			vCutPolyData.SetCutFunction(cutFunction);
			vCutPolyData.Update();

			int idx = numberLonCircles + i;
			tmps[idx] = new vtkPolyData();
			tmps[idx].DeepCopy(vCutPolyData.GetOutput());
			vAppendFilter.SetInputDataByNumber(idx, tmps[idx]);
		}

		vAppendFilter.Update();
		
//		aVtkPolyData.DeepCopy(vAppendFilter.GetOutput());
		
		vtkTransformFilter transformFilter=new vtkTransformFilter();
		transformFilter.SetInputData(vAppendFilter.GetOutput());
		transformFilter.SetTransform(aSmallBody.getCurrentTransform());
		transformFilter.Update();
		
		aVtkPolyData.DeepCopy(transformFilter.GetPolyDataOutput());

		int nlon = 3;
		for (double lon = 0; lon < 360; lon += 360. / nlon) // do meridians at nlon intervals from 0
		{

			// northern hemisphere
			for (int i = 1; i < numberLatCircles; i++) // Start at one to skip labeling the pole.
			{
				// Compute labels from the poles toward the equator so they coincide with
				// parallels from the grid
				// no matter what spacing.
				double lat = 90 - aLatSpacing * i;

				// Convert to radians.
				lat *= Math.PI / 180.0;

				// create caption
				double[] intersectPoint = new double[3];
				aSmallBody.getPointAndCellIdFromLatLon(lat, lon / 180 * Math.PI, intersectPoint);
				double[] translatedPoint = aSmallBody.getCurrentTransform().TransformDoublePoint(intersectPoint);
				String captionLabel = String.format(fmt, lat / Math.PI * 180) + deg + "N";
				var caption = formCaption(aSmallBody, translatedPoint, captionLabel);
				aVtkLabelActorL.add(caption);
			}

			// southern hemisphere
			for (int i = 1; i < numberLatCircles; i++) // Start at one to skip labeling the pole.
			{
				// Compute labels from the poles toward the equator so they coincide with
				// parallels from the grid
				// no matter what spacing.
				double lat = 90 - aLatSpacing * i;

				// Convert to radians.
				lat *= Math.PI / 180.0;

				// create caption
				double[] intersectPoint = new double[3];
				aSmallBody.getPointAndCellIdFromLatLon(-lat, lon / 180 * Math.PI, intersectPoint);
				double[] translatedPoint = aSmallBody.getCurrentTransform().TransformDoublePoint(intersectPoint);
				String captionLabel = String.format(fmt, lat / Math.PI * 180) + deg + "S";
				var caption = formCaption(aSmallBody, translatedPoint, captionLabel);
				aVtkLabelActorL.add(caption);

			}
		}

		// Release temporary VTK objects
		VtkUtil.deleteAll(deleteL);
	}

	/**
	 * Utility helper method to create a VTK caption.
	 */
	public static vtkCaptionActor2D formCaption(PolyhedralModel aSmallBody, double[] aIntersectPoint, String aLabel)
	{
		var retCaption = new OccludingCaptionActor(aIntersectPoint, null, aSmallBody);
		retCaption.BorderOff();
		retCaption.GetTextActor().SetTextScaleModeToNone();
		retCaption.SetCaption(aLabel);
		retCaption.SetAttachmentPoint(aIntersectPoint);
		return retCaption;
	}

}
