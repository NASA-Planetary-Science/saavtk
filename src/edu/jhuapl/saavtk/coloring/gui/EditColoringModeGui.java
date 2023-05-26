package edu.jhuapl.saavtk.coloring.gui;

/**
 * Interface that defines the methods to allow configuration of a shape model's
 * coloring (mode).
 *
 * @author lopeznr1
 */
public interface EditColoringModeGui
{
	/**
	 * Notifies the panel that it has been activated.
	 * <p>
	 * When this method is called the corresponding model's coloring (mode) should
	 * be updated to match the configuration as specified by the panel.
	 */
	public void activate(Object aSource);

	/**
	 * Notifies the panel that it is no longer active.
	 * <p>
	 * Note there is no need to update the reference small body since another panel
	 * will be activated when this one is deactivated.
	 */
	public void deactivate();

}
