package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.ClosedShape;
import edu.jhuapl.saavtk.structure.Structure;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the display interior state of the list of {@link Structure}s.
 * <p>
 * This {@link PopAction} should always be paired with a {@link JCheckBoxMenuItem} rather than a plain {@link JMenuItem}
 * - otherwise a runtime exception will be thrown.
 *
 * @author lopeznr1
 */
public class DisplayInteriorAction extends PopAction<Structure>
{
	// Reference vars
	private final AnyStructureManager refManager;

	// State vars
	private boolean nextIsShown;

	/** Standard Constructor */
	public DisplayInteriorAction(AnyStructureManager aManager, Component aParent)
	{
		refManager = aManager;
		nextIsShown = false;
	}

	@Override
	public void executeAction(List<Structure> aItemL)
	{
		var pickS = refManager.getSelectedItems();
		refManager.setShowInterior(pickS, nextIsShown);
	}

	@Override
	public void setChosenItems(Collection<Structure> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable if there are at least 1 ClosedShape
		var isEnabled = aItemC.stream().anyMatch(aItem -> aItem instanceof ClosedShape);
		aAssocMI.setEnabled(isEnabled);

		var isSelected = false;
		if (isEnabled == true)
		{
			// If any of the selected structures are not displaying interior then show
			// the display interior menu item as unchecked. Otherwise show it checked.
			isSelected = true;
			for (var aItem : aItemC)
			{
				if (aItem instanceof ClosedShape aClosedShape)
					isSelected &= aClosedShape.getShowInterior() == true;
			}
		}

		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);

		// Change (next) shown state to be inverted of the current selected state
		nextIsShown = !isSelected;
	}

}
