package edu.jhuapl.saavtk2.image.projection;

public class ConstantDepthFunction implements DepthFunction
{

	double depth;
	
	public ConstantDepthFunction(double depth)
	{
		this.depth=depth;
	}
	
	@Override
	public double value(MapCoordinates mapCoordinates)
	{
		return depth;
	}

	@Override
	public double getInfinityDepth()
	{
		return depth;
	}

}
