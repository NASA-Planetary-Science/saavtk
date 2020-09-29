package edu.jhuapl.saavtk.color.provider;

import java.awt.Color;

import edu.jhuapl.saavtk.feature.FeatureType;

/**
 * Object that provides the color that should be utilized given a value with a
 * range.
 *
 * @author lopeznr1
 */
public interface ColorProvider
{
	/**
	 * Returns the base color. The base color is the primary color for which all
	 * other returned values are a function of.
	 * <P>
	 * This may return null if there is no dominant color.
	 */
	public Color getBaseColor();

	/**
	 * Method that returns the color that should be utilized given the specified
	 * range and the actual value within that range.
	 *
	 * @param aMinVal
	 * @param aMaxVal
	 * @param aTargVal
	 * @return
	 */
	public Color getColor(double aMinVal, double aMaxVal, double aTargVal);

	/**
	 * Method that returns the {@link FeatureType} that this {@link ColorProvider}
	 * should colorize.
	 * <P>
	 * Note that the ColorProvider may not colorize based on Features in which case
	 * null should be returned.
	 */
	public FeatureType getFeatureType();

}
