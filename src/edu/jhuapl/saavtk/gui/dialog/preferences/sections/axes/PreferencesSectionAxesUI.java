package edu.jhuapl.saavtk.gui.dialog.preferences.sections.axes;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;

public class PreferencesSectionAxesUI extends JPanel
{
	private JLabel jLabel11;

	private JLabel jLabel15;
	private JLabel jLabel16;
	private JLabel jLabel17;
	private JLabel jLabel18;
	private JLabel jLabel19;
	private JLabel jLabel20;
	private JLabel jLabel21;
	private JLabel jLabel22;
	private JSpinner axesConeLengthSpinner;
	private JSpinner axesConeRadiusSpinner;
	private JSpinner axesFontSpinner;
	private JSpinner axesLineWidthSpinner;
	private JSpinner axesSizeSpinner;
    private JLabel jLabel3;
    private JSeparator jSeparator2;


//  private JPanel jPanel2;
//  private JPanel jPanel3;
//  private JButton xAxisColorButton;
//  private JLabel xAxisColorLabel;
//  private JButton yAxisColorButton;
//  private JLabel yAxisColorLabel;
//  private JButton zAxisColorButton;
//  private JLabel zAxisColorLabel;
//  private JCheckBox showAxesCheckBox;


	public PreferencesSectionAxesUI()
	{
		initGUI();
	}

	private void initGUI()
	{
//      showAxesCheckBox = new JCheckBox();
//      interactiveCheckBox = new JCheckBox();
		
//      showAxesCheckBox.setText("Show Axes");
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 1;
//      gridBagConstraints.gridy = 9;
//      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//      gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
//      jPanel11.add(showAxesCheckBox, gridBagConstraints);
//
		
//      jPanel2 = new JPanel();

//      jPanel3 = new JPanel();

		jLabel3 = new JLabel();
        jSeparator2 = new JSeparator();
		jLabel20 = new JLabel();
//        xAxisColorButton = new JButton();
		jLabel21 = new JLabel();
		jLabel22 = new JLabel();
//        yAxisColorButton = new JButton();
//        zAxisColorButton = new JButton();
//        xAxisColorLabel = new JLabel();
//        yAxisColorLabel = new JLabel();
//        zAxisColorLabel = new JLabel();
		jLabel15 = new JLabel();
		axesSizeSpinner = new JSpinner();
		jLabel16 = new JLabel();
		axesLineWidthSpinner = new JSpinner();
		jLabel17 = new JLabel();
		jLabel18 = new JLabel();
		jLabel19 = new JLabel();
		axesFontSpinner = new JSpinner();
		axesConeLengthSpinner = new JSpinner();
		axesConeRadiusSpinner = new JSpinner();
		jLabel11 = new JLabel();

		/*
		 * jLabel20.setText("X Axis Color"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 11; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; jPanel11.add(jLabel20, gridBagConstraints);
		 *
		 * xAxisColorButton.setText("Change..."); xAxisColorButton.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * xAxisColorButtonActionPerformed(evt); } }); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 3;
		 * gridBagConstraints.gridy = 11; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.LINE_START; jPanel11.add(xAxisColorButton,
		 * gridBagConstraints);
		 *
		 * jLabel21.setText("Y Axis Color"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 12; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; jPanel11.add(jLabel21, gridBagConstraints);
		 *
		 * jLabel22.setText("Z Axis Color"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 13; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; jPanel11.add(jLabel22, gridBagConstraints);
		 *
		 * yAxisColorButton.setText("Change..."); yAxisColorButton.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * yAxisColorButtonActionPerformed(evt); } }); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 3;
		 * gridBagConstraints.gridy = 12; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.LINE_START; jPanel11.add(yAxisColorButton,
		 * gridBagConstraints);
		 *
		 * zAxisColorButton.setText("Change..."); zAxisColorButton.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * zAxisColorButtonActionPerformed(evt); } }); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 3;
		 * gridBagConstraints.gridy = 13; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.LINE_START; jPanel11.add(zAxisColorButton,
		 * gridBagConstraints);
		 *
		 * xAxisColorLabel.setText("Default"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 2;
		 * gridBagConstraints.gridy = 11; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.LINE_START; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 4, 0, 0); jPanel11.add(xAxisColorLabel,
		 * gridBagConstraints);
		 *
		 * yAxisColorLabel.setText("Default"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 2;
		 * gridBagConstraints.gridy = 12; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.LINE_START; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 4, 0, 0); jPanel11.add(yAxisColorLabel,
		 * gridBagConstraints);
		 *
		 * zAxisColorLabel.setText("Default"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 2;
		 * gridBagConstraints.gridy = 13; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.LINE_START; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 4, 0, 0); jPanel11.add(zAxisColorLabel,
		 * gridBagConstraints);
		 *
		 * jLabel15.setText("Size"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 15; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 0, 0, 4); jPanel11.add(jLabel15, gridBagConstraints);
		 *
		 * axesSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.2d, 0.0d, 1.0d,
		 * 0.1d)); gridBagConstraints = new java.awt.GridBagConstraints();
		 * gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 15;
		 * gridBagConstraints.gridwidth = 2; gridBagConstraints.ipadx = 50;
		 * gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		 * jPanel11.add(axesSizeSpinner, gridBagConstraints);
		 *
		 * jLabel16.setText("Line Width"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 16; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 0, 0, 4); jPanel11.add(jLabel16, gridBagConstraints);
		 *
		 * axesLineWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d,
		 * 128.0d, 1.0d)); axesLineWidthSpinner.setMinimumSize(new
		 * java.awt.Dimension(41, 28)); axesLineWidthSpinner.setPreferredSize(new
		 * java.awt.Dimension(41, 28)); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 2;
		 * gridBagConstraints.gridy = 16; gridBagConstraints.gridwidth = 2;
		 * gridBagConstraints.ipadx = 50; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.WEST; jPanel11.add(axesLineWidthSpinner,
		 * gridBagConstraints);
		 *
		 * jLabel17.setText("Font Size"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 17; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 0, 0, 4); jPanel11.add(jLabel17, gridBagConstraints);
		 *
		 * jLabel18.setText("Cone Length"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 18; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 0, 0, 4); jPanel11.add(jLabel18, gridBagConstraints);
		 *
		 * jLabel19.setText("Cone Radius"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 19; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 0, 0, 4); jPanel11.add(jLabel19, gridBagConstraints);
		 *
		 * axesFontSpinner.setModel(new javax.swing.SpinnerNumberModel(12, 4, 128, 1));
		 * axesFontSpinner.setMinimumSize(new java.awt.Dimension(41, 28));
		 * axesFontSpinner.setPreferredSize(new java.awt.Dimension(41, 28));
		 * gridBagConstraints = new java.awt.GridBagConstraints();
		 * gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 17;
		 * gridBagConstraints.gridwidth = 2; gridBagConstraints.ipadx = 50;
		 * gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		 * jPanel11.add(axesFontSpinner, gridBagConstraints);
		 *
		 * axesConeLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0.2d, 0.0d,
		 * 1.0d, 0.1d)); gridBagConstraints = new java.awt.GridBagConstraints();
		 * gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 18;
		 * gridBagConstraints.gridwidth = 2; gridBagConstraints.ipadx = 50;
		 * gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		 * jPanel11.add(axesConeLengthSpinner, gridBagConstraints);
		 *
		 * axesConeRadiusSpinner.setModel(new javax.swing.SpinnerNumberModel(0.4d, 0.0d,
		 * 1.0d, 0.1d)); gridBagConstraints = new java.awt.GridBagConstraints();
		 * gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 19;
		 * gridBagConstraints.gridwidth = 2; gridBagConstraints.ipadx = 50;
		 * gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		 * jPanel11.add(axesConeRadiusSpinner, gridBagConstraints);
		 *
		 * jLabel11.setText("Font Color"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 1;
		 * gridBagConstraints.gridy = 14; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.EAST; jPanel11.add(jLabel11, gridBagConstraints);
		 *
		 * fontColorLabel.setText("Default"); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 2;
		 * gridBagConstraints.gridy = 14; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.LINE_START; gridBagConstraints.insets = new
		 * java.awt.Insets(0, 4, 0, 0); jPanel11.add(fontColorLabel,
		 * gridBagConstraints);
		 *
		 * fontColorButton.setText("Change..."); fontColorButton.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent evt) {
		 * fontColorButtonActionPerformed(evt); } }); gridBagConstraints = new
		 * java.awt.GridBagConstraints(); gridBagConstraints.gridx = 3;
		 * gridBagConstraints.gridy = 14; gridBagConstraints.anchor =
		 * java.awt.GridBagConstraints.LINE_START; jPanel11.add(fontColorButton,
		 * gridBagConstraints);
		 */
		
//      jPanel2.setLayout(new java.awt.GridBagLayout());

//      jPanel3.setLayout(new java.awt.GridBagLayout());
//
//      jLabel3.setText("Orientation Axes");
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 0;
//      gridBagConstraints.gridy = 0;
//      gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
//      jPanel3.add(jLabel3, gridBagConstraints);
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 1;
//      gridBagConstraints.gridy = 0;
//      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//      gridBagConstraints.weightx = 1.0;
//      jPanel3.add(jSeparator2, gridBagConstraints);
//
//      gridBagConstraints = new java.awt.GridBagConstraints();
//      gridBagConstraints.gridx = 0;
//      gridBagConstraints.gridy = 8;
//      gridBagConstraints.gridwidth = 4;
//      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//      gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
//      jPanel11.add(jPanel3, gridBagConstraints);
	}
}
