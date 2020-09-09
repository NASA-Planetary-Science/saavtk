package edu.jhuapl.saavtk.color.table;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Immutable class that defines a color table (which is used for mapping
 * floating values to a color).
 * <P>
 * A color table has the following attributes:
 * <UL>
 * <LI>Name of the color table
 * <LI>List of values used for interpolation
 * <LI>List of colors corresponding to the color values
 * <LI>The color to use for values of NaN
 * <LI>{@link ColorSpace} for which the table mapping should occur under
 * </UL>
 *
 * @author lopeznr1
 */
public class ColorTable
{
	// Constants
	/** The "invalid" color table. */
	public static final ColorTable Invalid = formColorTableInvalid();

	/** The (classic) rainbow color table. */
	public static final ColorTable Rainbow = formColorTableRainbow();

	// Attributes
	private final String name;
	private final ImmutableList<Double> interpolateL;
	private final ImmutableList<Color> colorL;
	private final Color nanColor;
	private final ColorSpace colorSpace;

	/** Standard Constructor */
	public ColorTable(String aName, Collection<Double> aInterpolateC, Collection<Color> aColorL, Color aNaNColor,
			ColorSpace aColorSpace)
	{
		name = aName;
		colorL = ImmutableList.copyOf(aColorL);
		colorSpace = aColorSpace;
		interpolateL = ImmutableList.copyOf(aInterpolateC);
		nanColor = aNaNColor;
	}

	/**
	 * Returns the name of the color table.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the {@link ColorSpace}.
	 */
	public ColorSpace getColorSpace()
	{
		return colorSpace;
	}

	/**
	 * Returns the list of colors used for interpolation in the color table.
	 */
	public ImmutableList<Color> getColorList()
	{
		return colorL;
	}

	/**
	 * Returns the list of values used for interpolation in the color table.
	 */
	public ImmutableList<Double> getInteropolateList()
	{
		return interpolateL;
	}

	/**
	 * Returns the color that will be used to color values of NaN.
	 */
	public Color getNanColor()
	{
		return nanColor;
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
		ColorTable other = (ColorTable) obj;
		if (colorL == null)
		{
			if (other.colorL != null)
				return false;
		}
		else if (!colorL.equals(other.colorL))
			return false;
		if (colorSpace != other.colorSpace)
			return false;
		if (interpolateL == null)
		{
			if (other.interpolateL != null)
				return false;
		}
		else if (!interpolateL.equals(other.interpolateL))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (nanColor == null)
		{
			if (other.nanColor != null)
				return false;
		}
		else if (!nanColor.equals(other.nanColor))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colorL == null) ? 0 : colorL.hashCode());
		result = prime * result + ((colorSpace == null) ? 0 : colorSpace.hashCode());
		result = prime * result + ((interpolateL == null) ? 0 : interpolateL.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nanColor == null) ? 0 : nanColor.hashCode());
		return result;
	}

	/**
	 * Utility helper method to form the "invalid" color table
	 */
	private static ColorTable formColorTableInvalid()
	{
		return new ColorTable("Invalid", ImmutableList.of(Double.NaN, Double.NaN),
				ImmutableList.of(Color.WHITE, Color.WHITE), Color.WHITE, ColorSpace.RGB);
	}

	/**
	 * Utility helper method to form the "classic" (rainbow) color table
	 */
	private static ColorTable formColorTableRainbow()
	{
		String name = "rainbow";

		List<Double> interpolateL = ImmutableList.of(-1.0, -0.5, 0.0, 0.5, 1.0);

		List<Color> colorL = ImmutableList.of( //
				new Color(0.0f, 0.0f, 1.0f), //
				new Color(0.0f, 1.0f, 1.0f), //
				new Color(0.0f, 1.0f, 0.0f), //
				new Color(1.0f, 1.0f, 0.0f), //
				new Color(1.0f, 0.0f, 0.0f));

		Color nanColor = Color.WHITE;

		return new ColorTable(name, interpolateL, colorL, nanColor, ColorSpace.RGB);
	}

}
