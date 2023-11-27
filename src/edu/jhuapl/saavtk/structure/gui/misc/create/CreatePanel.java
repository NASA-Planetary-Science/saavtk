package edu.jhuapl.saavtk.structure.gui.misc.create;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.structure.AnyStructurePicker;
import edu.jhuapl.saavtk.structure.Structure;
import net.miginfocom.swing.MigLayout;

/**
 * UI component that allows creation of a {@link Structure}.
 *
 * @author lopeznr1
 */
public class CreatePanel extends JPanel
{
	/** Standard Constructor */
	public CreatePanel(AnyStructurePicker aPicker)
	{
		// Form the gui
		setLayout(new MigLayout("ins 0", "[]", "[]"));

		var typePanel = new TypePanel(aPicker);
		var attrPanel = new AttrPanel(aPicker);
		add(typePanel, "grow,push");
		add(attrPanel, "ay top");
	}

}
