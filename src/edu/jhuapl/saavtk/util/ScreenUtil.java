package edu.jhuapl.saavtk.util;

import vtk.rendering.jogl.vtkJoglPanelComponent;

public class ScreenUtil
{
	public static double getScreenScale(vtkJoglPanelComponent aRenComp)
	{
		double openGlHeight = aRenComp.getComponent().getSurfaceHeight();
		double javaHeight = aRenComp.getComponent().getHeight();
		double scale = openGlHeight / javaHeight;
		return Math.max(scale, 1.0);
	}
}
