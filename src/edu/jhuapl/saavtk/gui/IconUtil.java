package edu.jhuapl.saavtk.gui;

import javax.swing.ImageIcon;

public class IconUtil
{

	/**
	 * Utility method to load an icon from the specified resource.
	 */
	public static ImageIcon loadIcon(String iconPath)
	{
		return new ImageIcon(ClassLoader.getSystemResource(iconPath));
	}

}
