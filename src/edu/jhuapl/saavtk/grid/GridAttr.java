package edu.jhuapl.saavtk.grid;

import java.awt.Color;

/**
 * Record that defines the attributes associated with a coordinate grid.
 * <p>
 * Note that the units of latitude and longitude are specified in degrees.
 *
 * @author lopeznr1
 */
public record GridAttr(boolean isVisible, Color mainColor, double lineWidth, double shiftFactor)
{
	// Constants
	private static final Color DefaultColor = new Color(0.20f, 0.20f, 0.20f);

	/** Default {@link GridAttr} */
	public static final GridAttr Default = new GridAttr(false, DefaultColor, 1.0, 7.0);

	/**
	 * Returns a {@link GridAttr} with the specified visibility.
	 */
	public GridAttr withIsVisible(boolean aIsVisible)
	{
		return new GridAttr(aIsVisible, mainColor, lineWidth, shiftFactor);
	}

	/**
	 * Returns a {@link GridAttr} with the specified color.
	 */
	public GridAttr withColor(Color aColor)
	{
		return new GridAttr(isVisible, aColor, lineWidth, shiftFactor);
	}

	/**
	 * Returns a {@link GridAttr} with the specified line width.
	 */
	public GridAttr withLineWidth(double aLineWidth)
	{
		return new GridAttr(isVisible, mainColor, aLineWidth, shiftFactor);
	}

	/**
	 * Returns a {@link GridAttr} with the specified shift factor.
	 */
	public GridAttr withOShiftFactor(double aShiftFactor)
	{
		return new GridAttr(isVisible, mainColor, lineWidth, aShiftFactor);
	}

}
