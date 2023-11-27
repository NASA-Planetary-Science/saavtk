package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Structure;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the visibility of the list of {@link Structure}s.
 * <P>
 * This {@link PopAction} should always be paired with a {@link JCheckBoxMenuItem} rather than a plain {@link JMenuItem}
 * - otherwise a runtime exception will be thrown.
 *
 * @author lopeznr1
 */
public class ShowAction extends PopAction<Structure>
{
	// Reference vars
	private final AnyStructureManager refManager;

	// State vars
	private boolean nextIsVisible;

	/** Standard Constructor */
	public ShowAction(AnyStructureManager aManager, Component aParent)
	{
		refManager = aManager;
		nextIsVisible = false;
	}

	@Override
	public void executeAction(List<Structure> aItemL)
	{
		refManager.setIsVisible(aItemL, nextIsVisible);
	}

	@Override
	public void setChosenItems(Collection<Structure> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// If any items are not visible then set checkbox to unselected
		// in order to allow all chosen items to be toggled on
		var isSelected = true;
		for (var aItem : aItemC)
			isSelected &= aItem.getVisible() == true;
		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);

		// Change (next) visibility to be inverted of the selected state
		nextIsVisible = !isSelected;
	}

}
