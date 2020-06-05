package edu.jhuapl.saavtk.structure.gui.load;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

import com.google.common.collect.Multimap;

import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Structure;
import glum.gui.FocusUtil;
import glum.gui.GuiUtil;
import glum.gui.action.ClickAction;
import glum.gui.panel.GlassPanel;
import glum.gui.panel.generic.GenericCodes;
import net.miginfocom.swing.MigLayout;

/**
 * Load panel that provides a custom UI for loading up structues.
 * <P>
 * This panel provides the following features:
 * <UL>
 * <LI>Textual listing of the files and associated number of (ambiguous) items.
 * <LI>Prompt for how to load ambiguous structures (either as circles or points)
 * into the system.
 * </UL>
 *
 * @author lopeznr1
 */
public class AmbiguousRoundStructurePanel extends GlassPanel implements ActionListener, GenericCodes
{
	// State vars
	private Mode pickMode;

	// GUI vars
	private JLabel titleL;
	private JTextArea infoTA;
	private JButton cancelB, circleB, pointB;

	/**
	 * Standard Constructor
	 */
	public AmbiguousRoundStructurePanel(Component aParent, int aSizeX, int aSizeY)
	{
		super(aParent);

		pickMode = null;

		setLayout(new MigLayout("", "[right][grow][][]", "[][grow][]"));
		setBorder(new BevelBorder(BevelBorder.RAISED));

		// Form the GUI
		titleL = new JLabel("Ambiguous Structures", JLabel.CENTER);
		add(titleL, "growx,span,wrap");

		infoTA = new JTextArea("No status", 3, 0);
		infoTA.setEditable(false);
		infoTA.setTabSize(3);
		infoTA.setLineWrap(true);
		infoTA.setWrapStyleWord(true);
		add(new JScrollPane(infoTA), "growx,growy,span,wrap");

		cancelB = GuiUtil.formButton(this, "Cancel");
		circleB = GuiUtil.formButton(this, "Circles");
		pointB = GuiUtil.formButton(this, "Points");
		add(circleB, "align right,span,split");
		add(pointB, "");
		add(cancelB, "");

		setSize(aSizeX, aSizeY);

		// Set up keyboard short cuts
		FocusUtil.addAncestorKeyBinding(this, "ESCAPE", new ClickAction(cancelB));
	}

	/**
	 * Returns the {@link Mode} the user selected. Returns null if the user pressed
	 * cancel.
	 */
	public Mode getSelection()
	{
		return pickMode;
	}

	/**
	 * Sets in a mapping of ambiguous structures (to sources)
	 */
	public void setAmbigousMap(Multimap<Object, Structure> aAmbigousMM)
	{
		String tmpMsg = "Some files contain structures that are ambiguously circles or points.";
		tmpMsg += " Please specify how you would like to load the ambigous files.\n\n";
		tmpMsg += "Files: " + aAmbigousMM.keySet().size() + "\n";
		for (Object aObject : aAmbigousMM.keySet())
			tmpMsg += "\t" + aAmbigousMM.get(aObject).size() + " items: " + aObject + "\n";

		infoTA.setText(tmpMsg);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == cancelB)
		{
			pickMode = null;
			setVisible(false);
			notifyListeners(this, ID_CANCEL, "Cancel");
		}
		else if (source == circleB)
		{
			pickMode = Mode.CIRCLE_MODE;
			setVisible(false);
			notifyListeners(this, ID_ACCEPT, "Accept");
		}
		else
		{
			pickMode = Mode.POINT_MODE;
			setVisible(false);
			notifyListeners(this, ID_ACCEPT, "Accept");
		}
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		// Reset the panel
		if (isVisible == true)
			pickMode = null;

		super.setVisible(isVisible);
	}

}
