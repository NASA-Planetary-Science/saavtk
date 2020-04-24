package edu.jhuapl.saavtk.status;

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
	 * @param aCellId Corresponds to the cell id that was picked.
	 */
	public String getDisplayInfo(int aCellId);
}
