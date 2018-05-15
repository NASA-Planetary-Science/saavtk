package edu.jhuapl.saavtk2.image.projection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.polydata.clip.PolyDataClip;
import edu.jhuapl.saavtk2.polydata.clip.PolyDataClipWithFrustum;
import edu.jhuapl.saavtk2.polydata.select.PolyDataCellSelector;
import edu.jhuapl.saavtk2.polydata.select.PolyDataRemoveSelectedCells;
import edu.jhuapl.saavtk2.polydata.select.VisibleNormalCellSelector;
import edu.jhuapl.saavtk2.util.Frustum;
import vtk.vtkDataObject;
import vtk.vtkExtractSelection;
import vtk.vtkGeometryFilter;
import vtk.vtkIdList;
import vtk.vtkIdTypeArray;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSelection;
import vtk.vtkSelectionNode;
import vtk.vtkSphereSource;
import vtk.vtkTriangle;
import vtk.vtkUnstructuredGrid;

public class PerspectiveProjection implements Projection
{
	Frustum frustum;
	PolyDataClip clipper;
	
	public PerspectiveProjection(Frustum frustum)
	{
		this.frustum=frustum;
		clipper=new PolyDataClipWithFrustum(frustum);
	}
	
	@Override
	public Vector3D getRayOrigin()
	{
		return frustum.getOrigin();
	}
	
	public Frustum getFrustumDefinition()
	{
		return frustum;
	}
		
	
	@Override
	public Vector3D getUpperRightUnit()
	{
		return frustum.getUpperRightUnit();
	}
	
	@Override
	public Vector3D getLowerLeftUnit()
	{
		return frustum.getLowerLeftUnit();
	}
	
	@Override
	public Vector3D getLowerRightUnit()
	{
		return frustum.getLowerRightUnit();
	}
	
	@Override
	public Vector3D getUpperLeftUnit()
	{
		return frustum.getUpperLeftUnit();
	}
	
	@Override
	public Vector3D getMidPointUnit()
	{
		return frustum.getBoresightUnit();
	}

	@Override
	public double getHorizontalMin()
	{
		return -1;
	}

	@Override
	public double getHorizontalMax()
	{
		return 1;
	}

	@Override
	public double getHorizontalExtent()
	{
		return 2;
	}
	
	@Override
	public double getHorizontalMid()
	{
		return 0;
	}

	@Override
	public double getVerticalMin()
	{
		return -1;
	}

	@Override
	public double getVerticalMax()
	{
		return 1;
	}

	@Override
	public double getVerticalExtent()
	{
		return 2;
	}

	@Override
	public double getVerticalMid()
	{
		return 0;
	}
	
	@Override
	public PerspectiveMapCoordinates project(Vector3D position)
	{
		Vector3D v=position.subtract(frustum.getOrigin());
		double x=v.dotProduct(frustum.getR());
		double y=v.dotProduct(frustum.getU());
		double z=v.dotProduct(frustum.getBoresightUnit());
		double mx=x/z/Math.tan(frustum.getFovXRad()/2);
		double my=y/z/Math.tan(frustum.getFovYRad()/2);
		//System.out.println(x+" "+y+" "+z+" "+" "+mx+" "+my);
		//System.out.println(mx+" "+my);
		return new PerspectiveMapCoordinates(mx,my);
	}
	
	@Override
	public Vector3D unproject(MapCoordinates mapCoordinates, DepthFunction depthFunction)
	{
		double scale=depthFunction.value(mapCoordinates);
		double tanfacx=Math.abs(Math.tan(Math.toRadians(frustum.getFovXDeg()/2)));
		double tanfacy=Math.abs(Math.tan(Math.toRadians(frustum.getFovYDeg()/2)));
		double rcoord=mapCoordinates.getX()*scale*tanfacx;
		double ucoord=mapCoordinates.getY()*scale*tanfacy;
		
		Vector3D bvec=frustum.getBoresightUnit().scalarMultiply(scale);
		Vector3D rvec=frustum.getR().scalarMultiply(rcoord);
		Vector3D uvec=frustum.getU().scalarMultiply(ucoord);
		return frustum.getOrigin().add(bvec).add(rvec).add(uvec);
	}

	@Override
	public vtkPolyData clipVisibleGeometry(vtkPolyData polyData)
	{
		PolyDataClipWithFrustum clipper=new PolyDataClipWithFrustum(frustum);
		vtkPolyData clippedPolyData=clipper.apply(polyData);
		PolyDataRemoveSelectedCells remover=new PolyDataRemoveSelectedCells();
		VisibleNormalCellSelector selector=new VisibleNormalCellSelector(clippedPolyData);
		selector.setViewPoint(frustum.getOrigin());
		selector.apply();
		remover.setIndicesToRemove(selector.getSelected());
		return remover.apply(clippedPolyData);
	}
	
	@Override
	public PerspectiveMapCoordinates createMapCoords(double x, double y)
	{
		return new PerspectiveMapCoordinates(x, y);
	}
	
	
	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		vtkSphereSource source=new vtkSphereSource();
		source.Update();
		vtkPolyData polyData=source.GetOutput();
		
		Vector3D origin=Vector3D.PLUS_K.scalarMultiply(2);
		Vector3D lookAt=Vector3D.ZERO;
		Vector3D up=Vector3D.PLUS_J;
		double fov=10;
		Frustum frustum=new Frustum(origin, lookAt, up, fov, fov);
		
		PerspectiveProjection projection=new PerspectiveProjection(frustum);
		vtkPolyData result=projection.clipVisibleGeometry(polyData);
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(result);
		writer.Write();

	}

}
