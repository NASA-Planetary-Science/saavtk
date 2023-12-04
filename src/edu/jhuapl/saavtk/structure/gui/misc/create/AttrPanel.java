package edu.jhuapl.saavtk.structure.gui.misc.create;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.structure.AnyStructurePicker;
import edu.jhuapl.saavtk.structure.StructureType;
import edu.jhuapl.saavtk.util.ColorIcon;
import glum.gui.GuiUtil;
import net.miginfocom.swing.MigLayout;

/**
 * UI component that allows the user to specify the attributes for the structure to be created.
 *
 * @author lopeznr1
 */
public class AttrPanel extends JPanel implements ActionListener, PickListener
{
	// Ref vars
	private final AnyStructurePicker refPicker;

	// Gui vars
	private final JButton mainColorB;
	private final JCheckBox showIntCB;

	/** Standard Constructor */
	public AttrPanel(AnyStructurePicker aPicker)
	{
		refPicker = aPicker;

		// Form the gui
		setLayout(new MigLayout("ins 0", "[]", "[]"));

		var mainColorL = new JLabel("Color:");
		mainColorB = new JButton("");
		mainColorB.addActionListener(this);
		add(mainColorL, "");
		add(mainColorB, "ay top,gapy 0,sgy g1,wrap");

		showIntCB = GuiUtil.createJCheckBox("Fill interior", this);
		add(showIntCB, "span");

		// Register for events of interest
		refPicker.addListener(this);

		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		var tmpSpawnAttr = refPicker.getSpawnAttr();

		var source = actionEvent.getSource();
		if (source == mainColorB)
		{
			var tmpColor = tmpSpawnAttr.color();
			tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", tmpColor);
			if (tmpColor == null)
				return;

			tmpSpawnAttr = tmpSpawnAttr.withColor(tmpColor);
		}
		else if (source == showIntCB)
			tmpSpawnAttr = tmpSpawnAttr.withIsIntShown(showIntCB.isSelected());
		else
			throw new Error("Unsupported source: " + source);

		refPicker.setSpawnAttr(tmpSpawnAttr);

		updateGui();
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		updateGui();
	}

	/**
	 * Helper method to update the colored icons/text for labelIconB and structIconB
	 */
	private void updateColoredButtons()
	{
		// These values may need to be fiddled with if there are sizing issues
		var iconW = 60;
		var iconH = (int) (showIntCB.getHeight() * 0.50);

		// Update the label / struct colors
		var spawnColor = refPicker.getSpawnAttr().color();
		var exteriorIcon = new ColorIcon(spawnColor, Color.BLACK, iconW, iconH);

		mainColorB.setIcon(exteriorIcon);
	}

	/**
	 * Helper method that keeps various UI elements synchronized.
	 */
	private void updateGui()
	{
		var spawnType = refPicker.getSpawnType();
		var isEnabled = spawnType == StructureType.Circle || spawnType == StructureType.Ellipse
				|| spawnType == StructureType.Polygon;
		showIntCB.setEnabled(isEnabled);

		updateColoredButtons();
	}

}
