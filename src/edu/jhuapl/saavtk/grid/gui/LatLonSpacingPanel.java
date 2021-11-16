package edu.jhuapl.saavtk.grid.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.grid.LatLonSpacing;
import glum.gui.GuiUtil;
import glum.gui.component.GNumberFieldSlider;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows a user to configure a {@link LatLonSpacing}.
 *
 * @author lopeznr1
 */
public class LatLonSpacingPanel extends GPanel implements ActionListener
{
	// Constants
	private static final Range<Double> LatSpacingRange = Range.closed(1.0, 90.0);
	private static final Range<Double> LonSpacingRange = Range.closed(1.0, 180.0);

	// State vars
	private LatLonSpacing currLatLonSpacing;

	// Gui vars
	private final GNumberFieldSlider latSpacingNFS;
	private final GNumberFieldSlider lonSpacingNFS;
	private final JCheckBox isSyncCB;
	private final JButton applyB;

	/** Standard Constructor */
	public LatLonSpacingPanel()
	{
		currLatLonSpacing = LatLonSpacing.Invalid;

		// Form the gui
		setLayout(new MigLayout("", "[]", "[]"));

		var latSpacingL = new JLabel("Lat Spacing:");
		latSpacingNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), LatSpacingRange);
		latSpacingNFS.setIntegralSteps();
		add(latSpacingL, "span,split");
		add(latSpacingNFS, "growx,pushx,span,wrap");

		var lonSpacingL = new JLabel("Lon Spacing:");
		lonSpacingNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), LonSpacingRange);
		lonSpacingNFS.setIntegralSteps();
		add(lonSpacingL, "span,split");
		add(lonSpacingNFS, "growx,pushx,span,wrap");

		isSyncCB = GuiUtil.createJCheckBox("Synchronized", this);
		applyB = GuiUtil.createJButton("Apply", this);
		add(isSyncCB, "growx,span,split");
		add(applyB, "ax right");

		// Initial update
		updateGui();
	}

	/**
	 * Returns the {@link LatLonSpacing} as configured in the GUI.
	 */
	public LatLonSpacing getLatLonSpacing()
	{
		var latSpacing = latSpacingNFS.getValue();
		var lonSpacing = lonSpacingNFS.getValue();
		return new LatLonSpacing(latSpacing, lonSpacing);
	}

	/**
	 * Configures the GUI to reflect the specified {@link LatLonSpacing}.
	 */
	public void setLatLonSpacing(LatLonSpacing aLatLonSpacing)
	{
		latSpacingNFS.setValue(aLatLonSpacing.latSpacing());
		lonSpacingNFS.setValue(aLatLonSpacing.lonSpacing());

		currLatLonSpacing = aLatLonSpacing;
		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		var source = aEvent.getSource();
		if (source == applyB || isSyncCB.isSelected() == true)
			applyAction();

		updateGui();
	}

	/**
	 * Helper method to apply the action (if appropriate).
	 */
	private void applyAction()
	{
		// Bail if we are not in a valid state
		var isReady = true;
		isReady &= latSpacingNFS.isValidInput() == true;
		isReady &= lonSpacingNFS.isValidInput() == true;
		if (isReady == false)
			return;

		notifyListeners(this, 0);
	}

	/**
	 * Helper method to keep the GUI synchronized.
	 */
	private void updateGui()
	{
		var isEnabled = false;
		isEnabled |= latSpacingNFS.getValue() != currLatLonSpacing.latSpacing();
		isEnabled |= lonSpacingNFS.getValue() != currLatLonSpacing.lonSpacing();
		isEnabled &= latSpacingNFS.isValidInput() == true && lonSpacingNFS.isValidInput() == true;
		applyB.setEnabled(isEnabled);
	}

}
