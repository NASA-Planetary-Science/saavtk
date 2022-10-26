package edu.jhuapl.saavtk.gui.panel;

import java.awt.BorderLayout;
import java.awt.Cursor; 
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.coloring.gui.ColoringModePanel;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomPlateDataDialog;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.main.gui.ShapeModelEditPanel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.plateColoring.BasicColoringDataManager;
import edu.jhuapl.saavtk.model.plateColoring.ColoringData;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.saavtk.model.plateColoring.CustomizableColoringDataManager;
import edu.jhuapl.saavtk.model.plateColoring.LoadableColoringData;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.DownloadableFileManager.StateListener;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileStateListenerTracker;
import edu.jhuapl.saavtk.util.Properties;
import glum.gui.GuiUtil;
import net.miginfocom.swing.MigLayout;

public class PolyhedralModelControlPanel extends JPanel implements ChangeListener
{
    public static PolyhedralModelControlPanel of(Renderer aRenderer, ModelManager modelManager, String bodyName)
    {
        PolyhedralModelControlPanel result = new PolyhedralModelControlPanel(aRenderer, modelManager, bodyName) {
            private static final long serialVersionUID = 256201608369977510L;
        };

        result.initialize();

        return result;
    }

    private static final long serialVersionUID = 7858613374590442069L;

    private final ModelManager modelManager;
    private final String bodyName;

    private final ShapeModelEditPanel shapeModelEditPanel;
    private final ColoringModePanel plateColoringPanel;
    private final JComboBoxWithItemState<String> coloringComboBox;
    private final JComboBoxWithItemState<String> customColorRedComboBox;
    private final JComboBoxWithItemState<String> customColorGreenComboBox;
    private final JComboBoxWithItemState<String> customColorBlueComboBox;

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

    protected PolyhedralModelControlPanel(Renderer aRenderer, ModelManager modelManager, String bodyName)
    {
        super(new BorderLayout());
        this.modelManager = modelManager;
        this.bodyName = bodyName;

        scrollPane = new JScrollPane();

        var tmpSmallBody = modelManager.getPolyhedralModel();
        shapeModelEditPanel = new ShapeModelEditPanel(aRenderer, tmpSmallBody, bodyName);

        coloringComboBox = new JComboBoxWithItemState<>();
        customColorRedComboBox = new JComboBoxWithItemState<>();
        customColorGreenComboBox = new JComboBoxWithItemState<>();
        customColorBlueComboBox = new JComboBoxWithItemState<>();
        plateColoringPanel = new ColoringModePanel(aRenderer, tmpSmallBody, coloringComboBox, customColorRedComboBox, customColorGreenComboBox, customColorBlueComboBox);

        saveColoringButton = new JButton("Save Plate Data...");

        customizeColoringButton = new JButton("Customize Plate Coloring...");

        additionalStatisticsButton = new JButton("Show more statistics");
        // The following snippet was taken from
        // https://explodingpixels.wordpress.com/2008/10/28/make-jeditorpane-use-the-system-font/
        // which shows how to make a JEditorPane behave look like a JLabel but still be
        // selectable.
        statisticsLabel = new JEditorPane(new HTMLEditorKit().getContentType(), "");
    }

    protected void initialize()
    {
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", ""));

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
        
        smallBodyModel.getColoringDataManager().addPropertyChangeListener(new PropertyChangeListener()
		{
			
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName().equals(BasicColoringDataManager.COLORING_DATA_CHANGE))
				{
					updateColoringOptions();
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

        updateColoringOptions(smallBodyModel.getModelResolution());

        saveColoringButton.setEnabled(true);
        saveColoringButton.addActionListener(new SavePlateDataAction());

        // if (smallBodyModel.getConfig().customTemporary)
        // customizeColoringButton.setEnabled(false);
        // else
        customizeColoringButton.setEnabled(true);
        customizeColoringButton.addActionListener(new CustomizePlateDataAction());

        setStatisticsLabel();

        JCheckBox axesCheckBox = new JCheckBox();
        axesCheckBox.setText("Show Orientation Axes");
        axesCheckBox.setSelected(true);
//        axesCheckBox.addItemListener(this);

        JSeparator statisticsSeparator = new JSeparator(SwingConstants.HORIZONTAL);

        additionalStatisticsButton.addActionListener(e -> {
            additionalStatisticsButton.setVisible(false);
            addAdditionalStatisticsToLabel();
        });

        panel.add(shapeModelEditPanel, "growx,span,wrap");
        panel.add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");
        if (smallBodyModel.getNumberResolutionLevels() > 1)
        {
            panel.add(resolutionLabel, "wrap");
            for (JRadioButton rb : resModelButtons)
                panel.add(rb, "wrap, gapleft 25");

            panel.add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");
        }

        // Only show coloring in APL version or if there are built in colors.
        // In the non-APL version, do not allow customization.
        if (Configuration.isAPLVersion())
        {
        	 panel.add(plateColoringPanel, "growx,span,wrap");
             panel.add(saveColoringButton, "wrap, gapleft 25");
             panel.add(customizeColoringButton, "wrap, gapleft 25");
        }

        addCustomControls(panel);

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

    public void updateColoringOptions()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        updateColoringOptions(smallBodyModel.getModelResolution());
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
                    ColoringData coloringData = coloringDataManager.get(name, numberElements);
                    if (coloringData instanceof LoadableColoringData)
                    {
                        String urlString = ((LoadableColoringData) coloringData).getFileId();

                        box.setEnabled(name, FileCache.instance().isAccessible(urlString));
                        StateListener listener = e -> {
                            box.setEnabled(name, e.isAccessible());
                        };
                        boxListeners.addStateChangeListener(urlString, listener);                        
                    }
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
