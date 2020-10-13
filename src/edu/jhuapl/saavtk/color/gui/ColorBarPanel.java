package edu.jhuapl.saavtk.color.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.color.painter.ColorBarChangeListener;
import edu.jhuapl.saavtk.color.painter.ColorBarChangeType;
import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.color.table.ColorTable;
import edu.jhuapl.saavtk.color.table.ColorTableUtil;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import glum.gui.GuiExeUtil;
import glum.gui.GuiUtil;
import glum.gui.component.GComboBox;
import glum.gui.component.GNumberField;
import glum.gui.misc.CustomListCellRenderer;
import glum.gui.panel.GPanel;
import glum.text.SigFigNumberFormat;
import net.miginfocom.swing.MigLayout;

/**
 * UI that allows the user to select a color bar and specify the range of values
 * associated with the color bar.
 * <P>
 * Usage of this UI component requires the manual addition of various
 * {@link FeatureType}s. The {@link FeatureType#Invalid} is used to designate an
 * invalid or no choice selection. When this FeatureType is selected relevant UI
 * components will be disabled.
 *
 * @author lopeznr1
 */
public class ColorBarPanel extends GPanel implements ActionListener, ColorBarChangeListener
{
	// Ref vars
	private final ColorBarPainter refColorBarPainter;

	// State vars
	private final Map<FeatureType, Range<Double>> resetRangeM;

	// Cache vars
	private FeatureType cFeatureType;
	private ColorMapAttr cColorMapAttr;

	// Gui vars
	private final ColorBarConfigPanel painterCBCP;
	private final JLabel featureL;
	private final CustomListCellRenderer featureLCR;
	private final GComboBox<FeatureType> featureBox;
	private final GComboBox<ColorTable> colorTableBox;
	private final JLabel colorTableL;
	private final JButton resetAllB, resetMinB, resetMaxB;
	private final JLabel minValueL, maxValueL, numLevelsL;
	private final GNumberField minValueNF, maxValueNF, numLevelsNF;
	private final JCheckBox logScaleCB;
	private final JCheckBox showColorBarCB;
	private final JButton applyB, configB;
	private final JToggleButton syncTB;

	/**
	 * Standard Constructor
	 *
	 * Constructs a panel that allows a user to provide configuration of a
	 * {@link ColorBarPainter}. This panel supports two modes: "regular" and
	 * "compact".
	 *
	 * A compact panel will be similar to the regular panel but be missing the
	 * following UI elements:
	 * <UL>
	 * <LI>Log UI control
	 * <LI>Colorize UI label
	 * <LI>Feature UI label
	 * </UL>
	 *
	 * @param aColorBarPainter Reference {@link ColorBarPainter} to be controlled by
	 *                         this panel.
	 * @param aIsRegular       If set to true then a regular panel will be created
	 *                         instead of a compact panel.
	 */
	public ColorBarPanel(ColorBarPainter aColorBarPainter, boolean aIsRegular)
	{
		refColorBarPainter = aColorBarPainter;

		resetRangeM = new HashMap<>();

		cFeatureType = FeatureType.Invalid;
		cColorMapAttr = ColorMapAttr.Invalid;

		// Set up the GUI
		setLayout(new MigLayout("", "0[][right][]0", "0[][]"));

		painterCBCP = new ColorBarConfigPanel(this, refColorBarPainter);
		painterCBCP.addActionListener(this);

		// Feature area
		featureL = new JLabel("Property:");
		featureLCR = new CustomListCellRenderer();
		featureBox = new GComboBox<>(this, featureLCR);
		if (aIsRegular == true)
			add(featureL, "span,split");
		add(featureBox, "growx,span,w 0:0:,wrap");

		// ColorTable area
		colorTableL = new JLabel();
		colorTableBox = new GComboBox<>(this, ColorTableUtil.getSystemColorTableList());
		colorTableBox.setRenderer(new ColorTableListCellRenderer(colorTableBox));
		colorTableBox.setChosenItem(ColorTableUtil.getSystemColorTableDefault());

		add(colorTableL, "growx,span,w 10::,wrap 3");
		add(colorTableBox, "growx,span,w 0:0:,wrap");

		// Min value area
		resetMinB = GuiUtil.formButton(this, IconUtil.getActionReset());
		resetMinB.setToolTipText(ToolTipUtil.getItemResetMinVal(null, Double.NaN));
		minValueL = new JLabel("Min:");
		minValueNF = new GNumberField(this, new SigFigNumberFormat(5));
		minValueNF.setColumns(7);
		add(resetMinB, "w 24!,h 24!");
		add(minValueL, "");
		add(minValueNF, "growx,pushx,wrap");

		// Max value area
		resetMaxB = GuiUtil.formButton(this, IconUtil.getActionReset());
		resetMaxB.setToolTipText(ToolTipUtil.getItemResetMaxVal(null, Double.NaN));
		maxValueL = new JLabel("Max:");
		maxValueNF = new GNumberField(this, new SigFigNumberFormat(5));
		maxValueNF.setColumns(7);
		add(resetMaxB, "w 24!,h 24!");
		add(maxValueL, "");
		add(maxValueNF, "growx,wrap");

		// Num color levels area
		numLevelsL = new JLabel("# Levels:");
		numLevelsNF = new GNumberField(this);
		numLevelsNF.setValue(32);
		add(numLevelsL, "ax right,span 2");
		if (aIsRegular == true)
			add(numLevelsNF, "growx,split");
		else
			add(numLevelsNF, "growx,wrap");

		// Log scale area
		logScaleCB = GuiUtil.createJCheckBox("Log scale", this);
		if (aIsRegular == true)
			add(logScaleCB, "gapx 20,w 20::,wrap");
//		add(logScaleCB, "span,wrap");

		// Show ColorBar area
		showColorBarCB = GuiUtil.createJCheckBox("Show Color Bar", this);
//		add(showColorBarCB, "span,wrap");

		// Action buttons
		resetAllB = GuiUtil.formButton(this, IconUtil.getActionReset());
		resetAllB.setToolTipText("Reset Min, Max");
		syncTB = GuiUtil.formToggleButton(this, IconUtil.getItemSyncFalse(), IconUtil.getItemSyncTrue());
		syncTB.setSelected(true);
		syncTB.setToolTipText("Keep synchronized");
		applyB = GuiUtil.formButton(this, "Apply");
		configB = GuiUtil.formButton(this, IconUtil.getActionConfig());
		configB.setToolTipText("Configure ColorBar");
		add(configB, "w 24!,h 24!,span,split");
		add(resetAllB, "w 24!,h 24!");
		add(syncTB, "w 24!,h 24!");
		add(applyB, "ax right,gapleft push,wrap 0");

		// Register for events of interest
		GuiExeUtil.executeOnceWhenShowing(this, () -> updateColorTableArea());
		refColorBarPainter.addListener(this);
	}

	/**
	 * Adds in a {@link FeatureType} to the supported list of features. The feature
	 * will be displayed in the combo box with the provided label.
	 */
	public void addFeatureType(FeatureType aType, String aLabel)
	{
		featureLCR.addMapping(aType, aLabel);
		featureBox.addItem(aType);
	}

	/**
	 * Returns the {@link ColorMapAttr} as configured in the gui.
	 */
	public ColorMapAttr getColorMapAttr()
	{
		ColorTable tmpColorTable = colorTableBox.getChosenItem();
		double minVal = minValueNF.getValue();
		double maxVal = maxValueNF.getValue();
		int numLevels = numLevelsNF.getValueAsInt(-1);
		boolean isLogScale = logScaleCB.isSelected();

		return new ColorMapAttr(tmpColorTable, minVal, maxVal, numLevels, isLogScale);
	}

	/**
	 * Returns the selected {@link FeatureType}.
	 * <P>
	 * The FeatureType is the physical quality attribute that coloring will be based
	 * off of.
	 */
	public FeatureType getFeatureType()
	{
		return featureBox.getChosenItem();
	}

	/**
	 * Returns the default (reset) min, max range for the specified
	 * {@link FeatureType}.
	 */
	public Range<Double> getResetRange(FeatureType aType)
	{
		return resetRangeM.get(aType);
	}

	/**
	 * Configures the gui to reflect the specified {@link ColorMapAttr}.
	 */
	public void setColorMapAttr(ColorMapAttr aColorMapAttr)
	{
		ColorTable tmpCT = aColorMapAttr.getColorTable();
		if (tmpCT != null)
			colorTableBox.setChosenItem(tmpCT);

		minValueNF.setValue(aColorMapAttr.getMinVal());
		maxValueNF.setValue(aColorMapAttr.getMaxVal());
		numLevelsNF.setValue(aColorMapAttr.getNumLevels());
		logScaleCB.setSelected(aColorMapAttr.getIsLogScale());
	}

	/**
	 * Sets in the selected {@link FeatureType}.
	 */
	public void setFeatureType(FeatureType aFeature)
	{
		featureBox.setChosenItem(aFeature);

		updateFeatureToolTips();
		updateControlArea();
	}

	/**
	 * Sets in the (default) reset min and max values for the specified
	 * {@link FeatureType}.
	 */
	public void setResetRange(FeatureType aType, Range<Double> aResetRange)
	{
		resetRangeM.put(aType, aResetRange);
		if (featureBox.getChosenItem() != aType)
			return;

		updateFeatureToolTips();
		updateControlArea();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == applyB)
			doActionApply();

		else if (source == configB)
			painterCBCP.setVisibleAsModal();

		else if (source == colorTableBox)
			doActionColorTableBox();

		else if (source == featureBox)
			doActionFeatureBox();

		else if (source == logScaleCB)
			doAutoSync();

		else if (source == resetAllB)
			doActionResetAll();
		else if (source == resetMinB)
			doActionResetMin();
		else if (source == resetMaxB)
			doActionResetMax();

		else if (source == syncTB)
			doAutoSync();

		else if (source == minValueNF || source == maxValueNF || source == numLevelsNF)
		{
			if (source == numLevelsNF)
				updateColorTableArea();

			doAutoSync();
		}

		else if (source == showColorBarCB)
			notifyListeners(this);

		updateControlArea();
	}

	@Override
	public void handleColorBarChanged(Object aSource, ColorBarChangeType aType)
	{
		if (aType == ColorBarChangeType.ColorMap)
			setColorMapAttr(refColorBarPainter.getColorMapAttr());

		notifyListeners(this);
	}

	/**
	 * Helper method that updates the colorTableL to reflect the selection of
	 * colorTableBox and numLevelsNF.
	 */
	protected void updateColorTableArea()
	{
		int iconW = colorTableL.getWidth();
		if (iconW < 16)
			iconW = 16;
		int iconH = colorTableL.getHeight();
		if (iconH < 16)
			iconH = 16;

		int numLevels = numLevelsNF.getValueAsInt(-1);
		if (numLevels == -1)
			numLevels = 0;

		ColorMapAttr tmpCMA = new ColorMapAttr(colorTableBox.getChosenItem(), 0.0, 1.0, numLevels, false);
		colorTableL.setIcon(ColorTableUtil.createIcon(tmpCMA, iconW, iconH));
	}

	/**
	 * Helper method that handles the Apply action.
	 */
	private void doActionApply()
	{
		cColorMapAttr = getColorMapAttr();
		cFeatureType = getFeatureType();

		notifyListeners(this);
	}

	/**
	 * Helper method that handles the colorTableBox action.
	 */
	private void doActionColorTableBox()
	{
		updateColorTableArea();

		doAutoSync();
	}

	/**
	 * Helper method that handles the featureBox action.
	 */
	private void doActionFeatureBox()
	{
		updateColorTableArea();

		// Retrieve the default min / max values
		double resetMin = Double.NaN;
		double resetMax = Double.NaN;
		Range<Double> resetRange = resetRangeM.get(featureBox.getChosenItem());
		if (resetRange != null)
		{
			resetMin = resetRange.lowerEndpoint();
			resetMax = resetRange.upperEndpoint();
		}

		// Update the min / max UI to reflect the default values
		minValueNF.setValue(resetMin);
		maxValueNF.setValue(resetMax);

		doAutoSync();
		updateFeatureToolTips();
	}

	/**
	 * Helper method that handles the min,max reset action.
	 */
	private void doActionResetAll()
	{
		double resetMin = Double.NaN;
		double resetMax = Double.NaN;
		Range<Double> resetRange = resetRangeM.get(featureBox.getChosenItem());
		if (resetRange != null)
		{
			resetMin = resetRange.lowerEndpoint();
			resetMax = resetRange.upperEndpoint();
		}

		minValueNF.setValue(resetMin);
		maxValueNF.setValue(resetMax);
		doAutoSync();
	}

	/**
	 * Helper method that handles the min reset action.
	 */
	private void doActionResetMin()
	{
		double resetMin = Double.NaN;
		Range<Double> resetRange = resetRangeM.get(featureBox.getChosenItem());
		if (resetRange != null)
			resetMin = resetRange.lowerEndpoint();

		minValueNF.setValue(resetMin);
		doAutoSync();
	}

	/**
	 * Helper method that handles the max reset action.
	 */
	private void doActionResetMax()
	{
		double resetMax = Double.NaN;
		Range<Double> resetRange = resetRangeM.get(featureBox.getChosenItem());
		if (resetRange != null)
			resetMax = resetRange.upperEndpoint();

		maxValueNF.setValue(resetMax);
		doAutoSync();
	}

	/**
	 * Helper method that will send out auto notification (to registered listeners)
	 * when the sync toggle is enabled.
	 */
	private void doAutoSync()
	{
		// Bail if the syncTB is not selected
		if (syncTB.isSelected() == false)
			return;

		// Bail if GUI is not in a valid state
		boolean isValidFeature = featureBox.getChosenItem() != FeatureType.Invalid;
		if (isValidFeature == true && isGuiValid() == false)
			return;

		// Delegate
		doActionApply();
	}

	/**
	 * Helper method to determine if the configuration as specified in the GUI is
	 * valid.
	 */
	private boolean isGuiValid()
	{
		boolean isValid = true;
		isValid &= minValueNF.isValidInput() == true;
		isValid &= maxValueNF.isValidInput() == true;
		isValid &= minValueNF.getValue() <= maxValueNF.getValue();
		isValid &= numLevelsNF.isValidInput() == true;

		return isValid;
	}

	/**
	 * Helper method to configure the various UI elements in the control section.
	 * <P>
	 * UI elements in the control area will be disabled for invalid configuration.
	 */
	private void updateControlArea()
	{
		boolean isEnabled, isSwapped;

		// Update MinValue MaxValue UI elements
		String errMsgMin = null;
		String errMsgMax = null;
		Color fgColorMin = Color.BLACK;
		Color fgColorMax = Color.BLACK;
		Color fgColorLev = Color.BLACK;
		Color fgColorFail = minValueNF.getFailColor();
		isSwapped = minValueNF.getValue() > maxValueNF.getValue();
		isSwapped &= minValueNF.isValidInput() == true;
		isSwapped &= maxValueNF.isValidInput() == true;
		if (isSwapped == true)
		{
			errMsgMin = errMsgMax = "Min, Max values are swapped.";
			fgColorMin = fgColorMax = fgColorFail;
		}
		if (minValueNF.getText().isEmpty() == true)
			fgColorMin = fgColorFail;
		if (maxValueNF.getText().isEmpty() == true)
			fgColorMax = fgColorFail;
		if (numLevelsNF.getText().isEmpty() == true)
			fgColorLev = fgColorFail;

		minValueL.setForeground(fgColorMin);
		maxValueL.setForeground(fgColorMax);
		numLevelsL.setForeground(fgColorLev);
		minValueL.setToolTipText(errMsgMin);
		maxValueL.setToolTipText(errMsgMax);

		// Update enable state of various UI elements
		boolean isValidFeature = featureBox.getSelectedItem() != FeatureType.Invalid;

		isEnabled = isValidFeature;
		GuiUtil.setEnabled(isEnabled, minValueL, minValueNF);
		GuiUtil.setEnabled(isEnabled, maxValueL, maxValueNF);
		GuiUtil.setEnabled(isEnabled, numLevelsL, numLevelsNF);
		GuiUtil.setEnabled(isEnabled, colorTableL, colorTableBox);
		configB.setEnabled(isEnabled);
		syncTB.setEnabled(isEnabled);

		double resetMin = Double.NaN;
		double resetMax = Double.NaN;
		Range<Double> resetRange = resetRangeM.get(featureBox.getChosenItem());
		if (resetRange != null)
		{
			resetMin = resetRange.lowerEndpoint();
			resetMax = resetRange.upperEndpoint();
		}

		isEnabled = isValidFeature == true && Double.compare(resetMin, minValueNF.getValue()) != 0;
		resetMinB.setEnabled(isEnabled);

		isEnabled = isValidFeature == true && Double.compare(resetMax, maxValueNF.getValue()) != 0;
		resetMaxB.setEnabled(isEnabled);

		isEnabled = resetMinB.isEnabled() == true || resetMaxB.isEnabled() == true;
		resetAllB.setEnabled(isEnabled);

		boolean isChanged;
		isChanged = cFeatureType != featureBox.getChosenItem();
		isChanged |= Objects.equals(cColorMapAttr, getColorMapAttr()) == false;

		isEnabled = syncTB.isSelected() == false && isChanged == true;
		isEnabled &= isGuiValid() == true;
		applyB.setEnabled(isEnabled);
	}

	/**
	 * Helper method to update the tool tips of the reset buttons.
	 */
	private void updateFeatureToolTips()
	{
		double resetMin = Double.NaN;
		double resetMax = Double.NaN;
		Range<Double> resetRange = resetRangeM.get(featureBox.getChosenItem());
		if (resetRange != null)
		{
			resetMin = resetRange.lowerEndpoint();
			resetMax = resetRange.upperEndpoint();
		}

		NumberFormat tmpNF = new SigFigNumberFormat(5);
		resetMinB.setToolTipText(ToolTipUtil.getItemResetMinVal(tmpNF, resetMin));
		resetMaxB.setToolTipText(ToolTipUtil.getItemResetMaxVal(tmpNF, resetMax));
	}

}
