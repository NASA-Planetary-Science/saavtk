package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.List;

import glum.gui.action.PopAction;
import glum.item.ItemManager;

/**
 * {@link PopAction} that will remove the list of items from the
 * {@link ItemManager}.
 * 
 * @author lopeznr1
 */
public class DeleteAction<G1> extends PopAction<G1>
{
	// Ref vars
	private final ItemManager<G1> refManager;

	/**
	 * Standard Constructor
	 */
	public DeleteAction(ItemManager<G1> aManager, Component aParent)
	{
		refManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		refManager.removeItems(aItemL);
	}

}
