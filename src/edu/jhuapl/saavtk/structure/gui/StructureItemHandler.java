package edu.jhuapl.saavtk.structure.gui;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * ItemHandler used to process {@link Structure}
 * 
 * @author lopeznr1
 */
public class StructureItemHandler<G1 extends Structure> extends BasicItemHandler<G1, LookUp>
{
	// Ref vars
	private final StructureManager<G1> refManager;

	/**
	 * Standard Constructor
	 */
	public StructureItemHandler(StructureManager<G1> aManager, QueryComposer<LookUp> aComposer)
	{
		super(aComposer);

		refManager = aManager;
	}

	@Override
	public Object getColumnValue(G1 aItem, LookUp aEnum)
	{
		switch (aEnum)
		{
			case IsVisible:
				return aItem.getVisible();
			case Id:
				return aItem.getId();
			case Type:
				return aItem.getType();
			case Name:
				return aItem.getName();
			case Details:
				return aItem.getInfo();
			case Color:
				return aItem.getColor();
			case Label:
				return aItem.getLabel();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(G1 aItem, LookUp aEnum, Object aValue)
	{
		List<G1> itemL = ImmutableList.of(aItem);
		if (aEnum == LookUp.IsVisible)
			refManager.setStructureVisible(itemL, (boolean) aValue);
		else if (aEnum == LookUp.Color)
			refManager.setStructureColor(itemL, (Color) aValue);
		else if (aEnum == LookUp.Name)
			aItem.setName((String) aValue);
		else if (aEnum == LookUp.Label)
			refManager.setStructureLabel(aItem, (String) aValue);
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

}
