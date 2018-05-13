package edu.jhuapl.saavtk.colormap;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

public class ColormapControllerWithContouring extends ColormapController implements ChangeListener
{
	private final JLabel lineWidthLabel;
	private final JSpinner contourLineWidthSpinner;
	private final JToggleButton showAsContourLinesButton;

	public static final String CONTOUR_BUTTON_TOGGLED = "Contour button toggled";

	public ColormapControllerWithContouring()
	{
		super();

		JPanel subPanel = new JPanel(new GridLayout(1, 2));

		lineWidthLabel = new JLabel("Line width", JLabel.RIGHT);
		subPanel.add(lineWidthLabel);
		contourLineWidthSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
		subPanel.add(contourLineWidthSpinner);

		JPanel panel = new JPanel(new GridLayout(1, 2));
		showAsContourLinesButton = new JToggleButton("Enable Contours");

		showAsContourLinesButton.setEnabled(false);
		updateLineWidthEnabled();

		panel.add(showAsContourLinesButton);
		panel.add(subPanel);
		add(panel, BorderLayout.SOUTH);

		contourLineWidthSpinner.addFocusListener(this);
		contourLineWidthSpinner.addChangeListener(this);
		showAsContourLinesButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource().equals(showAsContourLinesButton))
		{
			updateLineWidthEnabled();
		}
		super.actionPerformed(e);
		if (syncButton.isSelected())
			pcs.firePropertyChange(CONTOUR_BUTTON_TOGGLED, null, null);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		showAsContourLinesButton.setEnabled(enabled);
		updateLineWidthEnabled();
	}

	public double getLineWidth()
	{
		return (Integer) contourLineWidthSpinner.getValue();
	}

	public boolean getContourLinesRequested()
	{
		return showAsContourLinesButton.isSelected();
	}

	private final void updateLineWidthEnabled()
	{
		boolean enabled = showAsContourLinesButton.isEnabled() && showAsContourLinesButton.isSelected();
		lineWidthLabel.setEnabled(enabled);
		contourLineWidthSpinner.setEnabled(enabled);
	}
}
