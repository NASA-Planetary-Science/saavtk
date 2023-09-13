package edu.jhuapl.saavtk.gui.dialog.preferences.sections.interactor;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

public class PreferencesSectionInteractorUI extends JPanel
{
//  private JPanel jPanel5;
    private JLabel jLabel4;
    private JSeparator jSeparator4;
    private JRadioButton joystickRadioButton;
    private JRadioButton trackballRadioButton;
//  private JCheckBox interactiveCheckBox;
  private ButtonGroup interactorStyleButtonGroup;
	
	public PreferencesSectionInteractorUI()
	{
		initUI();
	}
	
	private void initUI()
	{
        interactorStyleButtonGroup = new ButtonGroup();

		jLabel4 = new JLabel();
        jSeparator4 = new JSeparator();
        trackballRadioButton = new JRadioButton();
        joystickRadioButton = new JRadioButton();
//      jPanel5.setLayout(new java.awt.GridBagLayout());

//      jLabel4.setText("Interactor Style");
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 0;
//      gridBagConstraints.gridy = 0;
//      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
//      jPanel5.add(jLabel4, gridBagConstraints);
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 1;
//      gridBagConstraints.gridy = 0;
//      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//      gridBagConstraints.weightx = 1.0;
//      jPanel5.add(jSeparator4, gridBagConstraints);
//
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 0;
//      gridBagConstraints.gridy = 22;
//      gridBagConstraints.gridwidth = 4;
//      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//      gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
//      jPanel11.add(jPanel5, gridBagConstraints);

//      interactorStyleButtonGroup.add(trackballRadioButton);
//      trackballRadioButton.setText("Trackball");
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 1;
//      gridBagConstraints.gridy = 23;
//      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//      gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
//      jPanel11.add(trackballRadioButton, gridBagConstraints);
//
//      interactorStyleButtonGroup.add(joystickRadioButton);
//      joystickRadioButton.setText("Joystick");
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 1;
//      gridBagConstraints.gridy = 24;
//      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//      gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
//      jPanel11.add(joystickRadioButton, gridBagConstraints);
        
//      interactiveCheckBox.setText("Interactive");
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 1;
//      gridBagConstraints.gridy = 10;
//      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//      gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
//      jPanel11.add(interactiveCheckBox, gridBagConstraints);
	}

	public JRadioButton getJoystickRadioButton()
	{
		return joystickRadioButton;
	}

	public JRadioButton getTrackballRadioButton()
	{
		return trackballRadioButton;
	}
}
