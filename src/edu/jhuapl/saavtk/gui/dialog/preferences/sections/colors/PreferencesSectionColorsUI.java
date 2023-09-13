package edu.jhuapl.saavtk.gui.dialog.preferences.sections.colors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class PreferencesSectionColorsUI extends JPanel
{
	private JButton applyToAllButton;
	private JButton applyToCurrentButton;
	
	private JLabel jLabel10;
	private JLabel backgroundColorLabel;
	private JButton backgroundColorButton;
	private JSeparator jSeparator7;

	private JPanel bgColorPanel;
	private JPanel selectionColorTitlePanel;
	private JButton selectionColorButton;
	private JLabel selectionColorLabel;
	private JLabel jLabel9;
	private JSeparator jSeparator6;
	private JPanel buttonsPanel;

	public PreferencesSectionColorsUI()
	{
		initGUI();
	}

	private void initGUI()
	{
		selectionColorTitlePanel = new JPanel();
		jSeparator6 = new JSeparator();
		jLabel9 = new JLabel();
		selectionColorLabel = new JLabel();
		selectionColorButton = new JButton();
		bgColorPanel = new JPanel();
		jLabel10 = new JLabel();
		jSeparator7 = new JSeparator();
		backgroundColorLabel = new JLabel();
		backgroundColorButton = new JButton();
		buttonsPanel = new JPanel();
		applyToAllButton = new JButton();
		applyToCurrentButton = new JButton();
		setLayout(new GridBagLayout());
		// Start of background color
		bgColorPanel.setLayout(new GridBagLayout());

		jLabel10.setText("Background Color");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		bgColorPanel.add(jLabel10, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		bgColorPanel.add(jSeparator7, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 34;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(15, 4, 5, 0);
		gridBagConstraints.weightx = 1.0;
		add(bgColorPanel, gridBagConstraints);

		backgroundColorLabel.setText("Default");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 35;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		add(backgroundColorLabel, gridBagConstraints);

		backgroundColorButton.setText("Change...");

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 35;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		add(backgroundColorButton, gridBagConstraints);
		// End of background color

		// Start of selection color
		selectionColorTitlePanel.setLayout(new java.awt.GridBagLayout());
		jLabel9.setText("Selection Color");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		selectionColorTitlePanel.add(jLabel9, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		selectionColorTitlePanel.add(jSeparator6, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 32;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(15, 4, 5, 0);
		gridBagConstraints.weightx = 1.0;
		add(selectionColorTitlePanel, gridBagConstraints);

		selectionColorLabel.setText("Default");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 33;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		add(selectionColorLabel, gridBagConstraints);

		selectionColorButton.setText("Change...");

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 33;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		add(selectionColorButton, gridBagConstraints);
		// End of selection color
		
		
		
        buttonsPanel.setLayout(new GridBagLayout());

        applyToCurrentButton.setText("Apply to Current View");
       
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        buttonsPanel.add(applyToCurrentButton, gridBagConstraints);

        applyToAllButton.setText("Apply to All Views");

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        buttonsPanel.add(applyToAllButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 43;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(15, 0, 0, 0);
        add(buttonsPanel, gridBagConstraints);
	}

	public JLabel getBackgroundColorLabel()
	{
		return backgroundColorLabel;
	}

	public JLabel getSelectionColorLabel()
	{
		return selectionColorLabel;
	}

	public JButton getBackgroundColorButton()
	{
		return backgroundColorButton;
	}

	public JButton getSelectionColorButton()
	{
		return selectionColorButton;
	}
	
	public JButton getApplyToAllButton()
	{
		return applyToAllButton;
	}

	public JButton getApplyToCurrentButton()
	{
		return applyToCurrentButton;
	}
}
