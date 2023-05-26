package edu.jhuapl.saavtk.view.lod;

/**
 * Interface that allows an an object to declare it's interest in
 * {@link LodMode} changes.
 * <P>
 * Implementors of this interface will be notified of the instantaneous
 * {@link LodMode} state.
 *
 * @author lopeznr1
 */
public interface LodActor
{
	/**
	 * Sets the instantaneous {@link LodMode}.
	 */
	public void setLodMode(LodMode aLodMode);

}
