package edu.jhuapl.saavtk.status;

/**
 * Implementation of {@link StatusNotifier} that silently ignores all
 * notifications.
 * <P>
 * Singleton instance is acquired via: {@link QuietStatusNotifier#Instance}
 *
 * @author lopeznr1
 */
public class QuietStatusNotifier implements StatusNotifier
{
	/** Singleton Instance **/
	public static final QuietStatusNotifier Instance = new QuietStatusNotifier();

	/** Singleton Constructor */
	private QuietStatusNotifier()
	{
	}

	@Override
	public void setPriStatus(String aBriefMsg, String aDetailMsg)
	{
		; // Nothing to do
	}

	@Override
	public void setSecStatus(String aBriefMsg, String aDetailMsg)
	{
		; // Nothing to do
	}

}
