package edu.jhuapl.saavtk.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.colormap.ColormapController;
import edu.jhuapl.saavtk.colormap.ColormapControllerWithContouring;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomPlateDataDialog;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import net.miginfocom.swing.MigLayout;

public class PolyhedralModelControlPanel extends JPanel implements ItemListener, ChangeListener
{
	private static final String NO_COLORING = "None";
	private static final String STANDARD_COLORING = "Standard";
	private static final String RGB_COLORING = "RGB";
	private static final String EMPTY_SELECTION = "None";
	private static final String IMAGE_MAP_TEXT = "Show Image Map";

	private JCheckBox modelCheckBox;
	private ModelManager modelManager;

	private JRadioButton noColoringButton;
	private JRadioButton standardColoringButton;
	private JRadioButton rgbColoringButton;
	private ButtonGroup coloringButtonGroup;
	private JComboBoxWithItemState<String> coloringComboBox;
	private JComboBoxWithItemState<String> customColorRedComboBox;
	private JComboBoxWithItemState<String> customColorGreenComboBox;
	private JComboBoxWithItemState<String> customColorBlueComboBox;
	private JLabel customColorRedLabel;
	private JLabel customColorGreenLabel;
	private JLabel customColorBlueLabel;
	private List<JRadioButton> resModelButtons = new ArrayList<>();
	private ButtonGroup resolutionButtonGroup;
	private JCheckBox gridCheckBox;
	protected JCheckBox gridLabelCheckBox;

	private JCheckBox axesCheckBox;
	private final JCheckBox imageMapCheckBox;
	private final JComboBox<String> imageMapComboBox;
	private JLabel opacityLabel;
	private JSpinner imageMapOpacitySpinner;

	ColormapControllerWithContouring colormapController = new ColormapControllerWithContouring();

	public void setSaveColoringButton(JButton saveColoringButton)
	{
		this.saveColoringButton = saveColoringButton;
	}

	private JButton saveColoringButton;
	private JButton customizeColoringButton;
	private JEditorPane statisticsLabel;
	private JScrollPane scrollPane;
	private JButton additionalStatisticsButton;
	private final ImmutableMap<String, Integer> resolutionLevels;

	public ModelManager getModelManager()
	{
		return modelManager;
	}

	public String getSelectedImageMapName()
	{
		String[] names = getModelManager().getPolyhedralModel().getImageMapNames();
		int index = 0;
		if (imageMapComboBox != null)
			index = imageMapComboBox.getSelectedIndex() - 1;
		if (index < 0 || index >= names.length)
			throw new IllegalStateException();
		return names[index];
	}

	public JSpinner getImageMapOpacitySpinner()
	{
		return imageMapOpacitySpinner;
	}

	public JButton getSaveColoringButton()
	{
		return saveColoringButton;
	}

	public JLabel getOpacityLabel()
	{
		return opacityLabel;
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
		ImmutableList<Integer> plateCount = smallBodyModel.getConfig().getResolutionNumberElements();

		ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
		if (numberResolutionLevels > 1)
		{
			resolutionButtonGroup = new ButtonGroup();
			ActionListener listener = (e) -> {
				updateModelResolution(e.getActionCommand());
			};
			ImmutableList<String> levels = smallBodyModel.getConfig().getResolutionLabels();
			for (int i = 0; i < numberResolutionLevels; ++i)
			{
				String label = levels.get(i);
				builder.put(label, i);
				JRadioButton resButton = new JRadioButton(label);
				resButton.setActionCommand(label);
				resButton.setEnabled(true);
				resButton.setToolTipText("<html>Click here to show a model of " + bodyName + " <br />" + "containing " + plateCount.get(i) + " plates</html>");
				resButton.addActionListener(listener);
				resModelButtons.add(resButton);

				resolutionButtonGroup.add(resButton);
				if (i == 0)
					resButton.setSelected(true);
			}
		}
		resolutionLevels = builder.build();

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
		String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
		((HTMLDocument) statisticsLabel.getDocument()).getStyleSheet().addRule(bodyRule);

		JLabel coloringLabel = new JLabel();
		coloringLabel.setText("Plate Coloring");

		coloringComboBox = new JComboBoxWithItemState<>();

		noColoringButton = new JRadioButton(NO_COLORING);

		standardColoringButton = new JRadioButton(STANDARD_COLORING);

		smallBodyModel.setColormap(colormapController.getColormap());
		colormapController.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				smallBodyModel.setColormap(colormapController.getColormap());
				smallBodyModel.setContourLineWidth(colormapController.getLineWidth());
				smallBodyModel.showScalarsAsContours(colormapController.getContourLinesRequested());
				if (evt.getPropertyName().equals(ColormapController.colormapChanged))
				{
					double[] range = smallBodyModel.getCurrentColoringRange(smallBodyModel.getColoringIndex());
					colormapController.setMinMax(range[0], range[1]);
					range = smallBodyModel.getDefaultColoringRange(smallBodyModel.getColoringIndex());
					colormapController.setDefaultRange(range[0], range[1]);
				}
				else if (evt.getPropertyName().equals(ColormapController.colormapRangeChanged))
				{
					try
					{
						smallBodyModel.setCurrentColoringRange(smallBodyModel.getColoringIndex(), colormapController.getMinMax());
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		rgbColoringButton = new JRadioButton(RGB_COLORING);

		customColorRedLabel = new JLabel("Red: ");
		customColorGreenLabel = new JLabel("Green: ");
		customColorBlueLabel = new JLabel("Blue: ");

		customColorRedComboBox = new JComboBoxWithItemState<>();
		customColorGreenComboBox = new JComboBoxWithItemState<>();
		customColorBlueComboBox = new JComboBoxWithItemState<>();

		updateColoringOptions(smallBodyModel.getModelResolution());

		saveColoringButton = new JButton("Save Plate Data...");
		saveColoringButton.setEnabled(true);
		saveColoringButton.addActionListener(new SavePlateDataAction());

		customizeColoringButton = new JButton("Customize Plate Coloring...");
		//        if (smallBodyModel.getConfig().customTemporary)
		//           customizeColoringButton.setEnabled(false);
		//       else
		customizeColoringButton.setEnabled(true);
		customizeColoringButton.addActionListener(new CustomizePlateDataAction());

		coloringButtonGroup = new ButtonGroup();
		coloringButtonGroup.add(noColoringButton);
		coloringButtonGroup.add(standardColoringButton);
		coloringButtonGroup.add(rgbColoringButton);

		ActionListener coloringButtonGroupListener = (e) -> {
			updateColoringControls();
			setColoring();
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

		gridCheckBox = new JCheckBox();
		gridCheckBox.setText("Show Coordinate Grid");
		gridCheckBox.setSelected(false);
		gridCheckBox.addItemListener(this);

		gridLabelCheckBox = new JCheckBox();
		gridLabelCheckBox.setText("Show Coord Labels");
		gridLabelCheckBox.setSelected(false);
		gridLabelCheckBox.setEnabled(false);
		gridLabelCheckBox.addItemListener(this);

		axesCheckBox = new JCheckBox();
		axesCheckBox.setText("Show Orientation Axes");
		axesCheckBox.setSelected(true);
		axesCheckBox.addItemListener(this);

		imageMapCheckBox = new JCheckBox();
		imageMapCheckBox.setText(IMAGE_MAP_TEXT);
		imageMapCheckBox.setSelected(false);
		imageMapCheckBox.addItemListener(this);

		imageMapComboBox = configureImageMapComboBox(modelManager.getPolyhedralModel());
		if (imageMapComboBox != null)
			imageMapComboBox.addItemListener(this);

		opacityLabel = new JLabel("Image opacity");
		imageMapOpacitySpinner = createOpacitySpinner();
		imageMapOpacitySpinner.addChangeListener(this);
		opacityLabel.setEnabled(false);
		imageMapOpacitySpinner.setEnabled(false);

		JSeparator statisticsSeparator = new JSeparator(SwingConstants.HORIZONTAL);

		JPanel surfacePropertiesEditorPanel = new DisplayPropertyEditorPanel(smallBodyModel);

		additionalStatisticsButton = new JButton("Show more statistics");
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
			panel.add(standardColoringButton, "split 2, gapleft 25");
			panel.add(coloringComboBox, "width 200!, wrap");
			panel.add(colormapController);

			ItemListener listener = (e) -> {
				setColoring();
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

		if (modelManager.getPolyhedralModel().isImageMapAvailable())
		{
			if (imageMapComboBox != null)
			{
				panel.add(new JLabel(IMAGE_MAP_TEXT), "wrap");
				panel.add(imageMapComboBox, "wrap");
			}
			else
			{
				panel.add(imageMapCheckBox, "wrap");
			}
			panel.add(opacityLabel, "gapleft 25, split 2");
			panel.add(imageMapOpacitySpinner, "wrap");
		}
		panel.add(gridCheckBox);
		panel.add(gridLabelCheckBox, "wrap");

		panel.add(surfacePropertiesEditorPanel, "wrap");

		panel.add(statisticsSeparator, "growx, span, wrap, gaptop 15");
		panel.add(statisticsLabel, "gaptop 15");
		panel.add(additionalStatisticsButton, "gaptop 15");

		scrollPane.setViewportView(panel);

		add(scrollPane, BorderLayout.CENTER);
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
			updateColoringOptions(newResolutionLevel);
			smallBodyModel.setModelResolution(newResolutionLevel);
			setStatisticsLabel();
			additionalStatisticsButton.setVisible(true);
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

		Picker.setPickingEnabled(false);

		PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

		if (e.getItemSelectable() == this.modelCheckBox)
		{
			// In the following we ensure that the graticule and image map are shown
			// only if the shape model is shown
			Graticule graticule = (Graticule) modelManager.getModel(ModelNames.GRATICULE);
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				smallBodyModel.setShowSmallBody(true);
				if (graticule != null && gridCheckBox.isSelected())
				{
					graticule.setShowGraticule(true);
					gridLabelCheckBox.setEnabled(true);
				}
				else
					gridLabelCheckBox.setEnabled(false);
			}
			else
			{
				smallBodyModel.setShowSmallBody(false);
				if (graticule != null && gridCheckBox.isSelected())
					graticule.setShowGraticule(false);
			}
			showImageMap(isImageMapEnabled());
		}
		else if (e.getItemSelectable() == this.gridCheckBox)
		{
			Graticule graticule = (Graticule) modelManager.getModel(ModelNames.GRATICULE);
			if (graticule != null)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					graticule.setShowGraticule(true);
					gridLabelCheckBox.setEnabled(true);
				}
				else
				{
					graticule.setShowGraticule(false);
					gridLabelCheckBox.setEnabled(false);
				}
			}
		}
		else if (e.getItemSelectable() == this.gridLabelCheckBox)
		{
			Graticule graticule = (Graticule) modelManager.getModel(ModelNames.GRATICULE);
			if (graticule != null)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
					graticule.setShowCaptions(true);
				else
					graticule.setShowCaptions(false);
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
		else if (e.getItemSelectable() == this.imageMapComboBox)
		{
			boolean show = this.imageMapComboBox.getSelectedIndex() != 0;
			showImageMap(show);
			opacityLabel.setEnabled(show);
			imageMapOpacitySpinner.setEnabled(show);
		}

		Picker.setPickingEnabled(true);
	}

	protected void updateColoringControls()
	{
		boolean selected = standardColoringButton.isSelected();
		coloringComboBox.setEnabled(selected);
		colormapController.setEnabled(selected);

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
			if (standardColoringButton.isSelected())
			{
				int selectedIndex = coloringComboBox.getSelectedIndex() - 1;
				if (selectedIndex < 0)
				{
					smallBodyModel.setColoringIndex(-1);
					return;
				}
				smallBodyModel.setColoringIndex(selectedIndex);
				smallBodyModel.setColormap(colormapController.getColormap());
				smallBodyModel.setContourLineWidth(colormapController.getLineWidth());
				smallBodyModel.showScalarsAsContours(colormapController.getContourLinesRequested());
				colormapController.refresh();
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
				colormapController.refresh();
				smallBodyModel.setFalseColoring(redIndex, greenIndex, blueIndex);
			}
			else
			{
				smallBodyModel.setColoringIndex(-1);
			}
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}

	protected void setColoring(int idx) throws IOException
	{
		PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
		//		if (idx < 0 || idx >= smallBodyModel.getColoringInfoList().size())
		//		{
		//			return;
		//		}
		smallBodyModel.setColoringIndex(idx);
		if (idx < 0)
		{
			return;
		}
		double[] range = smallBodyModel.getCurrentColoringRange(idx);
		colormapController.setMinMax(range[0], range[1]);
		range = smallBodyModel.getDefaultColoringRange(idx);
		colormapController.setDefaultRange(range[0], range[1]);
	}

	protected void updateColoringOptions(int newResolutionLevel)
	{
		PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

		ColoringDataManager coloringDataManager = smallBodyModel.getColoringDataManager();
		ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
		if (resolutions.isEmpty())
		{
			return;
		}
		int numberElements = resolutions.get(newResolutionLevel);

		updateColoringComboBox(coloringComboBox, coloringDataManager, numberElements);
		updateColoringComboBox(customColorRedComboBox, coloringDataManager, numberElements);
		updateColoringComboBox(customColorGreenComboBox, coloringDataManager, numberElements);
		updateColoringComboBox(customColorBlueComboBox, coloringDataManager, numberElements);
	}

	protected void updateColoringComboBox(JComboBoxWithItemState<String> box, ColoringDataManager coloringDataManager, int numberElements)
	{
		String newSelection = "";
		String previousSelection = (String) box.getSelectedItem();
		box.setSelectedIndex(-1);
		box.removeAllItems();
		box.addItem("");
		for (String name : coloringDataManager.getNames())
		{
			box.addItem(name);
			if (coloringDataManager.has(name, numberElements))
			{
				if (name.equals(previousSelection))
				{
					newSelection = previousSelection;
				}
			}
			else
			{
				box.setEnabled(name, false);
			}
		}
		box.setSelectedItem(newSelection);
	}

	protected void setStatisticsLabel()
	{
		PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

		BoundingBox bb = smallBodyModel.getBoundingBox();

		// We add a superscripted space at end of first 2 lines and last 6 lines so that spacing between all lines is the same.
		String text =
				"<html>Statistics:<br>" + "&nbsp;&nbsp;&nbsp;Number of Plates: " + smallBodyModel.getSmallBodyPolyData().GetNumberOfCells() + "<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;Number of Vertices: " + smallBodyModel.getSmallBodyPolyData().GetNumberOfPoints() + "<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;Surface Area: " + String.format("%.7g", smallBodyModel.getSurfaceArea()) + " km<sup>2</sup><br>" + "&nbsp;&nbsp;&nbsp;Volume: " + String.format("%.7g", smallBodyModel.getVolume()) + " km<sup>3</sup><br>" + "&nbsp;&nbsp;&nbsp;Plate Area Average: " + String.format("%.7g", 1.0e6 * smallBodyModel.getMeanCellArea()) + " m<sup>2</sup><br>" + "&nbsp;&nbsp;&nbsp;Plate Area Minimum: " + String.format("%.7g", 1.0e6 * smallBodyModel.getMinCellArea()) + " m<sup>2</sup><br>" + "&nbsp;&nbsp;&nbsp;Plate Area Maximum: " + String.format("%.7g", 1.0e6 * smallBodyModel.getMaxCellArea()) + " m<sup>2</sup><br>" + "&nbsp;&nbsp;&nbsp;Extent:<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;X: [" + String.format("%.7g, %.7g", bb.xmin, bb.xmax) + "] km<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Y: [" + String.format("%.7g, %.7g", bb.ymin, bb.ymax) + "] km<sup>&nbsp;</sup><br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Z: [" + String.format("%.7g, %.7g", bb.zmin, bb.zmax) + "] km<sup>&nbsp;</sup><br>";

		// There's some weird thing going one where changing the text of the label causes
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

	//
	// for subclasses that support images
	//
	protected void showImageMap(@SuppressWarnings("unused") boolean show)
	{}

	protected void addAdditionalStatisticsToLabel()
	{}

	protected boolean isImageMapEnabled()
	{
		if (imageMapComboBox != null)
			return !EMPTY_SELECTION.equals(imageMapComboBox.getSelectedItem());
		return imageMapCheckBox.isSelected();
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{}

	protected CustomPlateDataDialog getPlateDataDialog(ModelManager modelManager)
	{
		return new CustomPlateDataDialog(modelManager);
	}

	private static JSpinner createOpacitySpinner()
	{
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
		spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00"));
		spinner.setPreferredSize(new Dimension(80, 21));
		return spinner;
	}

	private static JComboBox<String> configureImageMapComboBox(PolyhedralModel model)
	{
		JComboBox<String> result = null;
		String[] mapNames = model.getImageMapNames();
		if (mapNames != null && mapNames.length > 1)
		{
			String[] allOptions = new String[mapNames.length + 1];
			int index = 0;
			allOptions[index] = EMPTY_SELECTION;
			for (; index < mapNames.length; ++index)
			{
				allOptions[index + 1] = mapNames[index].replaceAll(".*[/\\\\]", "");
			}
			result = new JComboBox<>(allOptions);
		}
		return result;
	}

	private class CustomizePlateDataAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			CustomPlateDataDialog dialog = getPlateDataDialog(modelManager);
			dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(PolyhedralModelControlPanel.this));
			dialog.setVisible(true);

			PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
			updateColoringOptions(smallBodyModel.getModelResolution());
		}
	}

	private class SavePlateDataAction extends AbstractAction
	{
		@Override
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
				JOptionPane.showMessageDialog(invoker, "An error occurred exporting the plate data.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
}
