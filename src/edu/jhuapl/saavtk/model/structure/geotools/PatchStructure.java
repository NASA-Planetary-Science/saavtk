package edu.jhuapl.saavtk.model.structure.geotools;

import java.util.Arrays;

public class PatchStructure implements Structure
{
	PatchStyle style;
	double[][] points;
	double[] centroid;
	String label;

	public PatchStructure(double[][] points)
	{
		this(points, new PatchStyle());
	}
	
	public PatchStructure(double[][] points, String label)
	{
		this(points, new PatchStyle(), label);
	}
	
	public PatchStructure(double[][] points, PatchStyle style, String label)	// last point must equal first point
	{
		this.points=points;
		this.style=style;
		this.label=label;
		if (Arrays.equals(points[0], points[points.length-1]))
			centroid=StructureUtil.centroidClosedPoly(points);
		else
			centroid=StructureUtil.centroid(points);
	}
	
	public PatchStructure(double[][] points, PatchStyle style)
	{
		this(points,style,"");
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
	
	public PatchStyle getStyle()
	{
		return style;
	}
	
	public double[][] getPoints()
	{
		return points;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"{centroid="+StructureUtil.toString(centroid)+",label="+label+",style="+style+",points="+StructureUtil.toString(points)+"}";
	}
}
