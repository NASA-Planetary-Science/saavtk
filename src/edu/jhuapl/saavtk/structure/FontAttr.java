package edu.jhuapl.saavtk.structure;

import java.awt.Color;

/**
 * Immutable class that defines the attributes associated with a Font.
 *
 * @author lopeznr1
 */
public class FontAttr
{
	// Constants
	public static final FontAttr Default = new FontAttr("Plain", Color.BLACK, 16, true);

	// Attributes
	private final String face;
	private final Color color;
	private final int size;
	private final boolean isVisible;

	/**
	 * Standard Constructor
	 */
	public FontAttr(String aFace, Color aColor, int aSize, boolean aIsVisible)
	{
		face = aFace;
		color = aColor;
		size = aSize;
		isVisible = aIsVisible;
	}

	public String getFace()
	{
		return face;
	}

	public Color getColor()
	{
		return color;
	}

	public int getSize()
	{
		return size;
	}

	public boolean getIsVisible()
	{
		return isVisible;
	}

}
