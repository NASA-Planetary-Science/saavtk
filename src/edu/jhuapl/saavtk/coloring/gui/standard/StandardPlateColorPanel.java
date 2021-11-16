package edu.jhuapl.saavtk.coloring.gui.standard;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.color.gui.ColorBarPanel;
import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.coloring.gui.EditColoringModeGui;
import edu.jhuapl.saavtk.coloring.legacy.LegacyUtil;
import edu.jhuapl.saavtk.feature.FeatureType;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import glum.gui.GuiPaneUtil;
import glum.gui.GuiUtil;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Class that provides UI controls and mechanism for the standard plate coloring
 * panel.
 * <P>
 * This class is a composite of the following gui elements:
 * <ul>
 * <li>{@link ColorBarPanel}
 * <li>{@link ContourPanel}
 * <li>Custom coloring / info row removed.
 *
 * @author lopeznr1
 */
public class StandardPlateColorPanel extends GPanel implements ActionListener, EditColoringModeGui
{
	// Ref vars
	private final PolyhedralModel refSmallBody;
	private final Renderer refRenderer;

	// State vars
	private final ColorBarPainter workCBP;
	private boolean isActive;

	// Cache vars
	private final Map<FeatureType, Range<Double>> cDefaultRangeM;
	private FeatureType cFeatureType;

	// GUI vars
	private final ColorBarPanel colorBarPanel;
	private final ContourPanel contourPanel;
	private final JComboBox<String> propertyBox;
	private final JButton plateInfoB;

	/** Standard Constructor */
	public StandardPlateColorPanel(Renderer aRenderer, PolyhedralModel aPolyModel, JComboBox<String> aPropertyBox)
	{
		refRenderer = aRenderer;
		refSmallBody = aPolyModel;

		workCBP = new ColorBarPainter(aRenderer);
		isActive = false;

		cFeatureType = FeatureType.Invalid;
		cDefaultRangeM = new HashMap<>();
		cDefaultRangeM.put(FeatureType.Invalid, Range.closed(Double.NaN, Double.NaN));

		// Setup the GUI
		setLayout(new MigLayout("", "0[][right][]0", "0[][]"));

		// Coloring Index area
		propertyBox = aPropertyBox;
		propertyBox.addActionListener(this);
		plateInfoB = GuiUtil.formButton(this, IconUtil.getActionInfo());
		plateInfoB.setToolTipText("Plate Properties");
		var featureL = new JLabel("Property:");
		add(featureL, "span,split");
		add(propertyBox, "growx,pushx,span,w 0:0:");
		add(plateInfoB, "w 24!,h 24!,wrap");

		// ColorBar area
		colorBarPanel = new ColorBarPanel(workCBP, true);
		colorBarPanel.addActionListener(this);
		colorBarPanel.hideFeatureControls();
		add(colorBarPanel, "growx,span,wrap 0");

		// Contour area
		contourPanel = new ContourPanel();
		contourPanel.addActionListener(this);
		add(contourPanel, "ax left,span,wrap 0");

		// Update the small body to match the ColorBarPanel
		refSmallBody.setColorMapAttr(colorBarPanel.getColorMapAttr());

		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		var source = aEvent.getSource();
		if (source == colorBarPanel)
			doActionColorBarPanel();

		else if (source == contourPanel)
			doActionContourPanel();

		else if (source == propertyBox)
			doActionPropertyBox();

		else if (source == plateInfoB)
			LegacyUtil.showColoringProperties(refSmallBody);
	}

	@Override
	public void activate(Object aSource)
	{
		isActive = true;

		updateSmallBodyColoring();

		// Bail if the selected plate color index is invalid
		var coloringIdx = getChosenColoringIndex();
		if (coloringIdx < 0)
			return;

		refSmallBody.setColorMapAttr(colorBarPanel.getColorMapAttr());
		refSmallBody.setContourLineWidth(contourPanel.getLineWidth());
		refSmallBody.showScalarsAsContours(contourPanel.getContourLinesRequested());

		// Add the ColorBarPainter to the renderer
		refRenderer.addVtkPropProvider(workCBP);
	}

	@Override
	public void deactivate()
	{
		isActive = false;

		// Remove the ColorBarPainter from the renderer
		refRenderer.delVtkPropProvider(workCBP);
	}

	/**
	 * Helper method that handles the color bar panel action.
	 */
	private void doActionColorBarPanel()
	{
		// Update the ColorBarPainter
		var tmpCMA = colorBarPanel.getColorMapAttr();
		colorBarPanel.delActionListener(this);
		workCBP.setColorMapAttr(tmpCMA);
		colorBarPanel.addActionListener(this);

		// Cache the custom ColorBar range
		var currRange = Range.closed(tmpCMA.getMinVal(), tmpCMA.getMaxVal());
		cDefaultRangeM.put(cFeatureType, currRange);

		// Update the small body
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		refSmallBody.setColorMapAttr(tmpCMA);
		try
		{
			double[] rangeArr = { tmpCMA.getMinVal(), tmpCMA.getMaxVal() };
			refSmallBody.setCurrentColoringRange(refSmallBody.getColoringIndex(), rangeArr);
		}
		catch (IOException aExp)
		{
			aExp.printStackTrace();
		}
		setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Helper method that handles the contour panel action.
	 */
	private void doActionContourPanel()
	{
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		refSmallBody.setContourLineWidth(contourPanel.getLineWidth());
		refSmallBody.showScalarsAsContours(contourPanel.getContourLinesRequested());

		setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Helper method that handles the propertyBox action.
	 */
	private void doActionPropertyBox()
	{
		// Bail if not the active panel
		if (isActive == false)
			return;

		// Due to poor design this must be called first
		updateSmallBodyColoring();

		// Assume the selected coloring corresponds to an Invalid feature
		cFeatureType = FeatureType.Invalid;
		var prevRange = cDefaultRangeM.get(cFeatureType);
		var resetRange = prevRange;

		// Locate the corresponding FeatureType and the previous range
		var coloringIdx = getChosenColoringIndex();
		if (coloringIdx >= 0)
		{
			var name = (String) propertyBox.getSelectedItem();
			var unit = "";
			var scale = 1.0;

			// Retrieve the default range
			var resetArr = refSmallBody.getDefaultColoringRange(coloringIdx);
			resetRange = Range.closed(resetArr[0], resetArr[1]);

			// Retrieve the cached previous range
			cFeatureType = new FeatureType(null, name, unit, scale);
			prevRange = cDefaultRangeM.getOrDefault(cFeatureType, resetRange);
		}

		// Update the ColorBarPanel with the appropriate ranges
		colorBarPanel.setResetRange(cFeatureType, resetRange);
		colorBarPanel.setFeatureType(cFeatureType);

		var tmpColorMapAttr = colorBarPanel.getColorMapAttr();
		var minVal = prevRange.lowerEndpoint();
		var maxVal = prevRange.upperEndpoint();
		tmpColorMapAttr = new ColorMapAttr(tmpColorMapAttr.getColorTable(), minVal, maxVal,
				tmpColorMapAttr.getNumLevels(), tmpColorMapAttr.getIsLogScale());
		colorBarPanel.setColorMapAttr(tmpColorMapAttr);

		updateColorBarPainter();

		activate(propertyBox);

		updateGui();
	}

	/**
	 * Helper method to return the chosen coloring index.
	 */
	private int getChosenColoringIndex()
	{
		// Logic to return the selected coloring index. Isolated to 1 location.
		var retIdx = propertyBox.getSelectedIndex() - 1;
		if (retIdx < 0)
			retIdx = -1;
		return retIdx;
	}

	/**
	 * Helper method that updates the color bar painter to match the small body's
	 * coloring.
	 */
	private void updateColorBarPainter()
	{
		// If the coloring index is invalid, then just remove the ColorBarPainter
		var tmpColoringIdx = getChosenColoringIndex();
		if (tmpColoringIdx < 0)
		{
			refRenderer.delVtkPropProvider(workCBP);
			return;
		}

		// Update the title
		String title = refSmallBody.getColoringName(tmpColoringIdx).trim();
		String units = refSmallBody.getColoringUnits(tmpColoringIdx).trim();
		if (units.isEmpty() == false)
			title += " (" + units + ")";
		workCBP.setTitle(title);

		// Show the ColorBarPainter (if appropriate)
		if (refSmallBody.isColoringDataAvailable() == true)
		{
			workCBP.setColorMapAttr(colorBarPanel.getColorMapAttr());
			refRenderer.addVtkPropProvider(workCBP);
		}
	}

	/**
	 * Helper method that updates the various UI elements to keep them synchronized.
	 */
	private void updateGui()
	{
		var isEnabled = getChosenColoringIndex() >= 0;
		plateInfoB.setEnabled(isEnabled);
	}

	/**
	 * Helper method that updates the reference small body to reflect the coloring
	 * as specified by the coloringBox.
	 */
	private void updateSmallBodyColoring()
	{
		// Switch to the selected coloring
		int selectedIndex = getChosenColoringIndex();

		try
		{
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			refSmallBody.setColoringIndex(selectedIndex);
		}
		catch (IOException aExp)
		{
			GuiPaneUtil.showFailMessage(this, "Plate Coloring Error", "Failure while changing plate coloring...", aExp);
			aExp.printStackTrace();

			// Switch back to "invalid" plate coloring
			propertyBox.setSelectedIndex(0);
		}
		finally
		{
			setCursor(Cursor.getDefaultCursor());
		}
	}

}
