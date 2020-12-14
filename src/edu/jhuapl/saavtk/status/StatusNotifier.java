package edu.jhuapl.saavtk.status;

/**
 * Interface that defines a mechanism to send notification that (textual) status
 * has changed.
 * <P>
 * Two levels of notification is provided (primary and secondary).
 *
 * @author lopeznr1
 */
public interface StatusNotifier
{
	/**
	 * Sets the primary status.
	 *
	 * @param aBriefMsg  A brief description of the status.
	 * @param aDetailMsg An expanded description of the status. The detail message
	 *                   may be shown at the same time as a tool tip.
	 */
	public void setPriStatus(String aBriefMsg, String aDetailMsg);

	/**
	 * Sets the secondary status;
	 *
	 * @param aBriefMsg  A brief description of the status.
	 * @param aDetailMsg An expanded description of the status. The detail message
	 *                   may be shown at the same time as a tool tip.
	 */
	public void setSecStatus(String aBriefMsg, String aDetailMsg);

}
