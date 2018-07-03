package edu.jhuapl.saavtk.model.structure.geotools;

public class EllipseStructure extends LineStructure
{
	public static class Parameters
	{
		public double[] center;
		public double majorRadius;
		public double flattening;
		public double angle;
		
		public Parameters(double[] center, double majorRadius, double flattening, double angle)
		{
			super();
			this.center = center;
			this.majorRadius = majorRadius;
			this.flattening = flattening;
			this.angle = angle;
		}
		
	}
	
	Parameters params;

	public EllipseStructure(LineSegment[] segments, LineStyle style, String label, Parameters params)
	{
		super(segments, style, label);
		this.params=params;
	}

	public EllipseStructure(LineSegment[] segments, String label, Parameters params)
	{
		super(segments, label);
		this.params=params;
	}

	public EllipseStructure(LineSegment[] segments, Parameters params)
	{
		super(segments);
		this.params=params;
	}
	
	public Parameters getParameters()
	{
		return params;
	}

}
