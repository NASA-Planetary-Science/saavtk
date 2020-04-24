package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the color for the list of {@link Structure}s.
 * 
 * @author lopeznr1
 */
public class ChangeColorAction<G1 extends Structure> extends PopAction<G1>
{
	// Ref vars
	private final StructureManager<G1> refManager;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public ChangeColorAction(StructureManager<G1> aManager, Component aParent)
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

		// Use the color of the first item as the default to show
		Color color = ColorChooser.showColorChooser(refParent, aItemL.get(0).getColor());
		if (color == null)
			return;

		for (G1 aItem : aItemL)
			refManager.setColor(aItem, color);
	}

}
