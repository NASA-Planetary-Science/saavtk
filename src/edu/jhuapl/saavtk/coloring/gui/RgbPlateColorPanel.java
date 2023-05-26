package edu.jhuapl.saavtk.coloring.gui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import glum.gui.GuiPaneUtil;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure the plate coloring for the reference
 * small body {@link PolyhedralModel} with a RGB plate coloring configuration.
 *
 * @author lopeznr1
 */
public class RgbPlateColorPanel extends GPanel implements ActionListener, EditColoringModeGui
{
	// Ref vars
	private final List<PolyhedralModel> refSmallBodies;

	// State vars
	private boolean isActive;

	// Gui vars
	private final JComboBox<String> redBox;
	private final JComboBox<String> greenBox;
	private final JComboBox<String> blueBox;

	/** Standard Constructor */
	public RgbPlateColorPanel(List<PolyhedralModel> aSmallBodies, JComboBox<String> aRedBox, JComboBox<String> aGreenBox,
			JComboBox<String> aBlueBox)
	{
		refSmallBodies = aSmallBodies;

		isActive = false;

		setLayout(new MigLayout("", "", ""));

		var redL = new JLabel("Red:");
		redBox = aRedBox;
		redBox.addActionListener(this);
		add(redL, "ax right");
		add(redBox, "growx,w 0:0:,wrap");

		var greenL = new JLabel("Green:");
		greenBox = aGreenBox;
		greenBox.addActionListener(this);
		add(greenL, "ax right");
		add(greenBox, "growx,pushx,w 0:0:,wrap");

		var blueL = new JLabel("Blue:");
		blueBox = aBlueBox;
		blueBox.addActionListener(this);
		add(blueL, "ax right");
		add(blueBox, "growx,w 0:0:,wrap");
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		// Bail if not the active panel
		if (isActive == false)
			return;

		activate(aEvent.getSource());
	}

	@Override
	public void activate(Object aSource)
	{
		isActive = true;

		// Subtract 1 to leave room for the blank string (no selection) at the top.
		int redIndex = redBox.getSelectedIndex() - 1;
		int greenIndex = greenBox.getSelectedIndex() - 1;
		int blueIndex = blueBox.getSelectedIndex() - 1;

		for (PolyhedralModel refSmallBody : refSmallBodies)
		{
			try
			{
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (redIndex < 0 && greenIndex < 0 && blueIndex < 0)
					refSmallBody.setColoringIndex(-1);
				else
					refSmallBody.setFalseColoring(redIndex, greenIndex, blueIndex);
			}
			catch (IOException aExp)
			{
				GuiPaneUtil.showFailMessage(this, "Plate Coloring Error", "Failure while changing plate coloring...", aExp);
				aExp.printStackTrace();
	
				if (aSource instanceof JComboBox<?> aBox)
					aBox.setSelectedIndex(0);
			}
			finally
			{
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	@Override
	public void deactivate()
	{
		isActive = false;
	}

}
