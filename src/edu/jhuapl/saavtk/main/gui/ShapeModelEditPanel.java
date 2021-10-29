package edu.jhuapl.saavtk.main.gui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.grid.gui.LatLonGridEditPanel;
import edu.jhuapl.saavtk.grid.painter.GridChangeListener;
import edu.jhuapl.saavtk.grid.painter.GridChangeType;
import edu.jhuapl.saavtk.grid.painter.LatLonGridPainter;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.gui.GuiUtil;
import glum.gui.component.GComboBox;
import glum.gui.component.GNumberFieldSlider;
import glum.gui.panel.CardPanel;
import net.miginfocom.swing.MigLayout;

/**
 * UI panel for specifying the rendering configuration associated with a
 * {@link PolyhedralModel}.
 *
 * @author lopeznr1
 */
public class ShapeModelEditPanel extends JPanel implements ActionListener, ItemListener, GridChangeListener
{
	// Constants
	private static final Range<Double> OpacityRange = Range.closed(0.0, 1.0);
	private static final Range<Double> LineWidthRange = Range.closed(1.0, 20.0);
	private static final Range<Double> PointSizeRange = Range.closed(1.0, 20.0);

	// Ref vars
	private final Renderer refRenderer;
	private final PolyhedralModel refSmallBody;

	// State vars
	private final LatLonGridPainter workGridPainter;

	// Gui vars
	private final JCheckBox showBodyCB;
	private final JLabel shadeModeL, representationL;
	private final GComboBox<Representation> representationBox;
	private final JRadioButton shadeFlatRB, shadeSmoothRB;

	private final GNumberFieldSlider opacityNFS, lineWidthNFS, pointSizeNFS;
	private final JButton opacityResetB, lineWidthResetB, pointSizeResetB;
	private final JLabel opacityL, lineWidthL, pointSizeL;

	private final JLabel coordL;
	private final JButton coordConfigB;
	private final JCheckBox coordGridCB, coordLabelCB;
	private final LatLonGridEditPanel coordinateEditPanel;
	private final CardPanel<JPanel> representationPanel;

	/** Standard Constructor */
	public ShapeModelEditPanel(Renderer aRenderer, PolyhedralModel aSmallBody, String aBodyName)
	{
		refRenderer = aRenderer;
		refSmallBody = aSmallBody;

		workGridPainter = new LatLonGridPainter(refRenderer, aSmallBody);
      aRenderer.addVtkPropProvider(workGridPainter);

		setLayout(new MigLayout("", "0[][][]0", ""));

		showBodyCB = GuiUtil.createJCheckBox("Show: " + aBodyName, this);
		showBodyCB.setSelected(true);
		add(showBodyCB, "span,wrap");

		// Opacity area
		opacityResetB = GuiUtil.formButton(this, IconUtil.getActionReset());
		opacityResetB.setToolTipText(ToolTipUtil.getItemReset());
		opacityL = new JLabel("Opacity:");
		opacityNFS = new GNumberFieldSlider(this, new DecimalFormat("0.00"), OpacityRange, 4);
		add(opacityResetB, "w 24!,h 24!");
		add(opacityL);
		add(opacityNFS, "growx,pushx,wrap");
		add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");

		// Coordinate grid area
		coordinateEditPanel = new LatLonGridEditPanel(this, workGridPainter);
		coordinateEditPanel.addActionListener(this);
		coordL = new JLabel("Coordinates:");
		coordConfigB = GuiUtil.formButton(this, IconUtil.getActionConfig());
		coordGridCB = GuiUtil.createJCheckBox("Show Grid", this);
		coordLabelCB = GuiUtil.createJCheckBox("Show Labels", this);
		add(coordConfigB, "span,split,w 24!,h 24!");
		add(coordL, "");
		add(coordGridCB, "gapleft 15");
		add(coordLabelCB, "gapleft 15,wrap");

		// ShadeMode area
		shadeModeL = new JLabel("Shading:");
		shadeFlatRB = GuiUtil.createJRadioButton(this, "Flat");
		shadeSmoothRB = GuiUtil.createJRadioButton(this, "Smooth");
		shadeSmoothRB.setSelected(true);
		GuiUtil.linkRadioButtons(shadeFlatRB, shadeSmoothRB);
		add(shadeModeL, "span,split");
		add(shadeFlatRB, "gapleft 15");
		add(shadeSmoothRB, "gapleft 15,wrap");

		// LineWidth gui elements
		lineWidthResetB = GuiUtil.formButton(this, IconUtil.getActionReset());
		lineWidthResetB.setToolTipText(ToolTipUtil.getItemReset());
		lineWidthL = new JLabel("Line Width:");
		lineWidthNFS = new GNumberFieldSlider(this, new DecimalFormat("#0"), LineWidthRange, 3);
		lineWidthNFS.setIntegralSteps();
		var lineWidthPanel = formLineWidthPanel();

		// PointSize gui elements
		pointSizeResetB = GuiUtil.formButton(this, IconUtil.getActionReset());
		pointSizeResetB.setToolTipText(ToolTipUtil.getItemReset());
		pointSizeL = new JLabel("Point Size:");
		pointSizeNFS = new GNumberFieldSlider(this, new DecimalFormat("#0"), PointSizeRange, 3);
		pointSizeNFS.setIntegralSteps();
		var pointSizePanel = formPointSizePanel();

		// Representation area
		representationL = new JLabel("Representation:");
		representationBox = new GComboBox<>(this, Representation.values());
		representationPanel = new CardPanel<>();
		representationPanel.addCard(Representation.Points, pointSizePanel);
		representationPanel.addCard(Representation.Wireframe, lineWidthPanel);
		representationPanel.addCard(Representation.Surface, new JPanel());
		add(representationL, "span,split");
		add(representationBox, "wrap");
		add(representationPanel, "growx,span");

		syncToGuiModel();
		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		var source = aEvent.getSource();
		if (source == showBodyCB)
			refSmallBody.setShowSmallBody(showBodyCB.isSelected());

		else if (source == opacityNFS)
			doActionOpacity();
		else if (source == opacityResetB)
			doActionOpacityReset();

		else if (source == representationBox)
			doActionRepresentation();
		else if (source == lineWidthNFS)
			doActionLineWidth();
		else if (source == lineWidthResetB)
			doActionLineWidthReset();
		else if (source == pointSizeNFS)
			doActionPointSize();
		else if (source == pointSizeResetB)
			doActionPointSizeReset();

		else if (source == coordConfigB)
			coordinateEditPanel.setVisible(true);
		else if (source == coordGridCB)
			doActionCoordinate(source);
		else if (source == coordLabelCB)
			doActionCoordinate(source);

		updateGui();
	}

	@Override
	public void handleGridChanged(Object aSource, GridChangeType aType)
	{
		var tmpGA = workGridPainter.getGridAttr();
		var tmpFA = workGridPainter.getFontAttr();
		coordGridCB.setSelected(tmpGA.isVisible());
		coordLabelCB.setSelected(tmpFA.getIsVisible());
		updateGui();
	}

	@Override
	public void itemStateChanged(ItemEvent aEvent)
	{
		doActionShadeMode();
	}

	/**
	 * Helper method that handles the coordinate modify action
	 */
	private void doActionCoordinate(Object aSource)
	{
		var tmpGA = workGridPainter.getGridAttr();
		var tmpFA = workGridPainter.getFontAttr();
		var tmpLLS = workGridPainter.getLatLonSpacing();

		if (aSource == coordGridCB)
			tmpGA = tmpGA.withIsVisible(coordGridCB.isSelected());
		else if (aSource == coordLabelCB)
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), tmpFA.getSize(), coordLabelCB.isSelected());

		// Disable picking: Basis of this hack is from PolyhedralModelControlPanel
		var isStale = workGridPainter.isStale();
		isStale |= tmpLLS.equals(workGridPainter.getLatLonSpacing()) == false;
		if (isStale == true)
		{
			PickUtil.setPickingEnabled(false);
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
		}

		workGridPainter.setGridAttr(tmpGA);
		workGridPainter.setFontAttr(tmpFA);
		workGridPainter.setLatLonSpacing(tmpLLS);

		// Restore picking: Basis of this hack is from PolyhedralModelControlPanel
		if (isStale == true)
		{
			PickUtil.setPickingEnabled(true);
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * Helper method that handles the opacity modify action
	 */
	private void doActionOpacity()
	{
		if (opacityNFS.isValidInput() == false)
			return;
		var tmpVal = opacityNFS.getValue();
		refSmallBody.setOpacity(tmpVal);
	}

	/**
	 * Helper method that handles the opacity reset action
	 */
	private void doActionOpacityReset()
	{
		opacityNFS.setValue(1.0);
		refSmallBody.setOpacity(1.0);
	}

	/**
	 * Helper method that handles the shade mode action
	 */
	private void doActionRepresentation()
	{
		var tmpRepresentation = representationBox.getChosenItem();
		if (tmpRepresentation == Representation.Points)
			refSmallBody.setRepresentationToPoints();
		else if (tmpRepresentation == Representation.Surface)
			refSmallBody.setRepresentationToSurface();
		else if (tmpRepresentation == Representation.SurfaceWithEdges)
			refSmallBody.setRepresentationToSurfaceWithEdges();
		else if (tmpRepresentation == Representation.Wireframe)
			refSmallBody.setRepresentationToWireframe();
		else
			throw new RuntimeException("Unsupported representation: " + tmpRepresentation);
	}

	/**
	 * Helper method that handles the opacity modify action
	 */
	private void doActionLineWidth()
	{
		if (lineWidthNFS.isValidInput() == false)
			return;

		var tmpVal = lineWidthNFS.getValue();
		refSmallBody.setLineWidth(tmpVal);
	}

	/**
	 * Helper method that handles the line width reset action.
	 */
	private void doActionLineWidthReset()
	{
		lineWidthNFS.setValue(1.0);
		refSmallBody.setLineWidth(1.0);
	}

	/**
	 * Helper method that handles the opacity modify action
	 */
	private void doActionPointSize()
	{
		if (pointSizeNFS.isValidInput() == false)
			return;

		var tmpVal = pointSizeNFS.getValue();
		refSmallBody.setPointSize(tmpVal);
	}

	/**
	 * Helper method that handles the point size reset action.
	 */
	private void doActionPointSizeReset()
	{
		pointSizeNFS.setValue(1.0);
		refSmallBody.setPointSize(1.0);
	}

	/**
	 * Helper method that handles the shade mode action
	 */
	private void doActionShadeMode()
	{
		if (shadeFlatRB.isSelected() == true)
			refSmallBody.setShadingToFlat();
		else if (shadeSmoothRB.isSelected() == true)
			refSmallBody.setShadingToSmooth();
	}

	/**
	 * Helper method to form the panel used to hold the point size gui elements.
	 */
	private JPanel formLineWidthPanel()
	{
		var retPanel = new JPanel(new MigLayout("", "[]", "0[]0"));
		retPanel.add(lineWidthResetB, "w 24!,h 24!");
		retPanel.add(lineWidthResetB, "w 24!,h 24!");
		retPanel.add(lineWidthL);
		retPanel.add(lineWidthNFS, "growx,pushx");
		return retPanel;
	}

	/**
	 * Helper method to form the panel used to hold the point size gui elements.
	 */
	private JPanel formPointSizePanel()
	{
		var retPanel = new JPanel(new MigLayout("", "[]", "0[]0"));
		retPanel.add(pointSizeResetB, "w 24!,h 24!");
		retPanel.add(pointSizeResetB, "w 24!,h 24!");
		retPanel.add(pointSizeL);
		retPanel.add(pointSizeNFS, "growx,pushx");
		return retPanel;
	}

	/**
	 * Helper method to synchronize the gui to the reference shape model.
	 */
	private void syncToGuiModel()
	{
		opacityNFS.setValue(1.0);
		lineWidthNFS.setValue(1.0);
		pointSizeNFS.setValue(1.0);
	}

	/**
	 * Helper method that updates the various UI elements to keep them synchronized.
	 */
	private void updateGui()
	{
		var isEnabled = true;
		var isShown = showBodyCB.isSelected();
		GuiUtil.setEnabled(isEnabled, shadeModeL, shadeFlatRB, shadeSmoothRB);
		GuiUtil.setEnabled(isEnabled, representationL, representationBox);
		GuiUtil.setEnabled(isEnabled, opacityL, opacityNFS);
		GuiUtil.setEnabled(isEnabled, lineWidthL, lineWidthNFS);
		GuiUtil.setEnabled(isEnabled, pointSizeL, pointSizeNFS);

		// Opacity area
		isEnabled = isShown == true;
		isEnabled &= opacityNFS.getValue() != 1.0;
		opacityResetB.setEnabled(isEnabled);

		// Coordinate area
		isEnabled = isShown == true;
		isEnabled &= coordGridCB.isSelected() == true;
		coordLabelCB.setEnabled(isEnabled);

		// Representation area
		var tmpRepresentation = representationBox.getChosenItem();
		var tmpKey = tmpRepresentation;
		if (tmpKey == Representation.SurfaceWithEdges)
			tmpKey = Representation.Wireframe;
		representationPanel.switchToCard(tmpKey);

		// LineWidth area
		isEnabled = isShown == true;
		isEnabled &= lineWidthNFS.getValue() != 1.0;
		lineWidthResetB.setEnabled(isEnabled);

		// PointSize area
		isEnabled = isShown == true;
		isEnabled &= pointSizeNFS.getValue() != 1.0;
		pointSizeResetB.setEnabled(isEnabled);
	}

}

enum Representation
{
	Surface,

	Wireframe,

	Points,

	SurfaceWithEdges
}
