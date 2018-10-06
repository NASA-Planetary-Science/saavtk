package edu.jhuapl.saavtk.colormap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

public class ContourPanel extends JPanel implements ActionListener, ChangeListener
{
	// Constants
	private static final long serialVersionUID = 1L;
	public static final String EVT_ContourChanged = "Event: ContourChanged";

	// GUI vars
	private final JCheckBox showAsContourCB;
	private final JLabel lineWidthL;
	private final JSpinner contourLineWidthSpinner;

	public ContourPanel()
	{
		super();

		// Instantiate the various GUI controls
		lineWidthL = new JLabel("Line width", JLabel.RIGHT);
		contourLineWidthSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
		contourLineWidthSpinner.addChangeListener(this);

		showAsContourCB = new JCheckBox("Show Contours", false);
		showAsContourCB.addActionListener(this);

		// Form the GUI
		setLayout(new MigLayout("", "0[]25[][]0", "0[]0"));
		add(showAsContourCB, "");
		add(lineWidthL, "");
		add(contourLineWidthSpinner, "");
	}

	public double getLineWidth()
	{
		return (Integer) contourLineWidthSpinner.getValue();
	}

	public boolean getContourLinesRequested()
	{
		return showAsContourCB.isSelected();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		if (aEvent.getSource() == showAsContourCB)
		{
			updateLineWidthEnabled();
			firePropertyChange(EVT_ContourChanged, null, null);
		}
	}

	@Override
	public void setEnabled(boolean aEnabled)
	{
		showAsContourCB.setEnabled(aEnabled);
		updateLineWidthEnabled();
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		if (aEvent.getSource() == contourLineWidthSpinner)
		{
			firePropertyChange(EVT_ContourChanged, null, null);
		}
	}

	/**
	 * Helper method to keep the UI's enable state properly synchronized.
	 */
	private final void updateLineWidthEnabled()
	{
		boolean enabled = showAsContourCB.isEnabled() && showAsContourCB.isSelected();
		lineWidthL.setEnabled(enabled);
		contourLineWidthSpinner.setEnabled(enabled);
	}

}
