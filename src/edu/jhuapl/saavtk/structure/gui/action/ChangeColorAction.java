package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.List;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Structure;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the color for the list of {@link Structure}s.
 *
 * @author lopeznr1
 */
public class ChangeColorAction extends PopAction<Structure>
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final Component refParent;

	/** Standard Constructor */
	public ChangeColorAction(AnyStructureManager aManager, Component aParent)
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

		// Use the color of the first item as the default to show
		var color = ColorChooser.showColorChooser(refParent, aItemL.get(0).getColor());
		if (color == null)
			return;

		refManager.setColor(aItemL, color);
	}

}
