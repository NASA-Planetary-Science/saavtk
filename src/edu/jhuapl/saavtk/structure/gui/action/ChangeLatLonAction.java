package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.ChangeLatLonDialog;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the lat,lon of a single {@link Structure}.
 * 
 * @author lopeznr1
 */
public class ChangeLatLonAction<G1 extends Structure> extends PopAction<G1>
{
	// Ref vars
	private final StructureManager<G1> refManager;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public ChangeLatLonAction(StructureManager<G1> aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if a single item is not selected
		if (aItemL.size() != 1)
			return;

		ChangeLatLonDialog<?> dialog = new ChangeLatLonDialog<>(refManager, aItemL.get(0));
		dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(refParent));
		dialog.setVisible(true);
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable the item if the number of selected items == 1
		boolean isEnabled = aItemC.size() == 1;
		aAssocMI.setEnabled(isEnabled);
	}

}
