package edu.jhuapl.saavtk.color.gui.bar;

import java.awt.Color;

import edu.jhuapl.saavtk.color.painter.ColorBarPainter;

/**
 * Immutable class that defines the attributes associated with the background of
 * a {@link ColorBarPainter} configuration.
 *
 * @author lopeznr1
 */
public class BackgroundAttr
{
	// Attributes
	private final ShowMode mode;
	private final Color color;

	/** Standard Constructor */
	public BackgroundAttr(ShowMode aMode, Color aColor)
	{
		mode = aMode;
		color = aColor;
	}

	public ShowMode getMode()
	{
		return mode;
	}

	public Color getColor()
	{
		return color;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
		BackgroundAttr other = (BackgroundAttr) obj;
		if (color == null)
		{
			if (other.color != null)
				return false;
		}
		else if (!color.equals(other.color))
			return false;
		if (mode != other.mode)
			return false;
		return true;
	}

}
