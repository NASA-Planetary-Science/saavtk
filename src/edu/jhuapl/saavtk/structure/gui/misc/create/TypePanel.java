package edu.jhuapl.saavtk.structure.gui.misc.create;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.structure.AnyStructurePicker;
import edu.jhuapl.saavtk.structure.StructureType;
import edu.jhuapl.saavtk.structure.gui.misc.SpawnInfo;
import glum.gui.GuiUtil;
import glum.gui.component.GComboBox;
import net.miginfocom.swing.MigLayout;

/**
 * UI component that allows the user to specify the type of structure to create.
 *
 * @author lopeznr1
 */
public class TypePanel extends JPanel implements ActionListener, PickListener
{
	// Ref vars
	private final AnyStructurePicker refPicker;

	// Gui vars
	private final GComboBox<StructureType> typeBox;
	private final JTextArea infoTA;

	/** Standard Constructor */
	public TypePanel(AnyStructurePicker aPicker)
	{
		refPicker = aPicker;

		// Form the gui
		setLayout(new MigLayout("ins 0", "[]", "[]"));

		var typeL = new JLabel("Type:");
		typeBox = new GComboBox<>(this, StructureType.values());
		add(typeL, "");
		add(typeBox, "pushx,wrap");

		// Info area
		infoTA = GuiUtil.createUneditableTextArea(6, 0);
		infoTA.setTabSize(3);
		infoTA.setLineWrap(false);
		var tmpPane = new JScrollPane(infoTA);
		add(tmpPane, "growx,growy,h pref::,w 50::,pushy,span");

		// Register for events of interest
		refPicker.addListener(this);

		// Initial update
		refPicker.setSpawnType(typeBox.getChosenItem());

		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		var source = aEvent.getSource();
		if (source == typeBox)
			refPicker.setSpawnType(typeBox.getChosenItem());

		updateGui();
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		updateGui();
	}

	/**
	 * Helper method that keeps the gui synchronized with user input.
	 */
	private void updateGui()
	{
		// The pickType is from typeBox unless, refPicker is in the midst of an edit
		var pickType = typeBox.getChosenItem();
		if (refPicker.getStepNumber() > 1 && refPicker.getHookItem() != null)
			pickType = refPicker.getHookItem().getType();

		var stepIdx = refPicker.getStepNumber();

		var spawnInfo = SpawnInfo.of(pickType);
		var numStepsStr = "" + spawnInfo.minSteps();
		if (spawnInfo.hasMaxSteps() == false)
			numStepsStr += "+";

		var infoMsg = "Number of steps: " + numStepsStr + ". You are at step: " + stepIdx + "\n";
		if (pickType == StructureType.Point)
		{
			infoMsg += "\tSpecify the point's location.\n";
		}
		else if (pickType == StructureType.Circle)
		{
			infoMsg += "\tPlace 1st boundary point\n";
			infoMsg += "\tPlace 2nd boundary point\n";
			infoMsg += "\tPlace 3rd and final boundary point\n";
			infoMsg += "\n";
			if (stepIdx > 1)
				infoMsg += "\tRight-Click to cancel the creation";
		}
		else if (pickType == StructureType.Ellipse)
		{
			infoMsg += "\tPlace 1st major axis endpoint\n";
			infoMsg += "\tPlace 2nd major axis endpoint\n";
			infoMsg += "\tPlace the boundary minor axis endpoint\n";
			infoMsg += "\n";
			if (stepIdx > 1)
				infoMsg += "\tRight-Click to cancel the creation";
		}
		else if (pickType == StructureType.Path)
		{
			infoMsg += "\tPlace the 1st endpoint\n";
			infoMsg += "\tPlace the nth endpoint\n";
			infoMsg += "\n";
			if (stepIdx >= 3)
				infoMsg += "\tRight-Click to complete the path";
			else if (stepIdx > 1)
				infoMsg += "\tRight-Click to cancel the creation";
		}
		else if (pickType == StructureType.Polygon)
		{
			infoMsg += "\tPlace the 1st endpoint\n";
			infoMsg += "\tPlace the 2nd endpoint\n";
			infoMsg += "\tPlace the nth endpoint\n";
			infoMsg += "\n";
			if (stepIdx >= 4)
				infoMsg += "\tRight-Click to complete the polygon";
			else if (stepIdx > 1)
				infoMsg += "\tRight-Click to cancel the creation";

		}
		infoMsg = infoMsg.strip();

		infoTA.setText(infoMsg);
	}

}
