package edu.jhuapl.saavtk.grid.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JLabel;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.grid.GridAttr;
import glum.gui.component.GNumberFieldSlider;
import glum.gui.panel.ColorInputPanel;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows a user to configure a {@link GridAttr}.
 *
 * @author lopeznr1
 */
public class GridAttrPanel extends GPanel implements ActionListener
{
	// Constants
	private static final Range<Double> LineWidthRange = Range.closed(1.0, 100.0);
	private static final Range<Double> ShiftFactorRange = Range.closed(0.0, 300.0);

	// Gui vars
	private final ColorInputPanel colorCIP;
	private final GNumberFieldSlider lineWidthNFS;
	private final GNumberFieldSlider shiftFactorNFS;

	/** Standard Constructor */
	public GridAttrPanel()
	{
		// Form the gui
		setLayout(new MigLayout("", "[]", "[]"));

		colorCIP = new ColorInputPanel(true, true, false);
		colorCIP.addActionListener(this);
		add(colorCIP, "growx,span,wrap");

		var lineWidthL = new JLabel("Line Width:");
		lineWidthNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), LineWidthRange);
		lineWidthNFS.setIntegralSteps();
		add(lineWidthL, "span,split");
		add(lineWidthNFS, "growx,pushx,span,wrap");

		var shiftFactorL = new JLabel("Shift Factor:");
		shiftFactorNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), ShiftFactorRange);
		shiftFactorNFS.setIntegralSteps();
		add(shiftFactorL, "span,split");
		add(shiftFactorNFS, "growx,pushx,span");
	}

	/**
	 * Returns the {@link GridAttr} as configured in the GUI.
	 */
	public GridAttr getGridAttr()
	{
		var isVisible = true;
		var color = colorCIP.getColorConfig();
		var lineWidth = lineWidthNFS.getValueAsInt(LineWidthRange.lowerEndpoint().intValue());
		var shiftFactor = shiftFactorNFS.getValueAsInt(ShiftFactorRange.lowerEndpoint().intValue());
		return new GridAttr(isVisible, color, lineWidth, shiftFactor);
	}

	/**
	 * Configures the GUI to reflect the specified {@link GridAttr}.
	 */
	public void setGridAttr(GridAttr aAttr)
	{
		colorCIP.setColorConfig(aAttr.mainColor());
		lineWidthNFS.setValue(aAttr.lineWidth());
		shiftFactorNFS.setValue(aAttr.shiftFactor());
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		notifyListeners(this, 0);
	}

}
