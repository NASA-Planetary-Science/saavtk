package edu.jhuapl.saavtk.model.structure.geotools;


public class PointStructure implements Structure
{
	double[] location;
	PointStyle style;
	String label;

	public PointStructure(double[] location)
	{
		this(location, "");
	}
	
	public PointStructure(double[] location, String label)
	{
		this(location, new PointStyle(), label);
	}
	
	public PointStructure(double[] location, PointStyle style, String label)
	{
		this.location=location;
		this.style=style;
		this.label=label;
	}
	
	@Override
	public String getLabel()
	{
		return label;
	}
	
	public PointStyle getStyle()
	{
		return style;
	}
	
	@Override
	public double[] getCentroid()
	{
		return location;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"{centroid="+StructureUtil.toString(location)+",label="+label+",style="+style+"}";
	}
	
	
	
}
