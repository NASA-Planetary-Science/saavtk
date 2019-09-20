package edu.jhuapl.saavtk.gui.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import edu.jhuapl.saavtk.popup.PopupMenu;
import glum.item.ItemManager;

/**
 * Object that provides the logic used to determine when a {@link PopupMenu}
 * should be displayed.
 * <P>
 * In order for a popup menu to be shown at least one item must be selected in
 * provided refManager. When a popup menu is triggered the current selection
 * will not be changed.
 * 
 * @author lopeznr1
 */
public class TablePopupHandler extends MouseAdapter
{
	// Ref vars
	private final ItemManager<?> refManager;
	private final PopupMenu refPopupMenu;

	/**
	 * Standard Constructor
	 */
	public TablePopupHandler(ItemManager<?> aManager, PopupMenu aPopupMenu)
	{
		refManager = aManager;
		refPopupMenu = aPopupMenu;
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		maybeShowPopup(aEvent);
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		maybeShowPopup(aEvent);
	}

	/**
	 * Helper method to handle the showing of the table popup menu.
	 */
	private void maybeShowPopup(MouseEvent aEvent)
	{
		// Bail if this is not a valid popup action
		if (aEvent.isPopupTrigger() == false)
			return;

		// Bail if no items are selected
		if (refManager.getSelectedItems().isEmpty() == true)
			return;

		refPopupMenu.showPopup(aEvent, null, 0, null);
	}

}
