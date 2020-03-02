package edu.jhuapl.saavtk.pick;

import java.awt.event.InputEvent;

/**
 * Listener that provides notification whenever the picker has selected an item.
 * 
 * @author lopeznr1
 */
public interface PickListener
{
	/**
	 * Method that handles the pick action. This method will be called whenever an
	 * item is picked.
	 * 
	 * @param aEvent       The input event that triggered this action
	 * @param aMode        Action mode associated with the event.
	 * @param aPrimaryTarg Corresponds to the primary target that was picked.
	 * @param aSurfaceTarg Corresponds to the shape model target that was picked.
	 */
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg);

}
