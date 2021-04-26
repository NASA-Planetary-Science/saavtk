package edu.jhuapl.saavtk.color.gui.bar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.color.painter.ColorBarDrawUtil;
import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.gui.util.Colors;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.gui.GuiUtil;
import glum.gui.component.GNumberField;
import glum.gui.component.GNumberFieldSlider;
import glum.gui.panel.CardPanel;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;
import plotkit.cadence.Cadence;
import plotkit.cadence.ModelStepIterator;
import plotkit.cadence.PlainModelCadence;

/**
 * Panel that allows a user to configure "details" of a {@link ColorBarPainter}.
 *
 * @author lopeznr1
 */
public class LayoutAttrPanel extends GPanel implements ActionListener, ItemListener
{
	// Constants
	private static final Range<Double> RangeBarLength = Range.closed(50.0, 2500.0);
	private static final Range<Double> RangeBarWidth = Range.closed(10.0, 200.0);
	private static final Range<Double> RangeNumTicks = Range.closed(0.0, ColorBarPainter.MaxNumTicks + 0.0);
	private static final Range<Double> RangeCadenceBeat = Range.openClosed(0.0, Double.POSITIVE_INFINITY);

	private static final String MsgErrInvalidBeat = "Please specify a valid cadence beat.";
	private static final String MsgErrInvalidMark = "Please specify a valid cadence mark.";
	private static final String MsgErrInvalidMarkMustBePositive = "Cadence beat must be (none zero) positive";
	private static final String MsgErrInvalidMinMax = "Invalid Min, Max vaules. Min < Max";
	private static final String MsgErrLogAxisMustBePositive = "Log axis is not compatible with values \u2264 0";
	private static final String MsgWrnBeatIsTooLarge = "Beat is too large. Axis values will not be displayed.";
	private static final String MsgWrnBeatIsTooSmall = "Beat is too small. Axis will be partial.";
	private static final String MsgWrnMarkIsNotShown = "Mark will not appear on axis.";

	private static final String ShowCadencePanel = "CadencePanel";
	private static final String ShowNumTicksPanel = "NumTicksPanel";

	private static final String ToolTipBeat = "Define the frequency of the ticks.";
	private static final String ToolTipMark = "Define the value used to align the ticks.";

	// Gui vars
	private final JCheckBox isHorizontalLayoutCB;
	private final JLabel numTicksL, barLengthL, barWidthL;
	private final JCheckBox revDisplayCB, revColoringCB;
	private final CardPanel<JPanel> axisModeCP;
	private final JRadioButton cadModeRB, logModeRB, stdModeRB;
	private final JLabel cadenceMarkL, cadenceBeatL;
	private final GNumberField cadenceBeatNF, cadenceMarkNF;
	private final GNumberFieldSlider numTicksNFS, barLengthNFS, barWidthNFS;
	private final JLabel statusL;

	// State vars
	private ColorMapAttr workCMA;
	private LayoutAttr workLA;

	/** Standard Constructor */
	public LayoutAttrPanel()
	{
		// Form the gui
		setLayout(new MigLayout("", "[]", "[]"));

		// GUI: orientation, reverse coloring/display
		isHorizontalLayoutCB = GuiUtil.createJCheckBox("Horizontal Layout", this);
		add(isHorizontalLayoutCB, "ax left,span,split,wrap");

		revColoringCB = GuiUtil.createJCheckBox("Rev. Colors", this);
		revColoringCB.setEnabled(false);
		revColoringCB.setToolTipText(ToolTipUtil.getFutureFunctionality());
		revDisplayCB = GuiUtil.createJCheckBox("Rev. Display", this);
		add(revColoringCB, "ax left,span,split");
		add(revDisplayCB, "ax right,gapx 40:,wrap");

		// GUI: Axis Mode
		add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");

		JLabel modeL = new JLabel("Axis Mode:");
		cadModeRB = GuiUtil.createJRadioButton(this, "Cadence");
		logModeRB = GuiUtil.createJRadioButton(this, "Logarithmic");
		stdModeRB = GuiUtil.createJRadioButton(this, "Standard");
		GuiUtil.linkRadioButtons(cadModeRB, logModeRB, stdModeRB);
		add(modeL, "span,split");
		add(cadModeRB, "");
		add(logModeRB, "");
		add(stdModeRB, "wrap");

		cadenceBeatL = new JLabel("Beat:");
		cadenceBeatL.setToolTipText(ToolTipBeat);
		cadenceBeatNF = new GNumberField(this, new DecimalFormat("#.#######"), RangeCadenceBeat);
		cadenceMarkL = new JLabel("Mark:");
		cadenceMarkL.setToolTipText(ToolTipMark);
		cadenceMarkNF = new GNumberField(this);
		JPanel cadencePanel = formCadencePanel();

		numTicksL = new JLabel("Num Ticks:");
		numTicksNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), RangeNumTicks);
		numTicksNFS.setIntegralSteps();
		JPanel numTicksPanel = formNumTicksPanel();

		axisModeCP = new CardPanel<>();
		axisModeCP.addCard(ShowCadencePanel, cadencePanel);
		axisModeCP.addCard(ShowNumTicksPanel, numTicksPanel);
		add(axisModeCP, "span,growx,wrap");

		// GUI: Num ticks, bar length, bar width
		add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");

		barLengthL = new JLabel("Bar Length:");
		barLengthNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), RangeBarLength);
		barLengthNFS.setIntegralSteps();
		add(barLengthL, "ax right,sgy G1");
		add(barLengthNFS, "growx,pushx,span,wrap");

		barWidthL = new JLabel("Bar Width:");
		barWidthNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), RangeBarWidth);
		barWidthNFS.setIntegralSteps();
		add(barWidthL, "ax right");
		add(barWidthNFS, "growx,span,wrap");

		// GUI: status
		statusL = new JLabel("");
		add(statusL, "growx,sgy G1,span,w 40::");

		workCMA = ColorMapAttr.Invalid;
		workLA = LayoutAttr.Invalid;

		updateGui();
	}

	/**
	 * Returns the ColorMa
	 */
	public ColorMapAttr getColorMapAttr()
	{
		boolean isLogScale = logModeRB.isSelected();
		if (workCMA.getIsLogScale() == isLogScale)
			return workCMA;

		return new ColorMapAttr(workCMA.getColorTable(), workCMA.getMinVal(), workCMA.getMaxVal(), workCMA.getNumLevels(),
				isLogScale);
	}

	/**
	 * Returns the {@link LayoutAttr} as configured in the GUI.
	 */
	public LayoutAttr getLayoutAttr()
	{
		boolean isHoriz = isHorizontalLayoutCB.isSelected();
//		boolean isRevColorBar = revColoringCB.isSelected();
		boolean isRevDisplay = revDisplayCB.isSelected();

		int numTicks = numTicksNFS.getValueAsInt(0);
		boolean isCadence = cadModeRB.isSelected();
		double tmpMark = cadenceMarkNF.getValue();
		double tmpBeat = cadenceBeatNF.getValue();
		Cadence tmpCadence = Cadence.Invalid;
		if (tmpBeat != 0 && Double.isNaN(tmpBeat) == false && Double.isNaN(tmpMark) == false)
			tmpCadence = new PlainModelCadence(tmpBeat, tmpMark);

		int barLength = barLengthNFS.getValueAsInt(RangeBarLength.lowerEndpoint().intValue());
		int barWidth = barWidthNFS.getValueAsInt(RangeBarWidth.lowerEndpoint().intValue());

		LayoutAttr retDA = new LayoutAttr(isHoriz, isRevDisplay, numTicks, barLength, barWidth, isCadence, tmpCadence);
		return retDA;
	}

	/**
	 * Configures the GUI to reflect the specified {@link FontAttr}.
	 */
	public void setLayoutAttr(LayoutAttr aTmpLA)
	{
		if (Objects.equals(workLA, aTmpLA) == true)
			return;
		workLA = aTmpLA;

		isHorizontalLayoutCB.setSelected(aTmpLA.getIsHorizontal());

		numTicksNFS.setValue(aTmpLA.getNumTicks());

		if (aTmpLA.getCadence() instanceof PlainModelCadence)
		{
			PlainModelCadence tmpCadence = (PlainModelCadence) aTmpLA.getCadence();

			cadenceBeatNF.setValue(tmpCadence.getBeat());
			cadenceMarkNF.setValue(tmpCadence.getAlignValue());
		}

		barLengthNFS.setValue(aTmpLA.getBarLength());
		barWidthNFS.setValue(aTmpLA.getBarWidth());

		revDisplayCB.setSelected(aTmpLA.getIsReverseOrder());
//		revColoringCB.setSelected(aAttr.getIsReverseColoring());

		updateAxisModeGui();
		updateGui();
	}

	/**
	 * Sets in the working {@link ColorMapAttr}.
	 */
	public void setWorkColorMapAttr(ColorMapAttr aTmpCMA)
	{
		if (Objects.equals(workCMA, aTmpCMA) == true)
			return;
		workCMA = aTmpCMA;

		updateAxisModeGui();
		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		notifyListeners(this, 0);
		updateGui();
	}

	@Override
	public void itemStateChanged(ItemEvent aEvent)
	{
		// Ignore deselects
		if (aEvent.getStateChange() == ItemEvent.DESELECTED)
			return;

		Object source = aEvent.getSource();
		if (source == cadModeRB && cadModeRB.isSelected() == true)
			axisModeCP.switchToCard(ShowCadencePanel);
		else if (source == logModeRB && logModeRB.isSelected() == true)
			axisModeCP.switchToCard(ShowNumTicksPanel);
		else if (source == stdModeRB && stdModeRB.isSelected() == true)
			axisModeCP.switchToCard(ShowNumTicksPanel);

		notifyListeners(this, 0);
		updateGui();
	}

	/**
	 * Helper method that creates and returns the cadence panel.
	 */
	private JPanel formCadencePanel()
	{
		JPanel retPanel = new JPanel();
		retPanel.setLayout(new MigLayout("", "[]", "[]"));

		retPanel.add(cadenceMarkL, "");
		retPanel.add(cadenceMarkNF, "growx,pushx,w 70::250");
		retPanel.add(cadenceBeatL, "gapx 30");
		retPanel.add(cadenceBeatNF, "growx,pushx,w 70::250");
		return retPanel;
	}

	/**
	 * Helper method that creates and returns the "num ticks" panel.
	 */
	private JPanel formNumTicksPanel()
	{
		JPanel retPanel = new JPanel();
		retPanel.setLayout(new MigLayout("", "[]", "[]"));

		// numTicksL = new JLabel("Num Ticks:");
		retPanel.add(numTicksL, "ax right");
		retPanel.add(numTicksNFS, "growx,pushx,span");
		return retPanel;
	}

	/**
	 * Helper method that returns a string describing the most pressing error.
	 * <P>
	 * Returns null if there are no errors.
	 */
	private String getFailMsg()
	{
		boolean isCadMode = cadModeRB.isSelected() == true;
		if (workCMA.getIsLogScale() == false && isCadMode == true)
		{
			if (cadenceMarkNF.isValidInput() == false)
				return MsgErrInvalidMark;
			if (cadenceBeatNF.getText().isEmpty() == true)
				return MsgErrInvalidBeat;
			if (cadenceBeatNF.isValidInput() == false)
				return MsgErrInvalidMarkMustBePositive;
		}

		return null;
	}

	/**
	 * Helper method that returns a string describing the most pressing warning.
	 * <P>
	 * Returns null if there are no warnings.
	 */
	private String getWarnMsg()
	{
		// Ensure the min and max values are not the same
		if (workCMA.getMinVal() >= workCMA.getMaxVal())
			return MsgErrInvalidMinMax;

		// Ensure min and / or max values are not negative when in log mode
		if (workCMA.getIsLogScale() == true)
		{
			if (workCMA.getMinVal() <= 0 || workCMA.getMaxVal() <= 0)
				return MsgErrLogAxisMustBePositive;
		}

		// Check for cadence related warning conditions
		LayoutAttr tmpLA = getLayoutAttr();
		Cadence tmpCadence = tmpLA.getCadence();
		if (tmpLA.getIsCadenceEnabled() == true && tmpCadence instanceof PlainModelCadence)
		{
			double cadBeat = ((PlainModelCadence) tmpCadence).getBeat();
			double cadMark = ((PlainModelCadence) tmpCadence).getAlignValue();
			double minVal = workCMA.getMinVal();
			double maxVal = workCMA.getMaxVal();

			int numLabels = ColorBarDrawUtil.computeNumLabelsNeededFor(tmpLA, workCMA);
			if (numLabels <= 1)
			{
				ModelStepIterator tmpIter = new ModelStepIterator(cadBeat, minVal, maxVal, cadMark);
				if (tmpIter.hasNext() == false)
					return MsgWrnBeatIsTooLarge;
			}
			if (numLabels > ColorBarPainter.MaxNumTicks)
				return MsgWrnBeatIsTooSmall;

			if (cadMark < minVal || cadMark > maxVal)
				return MsgWrnMarkIsNotShown;
		}

		return null;
	}

	/**
	 * Helper method to keep the axis mode GUI synchronized.
	 */
	private void updateAxisModeGui()
	{
		// Update to reflect the axis mode
		if (workCMA.getIsLogScale() == true)
		{
			logModeRB.setSelected(true);
			axisModeCP.switchToCard(ShowNumTicksPanel);
		}
		else if (workLA.getIsCadenceEnabled() == true)
		{
			cadModeRB.setSelected(true);
			axisModeCP.switchToCard(ShowCadencePanel);
		}
		else
		{
			stdModeRB.setSelected(true);
			axisModeCP.switchToCard(ShowNumTicksPanel);
		}
	}

	/**
	 * Helper method to keep the GUI synchronized.
	 */
	private void updateGui()
	{
		// Determine if there are any issues
		String errMsg = getFailMsg();
		String wrnMsg = getWarnMsg();

		Color tmpColor = Colors.getPassFG();
		if (errMsg != null)
			tmpColor = Colors.getFailFG();
		else if (wrnMsg != null)
			tmpColor = Colors.getInfoFG();

		String tmpMsg = errMsg;
		if (tmpMsg == null)
			tmpMsg = wrnMsg;

		statusL.setForeground(tmpColor);
		statusL.setText(tmpMsg);
	}

}
