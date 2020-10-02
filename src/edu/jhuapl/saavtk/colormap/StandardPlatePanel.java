package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import edu.jhuapl.saavtk.color.gui.ColorBarConfigPanel;
import edu.jhuapl.saavtk.color.gui.ColorBarPanel;
import edu.jhuapl.saavtk.color.gui.ColorTableListCellRenderer;
import edu.jhuapl.saavtk.color.painter.ColorBarChangeListener;
import edu.jhuapl.saavtk.color.painter.ColorBarChangeType;
import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.color.table.ColorTable;
import edu.jhuapl.saavtk.color.table.ColorTableUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import glum.gui.GuiExeUtil;
import glum.gui.GuiUtil;
import glum.gui.component.GComboBox;
import glum.gui.component.GNumberField;
import glum.gui.panel.GPanel;
import glum.text.SigFigNumberFormat;
import net.miginfocom.swing.MigLayout;

/**
 * Class that provides UI controls and mechanism for the standard plate coloring
 * panel.
 * <P>
 * This class is very similar to {@link ColorBarPanel} and will eventually be
 * removed.
 *
 * @author lopeznr1
 */
public class StandardPlatePanel extends GPanel implements ActionListener, ColorBarChangeListener
{
	// Ref vars
	private final Renderer refRenderer;
	private final ColorBarPainter refColorBarPainter;

	// State vars
	private double defaultMin, defaultMax;

	// Cache vars
	private int cColoringIdx;
	private ColorMapAttr cColorMapAttr;

	// GUI vars
	private final ColorBarConfigPanel painterCBCP;
	private final JLabel colorTableL;
	private final GComboBox<ColorTable> colorTableBox;
	private final JLabel minValueL, maxValueL, numLevelsL;
	private final GNumberField minValueNF, maxValueNF;
	private final GNumberField numLevelsNF;
	private final JCheckBox logScaleCB;
	private final JButton resetB;
	private final JButton applyB, configB;
	private final JToggleButton syncTB;

	/** Standard Constructor */
	public StandardPlatePanel(Renderer aRenderer)
	{
		refRenderer = aRenderer;
		refColorBarPainter = new ColorBarPainter(aRenderer);

		defaultMin = Double.NaN;
		defaultMax = Double.NaN;

		cColoringIdx = -1;
		cColorMapAttr = ColorMapAttr.Invalid;

		// Instantiate the various GUI controls
		painterCBCP = new ColorBarConfigPanel(this, refColorBarPainter);
		painterCBCP.addActionListener(this);

		// Color table area
		colorTableL = new JLabel();
		colorTableBox = new GComboBox<>(this, ColorTableUtil.getSystemColorTableList());
		colorTableBox.setRenderer(new ColorTableListCellRenderer(colorTableBox));
		colorTableBox.setChosenItem(ColorTableUtil.getSystemColorTableDefault());

		minValueL = new JLabel("Min:");
		minValueNF = new GNumberField(this, new SigFigNumberFormat(5));

		maxValueL = new JLabel("Max:");
		maxValueNF = new GNumberField(this, new SigFigNumberFormat(5));

		numLevelsL = new JLabel("# Levels:");
		numLevelsNF = new GNumberField(this);
		numLevelsNF.setValue(32);

		logScaleCB = new JCheckBox("Log scale");
		logScaleCB.addActionListener(this);

		resetB = GuiUtil.formButton(this, IconUtil.getActionReset());
		resetB.setToolTipText("Reset Min, Max");
		syncTB = GuiUtil.formToggleButton(this, IconUtil.getItemSyncFalse(), IconUtil.getItemSyncTrue());
		syncTB.setSelected(true);
		syncTB.setToolTipText("Keep synchronized");
		applyB = GuiUtil.formButton(this, "Apply");
		configB = GuiUtil.formButton(this, IconUtil.getActionConfig());
		configB.setToolTipText("Configure ColorBar");

		// Construct the GUI
		buildGui();

		// Register for events of interest
		GuiExeUtil.executeOnceWhenShowing(this, () -> updateColorTableArea());
		refColorBarPainter.addListener(this);
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
	 * Switches this panel to reflect the the specified coloring index.
	 *
	 * @param aColoringIdx
	 * @param aDefaultMin
	 * @param aDefaultMax
	 */
	public void switchToColoring(PolyhedralModel aSmallBody, int aColoringIdx, double aDefaultMin, double aDefaultMax)
	{
		// Bail if coloring index has not changed
		if (cColoringIdx == aColoringIdx)
			return;
		cColoringIdx = aColoringIdx;

		// Update our defaults
		defaultMin = aDefaultMin;
		defaultMax = aDefaultMax;
		minValueNF.setValue(defaultMin);
		maxValueNF.setValue(defaultMax);

		// Update the title
		if (cColoringIdx >= 0)
		{
			String title = aSmallBody.getColoringName(cColoringIdx).trim();
			String units = aSmallBody.getColoringUnits(cColoringIdx).trim();
			if (units.isEmpty() == false)
				title += " (" + units + ")";
			refColorBarPainter.setTitle(title);
		}

		// Show the ColorBarPainter (if appropriate)
		if (aSmallBody.isColoringDataAvailable() == true && cColoringIdx >= 0)
		{
			refColorBarPainter.setColorMapAttr(aSmallBody.getColorMapAttr());
			refRenderer.addVtkPropProvider(refColorBarPainter);
		}
		else
		{
			refRenderer.delVtkPropProvider(refColorBarPainter);
		}

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

		else if (source == logScaleCB)
			doAutoSync();

		if (source == resetB)
			doActionReset();

		else if (source == syncTB)
			doAutoSync();

		else if (source == minValueNF || source == maxValueNF || source == numLevelsNF)
		{
			if (source == numLevelsNF)
				updateColorTableArea();

			doAutoSync();
		}

		updateControlArea();
	}

	@Override
	public void handleColorBarChanged(Object aSource, ColorBarChangeType aType)
	{
		if (aType == ColorBarChangeType.ColorMap)
			setColorMapAttr(refColorBarPainter.getColorMapAttr());

		notifyListeners(this);
	}

	@Override
	public void setEnabled(boolean aEnabled)
	{
		GuiUtil.setEnabled(this, aEnabled);

		if (aEnabled == true)
		{
			updateControlArea();
			updateColorTableArea();
		}

		// Show the ColorBarPainter (if appropriate)
		if (aEnabled == true && cColoringIdx >= 0)
			refRenderer.addVtkPropProvider(refColorBarPainter);
		if (aEnabled == false)
			refRenderer.delVtkPropProvider(refColorBarPainter);
	}

	/**
	 * Helper method which layouts the panel.
	 */
	private void buildGui()
	{
		setLayout(new MigLayout("", "0[right][]0", "0[][]"));

		// ColorTable area
		add(colorTableL, "growx,span,w 10::,wrap 3");
		add(colorTableBox, "growx,span,w 0:0:,wrap");

		// Range, # Color levels, and # Ticks area
		add(minValueL, "");
		add(minValueNF, "growx,pushx,wrap");

		add(maxValueL, "");
		add(maxValueNF, "growx,wrap");

		add(numLevelsL, "ax right");
		add(numLevelsNF, "growx,split");
		add(logScaleCB, "gapx 20,w 20::,wrap");

		add(configB, "w 24!,h 24!,span,split");
		add(resetB, "w 24!,h 24!");
		add(syncTB, "w 24!,h 24!");
		add(applyB, "ax right,gapleft push,wrap 0");
	}

	/**
	 * Helper method that handles the Apply action.
	 */
	private void doActionApply()
	{
		cColorMapAttr = getColorMapAttr();
//		cFeatureType = getFeatureType();
		refColorBarPainter.setColorMapAttr(cColorMapAttr);

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
	 * Helper method that handles the min reset action.
	 */
	private void doActionReset()
	{
		ColorMapAttr tmpCMA = getColorMapAttr();
		tmpCMA = new ColorMapAttr(tmpCMA.getColorTable(), defaultMin, defaultMax, tmpCMA.getNumLevels(),
				tmpCMA.getIsLogScale());
		setColorMapAttr(tmpCMA);

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
		if (isGuiValid() == false)
			return;

		// Delegate
		doActionApply();
	}

	/**
	 * Helper method to determine if the configuration as specified in the UI is
	 * valid.
	 */
	private boolean isGuiValid()
	{
		boolean isValid = true;
		isValid &= minValueNF.isValidInput();
		isValid &= maxValueNF.isValidInput();
		isValid &= minValueNF.getValue() <= maxValueNF.getValue();
		isValid &= numLevelsNF.isValidInput();

		return isValid;
	}

	/**
	 * Helper method that updates the colorTableL to reflect the selection of
	 * colorTableBox and numLevelsNF.
	 */
	protected void updateColorTableArea()
	{
		int iconW = colorTableBox.getWidth();
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
	 * Helper method to configure the various UI elements in the control section.
	 * <P>
	 * UI elements in the control area will be disabled for invalid configuration.
	 */
	private void updateControlArea()
	{
		boolean isEnabled, isSwapped;

		// Update MinValue MaxValue UI elements
		String errMsg = null;
		Color fgColor = Color.BLACK;
		isSwapped = minValueNF.getValue() > maxValueNF.getValue();
		isSwapped &= minValueNF.isValidInput() == true;
		isSwapped &= maxValueNF.isValidInput() == true;
		if (isSwapped == true)
		{
			errMsg = "Min, Max values are swapped.";
			fgColor = minValueNF.getFailColor();
		}
		minValueL.setForeground(fgColor);
		maxValueL.setForeground(fgColor);
		minValueL.setToolTipText(errMsg);
		maxValueL.setToolTipText(errMsg);

		isEnabled = false;
		isEnabled |= Double.compare(defaultMin, minValueNF.getValue()) != 0;
		isEnabled |= Double.compare(defaultMax, maxValueNF.getValue()) != 0;
		resetB.setEnabled(isEnabled);

		// Update enable state of applyB
		isEnabled = syncTB.isSelected() != true;
		isEnabled &= isGuiValid();
		applyB.setEnabled(isEnabled);
	}

}
