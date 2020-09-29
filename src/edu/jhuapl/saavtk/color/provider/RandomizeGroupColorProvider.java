package edu.jhuapl.saavtk.color.provider;

import java.awt.Color;
import java.util.Random;

/**
 * GroupColorProvider that will provide a group of random ColorProviders for the
 * specified seed.
 * <P>
 * Note that same ColorProvider will be returned for a specifiec seed and a
 * specific index.
 *
 * @author lopeznr1
 */
public class RandomizeGroupColorProvider implements GroupColorProvider
{
	// Attributes
	private final long seed;

	// Cache vars
	private ColorProvider[] colorProviderArr;

	/**
	 * Private constructor
	 */
	public RandomizeGroupColorProvider(long aSeed)
	{
		seed = aSeed;

		colorProviderArr = null;
	}

	@Override
	public ColorProvider getColorProviderFor(Object aItem, int aIdx, int aMaxCnt)
	{
		if (colorProviderArr == null || aMaxCnt > colorProviderArr.length)
			colorProviderArr = formColorProviderArr(aMaxCnt);

		return colorProviderArr[aIdx];
	}

	/**
	 * Helper method to form an array of ColorProivders.
	 *
	 * @param aMaxCnt
	 */
	private ColorProvider[] formColorProviderArr(int aMaxCnt)
	{
		ColorProvider[] retItemArr = new ColorProvider[aMaxCnt];

		Random tmpRandom = new Random(seed);
		for (int c1 = 0; c1 < aMaxCnt; c1++)
		{
			int r = tmpRandom.nextInt(256);
			int g = tmpRandom.nextInt(256);
			int b = tmpRandom.nextInt(256);

			Color tmpColor = new Color(r, g, b);
			retItemArr[c1] = new SimpleColorProvider(tmpColor);
		}

		return retItemArr;
	}

}