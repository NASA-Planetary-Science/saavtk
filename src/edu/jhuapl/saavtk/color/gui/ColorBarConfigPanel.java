package edu.jhuapl.saavtk.color.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.color.gui.bar.BackgroundAttrPanel;
import edu.jhuapl.saavtk.color.gui.bar.LayoutAttr;
import edu.jhuapl.saavtk.color.gui.bar.LayoutAttrPanel;
import edu.jhuapl.saavtk.color.painter.ColorBarChangeListener;
import edu.jhuapl.saavtk.color.painter.ColorBarChangeType;
import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.vtk.font.FontAttrPanel;
import glum.gui.FocusUtil;
import glum.gui.GuiUtil;
import glum.gui.action.ClickAction;
import glum.gui.panel.GlassPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure the display aspects of a
 * {@link ColorBarPainter}
 *
 * @author lopeznr1
 */
public class ColorBarConfigPanel extends GlassPanel implements ActionListener, ColorBarChangeListener
{
	// Ref vars
	private final ColorBarPainter refColorBarPainter;

	// Gui vars
	private final JCheckBox isShownCB;
	private final BackgroundAttrPanel mainBAP;
	private final LayoutAttrPanel mainLAP;
	private final FontAttrPanel labelFAP;
	private final FontAttrPanel titleFAP;
	private final JButton closeB, resetB;

	/** Standard Constructor */
	public ColorBarConfigPanel(Component aParent, ColorBarPainter aColorBarPainter)
	{
		super(aParent);

		refColorBarPainter = aColorBarPainter;

		// Build the gui
		setLayout(new MigLayout("", "[]", "[]"));

		JLabel tmpLabel;
		tmpLabel = new JLabel("Color Bar Configuration", JLabel.CENTER);
		add(tmpLabel, "growx,span,wrap");

		isShownCB = GuiUtil.createJCheckBox("Show Color Bar", this);
		isShownCB.setEnabled(true);
		add(isShownCB, "growx,span,wrap");

		mainLAP = new LayoutAttrPanel();
		mainLAP.addActionListener(this);

		titleFAP = new FontAttrPanel();
		titleFAP.addActionListener(this);

		labelFAP = new FontAttrPanel();
		labelFAP.addActionListener(this);

		mainBAP = new BackgroundAttrPanel();
		mainBAP.addActionListener(this);

		JTabbedPane tmpTabbedPane = new JTabbedPane();
		tmpTabbedPane.add("Layout", mainLAP);
		tmpTabbedPane.add("Title", titleFAP);
		tmpTabbedPane.add("Label", labelFAP);
		tmpTabbedPane.add("Background", mainBAP);
		add(tmpTabbedPane, "growx,growy,pushx,pushy,span,wrap");

		// Control area
		closeB = GuiUtil.createJButton("Close", this);
		resetB = GuiUtil.createJButton("Reset Location", this);
		add(resetB, "ax right,span,split");
		add(closeB, "");

		// Set up keyboard short cuts
		FocusUtil.addAncestorKeyBinding(this, "ENTER", new ClickAction(closeB));

		// Register for events of interest
		refColorBarPainter.addListener(this);

		isShownCB.setSelected(refColorBarPainter.getIsVisible());
		mainLAP.setLayoutAttr(refColorBarPainter.getLayoutAttr());
		mainLAP.setWorkColorMapAttr(refColorBarPainter.getColorMapAttr());
		labelFAP.setFontAttr(refColorBarPainter.getFontAttrLabel());
		titleFAP.setFontAttr(refColorBarPainter.getFontAttrTitle());
		mainBAP.setAttr(refColorBarPainter.getBackgroundAttr());
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == closeB)
			doActionClose();
		else if (source == resetB)
			doActionReset();
		else if (source == isShownCB)
			refColorBarPainter.setIsVisible(isShownCB.isSelected());
		else if (source == mainBAP)
			refColorBarPainter.setBackgroundAttr(mainBAP.getAttr());
		else if (source == mainLAP)
			doActionMainLAP();
		else if (source == labelFAP)
			refColorBarPainter.setFontAttrLabel(labelFAP.getFontAttr());
		else if (source == titleFAP)
			refColorBarPainter.setFontAttrTitle(titleFAP.getFontAttr());
	}

	@Override
	public void handleColorBarChanged(Object aSource, ColorBarChangeType aType)
	{
		if (aType == ColorBarChangeType.ColorMap)
			mainLAP.setWorkColorMapAttr(refColorBarPainter.getColorMapAttr());
		else if (aType == ColorBarChangeType.Layout)
			mainLAP.setLayoutAttr(refColorBarPainter.getLayoutAttr());
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
	 * Helper method to handle events from the mainLAP panel.
	 */
	private void doActionMainLAP()
	{
		ColorMapAttr tmpCMA = mainLAP.getColorMapAttr();
		LayoutAttr tmpLA = mainLAP.getLayoutAttr();
		refColorBarPainter.setColorMapAttr(tmpCMA);
		refColorBarPainter.setLayoutAttr(tmpLA);
	}

	/**
	 * Helper method to handle the "Reset Location" action.
	 */
	private void doActionReset()
	{
		refColorBarPainter.resetLocation();
	}

}
