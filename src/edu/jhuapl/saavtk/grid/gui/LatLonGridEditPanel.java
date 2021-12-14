package edu.jhuapl.saavtk.grid.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.grid.painter.GridChangeListener;
import edu.jhuapl.saavtk.grid.painter.GridChangeType;
import edu.jhuapl.saavtk.grid.painter.LatLonGridPainter;
import edu.jhuapl.saavtk.vtk.font.FontAttrPanel;
import glum.gui.FocusUtil;
import glum.gui.GuiUtil;
import glum.gui.action.ClickAction;
import glum.gui.panel.GlassPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure the display aspects of a
 * {@link CoordinatePainter}
 *
 * @author lopeznr1
 */
public class LatLonGridEditPanel extends GlassPanel implements ActionListener, GridChangeListener
{
	// Ref vars
	private final LatLonGridPainter refGridPainter;

	// Gui vars
	private final JCheckBox isShownCB;
	private final FontAttrPanel labelFAP;
	private final GridAttrPanel mainGAP;
	private final LatLonSpacingPanel mainLLSP;
	private final JButton closeB;

	/** Standard Constructor */
	public LatLonGridEditPanel(Component aParent, LatLonGridPainter aGridPainter)
	{
		super(aParent);

		refGridPainter = aGridPainter;

		// Build the gui
		setLayout(new MigLayout("", "[]", "[]"));

		var tmpLabel = new JLabel("Coordinate Grid Configuration", JLabel.CENTER);
		add(tmpLabel, "growx,span,wrap");

		isShownCB = GuiUtil.createJCheckBox("Show Grid", this);
		isShownCB.setEnabled(true);
		add(isShownCB, "growx,span,wrap");

		mainGAP = new GridAttrPanel();
		mainGAP.addActionListener(this);

		mainLLSP = new LatLonSpacingPanel();
		mainLLSP.addActionListener(this);

		labelFAP = new FontAttrPanel();
		labelFAP.addActionListener(this);

		var tmpTabbedPane = new JTabbedPane();
		tmpTabbedPane.add("Grid", mainGAP);
		tmpTabbedPane.add("Label", labelFAP);
		tmpTabbedPane.add("Spacing", mainLLSP);
		add(tmpTabbedPane, "growx,growy,pushx,pushy,span,wrap");

		// Control area
		closeB = GuiUtil.createJButton("Close", this);
		add(closeB, "ax right,span,split");

		// Set up keyboard short cuts
		FocusUtil.addAncestorKeyBinding(this, "ENTER", new ClickAction(closeB));

		// Initial update
		handleGridChanged(this, GridChangeType.All);

		// Register for events of interest
		refGridPainter.addListener(this);

		isShownCB.setSelected(refGridPainter.getGridAttr().isVisible());
		mainGAP.setGridAttr(refGridPainter.getGridAttr());
		labelFAP.setFontAttr(refGridPainter.getFontAttr());
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		var source = aEvent.getSource();
		if (source == closeB)
			doActionClose();
		else if (source == isShownCB)
			doActionShowGrid();
		else if (source == mainGAP)
			doActionGridAttrPanel();
		else if (source == mainLLSP)
			refGridPainter.setLatLonSpacing(mainLLSP.getLatLonSpacing());
		else if (source == labelFAP)
			refGridPainter.setFontAttr(labelFAP.getFontAttr());
	}

	@Override
	public void handleGridChanged(Object aSource, GridChangeType aType)
	{
		var tmpFA = refGridPainter.getFontAttr();
		var tmpGA = refGridPainter.getGridAttr();
		var tmpLLS = refGridPainter.getLatLonSpacing();
		isShownCB.setSelected(tmpGA.isVisible());
		labelFAP.setFontAttr(tmpFA);
		mainGAP.setGridAttr(tmpGA);
		mainLLSP.setLatLonSpacing(tmpLLS);
	}

	/**
	 * Helper method to handle the "close" action.
	 */
	private void doActionClose()
	{
		setVisible(false);
		notifyListeners(this, ID_ACCEPT, "Close");
	}

	/**
	 * Helper method to handle the "show grid" action.
	 */
	private void doActionShowGrid()
	{
		var tmpGA = refGridPainter.getGridAttr();
		tmpGA = tmpGA.withIsVisible(isShownCB.isSelected());
		refGridPainter.setGridAttr(tmpGA);
	}

	/**
	 * Helper method to handle the "GridAttrPanel" action.
	 */
	private void doActionGridAttrPanel()
	{
		var tmpGA = mainGAP.getGridAttr();
		tmpGA = tmpGA.withIsVisible(isShownCB.isSelected());
		refGridPainter.setGridAttr(tmpGA);
	}

}
