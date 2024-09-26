package edu.jhuapl.saavtk.structure.gui.misc.view;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.AnyStructurePicker;
import net.miginfocom.swing.MigLayout;

/**
 * UI component that allows creation of a Structure.
 *
 * @author lopeznr1
 */
public class ViewPanel extends JPanel
{
	/** Standard Constructor */
	public ViewPanel(PolyhedralModel aSmallBody, AnyStructureManager aManager, AnyStructurePicker aPicker)
	{
		// Form the gui
		setLayout(new MigLayout("ins 0", "[]", "[]"));

		var attrPanel = new AttrPanel(aManager);
		var extendPanel = new ExtendPanel(aSmallBody, aManager);
		add(attrPanel, "grow,push");
		add(extendPanel, "ay top");
	}

}