package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.List;

import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.gui.StructureGuiUtil;
import glum.gui.action.PopAction;
import glum.item.ItemManager;

/**
 * {@link PopAction} that will remove the list of items from the {@link ItemManager}.
 *
 * @author lopeznr1
 */
public class DeleteAction extends PopAction<Structure>
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final Component refParent;

	/** Standard Constructor */
	public DeleteAction(AnyStructureManager aManager, Component aParent)
	{
		refManager = aManager;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<Structure> aItemL)
	{
		// Delegate
		StructureGuiUtil.promptAndDelete(refParent, refManager, aItemL);
	}

}
