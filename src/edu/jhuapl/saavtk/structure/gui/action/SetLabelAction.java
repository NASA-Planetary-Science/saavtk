package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Structure;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the labels of the list of {@link Structure}s.
 *
 * @author lopeznr1
 */
public class SetLabelAction extends PopAction<Structure>
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final Component refParent;

	/** Standard Constructor */
	public SetLabelAction(AnyStructureManager aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<Structure> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() == 0)
			return;

		var infoMsg = "Enter structure label text. Leave blank to remove label.";
		var oldVal = aItemL.get(0).getLabel();
		var newVal = JOptionPane.showInputDialog(refParent, infoMsg, oldVal);
		if (newVal == null)
			return;

		for (var aItem : aItemL)
			refManager.setLabel(aItem, newVal);
	}

}
