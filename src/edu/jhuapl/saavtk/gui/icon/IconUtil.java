package edu.jhuapl.saavtk.gui.icon;

import java.net.URL;

import javax.swing.ImageIcon;

public class IconUtil
{

	/**
	 * Utility method to load an icon from the specified resource.
	 */
	public static ImageIcon loadIcon(String aIconPath)
	{
		return new ImageIcon(ClassLoader.getSystemResource(aIconPath));
	}

	/**
	 * Utility method to load an icon from the specified resource.
	 */
	public static ImageIcon loadIcon(URL aURL)
	{
		return new ImageIcon(aURL);
	}

	/**
	 * Utility method to load an icon from the specified resource.
	 * <P>
	 * The resource must be located in the same package as ImageIcon.
	 */
	public static ImageIcon loadIconLocal(String aIconPath)
	{
		return loadIcon(IconUtil.class.getResource(aIconPath));
	}

}
