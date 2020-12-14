package edu.jhuapl.saavtk.gui.render;

/**
 * Implementation of {@link SceneChangeNotifier} that allows for the destination
 * of the notifications to be dynamically changed.
 *
 * @author lopeznr1
 */
public class ConfigurableSceneNotifier implements SceneChangeNotifier
{
	// State vars
	private SceneChangeNotifier targSceneChangeNotifier;

	/** Standard Constructor */
	public ConfigurableSceneNotifier()
	{
		targSceneChangeNotifier = null;
	}

	/**
	 * Dynamically sets the target SceneChangeNotifier
	 */
	public void setTarget(SceneChangeNotifier aSceneChangeNotifier)
	{
		targSceneChangeNotifier = aSceneChangeNotifier;
	}

	@Override
	public void notifySceneChange()
	{
		if (targSceneChangeNotifier == null)
			return;

		targSceneChangeNotifier.notifySceneChange();
	}

}
