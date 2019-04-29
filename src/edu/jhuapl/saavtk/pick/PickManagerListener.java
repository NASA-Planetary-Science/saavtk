package edu.jhuapl.saavtk.pick;

/**
 * Listener that provides feedback whenever the installed Picker is changed.
 */
public interface PickManagerListener
{
	/**
	 * Callback that provides notification that the active Picker has changed.
	 */
	public void pickerChanged();

}
