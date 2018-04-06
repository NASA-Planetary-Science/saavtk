package edu.jhuapl.saavtk2.image.projection;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.polydata.clip.PolyDataClipWithCone;
import edu.jhuapl.saavtk2.polydata.clip.PolyDataClipWithPlane;
import edu.jhuapl.saavtk2.util.LatLon;
import edu.jhuapl.saavtk2.util.MathUtil;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;

public class CylindricalProjection implements Projection
{
	
	double latMinDeg,latMaxDeg,lonMinDeg,lonMaxDeg;
	Vector3D llUnit,lrUnit,urUnit,ulUnit,midUnit;
	
	public CylindricalProjection(double latMinDeg, double latMaxDeg, double lonMinDeg, double lonMaxDeg)
	{
		this.latMinDeg=latMinDeg;
		this.latMaxDeg=latMaxDeg;
		this.lonMinDeg=lonMinDeg;
		this.lonMaxDeg=lonMaxDeg;
		//
		double[] llxyz=MathUtil.latrec(new LatLon(lonMinDeg, latMinDeg));
		double[] lrxyz=MathUtil.latrec(new LatLon(lonMaxDeg, latMinDeg));
		double[] ulxyz=MathUtil.latrec(new LatLon(lonMinDeg, latMaxDeg));
		double[] urxyz=MathUtil.latrec(new LatLon(lonMaxDeg, latMaxDeg));
		llUnit=new Vector3D(llxyz).normalize();
		lrUnit=new Vector3D(lrxyz).normalize();
		ulUnit=new Vector3D(ulxyz).normalize();
		urUnit=new Vector3D(urxyz).normalize();
		midUnit=llUnit.add(lrUnit).add(ulUnit).add(urUnit).scalarMultiply(1./4.).normalize();
	}

	@Override
	public Vector3D getRayOrigin()
	{
		return Vector3D.ZERO;
	}
	
	@Override
	public Vector3D getLowerLeftUnit()
	{
		return llUnit;
	}
	
	@Override
	public Vector3D getLowerRightUnit()
	{
		return lrUnit;
	}
	
	@Override
	public Vector3D getUpperLeftUnit()
	{
		return ulUnit;
	}
	
	@Override
	public Vector3D getUpperRightUnit()
	{
		return urUnit;
	}
	
	@Override
	public Vector3D getMidPointUnit()
	{
		return midUnit;
	}
	
	@Override
	public double getHorizontalMin()
	{
		return lonMinDeg;
	}

	@Override
	public double getHorizontalMax()
	{
		return lonMaxDeg;
	}

	@Override
	public double getHorizontalExtent()
	{
		return lonMaxDeg-lonMinDeg;
	}

	@Override
	public double getHorizontalMid()
	{
		return (lonMaxDeg+lonMinDeg)/2.;
	}

	@Override
	public double getVerticalMin()
	{
		return latMinDeg;
	}

	@Override
	public double getVerticalMax()
	{
		return latMaxDeg;
	}

	@Override
	public double getVerticalExtent()
	{
		return latMaxDeg-latMinDeg;
	}

	@Override
	public double getVerticalMid()
	{
		return (latMaxDeg+latMinDeg)/2.;
	}

	@Override
	public CylindricalMapCoordinates project(Vector3D position)	// +x is assumed to be the prime meridian, for now, and +z is the rotation axis of the body
	{
		double r=position.getNorm();
		double latRad=Math.PI/2.-Math.acos(position.getZ()/r);
		double lonRad=Math.atan2(position.getY(), position.getX());
		double latDeg=Math.toDegrees(latRad);
		double lonDeg=Math.toDegrees(lonRad);
		// TODO: wrap lat to allowed values
		return new CylindricalMapCoordinates(lonDeg,latDeg);
	}

	@Override
	public Vector3D unproject(MapCoordinates mapCoordinates, DepthFunction depthFunction)
	{
		double depth=depthFunction.value(mapCoordinates);
		double lonRad=Math.toRadians(mapCoordinates.getX());
		double latRad=Math.toRadians(mapCoordinates.getY());
		double x=depth*Math.cos(lonRad)*Math.sin(Math.PI/2.-latRad);
		double y=depth*Math.sin(lonRad)*Math.sin(Math.PI/2.-latRad);
		double z=depth*Math.cos(Math.PI/2.-latRad);
		return new Vector3D(x,y,z);
	}

	@Override
	public vtkPolyData clipVisibleGeometry(vtkPolyData polyData)
	{
		return clipAlongMaxLongitude(lonMaxDeg, clipAlongMinLongitude(lonMinDeg, clipAlongLatitude(latMinDeg, clipAlongLatitude(latMaxDeg, polyData))));//, clipAlongLatitude(latMinDeg, clipAlongLatitude(latMaxDeg, polyData))));
		
	}
	
	@Override
	public CylindricalMapCoordinates createMapCoords(double x, double y)
	{
		return new CylindricalMapCoordinates(x, y);
	}
	
	public static vtkPolyData clipAlongLatitude(double latDeg, vtkPolyData polyData)
	{
		if (latDeg>0)
			return new PolyDataClipWithCone(Vector3D.ZERO, Vector3D.PLUS_K, 90-latDeg).apply(polyData);
		else
			return new PolyDataClipWithCone(Vector3D.ZERO, Vector3D.MINUS_K, 90+latDeg).apply(polyData);
	}
	
	public static vtkPolyData clipAlongMinLongitude(double lonDeg, vtkPolyData polyData)
	{
		Plane plane=new Plane(Vector3D.ZERO, Vector3D.PLUS_J);	// setting the normal to +y aligns the plane with the prime meridian along +x at longitude 0
		plane=plane.rotate(Vector3D.ZERO, new Rotation(Vector3D.PLUS_K, Math.toRadians(lonDeg)));
		return new PolyDataClipWithPlane(plane).apply(polyData);
	}

	public static vtkPolyData clipAlongMaxLongitude(double lonDeg, vtkPolyData polyData)
	{
		Plane plane=new Plane(Vector3D.ZERO, Vector3D.MINUS_J);	// note the change in normal direction here, to invert the "front" of the plane
		plane=plane.rotate(Vector3D.ZERO, new Rotation(Vector3D.PLUS_K, Math.toRadians(lonDeg)));
		return new PolyDataClipWithPlane(plane).apply(polyData);
	}
	
	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		vtkSphereSource source=new vtkSphereSource();
		source.SetThetaResolution(360);
		source.SetPhiResolution(180);
		source.Update();
		vtkPolyData polyData=source.GetOutput();

		double latmax=50;
		double latmin=-50;
		double lonmax=45;
		double lonmin=0;
		CylindricalProjection projection=new CylindricalProjection(latmin,latmax,lonmin,lonmax);
		vtkPolyData result=projection.clipVisibleGeometry(polyData);
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(result);
		writer.Write();

	}

}
