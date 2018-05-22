package edu.jhuapl.saavtk2.image.projection;

public interface DepthFunction
{
	public double value(MapCoordinates mapCoordinates);
	public double getInfinityDepth();
}
