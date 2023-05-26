package edu.jhuapl.saavtk.color.provider;

import java.awt.Color;

import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.saavtk.util.ColorUtil;

/**
 * ColorProvider where the returned color will be a function of the specified
 * value and the baseline color.
 *
 * @author lopeznr1
 */
public class SimpleColorProvider implements ColorProvider
{
	// Attributes
	private final Color baseColor;

	// State vars
	private final float[] baseHSL;

	/**
	 * Standard Constructor
	 *
	 * @param aColor The color that will be used as the baseline color. All returned
	 *               colors should be a function of the baseline color.
	 */
	public SimpleColorProvider(Color aColor)
	{
		baseColor = aColor;
		baseHSL = ColorUtil.getHSLColorComponents(aColor);
	}

	@Override
	public Color getBaseColor()
	{
		return baseColor;
	}

	@Override
	public Color getColor(double aMinVal, double aMaxVal, double aTargVal)
	{
		// Color will be based on the baseColor scaled by aTargVal
		return ColorUtil.scaleLightness(baseHSL, aTargVal, aMinVal, aMaxVal, 0.5f);
	}

	@Override
	public FeatureType getFeatureType()
	{
		return null;
	}

}
