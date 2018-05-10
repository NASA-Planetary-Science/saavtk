package edu.jhuapl.saavtk2.image.boundary;

import java.util.Arrays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk2.util.PolyDataUtil;
import edu.jhuapl.saavtk2.image.keys.ImageKey;
import edu.jhuapl.saavtk2.image.projection.MapCoordinates;
import edu.jhuapl.saavtk2.image.projection.Projection;
import edu.jhuapl.saavtk2.image.projection.depthfunc.ConstantDepthFunction;
import edu.jhuapl.saavtk2.image.projection.depthfunc.DepthFunction;
import vtk.vtkCellArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkVertex;
import vtk.vtksbCellLocator;

public class GenericImageBoundary implements ImageBoundary
{
	protected Projection projection;
    protected vtkPolyData polyData;
    protected double offset =0.003;
    GenericPolyhedralModel bodyModel;
    double rayLength;
    ImageKey imageKey;
    
    public GenericImageBoundary(Projection projection, GenericPolyhedralModel bodyModel, ImageKey imageKey)
	{
    	this.projection=projection;
    	this.polyData=new vtkPolyData();
    	this.bodyModel=bodyModel;
    	rayLength=bodyModel.getBoundingBoxDiagonalLength()+projection.getRayOrigin().getNorm();
    	this.imageKey=imageKey;
    	initialize();
	}
    
    public vtkPolyData getPolyDataRepresentation()
    {
        return polyData;
    }

    public void setOffset(double offsetnew)
    {
        this.offset=offsetnew;
    }

    public double getOffset()
    {
        return offset;
    }

    public double getDefaultOffset()
    {
        // Subclasses should redefine this if they support offset.
        return 3.0;
    }

    @Override
    public Projection getProjection()
    {
    	return projection;
    }

    private void initialize()
    {
        // Using the frustum, go around the boundary of the frustum and intersect with
        // the asteroid.

        polyData=new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray verts = new vtkCellArray();

        vtksbCellLocator cellLocator = bodyModel.getCellLocator();
        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);

        final int NHORZ=100;
        final int NVERT=100;
        
        
        DepthFunction depthFunction=new ConstantDepthFunction(rayLength);
        double x,y;
        double[] rayEndpoint,pt;

        
        for (int i=0; i<NHORZ; i++)
        {
        	x=(double)i/((double)NHORZ-1.)*projection.getHorizontalExtent()+projection.getHorizontalMin();
        	// top
        	y=projection.getVerticalMax();
        	rayEndpoint=projection.unproject(new MapCoordinates(x, y), depthFunction).toArray();
        	pt=PolyDataUtil.rayIntersect(cellLocator, projection.getRayOrigin().toArray(), rayEndpoint);
        	if (pt!=null)
        	{
        		vtkVertex vert=new vtkVertex();
        		vert.GetPointIds().SetId(0, points.InsertNextPoint(pt));
        		verts.InsertNextCell(vert);
        	}
        	// bottom
        	y=projection.getVerticalMin();
        	rayEndpoint=projection.unproject(new MapCoordinates(x, y), depthFunction).toArray();
        	pt=PolyDataUtil.rayIntersect(cellLocator, projection.getRayOrigin().toArray(), rayEndpoint);
        	if (pt!=null)
        	{
        		vtkVertex vert=new vtkVertex();
        		vert.GetPointIds().SetId(0, points.InsertNextPoint(pt));
        		verts.InsertNextCell(vert);
        	}
        }

        for (int j=0; j<NVERT; j++)
        {
        	y=(double)j/((double)NVERT-1.)*projection.getVerticalExtent()+projection.getVerticalMin();
        	// top
        	x=projection.getHorizontalMax();
        	rayEndpoint=projection.unproject(new MapCoordinates(x, y), depthFunction).toArray();
        	pt=PolyDataUtil.rayIntersect(cellLocator, projection.getRayOrigin().toArray(), rayEndpoint);
        	if (pt!=null)
        	{
        		vtkVertex vert=new vtkVertex();
        		vert.GetPointIds().SetId(0, points.InsertNextPoint(pt));
        		verts.InsertNextCell(vert);
        	}
        	// bottom
        	x=projection.getHorizontalMin();
        	rayEndpoint=projection.unproject(new MapCoordinates(x, y), depthFunction).toArray();
        	pt=PolyDataUtil.rayIntersect(cellLocator, projection.getRayOrigin().toArray(), rayEndpoint);
        	if (pt!=null)
        	{
        		vtkVertex vert=new vtkVertex();
        		vert.GetPointIds().SetId(0, points.InsertNextPoint(pt));
        		verts.InsertNextCell(vert);
        	}
        }
        
        polyData.SetPoints(points);
        polyData.SetVerts(verts);
    }

	@Override
	public ImageKey getKey()
	{
		return imageKey;
	}


}
