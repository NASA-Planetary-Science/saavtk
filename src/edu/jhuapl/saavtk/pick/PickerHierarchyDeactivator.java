package edu.jhuapl.saavtk.pick;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JComponent;

/**
 * Object that will handle the Picker deactivation whenever the component
 * hierarchy changes.
 */
public class PickerHierarchyDeactivator implements HierarchyListener
{
	// Ref vars
	private final PickManager refPickManager;
	private final Picker refPicker;
	private final JComponent refComponent;

	public PickerHierarchyDeactivator(PickManager aPickManager, Picker aPicker, JComponent aComponent)
	{
		refPickManager = aPickManager;
		refPicker = aPicker;
		refComponent = aComponent;
	}

	@Override
	public void hierarchyChanged(HierarchyEvent aEvent)
	{
		// Ignore the event if our refPicker is not the active Picker
		if (refPickManager.getActivePicker() != refPicker)
			return;

		// Ignore the event if we are still showing
		if (refComponent.isShowing() == true)
			return;

		// Deactivate our Picker
		refPickManager.setActivePicker(null);
	}

}
