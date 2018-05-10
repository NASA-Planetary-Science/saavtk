package edu.jhuapl.saavtk2.image.projection.depthfunc;

import edu.jhuapl.saavtk2.image.projection.MapCoordinates;

public class UnitDepthFunction<C extends MapCoordinates> extends ConstantDepthFunction
{

	public UnitDepthFunction()
	{
		super(1);
	}

}
