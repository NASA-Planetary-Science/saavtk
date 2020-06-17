package edu.jhuapl.saavtk.view.lod.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.view.ViewActionListener;
import edu.jhuapl.saavtk.view.ViewChangeReason;
import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.LodStatusPainter;
import edu.jhuapl.saavtk.view.lod.LodUtil;
import glum.gui.component.GComboBox;
import net.miginfocom.swing.MigLayout;

public class LodPanel extends JPanel implements ActionListener, ViewActionListener
{
	// Ref vars
	private final Renderer refRenderer;
	private final LodStatusPainter refPainter;

	// Gui vars
	private final GComboBox<LodMode> lodBox;
	private final JLabel statusL;

	/**
	 * Standard Constructor
	 */
	public LodPanel(Renderer aRenderer, LodStatusPainter aPainter)
	{
		refRenderer = aRenderer;
		refPainter = aPainter;

		// Form the GUI
		setLayout(new MigLayout("", "[right][grow][]", "[]"));

		JLabel xSizeL = new JLabel("Level of Detail:");
		lodBox = new GComboBox<>(this, LodMode.values());
		lodBox.setRenderer(new LodModeRenderer());
		lodBox.setChosenItem(refRenderer.getLodMode());
		add(xSizeL, "");
		add(lodBox, "w pref::,wrap");

		JLabel tmpL = new JLabel("Status:");
		statusL = new JLabel("");
		add(tmpL, "");
		add(statusL, "growx,sgy G1,span,w 150::,wrap 0");

		// Register for events of interest
		refRenderer.addViewChangeListener(this);

		syncGuiToModel();
		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == lodBox)
			doChangeLodMode();

		updateGui();
	}

	@Override
	public void handleViewAction(Object aSource, ViewChangeReason aReason)
	{
		// Bail if not related to level-of-detail
		if (aReason != ViewChangeReason.Lod)
			return;

		syncGuiToModel();
		updateGui();
	}

	/**
	 * Helper method to handle action associated with changing the {@link LodMode}.
	 */
	private void doChangeLodMode()
	{
		refRenderer.setLodMode(lodBox.getChosenItem());
	}

	/**
	 * Helper method that will synchronize the GUI with the model.
	 */
	private void syncGuiToModel()
	{
		LodMode tmpMode = refRenderer.getLodMode();
		lodBox.setChosenItem(tmpMode);
	}

	/**
	 * Helper method that keeps the GUI synchronized with user input.
	 */
	private void updateGui()
	{
		// Retrieve the (configured) LodMode
		LodMode modePri = refRenderer.getLodMode();
		String tmpMsg = LodUtil.getDisplayString(modePri);

		// If the primary mode is set to auto then show the "instantaneous" mode
		if (modePri == LodMode.Auto)
		{
			// If the secondary mode is NOT auto then show details
			LodMode modeSec = refPainter.getLastLodMode();
			if (modeSec != LodMode.Auto && modeSec != null)
				tmpMsg += " - " + LodUtil.getDisplayString(modeSec);
		}

		statusL.setText(tmpMsg);
	}

}
