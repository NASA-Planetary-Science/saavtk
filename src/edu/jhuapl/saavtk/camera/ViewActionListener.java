package edu.jhuapl.saavtk.camera;

/**
 * Listener that provides notification whenever the {@link View}'s state has
 * changed.
 *
 * @author lopeznr1
 */
public interface ViewActionListener
{
	/**
	 * Method that handles the view changed event. This method will be called
	 * whenever the {@link View}'s state has been changed.
	 *
	 * @param aSource The object that generated this event.
	 */
	public void handleViewAction(Object aSource);

}
