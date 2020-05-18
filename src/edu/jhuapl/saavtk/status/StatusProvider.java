package edu.jhuapl.saavtk.status;

import edu.jhuapl.saavtk.pick.PickTarget;

/**
 * Interface that defines methods used acquire textual description associated
 * with the object.
 * <P>
 * The textual description will typically be utilized in the application's
 * status bar.
 *
 * @author lopeznr1
 */
public interface StatusProvider
{
	/**
	 * Returns a short informational description.
	 *
	 * @param aPickTarget Corresponds to the target that was picked.
	 */
	public String getDisplayInfo(PickTarget aPickTarget);
}
