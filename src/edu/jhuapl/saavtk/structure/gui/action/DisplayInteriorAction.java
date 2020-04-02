package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the display interior state of the list of
 * {@link Structure}s.
 * <P>
 * This {@link PopAction} should always be paired with a
 * {@link JCheckBoxMenuItem} rather than a plain {@link JMenuItem} - otherwise a
 * runtime exception will be thrown.
 * 
 * @author lopeznr1
 */
public class DisplayInteriorAction<G1 extends Structure> extends PopAction<G1>
{
	// Reference vars
	private final StructureManager<G1> refManager;

	// State vars
	private boolean nextIsShown;

	/**
	 * Standard Constructor
	 */
	public DisplayInteriorAction(StructureManager<G1> aManager, Component aParent)
	{
		refManager = aManager;
		nextIsShown = false;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		PolygonModel tmpManager = (PolygonModel) refManager;
		Set<Polygon> pickS = tmpManager.getSelectedItems();
		tmpManager.setShowInterior(pickS, nextIsShown);
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// If any of the selected structures are not displaying interior then show
		// the display interior menu item as unchecked. Otherwise show it checked.
		boolean isSelected = true;
		for (G1 aItem : aItemC)
		{
			Polygon tmpItem = (Polygon) aItem;
			isSelected &= ((PolygonModel) refManager).getShowInterior(tmpItem) == true;
		}

		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);

		// Change (next) shown state to be inverted of the current selected state
		nextIsShown = !isSelected;
	}

}
