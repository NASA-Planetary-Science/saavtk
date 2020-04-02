package edu.jhuapl.saavtk.gui.render;

/**
 * Interface that defines a mechanism to send a notification that the scene has
 * changed.
 * 
 * @author lopeznr1
 */
public interface SceneChangeNotifier
{
	/**
	 * Sends out notification that the scene has changed.
	 */
	public void notifySceneChange();

}
