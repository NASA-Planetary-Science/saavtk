package edu.jhuapl.saavtk.view.light.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.Colors;
import edu.jhuapl.saavtk.view.ViewActionListener;
import edu.jhuapl.saavtk.view.ViewChangeReason;
import edu.jhuapl.saavtk.view.light.LightCfg;
import edu.jhuapl.saavtk.view.light.LightUtil;
import glum.gui.GuiUtil;
import glum.gui.action.ActionComponentProvider;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * UI panel that provides the functionality for lighting (configuration) of the
 * {@link Renderer}.
 *
 * @author lopeznr1
 */
public class LightingPanel extends GPanel implements ActionComponentProvider, ActionListener, ViewActionListener
{
	// Ref vars
	private final Renderer refRenderer;

	// Gui vars
	private LightCfgPanel lightCfgPanel;
	private final JButton applyToAllViewsB;

	private final JLabel statusL;

	/** Standard Constructor */
	public LightingPanel(Renderer aRenderer)
	{
		refRenderer = aRenderer;

		// Form the GUI
		setLayout(new MigLayout("", "[]", "[]"));

		lightCfgPanel = new LightCfgPanel();
		lightCfgPanel.setLightCfg(refRenderer.getLightCfg());
		add(lightCfgPanel, "growx,pushx,wrap 0");

		statusL = new JLabel("");
		add(statusL, "growx,span,w 0::,h 15::,wrap 0");

		applyToAllViewsB = GuiUtil.createJButton("Apply To all Views", this);

		// Register for events of interest
		refRenderer.addViewChangeListener(this);
		lightCfgPanel.addActionListener(this);
	}

	/** Manual destruction */
	public void dispose()
	{
		refRenderer.delViewChangeListener(this);
		lightCfgPanel.delActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == lightCfgPanel)
			doActionLightPanel();
		else if (source == applyToAllViewsB)
			LightUtil.setSystemLightCfg(lightCfgPanel.getLightCfg());

		updateGui();
	}

	@Override
	public Collection<? extends Component> getActionButtons()
	{
		return ImmutableList.of(applyToAllViewsB);
	}

	@Override
	public void handleViewAction(Object aSource, ViewChangeReason aReason)
	{
		// Process events only relating to light
		if (aReason != ViewChangeReason.Light)
			return;

		lightCfgPanel.setLightCfg(refRenderer.getLightCfg());
	}

	/**
	 * Helper method to handle the LightPanel action.
	 */
	private void doActionLightPanel()
	{
		LightCfg tmpLightCfg = lightCfgPanel.getLightCfg();
		refRenderer.setLightCfg(tmpLightCfg);
	}

	/**
	 * Helper method that updates the various UI elements to keep them synchronized.
	 */
	private void updateGui()
	{
		// ApplyToAllView area
		boolean isValid = lightCfgPanel.getMsgFailList().size() == 0;
		boolean isEnabled = isValid == true;
		applyToAllViewsB.setEnabled(isEnabled);

		// Update the status area
		List<String> errMsgL = lightCfgPanel.getMsgFailList();
		String failMsg = null;
		if (errMsgL.size() > 0)
			failMsg = errMsgL.get(0);
		statusL.setText(failMsg);

		Color fgColor = Colors.getPassFG();
		if (failMsg != null)
			fgColor = Colors.getFailFG();
		statusL.setForeground(fgColor);
	}

}
