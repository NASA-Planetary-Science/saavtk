package edu.jhuapl.saavtk.view.lod.gui;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.view.ViewActionListener;
import edu.jhuapl.saavtk.view.ViewChangeReason;
import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.LodStatusPainter;
import edu.jhuapl.saavtk.view.lod.LodUtil;
import glum.gui.GuiUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure the {@link LodMode} for the specified
 * {@link Renderer}.
 *
 * @author lopeznr1
 */
public class LodPanel extends JPanel implements ItemListener, ViewActionListener
{
	// Ref vars
	private final Renderer refRenderer;
	private final LodStatusPainter refPainter;

	// Gui vars
	private final JRadioButton modeAutoRB;
	private final JRadioButton modeMaxSpeedRB;
	private final JRadioButton modeMaxQualityRB;
	private final JLabel modeAutoInfoL;

	/** Standard Constructor */
	public LodPanel(Renderer aRenderer, LodStatusPainter aPainter)
	{
		refRenderer = aRenderer;
		refPainter = aPainter;

		// Form the GUI
		setLayout(new MigLayout("", "[]", "[]"));

		JLabel titleL = new JLabel("Level of Detail", JLabel.CENTER);
		add(titleL, "align center,growx,span,wrap");

		modeAutoRB = GuiUtil.createJRadioButton(this, "Auto");
		modeAutoInfoL = new JLabel();
		modeAutoInfoL.setForeground(Color.GRAY);
		modeMaxSpeedRB = GuiUtil.createJRadioButton(this, "Max Speed");
		modeMaxQualityRB = GuiUtil.createJRadioButton(this, "Max Quality");
		GuiUtil.linkRadioButtons(modeAutoRB, modeMaxSpeedRB, modeMaxQualityRB);
		add(modeAutoRB, "span,split");
		add(modeAutoInfoL, "w 150::,wrap");
		add(modeMaxSpeedRB, "wrap");
		add(modeMaxQualityRB, "wrap");

		// Register for events of interest
		refRenderer.addViewChangeListener(this);

		syncGuiToModel();
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

	@Override
	public void itemStateChanged(ItemEvent aEvent)
	{
		// Ignore deselects
		if (aEvent.getStateChange() == ItemEvent.DESELECTED)
			return;

		Object source = aEvent.getSource();
		if (source == modeAutoRB || source == modeMaxSpeedRB || source == modeMaxQualityRB)
			doChangeLodMode();

		updateGui();
	}

	/**
	 * Helper method to handle action associated with changing the {@link LodMode}.
	 */
	private void doChangeLodMode()
	{
		LodMode tmpMode = LodMode.Auto;
		if (modeMaxSpeedRB.isSelected() == true)
			tmpMode = LodMode.MaxSpeed;
		else if (modeMaxQualityRB.isSelected() == true)
			tmpMode = LodMode.MaxQuality;

		refRenderer.setLodMode(tmpMode);
	}

	/**
	 * Helper method that will synchronize the GUI with the model.
	 */
	private void syncGuiToModel()
	{
		LodMode tmpMode = refRenderer.getLodMode();

		boolean tmpBool = tmpMode == LodMode.Auto;
		modeAutoRB.setSelected(tmpBool);

		tmpBool = tmpMode == LodMode.MaxQuality;
		modeMaxQualityRB.setSelected(tmpBool);

		tmpBool = tmpMode == LodMode.MaxSpeed;
		modeMaxSpeedRB.setSelected(tmpBool);
	}

	/**
	 * Helper method that keeps the GUI synchronized with user input.
	 */
	private void updateGui()
	{
		// Retrieve the (configured) LodMode
		LodMode modePri = refRenderer.getLodMode();

		// If the primary mode is set to auto then show the "instantaneous" mode
		String tmpMsg = "";
		if (modePri == LodMode.Auto)
		{
			// If the secondary mode is NOT auto then show details
			LodMode modeSec = refPainter.getLastLodMode();
			if (modeSec != LodMode.Auto && modeSec != null)
				tmpMsg = "(" + LodUtil.getDisplayString(modeSec) + ")";
		}
		modeAutoInfoL.setText(tmpMsg);
	}

}
