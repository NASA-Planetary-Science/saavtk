package edu.jhuapl.saavtk.gui.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Collection of utility methods that provide a singular location for the
 * definition, management, and retrieval of icons.
 * 
 * @author lopeznr1
 */
public class IconUtil
{
	/**
	 * Returns the icon that should be used for "Select All" buttons
	 */
	public static Icon getSelectAll()
	{
		return IconUtil.loadIcon("resources/icons/itemSelectAll.png");
	}

	/**
	 * Returns the icon that should be used for "Select Invert" buttons
	 */
	public static Icon getSelectInvert()
	{
		return IconUtil.loadIcon("resources/icons/itemSelectInvert.png");
	}

	/**
	 * Returns the icon that should be used for "Select None" buttons
	 */
	public static Icon getSelectNone()
	{
		return IconUtil.loadIcon("resources/icons/itemSelectNone.png");
	}

	/**
	 * Utility helper method to load an icon from the specified resource.
	 */
	private static ImageIcon loadIcon(String aIconPath)
	{
		return new ImageIcon(ClassLoader.getSystemResource(aIconPath));
	}

}
