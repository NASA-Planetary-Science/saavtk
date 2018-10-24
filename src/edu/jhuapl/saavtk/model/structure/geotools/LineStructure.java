package edu.jhuapl.saavtk.model.structure.geotools;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.EllipsePolygon;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.geotools.EllipseStructure.Parameters;
import vtk.vtkCellArray;
import vtk.vtkPoints;

public class LineStructure implements Structure
{
	LineSegment[]	segments;
	double[]		centroid;
	LineStyle		style;
	String			label;

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
		this.segments = segments;
		centroid = StructureUtil.centroid(segments);
		this.style = style;
		this.label = label;
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
	
	public static List<LineStructure> fromSbmtStructure(LineModel model)
	{
        List<LineStructure> structures = Lists.newArrayList();
        int[] ids = model.getSelectedStructures();
        if (ids.length == 0)
        {
            ids = new int[model.getNumberOfStructures()];
            for (int i = 0; i < ids.length; i++)
                ids[i] = i;
        }
        for (int i = 0; i < ids.length; i++)
        {
            Line poly = (Line) model.getStructure(ids[i]);
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
            
            
            structures.add(new LineStructure(segmentArr, style, label));
            

        }
        return structures;

	}
	

}
