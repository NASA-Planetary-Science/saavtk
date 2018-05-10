package edu.jhuapl.saavtk2.image.projection;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.util.LinearSpace;
import edu.jhuapl.saavtk2.image.projection.depthfunc.DepthFunction;
import edu.jhuapl.saavtk2.polydata.clip.PolyDataClip;
import edu.jhuapl.saavtk2.polydata.clip.PolyDataClipWithCone;
import edu.jhuapl.saavtk2.polydata.clip.PolyDataClipWithPlane;
import edu.jhuapl.saavtk2.util.LatLon;
import edu.jhuapl.saavtk2.util.MathUtil;
import vtk.vtkActor2D;
import vtk.vtkAppendPolyData;
import vtk.vtkCaptionActor2D;
import vtk.vtkCaptionWidget;
import vtk.vtkCellArray;
import vtk.vtkCone;
import vtk.vtkCutter;
import vtk.vtkImplicitBoolean;
import vtk.vtkImplicitFunction;
import vtk.vtkLine;
import vtk.vtkNativeLibrary;
import vtk.vtkPlane;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkPolyLine;
import vtk.vtkSphere;
import vtk.vtkSphereSource;

public class CylindricalProjection implements Projection {

	double latMinDeg, latMaxDeg, lonMinDeg, lonMaxDeg;
	Vector3D llUnit, lrUnit, urUnit, ulUnit, midUnit;

	public CylindricalProjection(double latMinDeg, double latMaxDeg, double lonMinDeg, double lonMaxDeg) {
		this.latMinDeg = latMinDeg;
		this.latMaxDeg = latMaxDeg;
		this.lonMinDeg = lonMinDeg;
		this.lonMaxDeg = lonMaxDeg;
		//
		double[] llxyz = MathUtil.latrec(new LatLon(latMinDeg / 180. * Math.PI, lonMinDeg / 180. * Math.PI));
		double[] lrxyz = MathUtil.latrec(new LatLon(latMaxDeg / 180. * Math.PI, lonMinDeg / 180. * Math.PI));
		double[] ulxyz = MathUtil.latrec(new LatLon(latMinDeg / 180. * Math.PI, lonMaxDeg / 180. * Math.PI));
		double[] urxyz = MathUtil.latrec(new LatLon(latMaxDeg / 180. * Math.PI, lonMaxDeg / 180. * Math.PI));
		llUnit = new Vector3D(llxyz).normalize();
		lrUnit = new Vector3D(lrxyz).normalize();
		ulUnit = new Vector3D(ulxyz).normalize();
		urUnit = new Vector3D(urxyz).normalize();
		midUnit = llUnit.add(lrUnit).add(ulUnit).add(urUnit).scalarMultiply(1. / 4.).normalize();
	}

	@Override
	public Vector3D getRayOrigin() {
		return Vector3D.ZERO;
	}

	@Override
	public Vector3D getLowerLeftUnit() {
		return llUnit;
	}

	@Override
	public Vector3D getLowerRightUnit() {
		return lrUnit;
	}

	@Override
	public Vector3D getUpperLeftUnit() {
		return ulUnit;
	}

	@Override
	public Vector3D getUpperRightUnit() {
		return urUnit;
	}

	@Override
	public Vector3D getMidPointUnit() {
		return midUnit;
	}

	@Override
	public double getHorizontalMin() {
		return lonMinDeg;
	}

	@Override
	public double getHorizontalMax() {
		return lonMaxDeg;
	}

	@Override
	public double getHorizontalExtent() {
		return lonMaxDeg - lonMinDeg;
	}

	@Override
	public double getHorizontalMid() {
		return (lonMaxDeg + lonMinDeg) / 2.;
	}

	@Override
	public double getVerticalMin() {
		return latMinDeg;
	}

	@Override
	public double getVerticalMax() {
		return latMaxDeg;
	}

	@Override
	public double getVerticalExtent() {
		return latMaxDeg - latMinDeg;
	}

	@Override
	public double getVerticalMid() {
		return (latMaxDeg + latMinDeg) / 2.;
	}

	@Override
	public CylindricalMapCoordinates project(Vector3D position) // +x is assumed
																// to be the
																// prime
																// meridian, for
																// now, and +z
																// is the
																// rotation axis
																// of the body
	{
		double r = position.getNorm();
		double latRad = Math.PI / 2. - Math.acos(position.getZ() / r);
		double lonRad = Math.atan2(position.getY(), position.getX());
		double latDeg = Math.toDegrees(latRad);
		double lonDeg = Math.toDegrees(lonRad);
		// TODO: wrap lat to allowed values
		return new CylindricalMapCoordinates(lonDeg, latDeg);
	}

	@Override
	public Vector3D unproject(MapCoordinates mapCoordinates, DepthFunction depthFunction) {
		//if (mapCoordinates.getX() < getHorizontalMin() || mapCoordinates.getX() > getHorizontalMax()
	//			|| mapCoordinates.getY() < getVerticalMin() || mapCoordinates.getY() > getVerticalMax())
		//	return Vector3D.NaN;
		double depth = depthFunction.value(mapCoordinates);
		double lonRad = Math.toRadians(mapCoordinates.getX());
		double latRad = Math.toRadians(mapCoordinates.getY());
		double x = depth * Math.cos(lonRad) * Math.sin(Math.PI / 2. - latRad);
		double y = depth * Math.sin(lonRad) * Math.sin(Math.PI / 2. - latRad);
		double z = depth * Math.cos(Math.PI / 2. - latRad);
		return new Vector3D(x, y, z);
	}

	@Override
	public vtkPolyData clipVisibleGeometry(vtkPolyData polyData) {
		throw new Error("not fully implemented");
		//
		// if (latMinDeg>0 && latMaxDeg>0)
		// {
		// vtkImplicitFunction
		// maxFunction=PolyDataClipWithCone.generateConeFunction(Vector3D.ZERO,
		// Vector3D.PLUS_K, 90-latMaxDeg);
		// vtkImplicitBoolean maxBool=new vtkImplicitBoolean();
		// vtkPlane maxPlane=new vtkPlane();
		// maxPlane.SetNormal(0,0,1);
		// maxBool.AddFunction(maxFunction);
		// maxBool.AddFunction(maxPlane);
		// vtkPolyData maxPolyData=new PolyDataClip(maxBool).apply(polyData);
		//
		// //vtkPolyData minPolyData=new
		// PolyDataClip(minFunction).apply(maxPolyData);
		// vtkImplicitFunction
		// minFunction=PolyDataClipWithCone.generateConeFunction(Vector3D.ZERO,
		// Vector3D.PLUS_K, 90-latMinDeg);
		//
		// return maxPolyData;
		// }
		// else if (latMinDeg<0 && latMaxDeg<0)
		// {
		// //vtkImplicitFunction
		// maxFunction=PolyDataClipWithCone.generateConeFunction(Vector3D.ZERO,
		// Vector3D.PLUS_K, 90+latMaxDeg);
		// vtkImplicitFunction
		// minFunction=PolyDataClipWithCone.generateConeFunction(Vector3D.ZERO,
		// Vector3D.PLUS_K, 90+latMinDeg);
		// //vtkImplicitBoolean bool=new vtkImplicitBoolean();
		// //bool.AddFunction(minFunction);
		// //bool.AddFunction(maxFunction);
		// //bool.SetOperationTypeToDifference();
		// return new PolyDataClip(minFunction).apply(polyData);
		//
		// }
		// else
		// {
		// return clipLatitude(latMinDeg, latMaxDeg, clipLongitude(lonMinDeg,
		// lonMaxDeg, polyData));
		// }
		// //

	}

	@Override
	public CylindricalMapCoordinates createMapCoords(double x, double y) {
		return new CylindricalMapCoordinates(x, y);
	}

	/*
	 * public static vtkPolyData clipBelowLatitude(double latDeg, vtkPolyData
	 * polyData) { if (latDeg>0) return new PolyDataClipWithCone(Vector3D.ZERO,
	 * Vector3D.PLUS_K, 90-latDeg).apply(polyData); else return new
	 * PolyDataClipWithCone(Vector3D.ZERO, Vector3D.PLUS_K,
	 * 90+latDeg).apply(polyData); }
	 * 
	 * public static vtkPolyData clipAboveLatitude(double latDeg, vtkPolyData
	 * polyData) { if (latDeg<0) return new PolyDataClipWithCone(Vector3D.ZERO,
	 * Vector3D.MINUS_K, 90+latDeg).apply(polyData); else return new
	 * PolyDataClipWithCone(Vector3D.ZERO, Vector3D.MINUS_K,
	 * 90-latDeg).apply(polyData); }
	 */

//	public static vtkImplicitFunction createDoubleConeFunction(double latMinDeg, double latMaxDeg) {
//		vtkImplicitBoolean bool = new vtkImplicitBoolean();
//
//		vtkImplicitFunction minFunction;
//		vtkImplicitFunction maxFunction;
//
//		maxFunction = PolyDataClipWithCone.generateConeFunction(Vector3D.ZERO, Vector3D.PLUS_K,
//				latMaxDeg > 0 ? 90 - latMaxDeg : 90 + latMaxDeg);
//
//		/*
//		 * if (latMinDeg>0)
//		 * minFunction=PolyDataClipWithCone.generateClipFunction(Vector3D.ZERO,
//		 * Vector3D.PLUS_K, 90-latMinDeg); else
//		 * minFunction=PolyDataClipWithCone.generateClipFunction(Vector3D.ZERO,
//		 * Vector3D.MINUS_K, 90+latMinDeg);
//		 */
//
//		// bool.AddFunction(minFunction);
//
//		// if (latMinDeg*latMaxDeg>0) // e.g. if both are negative or both are
//		// positive
//		// {
//		vtkPlane plane = new vtkPlane();
//		plane.SetNormal(0, 0, Math.signum(latMaxDeg));
//		bool.AddFunction(plane);
//		bool.AddFunction(maxFunction);
//		bool.SetOperationTypeToIntersection();
//		// }
//		// else
//		// bool.SetOperationTypeToUnion();
//
//		return bool;
//	}
//
//	public static vtkImplicitFunction createWedgeFunction(double lonMinDeg, double lonMaxDeg) {
//		vtkImplicitBoolean bool = new vtkImplicitBoolean();
//
//		Plane minPlane = new Plane(Vector3D.ZERO, Vector3D.PLUS_J); // setting
//																	// the
//																	// normal to
//																	// +y aligns
//																	// the plane
//																	// with the
//																	// prime
//																	// meridian
//																	// along +x
//																	// at
//																	// longitude
//																	// 0
//		minPlane = minPlane.rotate(Vector3D.ZERO, new Rotation(Vector3D.PLUS_K, Math.toRadians(lonMinDeg)));
//
//		Plane maxPlane = new Plane(Vector3D.ZERO, Vector3D.MINUS_J); // note the
//																		// change
//																		// in
//																		// normal
//																		// direction
//																		// here,
//																		// to
//																		// invert
//																		// the
//																		// "front"
//																		// of
//																		// the
//																		// plane
//		maxPlane = maxPlane.rotate(Vector3D.ZERO, new Rotation(Vector3D.PLUS_K, Math.toRadians(lonMaxDeg)));
//
//		bool.AddFunction(PolyDataClipWithPlane.createClipFunction(minPlane));
//		bool.AddFunction(PolyDataClipWithPlane.createClipFunction(maxPlane));
//		if (lonMaxDeg - lonMinDeg > 180)
//			bool.SetOperationTypeToIntersection();
//		else
//			bool.SetOperationTypeToUnion();
//		return bool;
//
//		/*
//		 * return new vtkImplicitFunction(){
//		 * 
//		 * @Override public double EvaluateFunction(double id0, double id1,
//		 * double id2) { double thDeg=Math.toDegrees(Math.atan(id1/id0)); if
//		 * (thDeg>=lonMinDeg && thDeg<=lonMaxDeg) return -1; else return 1; }
//		 * 
//		 * @Override public double EvaluateFunction(double[] id0) { return
//		 * this.EvaluateFunction(id0[0], id0[2], id0[2]); }
//		 * 
//		 * @Override public double FunctionValue(double id0, double id1, double
//		 * id2) { return this.EvaluateFunction(id0, id1, id2); }
//		 * 
//		 * @Override public double FunctionValue(double[] id0) { return
//		 * this.EvaluateFunction(id0); } };
//		 */
//	}
//
//	public static vtkPolyData clipLongitude(double lonMinDeg, double lonMaxDeg, vtkPolyData polyData) {
//		return new PolyDataClip(createWedgeFunction(lonMinDeg, lonMaxDeg)).apply(polyData);
//	}
//
//	public static vtkPolyData clipLatitude(double latMinDeg, double latMaxDeg, vtkPolyData polyData) {
//		return new PolyDataClip(createDoubleConeFunction(latMinDeg, latMaxDeg)).apply(polyData);
//	}
//
//	/*
//	 * public static vtkPolyData clipAboveLongitude(double lonDeg, vtkPolyData
//	 * polyData) { Plane plane=new Plane(Vector3D.ZERO, Vector3D.PLUS_J); //
//	 * setting the normal to +y aligns the plane with the prime meridian along
//	 * +x at longitude 0 plane=plane.rotate(Vector3D.ZERO, new
//	 * Rotation(Vector3D.PLUS_K, Math.toRadians(lonDeg))); return new
//	 * PolyDataClipWithPlane(plane).apply(polyData); }
//	 * 
//	 * public static vtkPolyData clipBelowLongitude(double lonDeg, vtkPolyData
//	 * polyData) {
//	 * 
//	 * Plane plane=new Plane(Vector3D.ZERO, Vector3D.MINUS_J); // note the
//	 * change in normal direction here, to invert the "front" of the plane
//	 * plane=plane.rotate(Vector3D.ZERO, new Rotation(Vector3D.PLUS_K,
//	 * Math.toRadians(lonDeg))); return new
//	 * PolyDataClipWithPlane(plane).apply(polyData); }
//	 */
//
//	public static vtkPolyData generateLongitudeLine(double lonDeg, vtkPolyData polyData) {
//		vtkCutter cutter = new vtkCutter();
//		Plane plane = new Plane(Vector3D.ZERO, Vector3D.PLUS_J); // setting the
//																	// normal to
//																	// +y aligns
//																	// the plane
//																	// with the
//																	// prime
//																	// meridian
//																	// along +x
//																	// at
//																	// longitude
//																	// 0
//		plane = plane.rotate(Vector3D.ZERO, new Rotation(Vector3D.PLUS_K, Math.toRadians(lonDeg)));
//
//		Vector3D meridian = new Vector3D(1, 0, 0);
//		vtkPlane planeFunc = new vtkPlane();
//		planeFunc.SetNormal(plane.getNormal().toArray());
//		cutter.SetInputData(polyData);
//		cutter.SetCutFunction(planeFunc);
//		cutter.Update();
//		vtkPolyData cutPolyData = new vtkPolyData();
//		cutPolyData.DeepCopy(cutter.GetOutput());
//		return cutPolyData;
//	}
//
//	public static vtkPolyData generateLatitudeLine(double latDeg, vtkPolyData polyData) {
//		vtkCutter cutter = new vtkCutter();
//		if (latDeg != 0) {
//			System.out.println(latDeg);
//			vtkCone cone = new vtkCone();
//			cone.SetTransform(PolyDataClipWithCone.generateClipTransform(Vector3D.ZERO, Vector3D.PLUS_K));
//			cone.SetAngle(90 - latDeg);
//			vtkPlane plane = new vtkPlane();
//			plane.SetNormal(0, 0, latDeg > 0 ? 1 : -1);
//			vtkImplicitBoolean bool = new vtkImplicitBoolean();
//			bool.AddFunction(cone);
//			bool.AddFunction(plane);
//			bool.SetOperationTypeToIntersection();
//			cutter.SetInputData(polyData);
//			cutter.SetCutFunction(bool);
//			cutter.Update();
//		} else {
//			// make sure equator is generated, too -- zimmemi1
//			System.out.println("!");
//			vtkPlane plane = new vtkPlane();
//			plane.SetNormal(0, 0, 1);
//			cutter.SetInputData(polyData);
//			cutter.SetCutFunction(plane);
//			cutter.Update();
//
//		}
//		vtkPolyData cutPolyData = new vtkPolyData();
//		cutPolyData.DeepCopy(cutter.GetOutput());
//		return cutPolyData;
//	}
//
//	public static vtkPolyData generateGraticules(int nLon, int nLatHemi, vtkPolyData polyData) {
//		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
//
//		double[] lonRange = LinearSpace.createTruncated(0, 360, nLon);
//		double[] latRange = LinearSpace.create(0, 90, nLatHemi);
//
//		vtkCutter cutter = new vtkCutter();
//		vtkPlane plane = new vtkPlane();
//		Vector3D meridian = new Vector3D(1, 0, 0);
//		for (int i = 0; i < lonRange.length; i++) {
//			Rotation rot = new Rotation(Vector3D.PLUS_K, Math.toRadians(lonRange[i]));
//			plane.SetNormal(rot.applyTo(meridian).toArray());
//			cutter.SetInputData(polyData);
//			cutter.SetCutFunction(plane);
//			cutter.Update();
//			vtkPolyData cutPolyData = new vtkPolyData();
//			cutPolyData.DeepCopy(cutter.GetOutput());
//			appendFilter.AddInputData(cutPolyData);
//		}
//
//		vtkCone cone = new vtkCone();
//		for (int i = 0; i < latRange.length; i++) {
//			cone.SetTransform(PolyDataClipWithCone.generateClipTransform(Vector3D.ZERO, Vector3D.PLUS_K));
//			cone.SetAngle(90 - latRange[i]);
//			cutter.SetInputData(polyData);
//			cutter.SetCutFunction(cone);
//			cutter.Update();
//			vtkPolyData cutPolyData = new vtkPolyData();
//			cutPolyData.DeepCopy(cutter.GetOutput());
//			appendFilter.AddInputData(cutPolyData);
//
//		}
//
//		// make sure equator is generated, too -- zimmemi1
//		plane.SetNormal(0, 0, 1);
//		cutter.SetInputData(polyData);
//		cutter.SetCutFunction(plane);
//		cutter.Update();
//		vtkPolyData cutPolyData = new vtkPolyData();
//		cutPolyData.DeepCopy(cutter.GetOutput());
//		appendFilter.AddInputData(cutPolyData);
//
//		appendFilter.Update();
//
//		// vtkPolyDataWriter writer=new vtkPolyDataWriter();
//		// writer.SetInputData(appendFilter.GetOutput());
//		// writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
//		// writer.SetFileTypeToBinary();
//		// writer.Write();
//
//		return appendFilter.GetOutput();
//	}
//
//	public static List<vtkActor2D> generateLongitudeLabels(int nLon, double latDeg, GenericPolyhedralModel model) {
//		String fmt = "%.0f";
//		String degSymbol = String.valueOf(Character.toChars(0x00B0));
//
//		List<vtkActor2D> actors = Lists.newArrayList();
//		double[] lonRange = LinearSpace.createTruncated(0, 360, nLon);
//		for (int i = 0; i < nLon / 2 + 1; i++) {
//			double lat = Math.toRadians(latDeg);
//			double lon = Math.toRadians(lonRange[i]);
//			// create caption
//			double[] intersectPoint = new double[3];
//			model.getPointAndCellIdFromLatLon(lat, lon, intersectPoint);
//			String captionLabel = String.format(fmt, Math.toDegrees(lon)) + degSymbol;
//			if (i != 0 && i != nLon)
//				captionLabel += "E";
//			vtkCaptionActor2D caption = new OccludingCaptionActor(intersectPoint, null, model);
//			caption.SetCaption(captionLabel);
//			caption.BorderOff();
//			caption.SetAttachmentPoint(intersectPoint);
//			actors.add(caption);
//		}
//
//		for (int i = 1; i < nLon / 2; i++) // don't do 0
//		{
//			double lat = Math.toRadians(latDeg);
//			double lon = Math.toRadians(lonRange[i]);
//			// create caption
//			double[] intersectPoint = new double[3];
//			model.getPointAndCellIdFromLatLon(lat, 2 * Math.PI - lon, intersectPoint);
//			String captionLabel = String.format(fmt, Math.toDegrees(lon)) + degSymbol + "W";
//			vtkCaptionActor2D caption = new OccludingCaptionActor(intersectPoint, null, model);
//			caption.SetCaption(captionLabel);
//			caption.BorderOff();
//			caption.SetAttachmentPoint(intersectPoint);
//			actors.add(caption);
//
//		}
//
//		return actors;
//
//	}
//
//	public static List<vtkActor2D> generateLatitudeLabels(int nLatHemi, double lonDeg, GenericPolyhedralModel model,
//			boolean labelPoles) {
//		String fmt = "%.0f";
//		String degSymbol = String.valueOf(Character.toChars(0x00B0));
//
//		List<vtkActor2D> actors = Lists.newArrayList();
//		double[] latRange = LinearSpace.create(0, 90, nLatHemi);
//		for (int i = labelPoles ? 0 : 1; i < nLatHemi; i++) // Start at one to
//															// skip labeling the
//															// pole.
//		{
//			// Compute labels from the poles toward the equator so they coincide
//			// with parallels from the grid
//			// no matter what spacing.
//			double lat = Math.toRadians(90 - latRange[i]);
//
//			// create caption
//			double[] intersectPoint = new double[3];
//			model.getPointAndCellIdFromLatLon(lat, Math.toRadians(lonDeg), intersectPoint);
//			String captionLabel = String.format(fmt, Math.toDegrees(lat)) + degSymbol + "N";
//			vtkCaptionActor2D caption = new OccludingCaptionActor(intersectPoint, null, model);
//			caption.SetCaption(captionLabel);
//			caption.BorderOff();
//			caption.SetAttachmentPoint(intersectPoint);
//			actors.add(caption);
//
//		}
//
//		// southern hemisphere
//		for (int i = labelPoles ? 0 : 1; i < nLatHemi - 1; i++) // Start at one
//																// to skip
//																// labeling the
//																// pole.
//		{
//			// Compute labels from the poles toward the equator so they coincide
//			// with parallels from the grid
//			// no matter what spacing.
//			double lat = Math.toRadians(90 - latRange[i]);
//
//			// create caption
//			double[] intersectPoint = new double[3];
//			model.getPointAndCellIdFromLatLon(-lat, Math.toRadians(lonDeg), intersectPoint);
//			String captionLabel = String.format(fmt, Math.toDegrees(lat)) + degSymbol + "S";
//			vtkCaptionActor2D caption = new OccludingCaptionActor(intersectPoint, null, model);
//			caption.SetCaption(captionLabel);
//			caption.BorderOff();
//			caption.SetAttachmentPoint(intersectPoint);
//			actors.add(caption);
//
//		}
//		return actors;
//
//	}
//

	public static void main(String[] args) {
		vtkNativeLibrary.LoadAllNativeLibraries();

		vtkSphereSource source = new vtkSphereSource();
		source.SetThetaResolution(360);
		source.SetPhiResolution(180);
		source.Update();
		vtkPolyData polyData = source.GetOutput();

		double latmax = 50;
		double latmin = -50;
		double lonmax = 45;
		double lonmin = 0;
		CylindricalProjection projection = new CylindricalProjection(latmin, latmax, lonmin, lonmax);
		vtkPolyData result = projection.clipVisibleGeometry(polyData);

		vtkPolyDataWriter writer = new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(result);
		writer.Write();

	}

}
