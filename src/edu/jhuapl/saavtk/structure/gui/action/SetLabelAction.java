package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the labels of the list of
 * {@link Structure}s.
 * 
 * @author lopeznr1
 */
public class SetLabelAction<G1 extends Structure> extends PopAction<G1>
{
	// Ref vars
	private final StructureManager<G1> refManager;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public SetLabelAction(StructureManager<G1> aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() == 0)
			return;

		String infoMsg = "Enter structure label text. Leave blank to remove label.";
		String oldVal = aItemL.get(0).getLabel();
		String newVal = JOptionPane.showInputDialog(refParent, infoMsg, oldVal);
		if (newVal == null)
			return;

		for (G1 aItem : aItemL)
			refManager.setLabel(aItem, newVal);
	}

}
