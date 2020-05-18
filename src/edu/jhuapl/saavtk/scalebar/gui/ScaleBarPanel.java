package edu.jhuapl.saavtk.scalebar.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import edu.jhuapl.saavtk.camera.ViewActionListener;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.Colors;
import edu.jhuapl.saavtk.scalebar.ScaleBarPainter;
import glum.gui.GuiUtil;
import glum.gui.component.GNumberField;
import glum.gui.component.GNumberFieldSlider;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows for configuration of a {@link ScaleBarPainter}.
 *
 * @author lopeznr1
 */
public class ScaleBarPanel extends JPanel implements ActionListener, ViewActionListener
{
	// Constants
	private static final Range<Double> RangeDecimalPlaces = Range.closed(0.0, 20.0);
	private static final Range<Double> RangeSizeX = Range.closed(50.0, 999.0);
	private static final Range<Double> RangeSizeY = Range.closed(12.0, 120.0);
	private static final int NumCols = 5;

	// Ref vars
	private final Renderer refRenderer;
	private final ScaleBarPainter refScaleBarPainter;

	// Gui vars
	private final JCheckBox showCB;
	private final GNumberFieldSlider xSizeNFS;
	private final GNumberFieldSlider ySizeNFS;
	private final GNumberField spanNF;
	private final GNumberField numDecimalPlacesNF;
	private final JLabel spanHeadL, spanTailL;
	private final JLabel statusL;

	/**
	 * Standard Constructor
	 */
	public ScaleBarPanel(Renderer aRenderer, ScaleBarPainter aScaleBarPainter)
	{
		refRenderer = aRenderer;
		refScaleBarPainter = aScaleBarPainter;

		setLayout(new MigLayout("", "[right][grow][]", "[]"));

		// Scale Bar area
		showCB = GuiUtil.createJCheckBox("Show Scale Bar", this);
		add(showCB, "center,span,wrap");

		JLabel xSizeL = new JLabel("X-Size:");
		xSizeNFS = new GNumberFieldSlider(this, new DecimalFormat("#.##"), RangeSizeX, NumCols);
		xSizeNFS.setIntegralSteps();
		add(xSizeL, "");
		add(xSizeNFS, "growx");
		add(new JLabel("pixels"), ",sgy G1,wrap");

		JLabel ySizeL = new JLabel("Y-Size:");
		ySizeNFS = new GNumberFieldSlider(this, new DecimalFormat("#"), RangeSizeY, NumCols);
		ySizeNFS.setIntegralSteps();
		add(ySizeL, "");
		add(ySizeNFS, "growx");
		add(new JLabel("pixels"), ",sgy G1,wrap");

		spanHeadL = new JLabel("Span:");
		spanTailL = new JLabel("meters");
		spanNF = new GNumberField(this, new DecimalFormat("#.##"));
		add(spanHeadL, "");
		add(spanNF, "growx");
		add(spanTailL, "span,split,wrap");

		JLabel numDecimalPlacesL = new JLabel("Decimal Places:");
		numDecimalPlacesNF = new GNumberField(this, new DecimalFormat("#"), RangeDecimalPlaces);
		add(numDecimalPlacesL, "span,split");
		add(numDecimalPlacesNF, "growx,wrap");

		statusL = new JLabel("");
		add(statusL, "growx,sgy G1,span,w 0::,wrap 0");

		// Register for events of interest
		refRenderer.addViewChangeListener(this);

		syncGuiToModel();
		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == xSizeNFS || source == ySizeNFS)
			doActionChangeSize();
		else if (source == spanNF)
			doActionChangeSpan();
		else if (source == numDecimalPlacesNF)
			doActionNumDecimalPlaces();
		else if (source == showCB)
			doActionShow();

		updateGui();
	}

	@Override
	public void handleViewAction(Object aSource)
	{
		syncGuiToModel();
		updateGui();
	}

	/**
	 * Helper method to handle action associated with changing the scale bar size.
	 */
	private void doActionChangeSize()
	{
		// Bail if there are errors
		if (xSizeNFS.isValidInput() == false)
			return;
		if (ySizeNFS.isValidInput() == false)
			return;

		// Update the refScaleBarPainter
		double tmpSizeX = xSizeNFS.getValue();
		double tmpSizeY = ySizeNFS.getValue();
		refScaleBarPainter.setSize(tmpSizeX, tmpSizeY);

		// Update the spanNF
		double tmpPixelSpan = refRenderer.getNominalPixelSpan() * 1000;
		double fullSpan = tmpPixelSpan * tmpSizeX;
		spanNF.setValue(fullSpan);
	}

	/**
	 * Helper method to handle action associated with changing the scale bar size.
	 */
	private void doActionChangeSpan()
	{
		// Bail if there are errors
		boolean isValid = true;
		isValid &= spanNF.isValidInput() == true;
		isValid &= ySizeNFS.isValidInput() == true;
		if (isValid == false)
			return;

		// Update the scale bar's dimension to reflect the (full) span
		double fullSpan = spanNF.getValue();
		double tmpPixelSpan = refRenderer.getNominalPixelSpan() * 1000;
		double tmpSizeX = fullSpan / tmpPixelSpan;

		double tmpSizeY = ySizeNFS.getValue();
		refScaleBarPainter.setSize(tmpSizeX, tmpSizeY);

		// Update the xSizeNFS
		xSizeNFS.setValue(tmpSizeX);
	}

	/**
	 * Helper method to handle action associated with display formatting.
	 */
	private void doActionNumDecimalPlaces()
	{
		// Bail if there are errors
		boolean isValid = true;
		isValid &= numDecimalPlacesNF.isValidInput() == true;
		if (isValid == false)
			return;

		int numDecimalPlaces = numDecimalPlacesNF.getValueAsInt(-1);
		String fmtStr = "0." + Strings.repeat("0", numDecimalPlaces);
		if (fmtStr.length() == 2)
			fmtStr = "0";
		NumberFormat tmpNF = new DecimalFormat(fmtStr);

		refScaleBarPainter.setDispNumberFormat(tmpNF);
	}

	/**
	 * Helper method to handle the apply show checkbox.
	 */
	private void doActionShow()
	{
		boolean isVisible = showCB.isSelected();
		refScaleBarPainter.setIsVisible(isVisible);
	}

	/**
	 * Helper method that will return a string describing invalid user input.
	 * <P>
	 * If all input is valid then null will be returned.
	 */
	private String getErrorMsg()
	{
		if (showCB.isSelected() == false)
			return "Scale bar is disabled.";

		if (xSizeNFS.isValidInput() == false)
			return String.format("Invalid X-Size. Range: [%1.0f, %1.0f]", RangeSizeX.lowerEndpoint(),
					RangeSizeX.upperEndpoint());

		if (ySizeNFS.isValidInput() == false)
			return String.format("Invalid Y-Size. Range: [%1.0f, %1.0f]", RangeSizeY.lowerEndpoint(),
					RangeSizeY.upperEndpoint());

		double tmpPixelSpan = refRenderer.getNominalPixelSpan() * 1000;
		if (Double.isNaN(tmpPixelSpan) == true)
			return "Surface must fully cover view.";

		Range<Double> spanRange = spanNF.getMinMaxRange();
		if (spanNF.isValidInput() == false)
			return String.format("Invalid Span. Range: [%1.2f, %1.2f]", spanRange.lowerEndpoint(),
					spanRange.upperEndpoint());

		if (numDecimalPlacesNF.isValidInput() == false)
			return String.format("Invalid num. Range: [%1.0f, %1.0f]", RangeDecimalPlaces.lowerEndpoint(),
					RangeDecimalPlaces.upperEndpoint());

		return null;
	}

	/**
	 * Helper method that will synchronize the GUI with the model.
	 */
	private void syncGuiToModel()
	{
		boolean isVisible = refScaleBarPainter.getIsVisible();
		showCB.setSelected(isVisible);

		double tmpSizeX = refScaleBarPainter.getSizeX();
		xSizeNFS.setValue(tmpSizeX);

		double tmpSizeY = refScaleBarPainter.getSizeY();
		ySizeNFS.setValue(tmpSizeY);

		double tmpPixelSpan = refRenderer.getNominalPixelSpan() * 1000;
		double spanMinVal = tmpPixelSpan * RangeSizeX.lowerEndpoint() - 0.01;
		double spanMaxVal = tmpPixelSpan * RangeSizeX.upperEndpoint() + 0.01;
		Range<Double> spanRange = Range.closed(spanMinVal, spanMaxVal);
		spanNF.setMinMaxRange(spanRange);

		NumberFormat tmpNF = refScaleBarPainter.getDispNumberFormat();
		int numDecimalPlaces = tmpNF.getMaximumFractionDigits();
		numDecimalPlacesNF.setValue(numDecimalPlaces);

		double fullSpan = tmpPixelSpan * tmpSizeX;
		spanNF.setValue(fullSpan);
	}

	/**
	 * Helper method that keeps the GUI synchronized with user input.
	 */
	private void updateGui()
	{
		// Update the status area
		String tmpMsg = null;
		String errMsg = getErrorMsg();
		if (errMsg != null)
			tmpMsg = errMsg;
		statusL.setText(tmpMsg);

		double tmpPixelSpan = refRenderer.getNominalPixelSpan() * 1000;
		boolean isEnabled = Double.isNaN(tmpPixelSpan) == false;
		spanNF.setEnabled(isEnabled);
		spanHeadL.setEnabled(isEnabled);
		spanTailL.setEnabled(isEnabled);

		Color fgColor = Colors.getPassFG();
		if (errMsg != null)
			fgColor = Colors.getFailFG();
		statusL.setForeground(fgColor);
	}

}
