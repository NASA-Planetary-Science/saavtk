package edu.jhuapl.saavtk.structure.gui.misc.create;

import java.awt.event.InputEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.AnyStructurePicker;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureType;
import glum.gui.GuiUtil;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import net.miginfocom.swing.MigLayout;

/**
 * UI component that allows creation of a {@link Structure}.
 *
 * @author lopeznr1
 */
public class EditPanel extends JPanel implements ItemEventListener, PickListener
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final AnyStructurePicker refPicker;

	// Gui vars
	private final JTextArea infoTA;

	/** Standard Constructor */
	public EditPanel(AnyStructureManager aManager, AnyStructurePicker aPicker)
	{
		refManager = aManager;
		refPicker = aPicker;

		// Form the gui
		setLayout(new MigLayout("ins 0", "[]", "[]"));

		// Info area
		infoTA = GuiUtil.createUneditableTextArea(6, 0);
		infoTA.setTabSize(3);
		infoTA.setLineWrap(false);
		var tmpPane = new JScrollPane(infoTA);
		add(tmpPane, "growx,growy,h pref::,w 50::,pushx,pushy,span");

		// Register for events of interest
		refManager.addListener(this);
		refPicker.addListener(this);

		// Initial update
		updateGui();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
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
		var fullItemL = refManager.getAllItems();
		var pickItemS = refManager.getSelectedItems();

		// Type if only 1 type of Structure is selected
		var uniType = (StructureType) null;
		if (pickItemS.size() > 0)
			uniType = pickItemS.iterator().next().getType();

		// Type if at least 1 round Structure (Circle, Ellipse, Point) is selected
		var rndType = (StructureType) null;

		// Calculate uniType and rndType
		for (var aItem : pickItemS)
		{
			var evalType = aItem.getType();
			if (evalType != uniType)
				uniType = null;

			var isBetterType = false;
			isBetterType |= evalType == StructureType.Ellipse;
			isBetterType |= evalType == StructureType.Circle && rndType != StructureType.Ellipse;
			isBetterType |= evalType == StructureType.Point && rndType == null;
			if (isBetterType == true)
				rndType = evalType;
		}

		var infoMsg = "Please select a structure.";
		if (fullItemL.size() == 0)
		{
			infoMsg = "There are no structures. Please create or load structures.";
		}
		else if (pickItemS.size() == 0)
		{
			infoMsg = "Please select a structure.";
		}
		else if (rndType != null)
		{
			var typeStr = rndType.getLabel();
			infoMsg = "Edit Actions: " + typeStr + "\n";
			infoMsg += "\tdrag: Move selected items\n";
			infoMsg += '\n';
			if (rndType == StructureType.Circle)
			{
				infoMsg += "\t'ctrl' + drag: Change radius\n";
			}
			else if (rndType == StructureType.Ellipse)
			{
				infoMsg += "\t'ctrl' + drag: Change radius\n";
				infoMsg += "\t'/' or 'z' + drag: Change flattening\n";
				infoMsg += "\t'x' or '.' + drag: Change angle";
			}
		}
		else if (pickItemS.size() == 1 && (uniType == StructureType.Path || uniType == StructureType.Polygon))
		{
			var typeStr = uniType.getLabel();
			infoMsg = "Edit Actions: " + typeStr + "\n";
			if (refPicker.getStepNumber() > 1)
			{
				infoMsg += "\tDrag on a control point to change it's position.\n";
				infoMsg += "\tClick on the small body to place new control point.\n";
				infoMsg += "\n";

				infoMsg += "\tRight-Click to exit control point editing.";
			}
			else
			{
				infoMsg += "\tThere is no support to move an entire: " + typeStr + ".\n";
				infoMsg += "\n";
				infoMsg += "\tClick on the " + typeStr + " to edit control points.";
			}

		}
		else if (pickItemS.size() > 1 && (uniType == StructureType.Path || uniType == StructureType.Polygon))
		{
			var typeStr = uniType.getLabel();
			infoMsg = "Edit Actions: " + typeStr + "\n";
			infoMsg += "\tNo edit support when multiple " + typeStr + "s are selected.";
		}
		else if (pickItemS.size() > 0)
		{
			infoMsg = "Edit Actions: multiple types\n";
			infoMsg += "\tNo edit support when multiple hard edge structures are selected.";
		}
		infoMsg = infoMsg.strip();

		infoTA.setText(infoMsg);
	}

}
