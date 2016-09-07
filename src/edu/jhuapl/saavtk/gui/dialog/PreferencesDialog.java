/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SettingsDialog.java
 *
 * Created on Mar 27, 2012, 9:37:47 PM
 */
package edu.jhuapl.saavtk.gui.dialog;

import java.awt.Color;
import java.util.List;
import java.util.LinkedHashMap;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.Renderer.InteractorStyleType;
import edu.jhuapl.saavtk.gui.Renderer.LightingType;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.Preferences;


public class PreferencesDialog extends javax.swing.JDialog {

    private ViewManager viewManager;
    private static final double MAX_TOLERANCE = 0.01;

    /** Creates new form SettingsDialog */
    public PreferencesDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setTitle("Preferences");
    }

    public void setViewManager(ViewManager viewManager)
    {
        this.viewManager = viewManager;
    }

    public void setVisible(boolean b) {
        if (b)
        {
            Renderer renderer = viewManager.getCurrentView().getRenderer();
            if (renderer.getLighting() == Renderer.LightingType.LIGHT_KIT)
                lightKitRadioButton.setSelected(true);
            else if (renderer.getLighting() == Renderer.LightingType.HEADLIGHT)
                headlightRadioButton.setSelected(true);
            else
                fixedLightRadioButton.setSelected(true);
            intensitySpinner.setValue(renderer.getLightIntensity());
            LatLon position = renderer.getFixedLightPosition();
            latitudeTextField.setValue(position.lat);
            longitudeTextField.setValue(position.lon);
            distanceTextField.setValue(position.rad);
            showAxesCheckBox.setSelected(renderer.getShowOrientationAxes());
            interactiveCheckBox.setSelected(renderer.getOrientationAxesInteractive());

            if (renderer.getDefaultInteractorStyleType() == Renderer.InteractorStyleType.JOYSTICK_CAMERA)
                joystickRadioButton.setSelected(true);
            else
                trackballRadioButton.setSelected(true);

            PolyhedralModel smallBodyModel =
                    viewManager.getCurrentView().getModelManager().getPolyhedralModel();
            showScaleBarCheckBox.setSelected(smallBodyModel.getShowScaleBar());

            PickManager pickManager = viewManager.getCurrentView().getPickManager();
            int value = getSliderValueFromTolerance(pickManager.getPickTolerance());
            pickToleranceSlider.setValue(value);

            mouseWheelMotionFactorSpinner.setValue(renderer.getMouseWheelMotionFactor());

            int[] color = viewManager.getCurrentView().getModelManager().getCommonData().getSelectionColor();
            updateColorLabel(color, selectionColorLabel);

            color = viewManager.getCurrentView().getRenderer().getBackgroundColor();
            updateColorLabel(color, backgroundColorLabel);

            color = viewManager.getCurrentView().getRenderer().getXAxisColor();
            updateColorLabel(color, xAxisColorLabel);

            color = viewManager.getCurrentView().getRenderer().getYAxisColor();
            updateColorLabel(color, yAxisColorLabel);

            color = viewManager.getCurrentView().getRenderer().getZAxisColor();
            updateColorLabel(color, zAxisColorLabel);

            color = viewManager.getCurrentView().getRenderer().getAxesLabelFontColor();
            updateColorLabel(color, fontColorLabel);

            axesSizeSpinner.setValue(viewManager.getCurrentView().getRenderer().getAxesSize());
            axesLineWidthSpinner.setValue(viewManager.getCurrentView().getRenderer().getAxesLineWidth());
            axesFontSpinner.setValue(viewManager.getCurrentView().getRenderer().getAxesLabelFontSize());
            axesConeLengthSpinner.setValue(viewManager.getCurrentView().getRenderer().getAxesConeLength());
            axesConeRadiusSpinner.setValue(viewManager.getCurrentView().getRenderer().getAxesConeRadius());

            updateEnabledItems();
        }

        super.setVisible(b);
    }

    private void updateEnabledItems()
    {
        boolean enabled = headlightRadioButton.isSelected() || fixedLightRadioButton.isSelected();
        intensityLabel.setEnabled(enabled);
        intensitySpinner.setEnabled(enabled);
        enabled = fixedLightRadioButton.isSelected();
        latitudeLabel.setEnabled(enabled);
        latitudeTextField.setEnabled(enabled);
        longitudeLabel.setEnabled(enabled);
        longitudeTextField.setEnabled(enabled);
        distanceLabel.setEnabled(enabled);
        distanceTextField.setEnabled(enabled);
    }

    private void applyToView(View v)
    {
        Renderer renderer = v.getRenderer();
        if (renderer != null)
        {
            if (lightKitRadioButton.isSelected())
            {
                renderer.setLighting(LightingType.LIGHT_KIT);
            }
            else if (headlightRadioButton.isSelected())
            {
                renderer.setLighting(LightingType.HEADLIGHT);
            }
            else
            {
                renderer.setLighting(LightingType.FIXEDLIGHT);
            }

            renderer.setLightIntensity((Double)intensitySpinner.getValue());

            LatLon position = new LatLon(
                    Double.parseDouble(latitudeTextField.getText()),
                    Double.parseDouble(longitudeTextField.getText()),
                    Double.parseDouble(distanceTextField.getText()));

            renderer.setFixedLightPosition(position);
            renderer.setShowOrientationAxes(showAxesCheckBox.isSelected());
            renderer.setOrientationAxesInteractive(interactiveCheckBox.isSelected());

            if (joystickRadioButton.isSelected())
                renderer.setDefaultInteractorStyleType(InteractorStyleType.JOYSTICK_CAMERA);
            else
                renderer.setDefaultInteractorStyleType(InteractorStyleType.TRACKBALL_CAMERA);

            PolyhedralModel smallBodyModel = v.getModelManager().getPolyhedralModel();
            smallBodyModel.setShowScaleBar(showScaleBarCheckBox.isSelected());

            PickManager pickManager = v.getPickManager();
            double tolerance = getToleranceFromSliderValue(pickToleranceSlider.getValue());
            pickManager.setPickTolerance(tolerance);

            renderer.setMouseWheelMotionFactor((Double)mouseWheelMotionFactorSpinner.getValue());

            int [] color = getColorFromLabel(selectionColorLabel);
            v.getModelManager().getCommonData().setSelectionColor(color);

            color = getColorFromLabel(backgroundColorLabel);
            renderer.setBackgroundColor(color);

            color = getColorFromLabel(xAxisColorLabel);
            renderer.setXAxisColor(color);

            color = getColorFromLabel(yAxisColorLabel);
            renderer.setYAxisColor(color);

            color = getColorFromLabel(zAxisColorLabel);
            renderer.setZAxisColor(color);

            color = getColorFromLabel(fontColorLabel);
            renderer.setAxesLabelFontColor(color);

            renderer.setAxesConeLength((Double)axesConeLengthSpinner.getValue());
            renderer.setAxesConeRadius((Double)axesConeRadiusSpinner.getValue());
            renderer.setAxesLabelFontSize((Integer)axesFontSpinner.getValue());
            renderer.setAxesLineWidth((Double)axesLineWidthSpinner.getValue());
            renderer.setAxesSize((Double)axesSizeSpinner.getValue());
        }
    }

    private double getToleranceFromSliderValue(int value)
    {
        return MAX_TOLERANCE * (double)value / (double)pickToleranceSlider.getMaximum();
    }

    private int getSliderValueFromTolerance(double tolerance)
    {
        return (int)((double)pickToleranceSlider.getMaximum()
                * tolerance / MAX_TOLERANCE);
    }

    private void updateColorLabel(int[] color, JLabel label)
    {
        int[] c = color;
        label.setText("["+c[0]+","+c[1]+","+c[2]+"]");
        label.setIcon(new ColorUtil.ColorIcon(new Color(c[0], c[1], c[2])));
    }

    private void showColorChooser(JLabel label)
    {
        int[] initialColor = getColorFromLabel(label);
        Color color = ColorChooser.showColorChooser(
                JOptionPane.getFrameForComponent(this),initialColor);

        if (color == null)
            return;

        int[] c = new int[3];
        c[0] = color.getRed();
        c[1] = color.getGreen();
        c[2] = color.getBlue();

        updateColorLabel(c, label);
    }

    private int[] getColorFromLabel(JLabel label)
    {
        Color color = ((ColorUtil.ColorIcon)label.getIcon()).getColor();
        int[] c = new int[3];
        c[0] = color.getRed();
        c[1] = color.getGreen();
        c[2] = color.getBlue();
        return c;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lightingButtonGroup = new javax.swing.ButtonGroup();
        interactorStyleButtonGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel11 = new javax.swing.JPanel();
        headlightRadioButton = new javax.swing.JRadioButton();
        intensityLabel = new javax.swing.JLabel();
        showAxesCheckBox = new javax.swing.JCheckBox();
        interactiveCheckBox = new javax.swing.JCheckBox();
        lightKitRadioButton = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        applyToCurrentButton = new javax.swing.JButton();
        applyToAllButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        intensitySpinner = new javax.swing.JSpinner();
        fixedLightRadioButton = new javax.swing.JRadioButton();
        latitudeLabel = new javax.swing.JLabel();
        latitudeTextField = new javax.swing.JFormattedTextField();
        longitudeLabel = new javax.swing.JLabel();
        longitudeTextField = new javax.swing.JFormattedTextField();
        distanceLabel = new javax.swing.JLabel();
        distanceTextField = new javax.swing.JFormattedTextField();
        jPanel4 = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        showScaleBarCheckBox = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        trackballRadioButton = new javax.swing.JRadioButton();
        joystickRadioButton = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        pickToleranceSlider = new javax.swing.JSlider();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        mouseWheelMotionFactorSpinner = new javax.swing.JSpinner();
        selectionColorLabel = new javax.swing.JLabel();
        selectionColorButton = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jSeparator9 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator7 = new javax.swing.JSeparator();
        backgroundColorLabel = new javax.swing.JLabel();
        backgroundColorButton = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        xAxisColorButton = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        yAxisColorButton = new javax.swing.JButton();
        zAxisColorButton = new javax.swing.JButton();
        xAxisColorLabel = new javax.swing.JLabel();
        yAxisColorLabel = new javax.swing.JLabel();
        zAxisColorLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        axesSizeSpinner = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        axesLineWidthSpinner = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        axesFontSpinner = new javax.swing.JSpinner();
        axesConeLengthSpinner = new javax.swing.JSpinner();
        axesConeRadiusSpinner = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        fontColorLabel = new javax.swing.JLabel();
        fontColorButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout());

        jPanel11.setLayout(new java.awt.GridBagLayout());

        lightingButtonGroup.add(headlightRadioButton);
        headlightRadioButton.setText("Headlight");
        headlightRadioButton.setToolTipText("A Headlight is a single light always positioned at the virtual camera. It's intensity can be changed below.");
        headlightRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headlightRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(headlightRadioButton, gridBagConstraints);

        intensityLabel.setText("Intensity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(intensityLabel, gridBagConstraints);

        showAxesCheckBox.setText("Show Axes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(showAxesCheckBox, gridBagConstraints);

        interactiveCheckBox.setText("Interactive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(interactiveCheckBox, gridBagConstraints);

        lightingButtonGroup.add(lightKitRadioButton);
        lightKitRadioButton.setText("Light Kit");
        lightKitRadioButton.setToolTipText("A Light Kit is a set of several lights of various strengths positioned to provide suitable illumination for most situations.");
        lightKitRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lightKitRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(lightKitRadioButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        applyToCurrentButton.setText("Apply to Current View");
        applyToCurrentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyToCurrentButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(applyToCurrentButton, gridBagConstraints);

        applyToAllButton.setText("Apply to All Views");
        applyToAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyToAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(applyToAllButton, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel1.add(closeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 33;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        jPanel11.add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Lighting");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel2.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 4, 5, 0);
        jPanel11.add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Orientation Axes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel3.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jSeparator2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        jPanel11.add(jPanel3, gridBagConstraints);

        intensitySpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, 1.0d, 0.1d));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(intensitySpinner, gridBagConstraints);

        lightingButtonGroup.add(fixedLightRadioButton);
        fixedLightRadioButton.setText("Fixed Light");
        fixedLightRadioButton.setToolTipText("A Fixed Light is a light fixed in space that does not move with the virtual camera. Its intensity and positon can be changed below.");
        fixedLightRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixedLightRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(fixedLightRadioButton, gridBagConstraints);

        latitudeLabel.setText("Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(latitudeLabel, gridBagConstraints);

        latitudeTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        latitudeTextField.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(latitudeTextField, gridBagConstraints);

        longitudeLabel.setText("Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(longitudeLabel, gridBagConstraints);

        longitudeTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        longitudeTextField.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(longitudeTextField, gridBagConstraints);

        distanceLabel.setText("Distance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(distanceLabel, gridBagConstraints);

        distanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        distanceTextField.setPreferredSize(new java.awt.Dimension(100, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(distanceTextField, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(jSeparator3, gridBagConstraints);

        jLabel2.setText("Scale Bar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel4.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        jPanel11.add(jPanel4, gridBagConstraints);

        showScaleBarCheckBox.setText("Show Scale Bar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(showScaleBarCheckBox, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel4.setText("Interactor Style");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel5.add(jLabel4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(jSeparator4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        jPanel11.add(jPanel5, gridBagConstraints);

        interactorStyleButtonGroup.add(trackballRadioButton);
        trackballRadioButton.setText("Trackball");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(trackballRadioButton, gridBagConstraints);

        interactorStyleButtonGroup.add(joystickRadioButton);
        joystickRadioButton.setText("Joystick");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(joystickRadioButton, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel6.add(jSeparator5, gridBagConstraints);

        jLabel5.setText("Pick Tolerance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel6.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        jPanel11.add(jPanel6, gridBagConstraints);

        jPanel7.setLayout(new java.awt.GridBagLayout());

        pickToleranceSlider.setMaximum(1000);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanel7.add(pickToleranceSlider, gridBagConstraints);

        jLabel6.setText("Most Sensitive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel7.add(jLabel6, gridBagConstraints);

        jLabel7.setText("Least Sensitive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel7.add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(jPanel7, gridBagConstraints);

        jPanel9.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel9.add(jSeparator6, gridBagConstraints);

        jLabel9.setText("Selection Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel9.add(jLabel9, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 29;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        jPanel11.add(jPanel9, gridBagConstraints);

        jLabel8.setText("Motion Factor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(jLabel8, gridBagConstraints);

        mouseWheelMotionFactorSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), null, null, Double.valueOf(0.1d)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel11.add(mouseWheelMotionFactorSpinner, gridBagConstraints);

        selectionColorLabel.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(selectionColorLabel, gridBagConstraints);

        selectionColorButton.setText("Change...");
        selectionColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionColorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel11.add(selectionColorButton, gridBagConstraints);

        jPanel10.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(jSeparator9, gridBagConstraints);

        jLabel12.setText("Mouse Wheel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel10.add(jLabel12, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 27;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        jPanel11.add(jPanel10, gridBagConstraints);

        jPanel8.setLayout(new java.awt.GridBagLayout());

        jLabel10.setText("Background Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel8.add(jLabel10, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel8.add(jSeparator7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 31;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        jPanel11.add(jPanel8, gridBagConstraints);

        backgroundColorLabel.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(backgroundColorLabel, gridBagConstraints);

        backgroundColorButton.setText("Change...");
        backgroundColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundColorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel11.add(backgroundColorButton, gridBagConstraints);

        jLabel20.setText("X Axis Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel11.add(jLabel20, gridBagConstraints);

        xAxisColorButton.setText("Change...");
        xAxisColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xAxisColorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel11.add(xAxisColorButton, gridBagConstraints);

        jLabel21.setText("Y Axis Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel11.add(jLabel21, gridBagConstraints);

        jLabel22.setText("Z Axis Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel11.add(jLabel22, gridBagConstraints);

        yAxisColorButton.setText("Change...");
        yAxisColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yAxisColorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel11.add(yAxisColorButton, gridBagConstraints);

        zAxisColorButton.setText("Change...");
        zAxisColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zAxisColorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel11.add(zAxisColorButton, gridBagConstraints);

        xAxisColorLabel.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(xAxisColorLabel, gridBagConstraints);

        yAxisColorLabel.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(yAxisColorLabel, gridBagConstraints);

        zAxisColorLabel.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(zAxisColorLabel, gridBagConstraints);

        jLabel15.setText("Size");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(jLabel15, gridBagConstraints);

        axesSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(0.2d, 0.0d, 1.0d, 0.1d));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(axesSizeSpinner, gridBagConstraints);

        jLabel16.setText("Line Width");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(jLabel16, gridBagConstraints);

        axesLineWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, 128.0d, 1.0d));
        axesLineWidthSpinner.setMinimumSize(new java.awt.Dimension(41, 28));
        axesLineWidthSpinner.setPreferredSize(new java.awt.Dimension(41, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(axesLineWidthSpinner, gridBagConstraints);

        jLabel17.setText("Font Size");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(jLabel17, gridBagConstraints);

        jLabel18.setText("Cone Length");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(jLabel18, gridBagConstraints);

        jLabel19.setText("Cone Radius");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel11.add(jLabel19, gridBagConstraints);

        axesFontSpinner.setModel(new javax.swing.SpinnerNumberModel(12, 4, 128, 1));
        axesFontSpinner.setMinimumSize(new java.awt.Dimension(41, 28));
        axesFontSpinner.setPreferredSize(new java.awt.Dimension(41, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(axesFontSpinner, gridBagConstraints);

        axesConeLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0.2d, 0.0d, 1.0d, 0.1d));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(axesConeLengthSpinner, gridBagConstraints);

        axesConeRadiusSpinner.setModel(new javax.swing.SpinnerNumberModel(0.4d, 0.0d, 1.0d, 0.1d));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(axesConeRadiusSpinner, gridBagConstraints);

        jLabel11.setText("Font Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel11.add(jLabel11, gridBagConstraints);

        fontColorLabel.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel11.add(fontColorLabel, gridBagConstraints);

        fontColorButton.setText("Change...");
        fontColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontColorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel11.add(fontColorButton, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel11);

        getContentPane().add(jScrollPane1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void applyToCurrentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyToCurrentButtonActionPerformed
        applyToView(viewManager.getCurrentView());
    }//GEN-LAST:event_applyToCurrentButtonActionPerformed

    private void applyToAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyToAllButtonActionPerformed
        List<View> views = viewManager.getAllViews();
        for (View v : views)
        {
            applyToView(v);
        }

        // In addition, save in preferences file for future use
        LinkedHashMap<String, String> preferencesMap = new LinkedHashMap<String, String>();
        if (lightKitRadioButton.isSelected())
        {
            preferencesMap.put(Preferences.LIGHTING_TYPE, LightingType.LIGHT_KIT.toString());
        }
        else if (headlightRadioButton.isSelected())
        {
            preferencesMap.put(Preferences.LIGHTING_TYPE, LightingType.HEADLIGHT.toString());
        }
        else
        {
            preferencesMap.put(Preferences.LIGHTING_TYPE, LightingType.FIXEDLIGHT.toString());
        }

        if (joystickRadioButton.isSelected())
            preferencesMap.put(Preferences.INTERACTOR_STYLE_TYPE, InteractorStyleType.JOYSTICK_CAMERA.toString());
        else
            preferencesMap.put(Preferences.INTERACTOR_STYLE_TYPE, InteractorStyleType.TRACKBALL_CAMERA.toString());

        preferencesMap.put(Preferences.LIGHT_INTENSITY, ((Double)intensitySpinner.getValue()).toString());
        preferencesMap.put(Preferences.FIXEDLIGHT_LATITUDE, Double.valueOf(latitudeTextField.getText()).toString());
        preferencesMap.put(Preferences.FIXEDLIGHT_LONGITUDE, Double.valueOf(longitudeTextField.getText()).toString());
        preferencesMap.put(Preferences.FIXEDLIGHT_DISTANCE, Double.valueOf(distanceTextField.getText()).toString());
        preferencesMap.put(Preferences.SHOW_AXES, ((Boolean)showAxesCheckBox.isSelected()).toString());
        preferencesMap.put(Preferences.INTERACTIVE_AXES, ((Boolean)interactiveCheckBox.isSelected()).toString());
        preferencesMap.put(Preferences.SHOW_SCALE_BAR, ((Boolean)showScaleBarCheckBox.isSelected()).toString());
        preferencesMap.put(Preferences.LIGHT_INTENSITY, ((Double)intensitySpinner.getValue()).toString());
        preferencesMap.put(Preferences.PICK_TOLERANCE, Double.valueOf(getToleranceFromSliderValue(pickToleranceSlider.getValue())).toString());
        preferencesMap.put(Preferences.MOUSE_WHEEL_MOTION_FACTOR, ((Double)mouseWheelMotionFactorSpinner.getValue()).toString());
        preferencesMap.put(Preferences.SELECTION_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(selectionColorLabel))));
        preferencesMap.put(Preferences.BACKGROUND_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(backgroundColorLabel))));
        preferencesMap.put(Preferences.AXES_XAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(xAxisColorLabel))));
        preferencesMap.put(Preferences.AXES_YAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(yAxisColorLabel))));
        preferencesMap.put(Preferences.AXES_ZAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(zAxisColorLabel))));
        preferencesMap.put(Preferences.AXES_CONE_LENGTH, ((Double)axesConeLengthSpinner.getValue()).toString());
        preferencesMap.put(Preferences.AXES_CONE_RADIUS, ((Double)axesConeRadiusSpinner.getValue()).toString());
        preferencesMap.put(Preferences.AXES_FONT_SIZE, ((Integer)axesFontSpinner.getValue()).toString());
        preferencesMap.put(Preferences.AXES_FONT_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(fontColorLabel))));
        preferencesMap.put(Preferences.AXES_LINE_WIDTH, ((Double)axesLineWidthSpinner.getValue()).toString());
        preferencesMap.put(Preferences.AXES_SIZE, ((Double)axesSizeSpinner.getValue()).toString());
        Preferences.getInstance().put(preferencesMap);
    }//GEN-LAST:event_applyToAllButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void lightKitRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lightKitRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_lightKitRadioButtonActionPerformed

    private void headlightRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headlightRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_headlightRadioButtonActionPerformed

    private void fixedLightRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixedLightRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_fixedLightRadioButtonActionPerformed

    private void selectionColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectionColorButtonActionPerformed
        showColorChooser(selectionColorLabel);
    }//GEN-LAST:event_selectionColorButtonActionPerformed

    private void backgroundColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundColorButtonActionPerformed
        showColorChooser(backgroundColorLabel);
    }//GEN-LAST:event_backgroundColorButtonActionPerformed

    private void xAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xAxisColorButtonActionPerformed
        showColorChooser(xAxisColorLabel);
    }//GEN-LAST:event_xAxisColorButtonActionPerformed

    private void yAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yAxisColorButtonActionPerformed
        showColorChooser(yAxisColorLabel);
    }//GEN-LAST:event_yAxisColorButtonActionPerformed

    private void zAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zAxisColorButtonActionPerformed
        showColorChooser(zAxisColorLabel);
    }//GEN-LAST:event_zAxisColorButtonActionPerformed

    private void fontColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontColorButtonActionPerformed
        showColorChooser(fontColorLabel);
    }//GEN-LAST:event_fontColorButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyToAllButton;
    private javax.swing.JButton applyToCurrentButton;
    private javax.swing.JSpinner axesConeLengthSpinner;
    private javax.swing.JSpinner axesConeRadiusSpinner;
    private javax.swing.JSpinner axesFontSpinner;
    private javax.swing.JSpinner axesLineWidthSpinner;
    private javax.swing.JSpinner axesSizeSpinner;
    private javax.swing.JButton backgroundColorButton;
    private javax.swing.JLabel backgroundColorLabel;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel distanceLabel;
    private javax.swing.JFormattedTextField distanceTextField;
    private javax.swing.JRadioButton fixedLightRadioButton;
    private javax.swing.JButton fontColorButton;
    private javax.swing.JLabel fontColorLabel;
    private javax.swing.JRadioButton headlightRadioButton;
    private javax.swing.JLabel intensityLabel;
    private javax.swing.JSpinner intensitySpinner;
    private javax.swing.JCheckBox interactiveCheckBox;
    private javax.swing.ButtonGroup interactorStyleButtonGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JRadioButton joystickRadioButton;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JFormattedTextField latitudeTextField;
    private javax.swing.JRadioButton lightKitRadioButton;
    private javax.swing.ButtonGroup lightingButtonGroup;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JFormattedTextField longitudeTextField;
    private javax.swing.JSpinner mouseWheelMotionFactorSpinner;
    private javax.swing.JSlider pickToleranceSlider;
    private javax.swing.JButton selectionColorButton;
    private javax.swing.JLabel selectionColorLabel;
    private javax.swing.JCheckBox showAxesCheckBox;
    private javax.swing.JCheckBox showScaleBarCheckBox;
    private javax.swing.JRadioButton trackballRadioButton;
    private javax.swing.JButton xAxisColorButton;
    private javax.swing.JLabel xAxisColorLabel;
    private javax.swing.JButton yAxisColorButton;
    private javax.swing.JLabel yAxisColorLabel;
    private javax.swing.JButton zAxisColorButton;
    private javax.swing.JLabel zAxisColorLabel;
    // End of variables declaration//GEN-END:variables
}
