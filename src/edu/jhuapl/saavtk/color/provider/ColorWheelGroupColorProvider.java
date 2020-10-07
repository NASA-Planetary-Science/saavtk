package edu.jhuapl.saavtk.color.provider;

import java.awt.Color;

import edu.jhuapl.saavtk.util.ColorUtil;

/**
 * GroupColorProvider that will provide a ColorProvider corresponding to an
 * items index. Returned ColorProviders are selected via the relative position
 * of the item and that of a color wheel.
 * <P>
 * Since this class has no state data, the only access provided is via a
 * singleton constant.
 *
 * @author lopeznr1
 */
public class ColorWheelGroupColorProvider implements GroupColorProvider
{
	// Constants
	public static final GroupColorProvider Instance = new ColorWheelGroupColorProvider();

	/**
	 * Private constructor
	 */
	private ColorWheelGroupColorProvider()
	{
		; // Nothing to do
	}

	@Override
	public ColorProvider getColorProviderFor(Object aItem, int aIdx, int aMaxCnt)
	{
		Color tmpColor = ColorUtil.generateColor(aIdx, aMaxCnt);
		return new SimpleColorProvider(tmpColor);
	}

}
