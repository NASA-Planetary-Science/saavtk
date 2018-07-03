package edu.jhuapl.saavtk.model.structure.geotools;

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
	

}
