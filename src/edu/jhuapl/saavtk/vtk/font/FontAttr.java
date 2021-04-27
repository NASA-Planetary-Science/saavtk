package edu.jhuapl.saavtk.vtk.font;

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
	private final boolean isBold;
	private final boolean isItalic;

	/** Standard Constructor */
	public FontAttr(String aFace, Color aColor, int aSize, boolean aIsVisible, boolean aIsBold, boolean aIsItalic)
	{
		face = aFace;
		color = aColor;
		size = aSize;

		isVisible = aIsVisible;
		isBold = aIsBold;
		isItalic = aIsItalic;
	}

	/** Simplified Constructor */
	public FontAttr(String aFace, Color aColor, int aSize, boolean aIsVisible)
	{
		this(aFace, aColor, aSize, aIsVisible, false, false);
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

	public boolean getIsBold()
	{
		return isBold;
	}

	public boolean getIsItalic()
	{
		return isItalic;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((face == null) ? 0 : face.hashCode());
		result = prime * result + (isBold ? 1231 : 1237);
		result = prime * result + (isItalic ? 1231 : 1237);
		result = prime * result + (isVisible ? 1231 : 1237);
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FontAttr other = (FontAttr) obj;
		if (color == null)
		{
			if (other.color != null)
				return false;
		}
		else if (!color.equals(other.color))
			return false;
		if (face == null)
		{
			if (other.face != null)
				return false;
		}
		else if (!face.equals(other.face))
			return false;
		if (isBold != other.isBold)
			return false;
		if (isItalic != other.isItalic)
			return false;
		if (isVisible != other.isVisible)
			return false;
		if (size != other.size)
			return false;
		return true;
	}

}
