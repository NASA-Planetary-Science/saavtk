package edu.jhuapl.saavtk.color.painter;

/**
 * Interface that provides the callback mechanism for notification of
 * {@link ColorBarPainter} changes.
 *
 * @author lopeznr1
 */
public interface ColorBarChangeListener
{
	/**
	 * Notification method that the {@link ColorBarPainter}'s configuration has
	 * changed.
	 *
	 * @param aSource The source that triggered this event.
	 * @param aType   The type of configuration change.
	 */
	public void handleColorBarChanged(Object aSource, ColorBarChangeType aType);

}
