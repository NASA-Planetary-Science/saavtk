package edu.jhuapl.saavtk.structure.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.RenderAttr;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import glum.gui.FocusUtil;
import glum.gui.GuiUtil;
import glum.gui.action.ClickAction;
import glum.gui.component.GNumberField;
import glum.gui.component.GSlider;
import glum.gui.panel.GlassPanel;
import glum.unit.NumberUnit;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure a {@link RenderAttr}.
 * <p>
 * This panel allows definition of the following:
 * <ul>
 * <li>Line width
 * <li>Radial offset
 * <li>Point size
 * <li>Responsive status feedback associated with user input.
 * </ul>
 *
 * @author lopeznr1
 */
public class RenderAttrPanel extends GlassPanel implements ActionListener
{
	// Constants
	private final String TIP_RADIAL_OFFSET = "<html>Structures displayed on a shape model need to be shifted slightly away from <br>"
			+ "the shape model in the direction normal to the surface as otherwise they will <br>"
			+ "interfere with the shape model itself and may not be visible. In general, the <br>"
			+ "smallest positive value should be chosen such that the objects are visible. </html>";

	// Ref vars
	private final PolyhedralModel refSmallBody;

	// Gui vars
	private final GSlider lineWidthS, radialOffsetS, pointSizeS;
	private final GNumberField lineWidthNF, radialOffsetNF, pointSizeNF;
	private final JButton lineWidthResetB, radialOffsetResetB, pointSizeResetB;

	private final JButton closeB;

	// State vars
	private int numRoundSides;
	private int numPointSides;

	/** Standard Constructor */
	public RenderAttrPanel(Component aParent, PolyhedralModel aSmallBody)
	{
		super(aParent);

		refSmallBody = aSmallBody;

		// Form the gui
		setLayout(new MigLayout("", "[]", "[]"));
		setBorder(new BevelBorder(BevelBorder.RAISED));

		var floatNU = new NumberUnit("", "", 1.0, 3);
		var lineWidthDef = 2.0;
		var lineWidthRange = Range.closed(1.0, 5.0);
		var radialOffsetDef = EllipseUtil.getRadialOffsetDef(refSmallBody);
		var radialOffsetRange = Range.closed(0.0, radialOffsetDef * 100);
		var pointSizeDef = EllipseUtil.getPointSizeDef(refSmallBody);
		var pointSizeRange = Range.closed(0.0, pointSizeDef * 20);

		var titleL = new JLabel("Render Properties", JLabel.CENTER);
		add(titleL, "growx,span,wrap");

		lineWidthResetB = GuiUtil.formButton(this, IconUtil.getActionReset());
		lineWidthResetB.setToolTipText(ToolTipUtil.getItemResetMsg(floatNU.getString(lineWidthDef)));
		var lineWidthL = new JLabel("Line Width:");
		lineWidthS = new GSlider(this, lineWidthRange, 100);
		lineWidthNF = new GNumberField(this, floatNU, lineWidthRange);
		add(lineWidthResetB, "w 24!,h 24!");
		add(lineWidthL, "");
		add(lineWidthNF, "w 60!");
		add(lineWidthS, "growx,pushx,wrap");

		radialOffsetResetB = GuiUtil.formButton(this, IconUtil.getActionReset());
		radialOffsetResetB.setToolTipText(ToolTipUtil.getItemResetMsg(floatNU.getString(radialOffsetDef)));
		var radialOffsetL = new JLabel("Radial Offset:");
		radialOffsetL.setToolTipText(TIP_RADIAL_OFFSET);
		radialOffsetS = new GSlider(this, radialOffsetRange, 100);
		radialOffsetNF = new GNumberField(this, floatNU, radialOffsetRange);
		add(radialOffsetResetB, "w 24!,h 24!");
		add(radialOffsetL, "");
		add(radialOffsetNF, "w 60!");
		add(radialOffsetS, "growx,wrap");

		pointSizeResetB = GuiUtil.formButton(this, IconUtil.getActionReset());
		pointSizeResetB.setToolTipText(ToolTipUtil.getItemResetMsg(floatNU.getString(pointSizeDef)));
		var pointSizeL = new JLabel("Point Size:");
		pointSizeS = new GSlider(this, pointSizeRange);
		pointSizeNF = new GNumberField(this, floatNU, pointSizeRange);
		add(pointSizeResetB, "w 24!,h 24!");
		add(pointSizeL, "");
		add(pointSizeNF, "w 60!");
		add(pointSizeS, "growx,wrap");

		closeB = GuiUtil.formButton(this, "Close");
		add(closeB, "ax right,span,split");

		// Set up keyboard short cuts
		FocusUtil.addAncestorKeyBinding(this, "ESCAPE", new ClickAction(closeB));
		FocusUtil.addAncestorKeyBinding(this, "ENTER", new ClickAction(closeB));
	}

	/**
	 * Returns the {@link RenderAttr} as configured via the gui.
	 */
	public RenderAttr getAttr()
	{
		var lineWidth = lineWidthNF.getValue();
		var radialOffset = radialOffsetNF.getValue();
		var pointSize = pointSizeNF.getValue();
		return new RenderAttr(lineWidth, radialOffset, numRoundSides, numPointSides, pointSize);
	}

	/**
	 * Updates the gui to reflect the specified {@link RenderAttr}.
	 */
	public void setAttr(RenderAttr aRenderAttr)
	{
		lineWidthNF.setValue(aRenderAttr.lineWidth());
		lineWidthS.setModelValue(aRenderAttr.lineWidth());
		radialOffsetNF.setValue(aRenderAttr.radialOffset());
		radialOffsetS.setModelValue(aRenderAttr.radialOffset());
		pointSizeNF.setValue(aRenderAttr.pointRadius());
		pointSizeS.setModelValue(aRenderAttr.pointRadius());
		numRoundSides = aRenderAttr.numRoundSides();
		numPointSides = aRenderAttr.numPointSides();

		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		var source = aEvent.getSource();
		if (source == closeB)
			setVisible(false);
		else if (source == lineWidthS || source == lineWidthNF || source == lineWidthResetB)
			doActionLineWidth(source);
		else if (source == pointSizeS || source == pointSizeNF || source == pointSizeResetB)
			doActionPointSize(source);
		else if (source == radialOffsetS || source == radialOffsetNF || source == radialOffsetResetB)
			doActionRadialOffset(source);

		updateGui();
		notifyListeners(this);
	}

	/**
	 * Helper method that handles the lineWidth action
	 */
	private void doActionLineWidth(Object aSource)
	{
		if (aSource == lineWidthNF && lineWidthNF.isValidInput() == false)
			return;

		var tmpVal = Double.NaN;
		if (aSource == lineWidthResetB)
			tmpVal = 2.0;
		else if (aSource == lineWidthNF)
			tmpVal = lineWidthNF.getValue();
		else if (aSource == lineWidthS)
			tmpVal = lineWidthS.getModelValue();

		lineWidthNF.setValue(tmpVal);
		lineWidthS.setModelValue(tmpVal);
	}

	/**
	 * Helper method that handles the pointSize action
	 */
	private void doActionPointSize(Object aSource)
	{
		if (aSource == pointSizeNF && pointSizeNF.isValidInput() == false)
			return;

		var tmpVal = Double.NaN;
		if (aSource == pointSizeResetB)
			tmpVal = EllipseUtil.getPointSizeDef(refSmallBody);
		else if (aSource == pointSizeNF)
			tmpVal = pointSizeNF.getValue();
		else if (aSource == pointSizeS)
			tmpVal = pointSizeS.getModelValue();

		pointSizeNF.setValue(tmpVal);
		pointSizeS.setModelValue(tmpVal);
	}

	/**
	 * Helper method that handles the radialOffset action
	 */
	private void doActionRadialOffset(Object aSource)
	{
		if (aSource == radialOffsetNF && radialOffsetNF.isValidInput() == false)
			return;

		var tmpVal = Double.NaN;
		if (aSource == radialOffsetResetB)
			tmpVal = 5.0 * refSmallBody.getMinShiftAmount();
		else if (aSource == radialOffsetNF)
			tmpVal = radialOffsetNF.getValue();
		else if (aSource == radialOffsetS)
			tmpVal = radialOffsetS.getModelValue();

		radialOffsetNF.setValue(tmpVal);
		radialOffsetS.setModelValue(tmpVal);
	}

	/**
	 * Helper method that keeps the gui synchronized with user input.
	 */
	private void updateGui()
	{
		var tmpRenderAttr = getAttr();

		var isEnabled = tmpRenderAttr.lineWidth() != 2.0;
		lineWidthResetB.setEnabled(isEnabled);

		isEnabled = tmpRenderAttr.radialOffset() != EllipseUtil.getRadialOffsetDef(refSmallBody);
		radialOffsetResetB.setEnabled(isEnabled);

		isEnabled = tmpRenderAttr.pointRadius() != EllipseUtil.getPointSizeDef(refSmallBody);
		pointSizeResetB.setEnabled(isEnabled);
	}

}
