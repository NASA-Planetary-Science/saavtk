package edu.jhuapl.saavtk2.image.projection.depthfunc;

import edu.jhuapl.saavtk2.image.projection.MapCoordinates;

public interface DepthFunction
{
	public double value(MapCoordinates mapCoordinates);
	public double getInfinityDepth();
}
