package edu.jhuapl.saavtk.structure.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.google.common.collect.Range;

import glum.gui.FocusUtil;
import glum.gui.GuiUtil;
import glum.gui.action.ClickAction;
import glum.gui.component.GNumberField;
import glum.gui.panel.GlassPanel;
import net.miginfocom.swing.MigLayout;

/**
 * GUI component used to prompt the user for a latitude and longitude inputs.
 * <P>
 * Inputs is specified in degrees.
 *
 * @author lopeznr1
 */
public class LatLonPanel extends GlassPanel implements ActionListener
{
	// Constants
	private static final Range<Double> LatRange = Range.closed(-90.0, 90.0);
	private static final Range<Double> LonRange = Range.closed(-180.0, 360.0);

	// Gui vars
	private final JLabel titleL;
	private final GNumberField latNF;
	private final GNumberField lonNF;
	private final JButton acceptB;
	private final JButton cancelB;

	// State vars
	private boolean isAccepted;

	/**
	 * Standard Constructor
	 */
	public LatLonPanel(Component aParent, String aTitle, int aSizeX, int aSizeY)
	{
		super(aParent);

		setLayout(new MigLayout("", "[right][grow][]", "[]"));
		setSize(aSizeX, aSizeY);

		// Title Area
		titleL = new JLabel(aTitle, JLabel.CENTER);
		add(titleL, "growx,span,wrap");

		// Latitude
		latNF = new GNumberField(this, new DecimalFormat("#.###"), LatRange);
		add(new JLabel("Latitude"), "");
		add(latNF, "growx");
		add(new JLabel("deg"), "wrap");

		// Longitude
		lonNF = new GNumberField(this, new DecimalFormat("#.###"), LonRange);
		add(new JLabel("Longitude"), "");
		add(lonNF, "growx");
		add(new JLabel("deg"), "wrap");

		// Action area
		acceptB = GuiUtil.createJButton("Accept", this);
		cancelB = GuiUtil.createJButton("Cancel", this);
		add(cancelB, "span,split,align right");
		add(acceptB);

		// Set up keyboard short cuts
		FocusUtil.addAncestorKeyBinding(this, "ESCAPE", new ClickAction(cancelB));
		FocusUtil.addAncestorKeyBinding(this, "ENTER", new ClickAction(acceptB));
	}

	/**
	 * Simplified Constructor
	 */
	public LatLonPanel(Component aParent, String aTitle)
	{
		this(aParent, aTitle, 250, 75);
	}

	/**
	 * Returns the input latitude.
	 */
	public double getLat()
	{
		return latNF.getValue();
	}

	/**
	 * Returns the input longitude.
	 */
	public double getLon()
	{
		return lonNF.getValue();
	}

	/**
	 * Returns true if the prompt was accepted
	 */
	public boolean isAccepted()
	{
		return isAccepted;
	}

	/**
	 * Sets the title of this panel
	 */
	public void setTitle(String aTitle)
	{
		titleL.setText(aTitle);
	}

	/**
	 * Sets the title of this panel
	 */
	public void setLatLon(double aLat, double aLon)
	{
		latNF.setValue(aLat);
		lonNF.setValue(aLon);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == cancelB)
		{
			isAccepted = false;
			setVisible(false);
			notifyListeners(this, ID_CANCEL, "Cancel");
		}
		else if (source == acceptB)
		{
			isAccepted = true;
			setVisible(false);
			notifyListeners(this, ID_ACCEPT, "Accept");
		}

		updateGui();
	}

	/**
	 * Updates the various UI elements to keep them synchronized
	 */
	private void updateGui()
	{
		boolean isValid = true;
		isValid &= latNF.isValidInput();
		isValid &= lonNF.isValidInput();

		acceptB.setEnabled(isValid);
	}

}
