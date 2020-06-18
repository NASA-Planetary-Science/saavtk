package edu.jhuapl.saavtk.view;

import edu.jhuapl.saavtk.camera.Camera;
import edu.jhuapl.saavtk.view.lod.LodMode;

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
	 * Method that returns the configured {@link LodMode}.
	 */
	public LodMode getLodMode();

	/**
	 * Method that sets the configured {@link LodMode}.
	 * <P>
	 * If the mode will just be changed on a temporal basis (user did not
	 * specifically request change but due to software logic) then utilize the
	 * {@link #setLodModeTemporal} mechanism.
	 */
	public void setLodMode(LodMode aMode);

	/**
	 * Sets in the temporal {@link LodMode} to be utilized. The temporal mode should
	 * be set whenever the mode should be changed on a temporary basis without a
	 * lasting impact. This is typically used when it is ideal to update the mode
	 * for a short lived time frame due to specific action.
	 * <P>
	 * Lasting changes to the {@link LodMode} should be configured via
	 * {@link #setLodMode}.
	 * <P>
	 * To clear out the temporal mode and revert to the configured {@link LodMode}
	 * just call this method again with null passed in.
	 */
	public void setLodModeTemporal(LodMode aLodMode);

}
