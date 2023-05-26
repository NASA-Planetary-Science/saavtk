package edu.jhuapl.saavtk.color.provider;

import java.awt.Color;

import edu.jhuapl.saavtk.feature.FeatureType;

/**
 * ColorProvider where there is no variation based on intensity. All values will
 * be mapped to the same color.
 *
 * @author lopeznr1
 */
public class ConstColorProvider implements ColorProvider
{
	// Attributes
	private final Color baseColor;

	/**
	 * Standard Constructor
	 *
	 * @param aColor The color will be utilized as the constant color.
	 */
	public ConstColorProvider(Color aColor)
	{
		baseColor = aColor;
	}

	@Override
	public Color getBaseColor()
	{
		return baseColor;
	}

	@Override
	public Color getColor(double aMinVal, double aMaxVal, double aTargVal)
	{
		return baseColor;
	}

	@Override
	public FeatureType getFeatureType()
	{
		return null;
	}

}
