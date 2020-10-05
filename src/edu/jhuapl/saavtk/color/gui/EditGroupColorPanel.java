package edu.jhuapl.saavtk.color.gui;

import edu.jhuapl.saavtk.color.provider.GroupColorProvider;

/**
 * Interface that defines the methods to allow configuration of a
 * GroupColorProvider UI element.
 *
 * @author lopeznr1
 */
public interface EditGroupColorPanel
{
	/**
	 * Notifies the panel of its active state.
	 */
	public abstract void activate(boolean aIsActive);

	/**
	 * Returns the {@link GroupColorProvider} that should be used for coloring
	 * items.
	 */
	public abstract GroupColorProvider getGroupColorProvider();

}
