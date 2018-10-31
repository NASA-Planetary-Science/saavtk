package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.EllipsePolygon;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure.Parameters;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkCellArray;
import vtk.vtkPoints;

public class LineStructure implements Structure
{
	LineSegment[]	segments;
	double[]		centroid;
	LineStyle		style;
	String			label;
	Vector3D[] controlPoints;

	public LineStructure(List<LineSegment> segments)
	{
	    this(segments.toArray(new LineSegment[segments.size()]));
	}
	
	public LineStructure(LineSegment[] segments)
	{
		this(segments, "");
	}

	public LineStructure(LineSegment[] segments, String label)
	{
		this(segments, new LineStyle(), label);
	}
    public LineStructure(LineSegment[] segments, LineStyle style, String label)
    {
        this(segments,style,label,null);
    }
    
	public LineStructure(LineSegment[] segments, LineStyle style, String label, Vector3D[] controlPoints)
	{
		this.segments = segments;
		centroid = StructureUtil.centroid(segments);
		this.style = style;
		this.label = label;
		this.controlPoints=controlPoints;
	}
	
	public int getNumberOfSegments()
	{
		return segments.length;
	}
	
	public LineSegment getSegment(int i)
	{
		return segments[i];
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public double[] getCentroid()
	{
		return centroid;
	}
	
	public LineStyle getStyle()
	{
		return style;
	}
	
	public int getNumberOfControlPoints()
	{
	    if (controlPoints==null)
	        return 0;
	    else
	        return controlPoints.length;
	}
	
	public Vector3D getControlPoint(int i)
	{
	    return controlPoints[i];
	}
	
	public static List<LineStructure> fromSbmtStructure(LineModel model)
	{
        List<LineStructure> structures = Lists.newArrayList();
        for (int i=0; i<model.getNumberOfStructures(); i++)
        {
            Line poly = (Line) model.getStructure(i);
            int[] c = poly.getColor();
            double w = model.getLineWidth();
            LineStyle style = new LineStyle(new Color(c[0], c[1], c[2]), w);
            String label = poly.getLabel();
            //
        
            List<LineSegment> segments=Lists.newArrayList();
            for (int j=0; j<poly.getNumberOfPoints()-1; j++)
            {
                    double[] start=poly.getPoint(j).toArray();
                    double[] end=poly.getPoint(j+1).toArray();
                    segments.add(new LineSegment(start, end));
            }
            LineSegment[] segmentArr=new LineSegment[segments.size()];
            for (int j=0; j<segments.size(); j++)
                segmentArr[j]=segments.get(j);

            Vector3D[] controlPoints=new Vector3D[poly.controlPoints.size()];
            for (int m=0; m<controlPoints.length; m++)
                controlPoints[m]=new Vector3D(MathUtil.latrec(poly.controlPoints.get(m)));
            
            
            
            structures.add(new LineStructure(segmentArr, style, label, controlPoints));
            

        }
        return structures;

	}
	

}
