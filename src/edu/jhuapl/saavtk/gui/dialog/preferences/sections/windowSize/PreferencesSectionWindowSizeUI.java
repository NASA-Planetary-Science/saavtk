package edu.jhuapl.saavtk.gui.dialog.preferences.sections.windowSize;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class PreferencesSectionWindowSizeUI extends JPanel
{
	private JPanel panelDimsPanel;
	private JPanel panelDimsTitlePanel;
	private JSpinner panelWidthTextField;
	private JLabel panelHeightTextField;
	private JPanel windowDimsPanel;
	private JPanel windowDimsTitlePanel;
	private JSpinner windowWidthTextField;
	private JSpinner windowHeightTextField;
	private JLabel jLabel26;
    private JLabel jLabel27;
    private JLabel jLabel28;
    private JLabel jLabel29;
    private JLabel jLabel30;
    private JLabel jLabel31;

	private JSeparator jSeparator11;
	private JSeparator jSeparator12;

	public PreferencesSectionWindowSizeUI()
	{
		initGUI();
	}
	
	private void initGUI()
	{
		panelDimsPanel = new JPanel();
		panelDimsTitlePanel = new JPanel();
		jLabel26 = new JLabel();
		jSeparator11 = new JSeparator();
		jLabel27 = new JLabel();
		jLabel28 = new JLabel();
		SpinnerModel panelWidthModel = new SpinnerNumberModel(1200, 800, 3000, 10);
		panelWidthTextField = new JSpinner(panelWidthModel);
		panelHeightTextField = new JLabel();
		windowDimsPanel = new JPanel();
		windowDimsTitlePanel = new JPanel();
		jLabel29 = new JLabel();
		jSeparator12 = new JSeparator();
		jLabel30 = new JLabel();
		jLabel31 = new JLabel();
		SpinnerModel windowWidthModel = new SpinnerNumberModel(1200, 800, 3000, 10);
		SpinnerModel windowHeightModel = new SpinnerNumberModel(900, 600, 2500, 10);
		windowWidthTextField = new JSpinner(windowWidthModel);
		windowHeightTextField  = new JSpinner(windowHeightModel);
		
    	setLayout(new GridBagLayout());
		
		// Start of panel dimensions
        panelDimsTitlePanel.setLayout(new GridBagLayout());
        
        jLabel26.setText("Rendering Panel Dimensions");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelDimsTitlePanel.add(jLabel26, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 4, 0, 4);
        panelDimsTitlePanel.add(jSeparator11, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 39;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(15, 0, 5, 0);
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(panelDimsTitlePanel, gridBagConstraints);
        
        panelDimsPanel.setLayout(new GridBagLayout());
        
        jLabel27.setText("Width (Pixels)");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        panelDimsPanel.add(jLabel27, gridBagConstraints);
        
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panelDimsPanel.add(panelWidthTextField, gridBagConstraints);
        
        jLabel28.setText("Height (Pixels): ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 4, 0, 4);
        panelDimsPanel.add(jLabel28, gridBagConstraints);
        
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        panelDimsPanel.add(panelHeightTextField, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 40;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 0, 5, 0);
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(panelDimsPanel, gridBagConstraints);
        // End of panel dimensions
        
        // Start of window dimensions
		windowDimsTitlePanel.setLayout(new GridBagLayout());

		jLabel29.setText("App Window Dimensions");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		windowDimsTitlePanel.add(jLabel29, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		windowDimsTitlePanel.add(jSeparator12, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 41;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(15, 0, 5, 0);
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		add(windowDimsTitlePanel, gridBagConstraints);
        
		windowDimsPanel.setLayout(new GridBagLayout());

		jLabel30.setText("Width (Pixels)");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 4);
		windowDimsPanel.add(jLabel30, gridBagConstraints);

		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		windowDimsPanel.add(windowWidthTextField, gridBagConstraints);

		jLabel31.setText("Height (Pixels)");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		windowDimsPanel.add(jLabel31, gridBagConstraints);

		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		windowDimsPanel.add(windowHeightTextField, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 42;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 0, 5, 0);
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(windowDimsPanel, gridBagConstraints);
		// End of window dimensions
	}

	public JSpinner getWindowWidthTextField()
	{
		return windowWidthTextField;
	}

	public JSpinner getWindowHeightTextField()
	{
		return windowHeightTextField;
	}

	public JSpinner getPanelWidthTextField()
	{
		return panelWidthTextField;
	}

	public JLabel getPanelHeightTextField()
	{
		return panelHeightTextField;
	}
}
