package edu.jhuapl.saavtk.model.structure.geotools;

public class LineSegment
{
	double[] start;
	double[] end;
	
	public LineSegment(double[] start, double[] end)
	{
		this.start=start;
		this.end=end;
	}
	
	public double[] getStart()
	{
		return start;
	}
	
	public double[] getEnd()
	{
		return end;
	}
}
