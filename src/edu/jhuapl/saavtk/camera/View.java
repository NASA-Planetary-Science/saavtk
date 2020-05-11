package edu.jhuapl.saavtk.camera;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.SaavtkLODActor;

/**
 * Interface that defines the methods to interact with a view.
 * <P>
 * The following functionality is provided:
 * <UL>
 * <LI>View change listener mechanism
 * <LI>Level-of-detail (LOD) configuration
 * <LI>Access to the associated {@link Camera}
 * </UL>
 *
 * @author lopeznr1
 */
public interface View
{
	/**
	 * Registers a {@link ViewActionListener} with this view.
	 */
	public void addViewChangeListener(ViewActionListener aListener);

	/**
	 * Deregisters a {@link ViewActionListener} with this view.
	 */
	public void delViewChangeListener(ViewActionListener aListener);

	/**
	 * Returns the associated {@link Camera}.
	 */
	public Camera getCamera();

	/**
	 * Method that returns true if LOD is flagged on.
	 * <P>
	 * Note this method will return false if there are no {@link SaavtkLODActor}s
	 * installed into the reference {@link ModelManager}. If any of the
	 * {@link SaavtkLODActor}s have had there LOD flag turned on this method will
	 * return true.
	 */
	public boolean getLodFlag();

	/**
	 * Method that will toggle the LOD flag of {@link SaavtkLODActor}s installed
	 * into the reference {@link ModelManager}.
	 * <P>
	 * See also {@link SaavtkLODActor#setLodFlag(boolean)}
	 */
	public void setLodFlag(boolean aFlag);

}
