package edu.jhuapl.saavtk.color.provider;

import java.awt.Color;

import edu.jhuapl.saavtk.feature.FeatureType;

/**
 * Singleton instance of a {@link ColorProvider} that defines the invalid
 * {@link ColorProvider}.
 *
 * @author lopeznr1
 */
class InvalidColorProvider implements ColorProvider
{
	// Constants
	/** The "invalid" {@link ColorProvider}. */
	static InvalidColorProvider Instance = new InvalidColorProvider();

	/** Private Singleton Constructor */
	private InvalidColorProvider()
	{
		; // Nothing to do
	}

	@Override
	public Color getBaseColor()
	{
		return null;
	}

	@Override
	public Color getColor(double aMinVal, double aMaxVal, double aTargVal)
	{
		return null;
	}

	@Override
	public FeatureType getFeatureType()
	{
		return FeatureType.Invalid;
	}

}
