package edu.jhuapl.saavtk.gui.dialog.preferences.sections.pickTolerance;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;

public class PreferencesSectionPickToleranceUI extends JPanel
{
	private JPanel sliderTitlePanel;
	private JPanel sliderPanel;
	private JSeparator jSeparator5;

	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JSlider pickToleranceSlider;
	
	public PreferencesSectionPickToleranceUI()
	{
		initGUI();
	}

	private void initGUI()
	{
		sliderTitlePanel = new JPanel();
		jSeparator5 = new JSeparator();
		jLabel5 = new JLabel();
		sliderPanel = new JPanel();
		pickToleranceSlider = new JSlider();
		jLabel6 = new JLabel();
		jLabel7 = new JLabel();
		setLayout(new GridBagLayout());
		// Start of pick tolerance
		jLabel5.setText("Pick Tolerance");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		sliderTitlePanel.add(jLabel5, gridBagConstraints);

		sliderTitlePanel.setLayout(new GridBagLayout());
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		sliderTitlePanel.add(jSeparator5, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(15, 4, 5, 0);
		add(sliderTitlePanel, gridBagConstraints);

		sliderPanel.setLayout(new GridBagLayout());

		pickToleranceSlider.setMaximum(1000);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		sliderPanel.add(pickToleranceSlider, gridBagConstraints);

		jLabel6.setText("Most Sensitive");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		sliderPanel.add(jLabel6, gridBagConstraints);

		jLabel7.setText("Least Sensitive");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		sliderPanel.add(jLabel7, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 26;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		add(sliderPanel, gridBagConstraints);
		// End of pick tolerance
	}

	public JSlider getPickToleranceSlider()
	{
		return pickToleranceSlider;
	}

}
