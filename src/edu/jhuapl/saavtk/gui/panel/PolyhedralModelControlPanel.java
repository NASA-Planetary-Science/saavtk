package edu.jhuapl.saavtk.gui.panel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.colormap.ContourPanel;
import edu.jhuapl.saavtk.colormap.StandardPlatePanel;
import edu.jhuapl.saavtk.gui.MetadataDisplay;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomPlateDataDialog;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.DownloadableFileManager.StateListener;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileStateListenerTracker;
import edu.jhuapl.saavtk.util.Properties;
import net.miginfocom.swing.MigLayout;

public class PolyhedralModelControlPanel extends JPanel implements ItemListener, ChangeListener
{
    public static PolyhedralModelControlPanel of(ModelManager modelManager, String bodyName)
    {
        PolyhedralModelControlPanel result = new PolyhedralModelControlPanel(modelManager, bodyName) {
            private static final long serialVersionUID = 256201608369977510L;
        };

        result.initialize();

        return result;
    }

    private static final long serialVersionUID = 7858613374590442069L;
    private static final String NO_COLORING = "None";
    private static final String STANDARD_COLORING = "Standard";
    private static final String RGB_COLORING = "RGB";

    private final ModelManager modelManager;
    private final String bodyName;

    protected final JCheckBox modelCheckBox;
    private final JRadioButton standardColoringButton;
    private final JRadioButton rgbColoringButton;
    private final JComboBoxWithItemState<String> coloringComboBox;
    private final JButton showColoringProperties;
    private final JComboBoxWithItemState<String> customColorRedComboBox;
    private final JComboBoxWithItemState<String> customColorGreenComboBox;
    private final JComboBoxWithItemState<String> customColorBlueComboBox;
    private final JLabel customColorRedLabel;
    private final JLabel customColorGreenLabel;
    private final JLabel customColorBlueLabel;
    private final JCheckBox gridCheckBox;
    protected final JCheckBox gridLabelCheckBox;

    private final ContourPanel contourPanel = new ContourPanel();
    private final StandardPlatePanel colormapController = new StandardPlatePanel(contourPanel);

    private final JButton saveColoringButton;
    private final JButton customizeColoringButton;
    private final JEditorPane statisticsLabel;
    private final JScrollPane scrollPane;
    private final JButton additionalStatisticsButton;
    private ImmutableMap<String, Integer> resolutionLevels;

    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public JEditorPane getStatisticsLabel()
    {
        return statisticsLabel;
    }

    public JScrollPane getScrollPane()
    {
        return scrollPane;
    }

    protected PolyhedralModelControlPanel(ModelManager modelManager, String bodyName)
    {
        super(new BorderLayout());
        this.modelManager = modelManager;
        this.bodyName = bodyName;

        scrollPane = new JScrollPane();

        modelCheckBox = new JCheckBox();

        coloringComboBox = new JComboBoxWithItemState<>();

        showColoringProperties = new JButton("Properties");

        standardColoringButton = new JRadioButton(STANDARD_COLORING);

        rgbColoringButton = new JRadioButton(RGB_COLORING);

        customColorRedLabel = new JLabel("Red: ");
        customColorGreenLabel = new JLabel("Green: ");
        customColorBlueLabel = new JLabel("Blue: ");

        customColorRedComboBox = new JComboBoxWithItemState<>();
        customColorGreenComboBox = new JComboBoxWithItemState<>();
        customColorBlueComboBox = new JComboBoxWithItemState<>();

        saveColoringButton = new JButton("Save Plate Data...");

        customizeColoringButton = new JButton("Customize Plate Coloring...");

        gridCheckBox = new JCheckBox();
        gridLabelCheckBox = new JCheckBox();

        additionalStatisticsButton = new JButton("Show more statistics");
        // The following snippet was taken from
        // https://explodingpixels.wordpress.com/2008/10/28/make-jeditorpane-use-the-system-font/
        // which shows how to make a JEditorPane behave look like a JLabel but still be
        // selectable.
        statisticsLabel = new JEditorPane(new HTMLEditorKit().getContentType(), "");
    }

    protected void initialize()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("wrap 1"));

        modelCheckBox.setText("Show " + bodyName);
        modelCheckBox.setSelected(true);
        modelCheckBox.addItemListener(this);

        JLabel resolutionLabel = new JLabel("Resolution");

        final PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        int numberResolutionLevels = smallBodyModel.getNumberResolutionLevels();
        ImmutableList<Integer> plateCount = smallBodyModel.getConfig().getResolutionNumberElements();

        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        List<JRadioButton> resModelButtons = new ArrayList<>();
        if (numberResolutionLevels > 1)
        {
            ActionListener listener = (e) -> {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                updateModelResolution(e.getActionCommand());
                setCursor(Cursor.getDefaultCursor());
            };
            ImmutableList<String> levels = smallBodyModel.getConfig().getResolutionLabels();
            ButtonGroup resolutionButtonGroup = new ButtonGroup();

            for (int i = 0; i < numberResolutionLevels; ++i)
            {
                String label = levels.get(i);
                builder.put(label, i);
                JRadioButton resButton = new JRadioButton(label);
                resButton.setActionCommand(label);
                resButton.setToolTipText("<html>Click here to show a model of " + bodyName + " <br />" + "containing " + plateCount.get(i) + " plates</html>");
                resButton.addActionListener(listener);
                resModelButtons.add(resButton);

                resButton.setEnabled(FileCache.instance().isAccessible(smallBodyModel.getModelFileNames().get(i)));

                FileCache.instance().addStateListener(smallBodyModel.getModelFileNames().get(i), state -> {
                    resButton.setEnabled(state.isAccessible());
                });

                resolutionButtonGroup.add(resButton);
                if (i == 0)
                    resButton.setSelected(true);
            }
        }
        resolutionLevels = builder.build();
        smallBodyModel.addPropertyChangeListener((evt) -> {
            if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
            {
                int resolutionLevel = smallBodyModel.getModelResolution();
                if (resolutionLevel >= 0 && resolutionLevel < resModelButtons.size())
                {
                    JRadioButton button = resModelButtons.get(resolutionLevel);
                    if (!button.isSelected())
                    {
                        resModelButtons.get(resolutionLevel).setSelected(true);
                    }
                }
            }
        });

        statisticsLabel.setBorder(null);
        statisticsLabel.setOpaque(false);
        statisticsLabel.setEditable(false);
        statisticsLabel.setForeground(UIManager.getColor("Label.foreground"));
        // add a CSS rule to force body tags to use the default label font
        // instead of the value in javax.swing.text.html.default.csss
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument) statisticsLabel.getDocument()).getStyleSheet().addRule(bodyRule);

        JLabel coloringLabel = new JLabel();
        coloringLabel.setText("Plate Coloring");

        showColoringProperties.setEnabled(false);
        showColoringProperties.addActionListener(e -> {
            showColoringProperties();
        });

        smallBodyModel.setColormap(colormapController.getColormap());

        colormapController.addPropertyChangeListener((aEvent) -> {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            String evtName = aEvent.getPropertyName();
            if (evtName != null && evtName.equals(StandardPlatePanel.EVT_ColormapChanged))
            {
                smallBodyModel.setColormap(colormapController.getColormap());
                try
                {
                    smallBodyModel.setCurrentColoringRange(smallBodyModel.getColoringIndex(), colormapController.getCurrentMinMax());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            setCursor(Cursor.getDefaultCursor());
        });

        contourPanel.addPropertyChangeListener((aEvent) -> {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            String evtName = aEvent.getPropertyName();
            if (evtName != null && evtName.equals(ContourPanel.EVT_ContourChanged))
            {
                smallBodyModel.setContourLineWidth(contourPanel.getLineWidth());
                smallBodyModel.showScalarsAsContours(contourPanel.getContourLinesRequested());
            }
            setCursor(Cursor.getDefaultCursor());
        });

        updateColoringOptions(smallBodyModel.getModelResolution());

        saveColoringButton.setEnabled(true);
        saveColoringButton.addActionListener(new SavePlateDataAction());

        // if (smallBodyModel.getConfig().customTemporary)
        // customizeColoringButton.setEnabled(false);
        // else
        customizeColoringButton.setEnabled(true);
        customizeColoringButton.addActionListener(new CustomizePlateDataAction());

        ButtonGroup coloringButtonGroup = new ButtonGroup();

        JRadioButton noColoringButton = new JRadioButton(NO_COLORING);

        coloringButtonGroup.add(noColoringButton);
        coloringButtonGroup.add(standardColoringButton);
        coloringButtonGroup.add(rgbColoringButton);

        ActionListener coloringButtonGroupListener = (e) -> {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            updateColoringControls();
            setColoring(e.getSource());
            setCursor(Cursor.getDefaultCursor());
        };
        noColoringButton.addActionListener(coloringButtonGroupListener);
        standardColoringButton.addActionListener(coloringButtonGroupListener);
        rgbColoringButton.addActionListener(coloringButtonGroupListener);
        noColoringButton.setSelected(true);
        updateColoringControls();

        // First item is blank -- select first plate coloring by default.
        if (coloringComboBox.getItemCount() > 1)
        {
            coloringComboBox.setSelectedIndex(1);
        }

        setStatisticsLabel();

        gridCheckBox.setText("Show Coordinate Grid");
        gridCheckBox.setSelected(false);
        gridCheckBox.addItemListener(this);

        gridLabelCheckBox.setText("Show Coord Labels");
        gridLabelCheckBox.setSelected(false);
        gridLabelCheckBox.setEnabled(false);
        gridLabelCheckBox.addItemListener(this);

        JCheckBox axesCheckBox = new JCheckBox();
        axesCheckBox.setText("Show Orientation Axes");
        axesCheckBox.setSelected(true);
        axesCheckBox.addItemListener(this);

        JSeparator statisticsSeparator = new JSeparator(SwingConstants.HORIZONTAL);

        JPanel surfacePropertiesEditorPanel = new DisplayPropertyEditorPanel(smallBodyModel);

        additionalStatisticsButton.addActionListener(e -> {
            additionalStatisticsButton.setVisible(false);
            addAdditionalStatisticsToLabel();
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
        if (Configuration.isAPLVersion())
        {
            panel.add(coloringLabel, "wrap");
            panel.add(noColoringButton, "wrap, gapleft 25");
            panel.add(standardColoringButton, "split 3, gapleft 25");
            panel.add(coloringComboBox, "width 200!");
            panel.add(showColoringProperties, "gapleft 25, wrap");
            panel.add(colormapController);

            ItemListener listener = (e) -> {
                if (e.getStateChange() != ItemEvent.DESELECTED)
                {
                    setColoring(e.getSource());
                }
            };
            coloringComboBox.addItemListener(listener);
            customColorRedComboBox.addItemListener(listener);
            customColorGreenComboBox.addItemListener(listener);
            customColorBlueComboBox.addItemListener(listener);

            panel.add(rgbColoringButton, "wrap, gapleft 25");
            panel.add(customColorRedLabel, "gapleft 75, split 2");
            panel.add(customColorRedComboBox, "width 200!, gapleft push, wrap");
            panel.add(customColorGreenLabel, "gapleft 75, split 2");
            panel.add(customColorGreenComboBox, "width 200!, gapleft push, wrap");
            panel.add(customColorBlueLabel, "gapleft 75, split 2");
            panel.add(customColorBlueComboBox, "width 200!, gapleft push, wrap");

            panel.add(saveColoringButton, "wrap, gapleft 25");
            panel.add(customizeColoringButton, "wrap, gapleft 25");
        }

        addCustomControls(panel);

        panel.add(gridCheckBox);
        panel.add(gridLabelCheckBox, "wrap");

        panel.add(surfacePropertiesEditorPanel, "wrap");

        panel.add(statisticsSeparator, "growx, span, wrap, gaptop 15");
        panel.add(statisticsLabel, "gaptop 15");
        panel.add(additionalStatisticsButton, "gaptop 15");

        scrollPane.setViewportView(panel);

        add(scrollPane, BorderLayout.CENTER);

    }

    protected void addCustomControls(@SuppressWarnings("unused") JPanel panel)
    {

    }

    private void updateModelResolution(String actionCommand)
    {
        if (!resolutionLevels.containsKey(actionCommand))
        {
            // This should not (probably can't) happen
            throw new AssertionError();
        }
        int newResolutionLevel = resolutionLevels.get(actionCommand);
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        if (smallBodyModel.getModelResolution() == newResolutionLevel)
        {
            // This probably won't happen, but just in case this gets
            // called when there was no change, do no harm.
            return;
        }

        // If we get this far, the model has changed, so update everything that
        // needs to be updated in this case.
        try
        {
            smallBodyModel.setModelResolution(newResolutionLevel);
            setStatisticsLabel();
            additionalStatisticsButton.setVisible(true);
            updateColoringOptions(newResolutionLevel);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {

        PickUtil.setPickingEnabled(false);

        try
        {
            PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

            if (e.getItemSelectable() == this.modelCheckBox)
            {
                // In the following we ensure that the graticule is shown
                // only if the shape model is shown
                Graticule graticule = (Graticule) modelManager.getModel(ModelNames.GRATICULE);
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    smallBodyModel.setShowSmallBody(true);
                    if (graticule != null && gridCheckBox.isSelected())
                    {
                        setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        graticule.setShowGraticule(true);
                        gridLabelCheckBox.setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                    else
                        gridLabelCheckBox.setEnabled(false);
                }
                else
                {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    smallBodyModel.setShowSmallBody(false);
                    if (graticule != null && gridCheckBox.isSelected())
                        graticule.setShowGraticule(false);
                    setCursor(Cursor.getDefaultCursor());
                }
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                setCursor(Cursor.getDefaultCursor());
            }
            else if (e.getItemSelectable() == this.gridCheckBox)
            {
                Graticule graticule = (Graticule) modelManager.getModel(ModelNames.GRATICULE);
                if (graticule != null)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        graticule.setShowGraticule(true);
                        gridLabelCheckBox.setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                    else
                    {
                        setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        graticule.setShowGraticule(false);
                        gridLabelCheckBox.setEnabled(false);
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
            else if (e.getItemSelectable() == this.gridLabelCheckBox)
            {
                Graticule graticule = (Graticule) modelManager.getModel(ModelNames.GRATICULE);
                if (graticule != null)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        graticule.setShowCaptions(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                    else
                        graticule.setShowCaptions(false);
                }
            }

        }
        finally
        {
            PickUtil.setPickingEnabled(true);
        }
    }

    public void updateColoringOptions()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        updateColoringOptions(smallBodyModel.getModelResolution());
    }

    protected void updateColoringControls()
    {
        boolean selected = standardColoringButton.isSelected();
        coloringComboBox.setEnabled(selected);
        showColoringProperties.setEnabled(coloringComboBox.isEnabled() && coloringComboBox.getSelectedIndex() >= 0);
        colormapController.setEnabled(selected);

        selected = rgbColoringButton.isSelected();
        customColorRedComboBox.setEnabled(selected);
        customColorGreenComboBox.setEnabled(selected);
        customColorBlueComboBox.setEnabled(selected);
        customColorRedLabel.setEnabled(selected);
        customColorGreenLabel.setEnabled(selected);
        customColorBlueLabel.setEnabled(selected);
    }

    protected void setColoring(Object source)
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        try
        {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            if (standardColoringButton.isSelected())
            {
                int selectedIndex = coloringComboBox.getSelectedIndex() - 1;
                if (selectedIndex < 0)
                {
                    showColoringProperties.setEnabled(false);
                    smallBodyModel.setColoringIndex(-1);
                    return;
                }
                smallBodyModel.setColoringIndex(selectedIndex);
                smallBodyModel.setColormap(colormapController.getColormap());
                smallBodyModel.setContourLineWidth(contourPanel.getLineWidth());
                smallBodyModel.showScalarsAsContours(contourPanel.getContourLinesRequested());
                double defaultArr[] = smallBodyModel.getDefaultColoringRange(selectedIndex);
                colormapController.setDefaultRange(defaultArr[0], defaultArr[1]);
                double currentArr[] = smallBodyModel.getCurrentColoringRange(selectedIndex);
                colormapController.setCurrentMinMax(currentArr[0], currentArr[1]);
                setCursor(Cursor.getDefaultCursor());
            }
            else if (rgbColoringButton.isSelected())
            {
                // Subtract 1 to leave room for the blank string (no selection) at the top.
                int redIndex = customColorRedComboBox.getSelectedIndex() - 1;
                int greenIndex = customColorGreenComboBox.getSelectedIndex() - 1;
                int blueIndex = customColorBlueComboBox.getSelectedIndex() - 1;
                if (redIndex < 0 && greenIndex < 0 && blueIndex < 0)
                {
                    smallBodyModel.setColoringIndex(-1);
                    return;
                }
                smallBodyModel.setFalseColoring(redIndex, greenIndex, blueIndex);
            }
            else
            {
                smallBodyModel.setColoringIndex(-1);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            String message = e.getMessage();
            JOptionPane.showMessageDialog(null, message, "error", JOptionPane.ERROR_MESSAGE);
            JComboBox<?> box = null;
            if (source instanceof JComboBox)
            {
                box = (JComboBox<?>) source;
            }
            else if (standardColoringButton.isSelected())
            {
                box = coloringComboBox;
                showColoringProperties.setEnabled(coloringComboBox.isEnabled() && coloringComboBox.getSelectedIndex() >= 0);
            }
            if (box != null)
            {
                box.setSelectedIndex(0);
            }
        }
        finally
        {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    protected void showColoringProperties()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        int index = smallBodyModel.getColoringIndex();
        File file = FileCache.getFileFromServer(smallBodyModel.getAllColoringData().get(index).getFileName());

        try
        {
            JTabbedPane jTabbedPane = MetadataDisplay.summary(file);
            int tabCount = jTabbedPane.getTabCount();
            if (tabCount > 0)
            {
                JFrame jFrame = new JFrame("Coloring File Properties");

                jFrame.add(jTabbedPane);
                jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                jFrame.pack();
                jFrame.setVisible(true);
            }
            else
            {
                JOptionPane.showMessageDialog(null, "No properties available for file " + file, "Coloring File Properties", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void updateColoringOptions(int newResolutionLevel)
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        ColoringDataManager coloringDataManager = smallBodyModel.getColoringDataManager();
        ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();

        int numberElements = resolutions.size() > newResolutionLevel ? resolutions.get(newResolutionLevel) : -1;

        updateColoringComboBox(coloringComboBox, coloringDataManager, numberElements);
        updateColoringComboBox(customColorRedComboBox, coloringDataManager, numberElements);
        updateColoringComboBox(customColorGreenComboBox, coloringDataManager, numberElements);
        updateColoringComboBox(customColorBlueComboBox, coloringDataManager, numberElements);
        showColoringProperties.setEnabled(coloringComboBox.isEnabled() && coloringComboBox.getSelectedIndex() >= 0);
    }

    protected final Map<JComboBoxWithItemState<?>, FileStateListenerTracker> listenerTrackers = new HashMap<>();

    protected void updateColoringComboBox(JComboBoxWithItemState<String> box, ColoringDataManager coloringDataManager, int numberElements)
    {
        // Store the current selection and number of items in the combo box.
        int previousSelection = box.getSelectedIndex();

        // Clear the current content.
        box.setSelectedIndex(-1);
        box.removeAllItems();

        synchronized (this.listenerTrackers)
        {
            // Get rid of current file access state listeners.
            FileStateListenerTracker boxListeners = listenerTrackers.get(box);
            if (boxListeners == null)
            {
                boxListeners = FileStateListenerTracker.of(FileCache.instance());
                listenerTrackers.put(box, boxListeners);
            }
            else
            {
                boxListeners.removeAllStateChangeListeners();
            }

            // Add one item for blank (no coloring).
            box.addItem("");
            for (String name : coloringDataManager.getNames())
            {
                // Re-add the current colorings.
                box.addItem(name);
                if (!coloringDataManager.has(name, numberElements))
                {
                    // This coloring is not available at this resolution. List it but grey it out.
                    box.setEnabled(name, false);
                }
                else
                {
                    String urlString = coloringDataManager.get(name, numberElements).getFileName();
                    if (urlString == null) continue;
                    box.setEnabled(name, FileCache.instance().isAccessible(urlString));
                    StateListener listener = e -> {
                        box.setEnabled(name, e.isAccessible());
                    };
                    boxListeners.addStateChangeListener(urlString, listener);
                }
            }

            int numberColorings = box.getItemCount();
            int selection = 0;
            if (previousSelection < numberColorings)
            {
                // A coloring was replaced/edited. Re-select the current selection.
                selection = previousSelection;
            }

            box.setSelectedIndex(selection);
        }
    }

    protected void setStatisticsLabel()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        BoundingBox bb = smallBodyModel.getBoundingBox();

        // We add a superscripted space at end of first 2 lines and last 6 lines so that
        // spacing between all lines is the same.
        String text =
                "<html>Statistics:<br>" + "&nbsp;&nbsp;&nbsp;Number of Plates: " + smallBodyModel.getSmallBodyPolyData().GetNumberOfCells() + "<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;Number of Vertices: " //
                        + smallBodyModel.getSmallBodyPolyData().GetNumberOfPoints() + "<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;Surface Area: " + String.format("%.7g", smallBodyModel.getSurfaceArea()) + " km<sup>2</sup><br>" //
                        + "&nbsp;&nbsp;&nbsp;Volume: " + String.format("%.7g", smallBodyModel.getVolume()) + " km<sup>3</sup><br>" + "&nbsp;&nbsp;&nbsp;Plate Area Average: " + String.format("%.7g", 1.0e6 * smallBodyModel.getMeanCellArea()) //
                        + " m<sup>2</sup><br>" + "&nbsp;&nbsp;&nbsp;Plate Area Minimum: " + String.format("%.7g", 1.0e6 * smallBodyModel.getMinCellArea()) + " m<sup>2</sup><br>" + "&nbsp;&nbsp;&nbsp;Plate Area Maximum: " //
                        + String.format("%.7g", 1.0e6 * smallBodyModel.getMaxCellArea()) + " m<sup>2</sup><br>" + "&nbsp;&nbsp;&nbsp;Extent:<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;X: [" + String.format("%.7g, %.7g", bb.xmin, bb.xmax) //
                        + "] km<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Y: [" + String.format("%.7g, %.7g", bb.ymin, bb.ymax) + "] km<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Z: [" //
                        + String.format("%.7g, %.7g", bb.zmin, bb.zmax) + "] km<sup>&nbsp;</sup><br>";

        // There's some weird thing going one where changing the text of the label
        // causes
        // the scoll bar of the panel to scroll all the way down. Therefore, reset it to
        // the original value after changing the text.
        // TODO not sure if this is the best solution since there is still a slight
        // flicker occasionally when you start up the tool, probably due to the change
        // in the scroll bar position.
        final int originalScrollBarValue = scrollPane.getVerticalScrollBar().getValue();

        statisticsLabel.setText(text);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                scrollPane.getVerticalScrollBar().setValue(originalScrollBarValue);
            }
        });
    }

    protected void addAdditionalStatisticsToLabel()
    {

    }

    @Override
    public void stateChanged(@SuppressWarnings("unused") ChangeEvent e)
    {}

    protected CustomPlateDataDialog getPlateDataDialog(@SuppressWarnings("unused") ModelManager modelManager)
    {
        return new CustomPlateDataDialog(this);
    }

    private class CustomizePlateDataAction extends AbstractAction
    {
        private static final long serialVersionUID = -1968139736125692972L;

        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
        {
            CustomPlateDataDialog dialog = getPlateDataDialog(modelManager);
            dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(PolyhedralModelControlPanel.this));
            dialog.setVisible(true);
        }
    }

    private class SavePlateDataAction extends AbstractAction
    {
        private static final long serialVersionUID = 1158470836083999433L;

        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
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
                JOptionPane.showMessageDialog(invoker, "An error occurred exporting the plate data.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
