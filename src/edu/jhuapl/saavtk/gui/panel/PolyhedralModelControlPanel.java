package edu.jhuapl.saavtk.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomPlateDataDialog;
import edu.jhuapl.saavtk.gui.dialog.ScaleDataRangeDialog;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;

public class PolyhedralModelControlPanel extends JPanel implements ItemListener, ChangeListener
{
    private JCheckBox modelCheckBox;
    private ModelManager modelManager;

    private JRadioButton noColoringButton;
    private JRadioButton standardColoringButton;
    private JRadioButton rgbColoringButton;
    private ButtonGroup coloringButtonGroup;
    private JComboBox coloringComboBox;
    private JComboBox customColorRedComboBox;
    private JComboBox customColorGreenComboBox;
    private JComboBox customColorBlueComboBox;
    private JLabel customColorRedLabel;
    private JLabel customColorGreenLabel;
    private JLabel customColorBlueLabel;
    private List<JRadioButton> resModelButtons = new ArrayList<JRadioButton>();
    private ButtonGroup resolutionButtonGroup;
    private JCheckBox gridCheckBox;
    private JCheckBox axesCheckBox;
    private JCheckBox imageMapCheckBox;
    private JLabel opacityLabel;
    private JSpinner imageMapOpacitySpinner;

    public void setSaveColoringButton(JButton saveColoringButton)
    {
        this.saveColoringButton = saveColoringButton;
    }

    private JButton scaleColoringButton;
    private JButton saveColoringButton;
    private JButton customizeColoringButton;
    private JEditorPane statisticsLabel;
    private JScrollPane scrollPane;
    private JButton additionalStatisticsButton;

    private static final String NO_COLORING = "None";
    private static final String STANDARD_COLORING = "Standard";
    private static final String RGB_COLORING = "RGB";

    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public JCheckBox getModelCheckBox()
    {
        return modelCheckBox;
    }

    public JRadioButton getNoColoringButton()
    {
        return noColoringButton;
    }

    public JRadioButton getRgbColoringButton()
    {
        return rgbColoringButton;
    }

    public JCheckBox getGridCheckBox()
    {
        return gridCheckBox;
    }

    public JCheckBox getImageMapCheckBox()
    {
        return imageMapCheckBox;
    }

    public JSpinner getImageMapOpacitySpinner()
    {
        return imageMapOpacitySpinner;
    }

    public JButton getSaveColoringButton()
    {
        return saveColoringButton;
    }




    public JRadioButton getStandardColoringButton()
    {
        return standardColoringButton;
    }

    public JComboBox getColoringComboBox()
    {
        return coloringComboBox;
    }

    public JComboBox getCustomColorRedComboBox()
    {
        return customColorRedComboBox;
    }

    public JComboBox getCustomColorGreenComboBox()
    {
        return customColorGreenComboBox;
    }

    public JComboBox getCustomColorBlueComboBox()
    {
        return customColorBlueComboBox;
    }

    public JLabel getCustomColorRedLabel()
    {
        return customColorRedLabel;
    }

    public JLabel getCustomColorGreenLabel()
    {
        return customColorGreenLabel;
    }

    public JLabel getCustomColorBlueLabel()
    {
        return customColorBlueLabel;
    }

    public List<JRadioButton> getResModelButtons()
    {
        return resModelButtons;
    }

    public JLabel getOpacityLabel()
    {
        return opacityLabel;
    }

    public JButton getScaleColoringButton()
    {
        return scaleColoringButton;
    }

    public JButton getCustomizeColoringButton()
    {
        return customizeColoringButton;
    }

    public JEditorPane getStatisticsLabel()
    {
        return statisticsLabel;
    }

    public JScrollPane getScrollPane()
    {
        return scrollPane;
    }

    public JButton getAdditionalStatisticsButton()
    {
        return additionalStatisticsButton;
    }



    public PolyhedralModelControlPanel(ModelManager modelManager, String bodyName)
    {
        super(new BorderLayout());
        this.modelManager = modelManager;

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("wrap 1"));

        scrollPane = new JScrollPane();

        modelCheckBox = new JCheckBox();
        modelCheckBox.setText("Show " + bodyName);
        modelCheckBox.setSelected(true);
        modelCheckBox.addItemListener(this);

        JLabel resolutionLabel = new JLabel("Resolution");

        final PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        int numberResolutionLevels = smallBodyModel.getNumberResolutionLevels();
        String[] labels = smallBodyModel.getConfig().smallBodyLabelPerResolutionLevel;
        int[] plateCount = smallBodyModel.getConfig().smallBodyNumberOfPlatesPerResolutionLevel;

        if (numberResolutionLevels > 1)
        {
            resolutionButtonGroup = new ButtonGroup();

            for (int i=0; i < numberResolutionLevels; ++i)
            {
                JRadioButton resButton = new JRadioButton(labels[i]);
                if (i == 0)
                    resButton.setSelected(true);
                resButton.setActionCommand(labels[i]);
                resButton.addItemListener(this);
                resButton.setEnabled(true);
                resButton.setToolTipText(
                        "<html>Click here to show a model of " + bodyName + " <br />" +
                                "containing " + plateCount[i] + " plates</html>");
                resModelButtons.add(resButton);

                resolutionButtonGroup.add(resButton);
            }
        }

        // The following snippet was taken from https://explodingpixels.wordpress.com/2008/10/28/make-jeditorpane-use-the-system-font/
        // which shows how to make a JEditorPane behave look like a JLabel but still be selectable.
        statisticsLabel = new JEditorPane(new HTMLEditorKit().getContentType(), "");
        statisticsLabel.setBorder(null);
        statisticsLabel.setOpaque(false);
        statisticsLabel.setEditable(false);
        statisticsLabel.setForeground(UIManager.getColor("Label.foreground"));
        // add a CSS rule to force body tags to use the default label font
        // instead of the value in javax.swing.text.html.default.csss
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)statisticsLabel.getDocument()).getStyleSheet().addRule(bodyRule);

        JLabel coloringLabel = new JLabel();
        coloringLabel.setText("Plate Coloring");

        coloringComboBox = new JComboBox();
        coloringComboBox.addItemListener(this);

        noColoringButton = new JRadioButton(NO_COLORING);
        noColoringButton.setActionCommand(NO_COLORING);
        noColoringButton.addItemListener(this);
        noColoringButton.setEnabled(true);

        standardColoringButton = new JRadioButton(STANDARD_COLORING);
        standardColoringButton.setActionCommand(STANDARD_COLORING);
        standardColoringButton.addItemListener(this);
        standardColoringButton.setEnabled(smallBodyModel.getNumberOfColors() > 0);

        rgbColoringButton = new JRadioButton(RGB_COLORING);
        rgbColoringButton.setActionCommand(RGB_COLORING);
        rgbColoringButton.addItemListener(this);
        rgbColoringButton.setEnabled(smallBodyModel.getNumberOfColors() > 0);

        scaleColoringButton = new JButton("Rescale Data Range...");
        scaleColoringButton.setEnabled(false);
        scaleColoringButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ScaleDataRangeDialog scaleDataDialog = new ScaleDataRangeDialog(smallBodyModel);
                scaleDataDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(scaleColoringButton));
                scaleDataDialog.setVisible(true);
            }
        });

        customColorRedLabel = new JLabel("Red: ");
        customColorGreenLabel = new JLabel("Green: ");
        customColorBlueLabel = new JLabel("Blue: ");

        customColorRedComboBox = new JComboBox();
        customColorRedComboBox.addItemListener(this);
        customColorGreenComboBox = new JComboBox();
        customColorGreenComboBox.addItemListener(this);
        customColorBlueComboBox = new JComboBox();
        customColorBlueComboBox.addItemListener(this);

        customColorRedComboBox.setEnabled(false);
        customColorGreenComboBox.setEnabled(false);
        customColorBlueComboBox.setEnabled(false);
        customColorRedLabel.setEnabled(false);
        customColorGreenLabel.setEnabled(false);
        customColorBlueLabel.setEnabled(false);

        saveColoringButton = new JButton("Save Plate Data...");
        saveColoringButton.setEnabled(true);
        saveColoringButton.addActionListener(new SavePlateDataAction());

        customizeColoringButton = new JButton("Customize Plate Coloring...");
        if (smallBodyModel.getConfig().customTemporary)
            customizeColoringButton.setEnabled(false);
        else
            customizeColoringButton.setEnabled(true);
        customizeColoringButton.addActionListener(new CustomizePlateDataAction());

        coloringButtonGroup = new ButtonGroup();
        coloringButtonGroup.add(noColoringButton);
        coloringButtonGroup.add(standardColoringButton);
        coloringButtonGroup.add(rgbColoringButton);
        coloringButtonGroup.setSelected(noColoringButton.getModel(), true);

        setStatisticsLabel();
        updateColoringComboBoxes();

        gridCheckBox = new JCheckBox();
        gridCheckBox.setText("Show Coordinate Grid");
        gridCheckBox.setSelected(false);
        gridCheckBox.addItemListener(this);

        axesCheckBox = new JCheckBox();
        axesCheckBox.setText("Show Orientation Axes");
        axesCheckBox.setSelected(true);
        axesCheckBox.addItemListener(this);

        imageMapCheckBox = new JCheckBox();
        imageMapCheckBox.setText("Show Image Map");
        imageMapCheckBox.setSelected(false);
        imageMapCheckBox.addItemListener(this);

        opacityLabel = new JLabel("Opacity");
        imageMapOpacitySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
        imageMapOpacitySpinner.setEditor(new JSpinner.NumberEditor(imageMapOpacitySpinner, "0.00"));
        imageMapOpacitySpinner.setPreferredSize(new Dimension(80, 21));
        imageMapOpacitySpinner.addChangeListener(this);
        opacityLabel.setEnabled(false);
        imageMapOpacitySpinner.setEnabled(false);

        JSeparator statisticsSeparator = new JSeparator(SwingConstants.HORIZONTAL);

        JPanel surfacePropertiesEditorPanel = new DisplayPropertyEditorPanel(smallBodyModel);

        additionalStatisticsButton = new JButton("Show more statistics");
        additionalStatisticsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                additionalStatisticsButton.setVisible(false);
                addAdditionalStatisticsToLabel();
            }
        });

        panel.add(modelCheckBox, "wrap");
        if (smallBodyModel.getNumberResolutionLevels() > 1)
        {
            panel.add(resolutionLabel, "wrap");
            for (JRadioButton rb : resModelButtons)
                panel.add(rb, "wrap, gapleft 25");
        }

        // Only show coloring in APL version or if there are built in colors.
        // In the non-APL version, do not allow customization.
        if (Configuration.isAPLVersion() || smallBodyModel.getNumberOfBuiltInColors() > 0)
        {
            panel.add(coloringLabel, "wrap");
            panel.add(noColoringButton, "wrap, gapleft 25");
            panel.add(standardColoringButton, "split 2, gapleft 25");
            panel.add(coloringComboBox, "width 200!, wrap");
            panel.add(scaleColoringButton, "wrap, gapleft 75");
            if (Configuration.isAPLVersion())
            {
                panel.add(rgbColoringButton, "wrap, gapleft 25");
                panel.add(customColorRedLabel, "gapleft 75, split 2");
                panel.add(customColorRedComboBox, "width 200!, gapleft push, wrap");
                panel.add(customColorGreenLabel, "gapleft 75, split 2");
                panel.add(customColorGreenComboBox, "width 200!, gapleft push, wrap");
                panel.add(customColorBlueLabel, "gapleft 75, split 2");
                panel.add(customColorBlueComboBox, "width 200!, gapleft push, wrap");
            }
            panel.add(saveColoringButton, "wrap, gapleft 25");
            if (Configuration.isAPLVersion())
            {
                panel.add(customizeColoringButton, "wrap, gapleft 25");
            }
        }

        if (modelManager.getPolyhedralModel().isImageMapAvailable())
        {
            panel.add(imageMapCheckBox, "wrap");
            panel.add(opacityLabel, "gapleft 25, split 2");
            panel.add(imageMapOpacitySpinner, "wrap");
        }
        panel.add(gridCheckBox, "wrap");

        panel.add(surfacePropertiesEditorPanel, "wrap");

        panel.add(statisticsSeparator, "growx, span, wrap, gaptop 15");
        panel.add(statisticsLabel, "gaptop 15");
        panel.add(additionalStatisticsButton, "gaptop 15");

        scrollPane.setViewportView(panel);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void itemStateChanged(ItemEvent e)
    {
        Picker.setPickingEnabled(false);

        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        if (e.getItemSelectable() == this.modelCheckBox)
        {
            // In the following we ensure that the graticule and image map are shown
            // only if the shape model is shown
            Graticule graticule = (Graticule)modelManager.getModel(ModelNames.GRATICULE);
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                smallBodyModel.setShowSmallBody(true);
                if (graticule != null && gridCheckBox.isSelected())
                    graticule.setShowGraticule(true);
                if (imageMapCheckBox.isSelected())
                    showImageMap(true);
            }
            else
            {
                smallBodyModel.setShowSmallBody(false);
                if (graticule != null && gridCheckBox.isSelected())
                    graticule.setShowGraticule(false);
                if (imageMapCheckBox.isSelected())
                    showImageMap(false);
            }
        }
        else if (e.getItemSelectable() == this.gridCheckBox)
        {
            Graticule graticule = (Graticule)modelManager.getModel(ModelNames.GRATICULE);
            if (graticule != null)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    graticule.setShowGraticule(true);
                else
                    graticule.setShowGraticule(false);
            }
        }
        else if (e.getItemSelectable() == this.imageMapCheckBox)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                showImageMap(true);
                opacityLabel.setEnabled(true);
                imageMapOpacitySpinner.setEnabled(true);
            }
            else
            {
                showImageMap(false);
                opacityLabel.setEnabled(false);
                imageMapOpacitySpinner.setEnabled(false);
            }
        }
        else if (this.resModelButtons.contains(e.getItemSelectable()))
        {
            if (((AbstractButton)e.getItemSelectable()).isSelected())
                try {
                    int level = this.resModelButtons.indexOf(e.getItemSelectable());
                    smallBodyModel.setModelResolution(level);
                    setStatisticsLabel();
                    additionalStatisticsButton.setVisible(true);
                    updateColoringComboBoxes();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        else if (e.getItemSelectable() == this.noColoringButton)
        {
            updateColoringControls();

            try
            {
                smallBodyModel.setColoringIndex(-1);
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else if (e.getItemSelectable() == this.standardColoringButton)
        {
            updateColoringControls();
            setColoring();
        }
        else if (e.getItemSelectable() == this.rgbColoringButton)
        {
            updateColoringControls();
            setColoring();
        }
        else if (e.getItemSelectable() == this.coloringComboBox)
        {
            setColoring();
        }
        else if (e.getItemSelectable() == rgbColoringButton)
        {
            setColoring();
        }
        else if (e.getItemSelectable() == customColorRedComboBox ||
                e.getItemSelectable() == customColorGreenComboBox ||
                e.getItemSelectable() == customColorBlueComboBox)
        {
            setColoring();
        }

        Picker.setPickingEnabled(true);
    }

    protected void updateColoringControls()
    {
        boolean selected = standardColoringButton.isSelected();
        coloringComboBox.setEnabled(selected);
        scaleColoringButton.setEnabled(selected);
        selected = rgbColoringButton.isSelected();
        customColorRedComboBox.setEnabled(selected);
        customColorGreenComboBox.setEnabled(selected);
        customColorBlueComboBox.setEnabled(selected);
        customColorRedLabel.setEnabled(selected);
        customColorGreenLabel.setEnabled(selected);
        customColorBlueLabel.setEnabled(selected);
    }

    protected void setColoring()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        try
        {
            if (rgbColoringButton.isSelected())
            {
                smallBodyModel.setFalseColoring(
                        customColorRedComboBox.getSelectedIndex(),
                        customColorGreenComboBox.getSelectedIndex(),
                        customColorBlueComboBox.getSelectedIndex());
            }
            else if (standardColoringButton.isSelected())
            {
                smallBodyModel.setColoringIndex(coloringComboBox.getSelectedIndex());
            }
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }


    protected void updateColoringComboBoxes()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        coloringComboBox.removeItemListener(this);
        customColorRedComboBox.removeItemListener(this);
        customColorGreenComboBox.removeItemListener(this);
        customColorBlueComboBox.removeItemListener(this);

        coloringComboBox.removeAllItems();
        customColorRedComboBox.removeAllItems();
        customColorGreenComboBox.removeAllItems();
        customColorBlueComboBox.removeAllItems();

        for (int i=0; i<smallBodyModel.getNumberOfColors(); ++i)
        {
            coloringComboBox.addItem(smallBodyModel.getColoringName(i));
            customColorRedComboBox.addItem(smallBodyModel.getColoringName(i));
            customColorGreenComboBox.addItem(smallBodyModel.getColoringName(i));
            customColorBlueComboBox.addItem(smallBodyModel.getColoringName(i));
        }

        // Add Ancillary selections here... -turnerj1

        if (smallBodyModel.getColoringIndex() < 0 && !smallBodyModel.isFalseColoringEnabled())
        {
            noColoringButton.setSelected(true);
        }
        else if (smallBodyModel.getNumberOfColors() > 0)
        {
            int index = smallBodyModel.getColoringIndex();
            coloringComboBox.setSelectedIndex(Math.max(index, 0));
            int[] falseColoring = smallBodyModel.getFalseColoring();
            customColorRedComboBox.setSelectedIndex(Math.max(falseColoring[0], 0));
            customColorGreenComboBox.setSelectedIndex(Math.max(falseColoring[1], 0));
            customColorBlueComboBox.setSelectedIndex(Math.max(falseColoring[2], 0));
        }

        coloringComboBox.addItemListener(this);
        customColorRedComboBox.addItemListener(this);
        customColorGreenComboBox.addItemListener(this);
        customColorBlueComboBox.addItemListener(this);
    }

    protected void setStatisticsLabel()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        BoundingBox bb = smallBodyModel.getBoundingBox();

        // We add a superscripted space at end of first 2 lines and last 6 lines so that spacing between all lines is the same.
        String text = "<html>Statistics:<br>"
                + "&nbsp;&nbsp;&nbsp;Number of Plates: " + smallBodyModel.getSmallBodyPolyData().GetNumberOfCells() + "<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Number of Vertices: " + smallBodyModel.getSmallBodyPolyData().GetNumberOfPoints() + "<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Surface Area: " + String.format("%.7g", smallBodyModel.getSurfaceArea()) + " km<sup>2</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Volume: " + String.format("%.7g", smallBodyModel.getVolume()) + " km<sup>3</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Plate Area Average: " + String.format("%.7g", 1.0e6 * smallBodyModel.getMeanCellArea()) + " m<sup>2</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Plate Area Minimum: " + String.format("%.7g", 1.0e6 * smallBodyModel.getMinCellArea()) + " m<sup>2</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Plate Area Maximum: " + String.format("%.7g", 1.0e6 * smallBodyModel.getMaxCellArea()) + " m<sup>2</sup><br>"
                + "&nbsp;&nbsp;&nbsp;Extent:<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;X: [" + String.format("%.7g, %.7g", bb.xmin, bb.xmax) + "] km<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Y: [" + String.format("%.7g, %.7g", bb.ymin, bb.ymax) + "] km<sup>&nbsp;</sup><br>"
                + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Z: [" + String.format("%.7g, %.7g", bb.zmin, bb.zmax) + "] km<sup>&nbsp;</sup><br>";

        // There's some weird thing going one where changing the text of the label causes
        // the scoll bar of the panel to scroll all the way down. Therefore, reset it to
        // the original value after changing the text.
        // TODO not sure if this is the best solution since there is still a slight
        // flicker occasionally when you start up the tool, probably due to the change
        // in the scroll bar position.
        final int originalScrollBarValue = scrollPane.getVerticalScrollBar().getValue();

        statisticsLabel.setText(text);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                scrollPane.getVerticalScrollBar().setValue(originalScrollBarValue);
            }
        });
    }

    //
    // for subclasses that support images
    //
    protected void showImageMap(boolean show) {}

    protected void addAdditionalStatisticsToLabel() {}

    public void stateChanged(ChangeEvent e) {}

    protected CustomPlateDataDialog getPlateDataDialog(ModelManager modelManager)
    {
        return new CustomPlateDataDialog(modelManager);
    }

    private class CustomizePlateDataAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            CustomPlateDataDialog dialog = getPlateDataDialog(modelManager);
            dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(PolyhedralModelControlPanel.this));
            dialog.setVisible(true);

            PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
            standardColoringButton.setEnabled(smallBodyModel.getNumberOfColors() > 0);
            rgbColoringButton.setEnabled(smallBodyModel.getNumberOfColors() > 0);

            if (smallBodyModel.getNumberOfColors() == 0)
                noColoringButton.setSelected(true);

            updateColoringComboBoxes();
        }
    }

    private class SavePlateDataAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent actionEvent)
        {
            PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
            Frame invoker = JOptionPane.getFrameForComponent(PolyhedralModelControlPanel.this);
            String name = "platedata.csv";
            File file = CustomFileChooser.showSaveDialog(invoker, "Export Plate Data", name);

            try
            {
                if (file != null)
                    smallBodyModel.savePlateData(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(invoker,
                        "An error occurred exporting the plate data.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
