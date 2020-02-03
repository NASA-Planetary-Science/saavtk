package edu.jhuapl.saavtk.pick;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.Configuration;
import glum.item.ItemManager;

/**
 * Collection of utility items useful for handling hooked items.
 * <P>
 * This utility class provides a single location for defining the behavior for
 * "hook" actions. A "hook" action is one where the (application) user has
 * expressed interest in a specific item by moving the mouse to the item.
 *
 * @author lopeznr1
 */
public class HookUtil
{
	/**
	 * Returns true if the specified MouseEvent has the "primary" modifier key
	 * activated.
	 * <P>
	 * For Linux and Windows systems the primary modifier key is defined as control
	 * key (Ctrl) button.
	 * <P>
	 * For Apple systems the primary modifier key is defined as the meta key
	 * (Command) button.
	 */
	public static boolean isModifyKey(InputEvent aEvent)
	{
		if (Configuration.isMac() == true && aEvent.isMetaDown() == true)
			return true;
		else if (Configuration.isMac() == false && aEvent.isControlDown() == true)
			return true;

		return false;
	}

	/**
	 * Utility method that will update the selection in a uniform manner.
	 * <P>
	 * The actual selection mechanism will have the following behavior on the
	 * provided hook item:
	 * <UL>
	 * <LI>If no modifier key, then the selection will be set to aHookItem,
	 * otherwise...
	 * <LI>If aHookItem is not included, then the current selection will be updated
	 * to include the item.
	 * <LI>If aHookItem is included in the current selection then it will be
	 * removed.
	 * </UL>
	 *
	 * @param aManager  The ItemManager of interest
	 * @param aEvent    The associated {@link MouseEvent}
	 * @param aHookItem The item that was hooked (by the MouseEvent)
	 */
	public static <G1> void updateSelection(ItemManager<G1> aManager, InputEvent aEvent, G1 aHookItem)
	{
		// Determine if this is a modified action
		boolean isModifyKey = isModifyKey(aEvent);

		// Determine the items that will be marked as selected
		List<G1> tmpL = new ArrayList<>(aManager.getSelectedItems());
		if (isModifyKey == false)
			tmpL = ImmutableList.of(aHookItem);
		else if (aManager.getSelectedItems().contains(aHookItem) == false)
			tmpL.add(aHookItem);
		else
			tmpL.remove(aHookItem);

		// Update the selected items
		aManager.setSelectedItems(tmpL);
	}

}
