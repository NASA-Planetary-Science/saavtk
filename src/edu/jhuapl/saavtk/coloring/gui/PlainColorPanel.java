package edu.jhuapl.saavtk.coloring.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import edu.jhuapl.saavtk.coloring.ColoringMode;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import glum.gui.panel.ColorInputPanel;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure a small body with the
 * {@link ColoringMode#Plain}.
 *
 * @author lopeznr1
 */
public class PlainColorPanel extends GPanel implements ActionListener, EditColoringModeGui
{
	// Ref vars
	private final List<PolyhedralModel> refSmallBodies;

	// Gui vars
	private final ColorInputPanel colorCIP;

	/** Standard Constructor */
	public PlainColorPanel(List<PolyhedralModel> aSmallBodies)
	{
		refSmallBodies = aSmallBodies;

		setLayout(new MigLayout("", "", ""));

		colorCIP = new ColorInputPanel(true, true, false);
		colorCIP.setColorConfig(Color.WHITE);
		colorCIP.addActionListener(this);
		add(colorCIP, "growx,pushx,span");
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		activate(aEvent.getSource());
	}

	@Override
	public void activate(Object aSource)
	{
		var tmpColor = colorCIP.getColorConfig();
		for (PolyhedralModel refSmallBody : refSmallBodies)
			refSmallBody.setPlainColor(tmpColor);
	}

	@Override
	public void deactivate()
	{
		; // Nothing to do
	}

}
