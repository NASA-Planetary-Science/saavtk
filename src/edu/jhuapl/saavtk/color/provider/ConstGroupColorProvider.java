package edu.jhuapl.saavtk.color.provider;

/**
 * Immutable class that provides a GroupColorProvider which always returns the
 * same ColorProvider for all items.
 *
 * @author lopeznr1
 */
public class ConstGroupColorProvider implements GroupColorProvider
{
	// Attributes
	private final ColorProvider refColorProvider;

	/**
	 * Standard Constructor
	 */
	public ConstGroupColorProvider(ColorProvider aColorProvider)
	{
		refColorProvider = aColorProvider;
	}

	@Override
	public ColorProvider getColorProviderFor(Object aItem, int aIdx, int aMaxCnt)
	{
		return refColorProvider;
	}

}
