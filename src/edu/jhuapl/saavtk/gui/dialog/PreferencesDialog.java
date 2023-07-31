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
import java.awt.GridBagConstraints;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.axes.AxesPanel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.ColorIcon;
import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesDialog extends javax.swing.JDialog
{

    private ViewManager viewManager;
    private static final double MAX_TOLERANCE = 0.01;
    private String displayedSection;
    private PreferencesSectionPickTolerance pickTolStore;
    private PreferencesSectionDefColorMap defColorMapStore;
    private PreferencesSectionSelectAndBGColors selectandBGColorStore;
    private PreferencesSectionProxy proxyStore;

    /** Creates new form SettingsDialog */
    public PreferencesDialog(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        setTitle("Preferences");
    }

    public void setViewManager(ViewManager viewManager)
    {
        this.viewManager = viewManager;
    }

    @Override
    public void setVisible(boolean b)
    {
        if (b)
        {
//            showAxesCheckBox.setSelected(renderer.getShowOrientationAxes());
//            interactiveCheckBox.setSelected(renderer.getOrientationAxesInteractive());

            /*
             * if (renderer.getDefaultInteractorStyleType() ==
             * Renderer.InteractorStyleType.JOYSTICK_CAMERA)
             * joystickRadioButton.setSelected(true); else
             * trackballRadioButton.setSelected(true);
             */

            joystickRadioButton.setEnabled(false);
            trackballRadioButton.setEnabled(false);

            PickManager pickManager = viewManager.getCurrentView().getPickManager();
            int value = getSliderValueFromTolerance(pickManager.getPickTolerance());
            pickToleranceSlider.setValue(value);

//            mouseWheelMotionFactorSpinner.setValue(renderer.getMouseWheelMotionFactor());

            Color color = viewManager.getCurrentView().getModelManager().getCommonData().getSelectionColor();
            updateColorLabel(color, selectionColorLabel);

            int[] rgbArr = viewManager.getCurrentView().getRenderer().getBackgroundColor();
            updateColorLabel(rgbArr, backgroundColorLabel);

            RenderPanel renderPanel = viewManager.getCurrentView().getRenderer().getRenderWindowPanel();
            AxesPanel axesPanel = renderPanel.getAxesPanel();

            defaultColorMapSelection.setSelectedItem(Colormaps.getCurrentColormapName());
            
            proxyHostTextField.setText(proxyStore.getProperties().get(Preferences.PROXY_HOST));
            proxyPortTextField.setText(proxyStore.getProperties().get(Preferences.PROXY_PORT));
            proxyEnableCheckBox.setSelected(Boolean.parseBoolean(proxyStore.getProperties().get(Preferences.PROXY_ENABLED)));

            /*
             * updateColorLabel(axesPanel.getxColor(), xAxisColorLabel);
             * updateColorLabel(axesPanel.getyColor(), yAxisColorLabel);
             * updateColorLabel(axesPanel.getzColor(), zAxisColorLabel);
             *
             * updateColorLabel(axesPanel.getFontColor(), fontColorLabel);
             *
             * axesSizeSpinner.setValue(axesPanel.getShaftlength());
             * axesLineWidthSpinner.setValue(axesPanel.getLinewidth());
             * axesFontSpinner.setValue(axesPanel.getFontsize());
             * axesConeLengthSpinner.setValue(axesPanel.getConelength());
             * axesConeRadiusSpinner.setValue(axesPanel.getConeradius());
             */
        }

        super.setVisible(b);
    }

    private void applyToView(View v)
    {
        Renderer renderer = v.getRenderer();
        if (renderer != null)
        {
//            renderer.setShowOrientationAxes(showAxesCheckBox.isSelected());
//            renderer.setOrientationAxesInteractive(interactiveCheckBox.isSelected());

            /*
             * if (joystickRadioButton.isSelected())
             * renderer.setDefaultInteractorStyleType(InteractorStyleType.JOYSTICK_CAMERA);
             * else
             * renderer.setDefaultInteractorStyleType(InteractorStyleType.TRACKBALL_CAMERA);
             */

            PickManager pickManager = v.getPickManager();
            double tolerance = getToleranceFromSliderValue(pickToleranceSlider.getValue());
            pickManager.setPickTolerance(tolerance);

//            renderer.setMouseWheelMotionFactor((Double)mouseWheelMotionFactorSpinner.getValue());

            Color color = getColorInstanceFromLabel(selectionColorLabel);
            v.getModelManager().getCommonData().setSelectionColor(color);

            int[] rgbArr = getColorFromLabel(backgroundColorLabel);
            renderer.setBackgroundColor(rgbArr);

            RenderPanel renderPanel = v.getRenderer().getRenderWindowPanel();
            AxesPanel axesPanel = renderPanel.getAxesPanel();
            axesPanel.getRenderer().SetBackground(rgbArr[0] / 255.0, rgbArr[1] / 255.0, rgbArr[2] / 255.0);
            axesPanel.Render();

            /*
             * axesPanel.setxColor(getColorInstanceFromLabel(xAxisColorLabel));
             * axesPanel.setyColor(getColorInstanceFromLabel(yAxisColorLabel));
             * axesPanel.setzColor(getColorInstanceFromLabel(zAxisColorLabel));
             * axesPanel.setFontColor(getColorInstanceFromLabel(fontColorLabel));
             * axesPanel.setConelength((Double)axesConeLengthSpinner.getValue());
             * axesPanel.setConeradius((Double)axesConeRadiusSpinner.getValue());
             * axesPanel.setFontsize((Integer)axesFontSpinner.getValue());
             * axesPanel.setLinewidth((Double)axesLineWidthSpinner.getValue());
             * axesPanel.setShaftlength((Double)axesSizeSpinner.getValue());
             */
        }
    }

    private double getToleranceFromSliderValue(int value)
    {
        return MAX_TOLERANCE * value / pickToleranceSlider.getMaximum();
    }

    private int getSliderValueFromTolerance(double tolerance)
    {
        return (int) (pickToleranceSlider.getMaximum()
                * tolerance / MAX_TOLERANCE);
    }

    private void updateColorLabel(int[] color, JLabel label)
    {
        int[] c = color;
        label.setText("[" + c[0] + "," + c[1] + "," + c[2] + "]");
        label.setIcon(new ColorIcon(new Color(c[0], c[1], c[2])));
    }

    private void updateColorLabel(Color color, JLabel label)
    {
        int[] c = new int[] { color.getRed(), color.getGreen(), color.getBlue() };
        updateColorLabel(c, label);
    }

    private void showColorChooser(JLabel label)
    {
        int[] initialColor = getColorFromLabel(label);
        Color color = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(this), initialColor);

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
        Color color = ((ColorIcon) label.getIcon()).getColor();
        int[] c = new int[3];
        c[0] = color.getRed();
        c[1] = color.getGreen();
        c[2] = color.getBlue();
        return c;
    }

    private Color getColorInstanceFromLabel(JLabel label)
    {
        return ((ColorIcon) label.getIcon()).getColor();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;
        
        displayedSection = "Pick Tolerance";
        pickTolStore = PreferencesSectionPickTolerance.getInstance();
        defColorMapStore = PreferencesSectionDefColorMap.getInstance();
        selectandBGColorStore = PreferencesSectionSelectAndBGColors.getInstance();
        proxyStore = PreferencesSectionProxy.getInstance();

        interactorStyleButtonGroup = new ButtonGroup();
        jScrollPane1 = new JScrollPane();
        mainPanel = new JPanel();
        sectionPanel = new JPanel();
//        showAxesCheckBox = new JCheckBox();
//        interactiveCheckBox = new JCheckBox();
        buttonsPanel = new JPanel();
        applyToCurrentButton = new JButton();
        applyToAllButton = new JButton();
        closeButton = new JButton();
//        jPanel2 = new JPanel();
        jLabel1 = new JLabel();
        jSeparator1 = new JSeparator();
//        jPanel3 = new JPanel();
        jLabel3 = new JLabel();
        jSeparator2 = new JSeparator();
//        jPanel5 = new JPanel();
        jLabel4 = new JLabel();
        jSeparator4 = new JSeparator();
        trackballRadioButton = new JRadioButton();
        joystickRadioButton = new JRadioButton();
        sliderTitlePanel = new JPanel();
        jSeparator5 = new JSeparator();
        jLabel5 = new JLabel();
        sliderPanel = new JPanel();
        pickToleranceSlider = new JSlider();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        selectionColorTitlePanel = new JPanel();
        jSeparator6 = new JSeparator();
        jLabel9 = new JLabel();
        jLabel8 = new JLabel();
//        mouseWheelMotionFactorSpinner = new JSpinner();
        defaultColorMapLabel = new JLabel();
        defaultColorMapSelection = new JComboBox<>();
        selectionColorLabel = new JLabel();
        selectionColorButton = new JButton();
        jPanel10 = new JPanel();
        jSeparator9 = new JSeparator();
        jLabel12 = new JLabel();
        bgColorPanel = new JPanel();
        jLabel10 = new JLabel();
        jSeparator7 = new JSeparator();
        backgroundColorLabel = new JLabel();
        backgroundColorButton = new JButton();
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
        fontColorLabel = new JLabel();
        fontColorButton = new JButton();
        sideListPanel = new JPanel();
        proxyTitlePanel = new JPanel();
        proxyHostPanel = new JPanel();
        proxyPortPanel = new JPanel();
        jLabel23 = new JLabel();
        jSeparator10 = new JSeparator();
        jLabel24 = new JLabel();
        jLabel25 = new JLabel();
        proxyHostTextField = new JTextField(30);
        proxyPortTextField = new JTextField(30);
        proxyEnableCheckBox = new JCheckBox();
        sectionList = new JList<String>(prefSectionNames);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        sectionPanel.setLayout(new java.awt.GridBagLayout());

//        showAxesCheckBox.setText("Show Axes");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 9;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
//        jPanel11.add(showAxesCheckBox, gridBagConstraints);
//
//        interactiveCheckBox.setText("Interactive");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 10;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
//        jPanel11.add(interactiveCheckBox, gridBagConstraints);

        sideListPanel.setLayout(new java.awt.GridBagLayout());
        
     // Adds preference section labels to left side menu
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        sectionList.setSelectedIndex(0);
        sectionList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				displayedSection = sectionList.getSelectedValue();
    			if (!displayedSection.equals("Pick Tolerance")) {
    	        	sliderTitlePanel.setVisible(false);
    	        	sliderPanel.setVisible(false);
    	        } else {
    	        	sliderTitlePanel.setVisible(true);
    	        	sliderPanel.setVisible(true);
    	        }
				
				if (!displayedSection.equals("Default Color Map")) {
					defaultColorMapLabel.setVisible(false);
					defaultColorMapSelection.setVisible(false);
				} else {
					defaultColorMapLabel.setVisible(true);
					defaultColorMapSelection.setVisible(true);
				}
				
				if (!displayedSection.equals("Selection and Background Colors")) {
					selectionColorTitlePanel.setVisible(false);
					selectionColorLabel.setVisible(false);
					selectionColorButton.setVisible(false);
					bgColorPanel.setVisible(false);
					backgroundColorLabel.setVisible(false);
					backgroundColorButton.setVisible(false);
				} else {
					selectionColorTitlePanel.setVisible(true);
					selectionColorLabel.setVisible(true);
					selectionColorButton.setVisible(true);
					bgColorPanel.setVisible(true);
					backgroundColorLabel.setVisible(true);
					backgroundColorButton.setVisible(true);
				}
				
				if (!displayedSection.equals("Configure Proxy")) {
					proxyTitlePanel.setVisible(false);
					proxyHostPanel.setVisible(false);
					proxyPortPanel.setVisible(false);
					proxyEnableCheckBox.setVisible(false);
					applyToAllButton.setVisible(true);
					applyToCurrentButton.setVisible(true);
				} else {
					proxyTitlePanel.setVisible(true);
					proxyHostPanel.setVisible(true);
					proxyPortPanel.setVisible(true);
					proxyEnableCheckBox.setVisible(true);
					applyToAllButton.setVisible(false);
					applyToCurrentButton.setVisible(false);
				}
			}
		});
        sideListPanel.add(sectionList, gridBagConstraints);
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(sideListPanel, gridBagConstraints);
        
        // Beginning of buttons section at bottom of preference changes section panel
        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        applyToCurrentButton.setText("Apply to Current View");
        applyToCurrentButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                applyToCurrentButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        buttonsPanel.add(applyToCurrentButton, gridBagConstraints);

        applyToAllButton.setText("Apply to All Views");
        applyToAllButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                applyToAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        buttonsPanel.add(applyToAllButton, gridBagConstraints);
        
        proxyEnableCheckBox.setText("Enable");
        proxyEnableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
            	if (proxyEnableCheckBox.isSelected()) {
            		applyToAllButtonActionPerformed(evt);
            	} else {
            		proxyStore.turnOffProxy();
            	}
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        buttonsPanel.add(proxyEnableCheckBox, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                closeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        buttonsPanel.add(closeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 39;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        sectionPanel.add(buttonsPanel, gridBagConstraints);

//        jPanel2.setLayout(new java.awt.GridBagLayout());

//        jPanel3.setLayout(new java.awt.GridBagLayout());
//
//        jLabel3.setText("Orientation Axes");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
//        jPanel3.add(jLabel3, gridBagConstraints);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.weightx = 1.0;
//        jPanel3.add(jSeparator2, gridBagConstraints);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 8;
//        gridBagConstraints.gridwidth = 4;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
//        jPanel11.add(jPanel3, gridBagConstraints);

//        jPanel5.setLayout(new java.awt.GridBagLayout());

//        jLabel4.setText("Interactor Style");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
//        jPanel5.add(jLabel4, gridBagConstraints);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.weightx = 1.0;
//        jPanel5.add(jSeparator4, gridBagConstraints);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 22;
//        gridBagConstraints.gridwidth = 4;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
//        jPanel11.add(jPanel5, gridBagConstraints);

//        interactorStyleButtonGroup.add(trackballRadioButton);
//        trackballRadioButton.setText("Trackball");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 23;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
//        jPanel11.add(trackballRadioButton, gridBagConstraints);
//
//        interactorStyleButtonGroup.add(joystickRadioButton);
//        joystickRadioButton.setText("Joystick");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 24;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
//        jPanel11.add(joystickRadioButton, gridBagConstraints);

     // Start of pick tolerance
        jLabel5.setText("Pick Tolerance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        sliderTitlePanel.add(jLabel5, gridBagConstraints);
        
        sliderTitlePanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        sliderTitlePanel.add(jSeparator5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        sectionPanel.add(sliderTitlePanel, gridBagConstraints);

        sliderPanel.setLayout(new java.awt.GridBagLayout());

        pickToleranceSlider.setMaximum(1000);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        sliderPanel.add(pickToleranceSlider, gridBagConstraints);

        jLabel6.setText("Most Sensitive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        sliderPanel.add(jLabel6, gridBagConstraints);

        jLabel7.setText("Least Sensitive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        sliderPanel.add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        sectionPanel.add(sliderPanel, gridBagConstraints);
        // End of pick tolerance
        
        // Start of default color map name
        defaultColorMapLabel.setText("Default Color Map Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 31;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 0, 10);
        sectionPanel.add(defaultColorMapLabel, gridBagConstraints);

        for (String colorMapName : Colormaps.getAllBuiltInColormapNames())
        {
            defaultColorMapSelection.addItem(colorMapName);
        }
        defaultColorMapSelection.setSelectedItem(Colormaps.getCurrentColormapName());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 31;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        sectionPanel.add(defaultColorMapSelection, gridBagConstraints);
        // End of default color map name

        // Start of selection color
        selectionColorTitlePanel.setLayout(new java.awt.GridBagLayout());
        jLabel9.setText("Selection Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        selectionColorTitlePanel.add(jLabel9, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        selectionColorTitlePanel.add(jSeparator6, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        sectionPanel.add(selectionColorTitlePanel, gridBagConstraints);
        
        selectionColorLabel.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 33;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        sectionPanel.add(selectionColorLabel, gridBagConstraints);

        selectionColorButton.setText("Change...");
        selectionColorButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                selectionColorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 33;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        sectionPanel.add(selectionColorButton, gridBagConstraints);
        // End of selection color

//        jLabel8.setText("Motion Factor");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 28;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
//        jPanel11.add(jLabel8, gridBagConstraints);

//        mouseWheelMotionFactorSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), null, null, Double.valueOf(0.1d)));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 28;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.ipadx = 50;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
//        jPanel11.add(mouseWheelMotionFactorSpinner, gridBagConstraints);

//        jPanel10.setLayout(new java.awt.GridBagLayout());
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.weightx = 1.0;
//        jPanel10.add(jSeparator9, gridBagConstraints);

//        jLabel12.setText("Mouse Wheel");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        jPanel10.add(jLabel12, gridBagConstraints);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 27;
//        gridBagConstraints.gridwidth = 4;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
//        sectionPanel.add(jPanel10, gridBagConstraints);

        // Start of background color
        bgColorPanel.setLayout(new java.awt.GridBagLayout());

        jLabel10.setText("Background Color");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        bgColorPanel.add(jLabel10, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        bgColorPanel.add(jSeparator7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 34;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 4, 5, 0);
        sectionPanel.add(bgColorPanel, gridBagConstraints);

        backgroundColorLabel.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 35;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        sectionPanel.add(backgroundColorLabel, gridBagConstraints);

        backgroundColorButton.setText("Change...");
        backgroundColorButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                backgroundColorButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 35;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        sectionPanel.add(backgroundColorButton, gridBagConstraints);
        // End of background color
        
        // Start of configure proxy      
        proxyTitlePanel.setLayout(new java.awt.GridBagLayout());
        
        jLabel23.setText("Configure Proxy");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        proxyTitlePanel.add(jLabel23, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        proxyTitlePanel.add(jSeparator10, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 36;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 5, 0);
        sectionPanel.add(proxyTitlePanel, gridBagConstraints);
        
        proxyHostPanel.setLayout(new java.awt.GridBagLayout());
        
        jLabel24.setText("http.proxyHost");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        proxyHostPanel.add(jLabel24, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        proxyHostPanel.add(proxyHostTextField);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 37;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        sectionPanel.add(proxyHostPanel, gridBagConstraints);
        
        jLabel25.setText("http.proxyPort");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        proxyPortPanel.add(jLabel25);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        proxyPortPanel.add(proxyPortTextField);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 38;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        sectionPanel.add(proxyPortPanel, gridBagConstraints);
        // End of configure proxy

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
        
        // Adds the section panel to the full-dialog main panel
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        mainPanel.add(sectionPanel, gridBagConstraints);
        
        jScrollPane1.setViewportView(mainPanel);
        getContentPane().add(jScrollPane1);

        pack();
        
		if (!displayedSection.equals("Pick Tolerance")) {
        	sliderTitlePanel.setVisible(false);
        	sliderPanel.setVisible(false);
        }
		if (!displayedSection.equals("Default Color Map")) {
			defaultColorMapLabel.setVisible(false);
			defaultColorMapSelection.setVisible(false);
		}
		if (!displayedSection.equals("Selection and Background Colors")) {
			selectionColorTitlePanel.setVisible(false);
			selectionColorLabel.setVisible(false);
			selectionColorButton.setVisible(false);
			bgColorPanel.setVisible(false);
			backgroundColorLabel.setVisible(false);
			backgroundColorButton.setVisible(false);
		}
		if (!displayedSection.equals("Configure Proxy")) {
			proxyTitlePanel.setVisible(false);
			proxyHostPanel.setVisible(false);
			proxyPortPanel.setVisible(false);
			proxyEnableCheckBox.setVisible(false);
		}
    }// </editor-fold>//GEN-END:initComponents

    private void applyToCurrentButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_applyToCurrentButtonActionPerformed
        applyToView(viewManager.getCurrentView());
    }// GEN-LAST:event_applyToCurrentButtonActionPerformed

    private void applyToAllButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_applyToAllButtonActionPerformed

        // View-independent preferences.
        Object colormapSelection = defaultColorMapSelection.getSelectedItem();

        // Ensure colormapSelection is a (possibly null) string:
        if (colormapSelection != null)
        {
            colormapSelection = colormapSelection.toString();
        }

        Colormaps.setCurrentColormapName((String) colormapSelection);

        List<View> views = viewManager.getAllViews();
        for (View v : views)
        {
            applyToView(v);
        }

        // In addition, save in preferences file for future use
        LinkedHashMap<String, String> preferencesMap = new LinkedHashMap<String, String>();

//        if (joystickRadioButton.isSelected())
        // preferencesMap.put(Preferences.INTERACTOR_STYLE_TYPE,
        // InteractorStyleType.JOYSTICK_CAMERA.toString());
        // else
        // preferencesMap.put(Preferences.INTERACTOR_STYLE_TYPE,
        // InteractorStyleType.TRACKBALL_CAMERA.toString());

//        preferencesMap.put(Preferences.SHOW_AXES, ((Boolean)showAxesCheckBox.isSelected()).toString());
//        preferencesMap.put(Preferences.INTERACTIVE_AXES, ((Boolean)interactiveCheckBox.isSelected()).toString());
        preferencesMap.put(Preferences.PICK_TOLERANCE, Double.valueOf(getToleranceFromSliderValue(pickToleranceSlider.getValue())).toString());
//        preferencesMap.put(Preferences.MOUSE_WHEEL_MOTION_FACTOR, ((Double)mouseWheelMotionFactorSpinner.getValue()).toString());
        preferencesMap.put(Preferences.DEFAULT_COLORMAP_NAME, (String) colormapSelection);
        preferencesMap.put(Preferences.SELECTION_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(selectionColorLabel))));
        preferencesMap.put(Preferences.BACKGROUND_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(backgroundColorLabel))));
//        preferencesMap.put(Preferences.AXES_XAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(xAxisColorLabel))));
//        preferencesMap.put(Preferences.AXES_YAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(yAxisColorLabel))));
//        preferencesMap.put(Preferences.AXES_ZAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(zAxisColorLabel))));
//        preferencesMap.put(Preferences.AXES_CONE_LENGTH, ((Integer)axesConeLengthSpinner.getValue()).toString());
//        preferencesMap.put(Preferences.AXES_CONE_RADIUS, ((Integer)axesConeRadiusSpinner.getValue()).toString());
//        preferencesMap.put(Preferences.AXES_FONT_SIZE, ((Integer)axesFontSpinner.getValue()).toString());
//        preferencesMap.put(Preferences.AXES_FONT_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(fontColorLabel))));
//        preferencesMap.put(Preferences.AXES_LINE_WIDTH, ((Integer)axesLineWidthSpinner.getValue()).toString());
//        preferencesMap.put(Preferences.AXES_SIZE, ((Integer)axesSizeSpinner.getValue()).toString());
        Preferences.getInstance().put(preferencesMap);
    }// GEN-LAST:event_applyToAllButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }// GEN-LAST:event_closeButtonActionPerformed

    private void selectionColorButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_selectionColorButtonActionPerformed
        showColorChooser(selectionColorLabel);
    }// GEN-LAST:event_selectionColorButtonActionPerformed

    private void backgroundColorButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_backgroundColorButtonActionPerformed
        showColorChooser(backgroundColorLabel);
    }// GEN-LAST:event_backgroundColorButtonActionPerformed

//    private void xAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xAxisColorButtonActionPerformed
//        showColorChooser(xAxisColorLabel);
//    }//GEN-LAST:event_xAxisColorButtonActionPerformed
//
//    private void yAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yAxisColorButtonActionPerformed
//        showColorChooser(yAxisColorLabel);
//    }//GEN-LAST:event_yAxisColorButtonActionPerformed
//
//    private void zAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zAxisColorButtonActionPerformed
//        showColorChooser(zAxisColorLabel);
//    }//GEN-LAST:event_zAxisColorButtonActionPerformed

    private void fontColorButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_fontColorButtonActionPerformed
        showColorChooser(fontColorLabel);
    }// GEN-LAST:event_fontColorButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton applyToAllButton;
    private JButton applyToCurrentButton;
    private JSpinner axesConeLengthSpinner;
    private JSpinner axesConeRadiusSpinner;
    private JSpinner axesFontSpinner;
    private JSpinner axesLineWidthSpinner;
    private JSpinner axesSizeSpinner;
    private JButton backgroundColorButton;
    private JLabel backgroundColorLabel;
    private JButton closeButton;
    private JButton fontColorButton;
    private JLabel fontColorLabel;
//    private JCheckBox interactiveCheckBox;
    private ButtonGroup interactorStyleButtonGroup;
    private JLabel jLabel1;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel15;
    private JLabel jLabel16;
    private JLabel jLabel17;
    private JLabel jLabel18;
    private JLabel jLabel19;
    private JLabel jLabel20;
    private JLabel jLabel21;
    private JLabel jLabel22;
    private JLabel jLabel23;
    private JLabel jLabel24;
    private JLabel jLabel25;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JPanel buttonsPanel;
    private JPanel jPanel10;
    private JPanel mainPanel;
    private JPanel sectionPanel;
    private JPanel sideListPanel;
//    private JPanel jPanel2;
//    private JPanel jPanel3;
//    private JPanel jPanel5;
    private JPanel sliderTitlePanel;
    private JPanel sliderPanel;
    private JPanel bgColorPanel;
    private JPanel selectionColorTitlePanel;
    private JScrollPane jScrollPane1;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JSeparator jSeparator4;
    private JSeparator jSeparator5;
    private JSeparator jSeparator6;
    private JSeparator jSeparator7;
    private JSeparator jSeparator9;
    private JSeparator jSeparator10;
    private JRadioButton joystickRadioButton;
//    private JSpinner mouseWheelMotionFactorSpinner;
    private JSlider pickToleranceSlider;
    private JLabel defaultColorMapLabel;
    private JComboBox<String> defaultColorMapSelection;
    private JButton selectionColorButton;
    private JLabel selectionColorLabel;
//    private JCheckBox showAxesCheckBox;
    private JRadioButton trackballRadioButton;
//    private JButton xAxisColorButton;
//    private JLabel xAxisColorLabel;
//    private JButton yAxisColorButton;
//    private JLabel yAxisColorLabel;
//    private JButton zAxisColorButton;
//    private JLabel zAxisColorLabel;
    private JPanel proxyTitlePanel;
    private JPanel proxyHostPanel;
    private JPanel proxyPortPanel;
    private JTextField proxyHostTextField;
    private JTextField proxyPortTextField;
    private JCheckBox proxyEnableCheckBox;
	private String[] prefSectionNames = { "Pick Tolerance", "Default Color Map", "Selection and Background Colors",
			"Configure Proxy" };
	private JList<String> sectionList;
    // End of variables declaration//GEN-END:variables
}
