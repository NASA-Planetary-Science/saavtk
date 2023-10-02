package edu.jhuapl.saavtk.gui.dialog.preferences.sections.colorMap;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import edu.jhuapl.saavtk.colormap.Colormaps;

public class PreferencesSectionColorMapUI extends JPanel 
{
    private JLabel defaultColorMapLabel;
    private JComboBox<String> defaultColorMapSelection;
	
	public PreferencesSectionColorMapUI()
	{
		initGUI();
	}
	
	private void initGUI()
	{
		defaultColorMapLabel = new JLabel();
	    defaultColorMapSelection = new JComboBox<>();
		setLayout(new GridBagLayout());
		
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		titlePanel.add(new JLabel("Color Map"), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 4, 0, 4);
		titlePanel.add(new JSeparator(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(15, 4, 5, 0);
		gridBagConstraints.weightx = 1.0;
		add(titlePanel, gridBagConstraints);
		
        // Start of default color map name
        defaultColorMapLabel.setText("Default Color Map Name");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 31;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 0, 10);
        add(defaultColorMapLabel, gridBagConstraints);

        for (String colorMapName : Colormaps.getAllBuiltInColormapNames())
        {
            defaultColorMapSelection.addItem(colorMapName);
        }
        defaultColorMapSelection.setSelectedItem(Colormaps.getCurrentColormapName());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 31;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(defaultColorMapSelection, gridBagConstraints);
        // End of default color map name
	}

	public JComboBox<String> getDefaultColorMapSelection()
	{
		return defaultColorMapSelection;
	}
}
