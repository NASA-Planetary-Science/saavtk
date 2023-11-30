package edu.jhuapl.saavtk.structure.gui;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.AnyStructurePicker;
import edu.jhuapl.saavtk.structure.AnyStructurePicker.Mode;
import edu.jhuapl.saavtk.structure.Structure;
import glum.gui.action.PopupMenu;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import net.miginfocom.swing.MigLayout;

/**
 * Top level panel used to display the main structure panel.
 *
 * @author lopeznr1
 */
public class StructureMainPanel extends JPanel implements ItemEventListener, PickListener, PickManagerListener
{
	// Ref vars
	private final PickManager refPickManager;
	private final AnyStructureManager refManager;

	// Gui vars
	private final AnyStructurePicker mainPicker;
	private final PopupMenu<?> popupMenu;

	/** Standard Constructor */
	public StructureMainPanel(Renderer aRenderer, PolyhedralModel aSmallBody, StatusNotifier aStatusNotifier,
			PickManager aPickManager, AnyStructureManager aStructureManager)
	{
		refPickManager = aPickManager;
		refManager = aStructureManager;

		mainPicker = new AnyStructurePicker(aRenderer, aSmallBody, aPickManager, refManager);
		popupMenu = StructureGuiUtil.formPopupMenu(refManager, aRenderer, aSmallBody, this);

		// Form the gui
		setLayout(new MigLayout("", "[]", "[]"));

		var listPanel = new StructureListPanel(refManager, aSmallBody, aStatusNotifier, popupMenu);
		add(listPanel, "growx,growy,pushx,pushy,wrap");

		var actionPanel = new StructureActionPanel(refManager, aRenderer, aSmallBody, aStatusNotifier, aPickManager,
				mainPicker);
		add(actionPanel, "growx");

		// Manually register the StructureManager for events of interest
		aPickManager.getDefaultPicker().addListener(refManager);
		aPickManager.getDefaultPicker().addPropProvider(refManager);
		aRenderer.addVtkPropProvider(refManager);

		// Register for events of interest
		PickUtil.autoDeactivatePickerWhenComponentHidden(refPickManager, mainPicker, this);
		refManager.addListener(this);
		refPickManager.addListener(this);
		refPickManager.getDefaultPicker().addListener(this);

	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		if (aEventType == ItemEventType.ItemsSelected)
		{
			// Update the hook item if any:
			// - current hookItem is not selected
			// - just one item selected
			var hookItem = mainPicker.getHookItem();
			var pickItemS = refManager.getSelectedItems();
			if (pickItemS.contains(hookItem) == false || pickItemS.size() == 1)
				updateHookedItem();
		}
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if our StructureManager is not associated with the primary PickTarget
		if (refManager.isAssociatedPickTarget(aPrimaryTarg) == false)
			return;

		// Bail if not a valid popup action
		if (PickUtil.isPopupTrigger(aEvent) == false || aMode != PickMode.ActiveSec)
			return;

		// Bail if the picker is in the mist of editing an item
		if (mainPicker.getStepNumber() > 1)
			return;

		// Show the popup (if appropriate)
		var tmpComp = aEvent.getComponent();
		var posX = ((MouseEvent) aEvent).getX();
		var posY = ((MouseEvent) aEvent).getY();
		popupMenu.show(tmpComp, posX, posY);
	}

	@Override
	public void pickerChanged()
	{
		// Update the activated structure
		updateHookedItem();

		// Allow the mainManager to handle pick actions if the mainPicker is not active
		if (refPickManager.getActivePicker() != mainPicker)
		{
			refManager.setAllowPickAction(true);
			mainPicker.setWorkMode(Mode.None);
		}
	}

	/**
	 * Helper method that updates the mainPicker with the hooked item.
	 */
	private void updateHookedItem()
	{
		// Only 1 structure must be selected for it to be "hooked"
		var hookItem = (Structure) null;

		var pickItemS = refManager.getSelectedItems();
		if (pickItemS.size() == 1)
			hookItem = pickItemS.asList().get(0);

		mainPicker.setHookItem(hookItem);
	}

}
