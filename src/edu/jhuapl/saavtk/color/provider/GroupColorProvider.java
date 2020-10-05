package edu.jhuapl.saavtk.color.provider;

/**
 * Interface which defines a method for retrieving the {@link ColorProvider}s
 * that should be used to render a specific item.
 * <P>
 * This interface allows for ColorProviders to be provided for each specific
 * item in a group of n-items.
 *
 * @author lopeznr1
 */
public interface GroupColorProvider
{
	/**
	 * Returns the {@link ColorProvider} that is used to colorize the specified
	 * item.
	 *
	 * @param aIdx    The n-th item of a list of items. aIdx should be in the range
	 *                of: [0, aMaxCnt - 1]
	 * @param aMaxIdx The number of items in the list.
	 */
	public ColorProvider getColorProviderFor(Object aItem, int aIdx, int aMaxCnt);

}
