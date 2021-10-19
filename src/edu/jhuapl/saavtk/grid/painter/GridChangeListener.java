package edu.jhuapl.saavtk.grid.painter;

/**
 * Interface that provides the callback mechanism for notification of
 * {@link CoordinatePainter} changes.
 *
 * @author lopeznr1
 */
public interface GridChangeListener
{
	/**
	 * Notification method that the {@link CoordinatePainter}'s configuration has
	 * changed.
	 *
	 * @param aSource The source that triggered this event.
	 * @param aType   The type of configuration change.
	 */
	public void handleGridChanged(Object aSource, GridChangeType aType);

}
