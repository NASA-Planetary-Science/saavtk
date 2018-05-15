package edu.jhuapl.saavtk2.image.filters;

import edu.jhuapl.saavtk.colormap.Colormaps;

public class GrayscaleColormappingFilter extends ColormappingFilter
{

	public GrayscaleColormappingFilter()
	{
		super(Colormaps.getNewInstanceOfBuiltInColormap("grayscale"));
	}

	
}
