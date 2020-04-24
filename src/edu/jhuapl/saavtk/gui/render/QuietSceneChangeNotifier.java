package edu.jhuapl.saavtk.gui.render;

/**
 * Implementation of {@link SceneChangeNotifier} that silently ignores all
 * notifications.
 * <P>
 * Singleton instance is acquired via: {@link QuietSceneChangeNotifier#Instance}
 * 
 * @author lopeznr1
 */
public class QuietSceneChangeNotifier implements SceneChangeNotifier
{
	/** Singleton Instance **/
	public static final QuietSceneChangeNotifier Instance = new QuietSceneChangeNotifier();

	/**
	 * Singleton Constructor
	 */
	private QuietSceneChangeNotifier()
	{
	}

	@Override
	public void notifySceneChange()
	{
		; // Nothing to do
	}

}
