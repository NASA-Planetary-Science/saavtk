package edu.jhuapl.saavtk.camera;

/**
 * Listener that provides notification whenever the {@link Camera}'s state has
 * changed.
 *
 * @author lopeznr1
 */
public interface CameraActionListener
{
	/**
	 * Notification method for when the {@link Camera}'s state has changed.
	 *
	 * @param aSource The object that generated this event.
	 */
	public void handleCameraAction(Object aSource);

}
