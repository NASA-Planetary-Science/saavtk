package edu.jhuapl.saavtk.gui.dialog.preferences;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import com.github.davidmoten.guavamini.Lists;

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.preferences.sections.colorMap.PreferencesSectionColorMapController;
import edu.jhuapl.saavtk.gui.dialog.preferences.sections.colors.PreferencesSectionColorsController;
import edu.jhuapl.saavtk.gui.dialog.preferences.sections.pickTolerance.PreferencesSectionPickToleranceController;
import edu.jhuapl.saavtk.gui.dialog.preferences.sections.proxy.PreferencesSectionProxyController;
import edu.jhuapl.saavtk.gui.dialog.preferences.sections.windowSize.PreferencesSectionWindowSizeController;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesController
{
	PreferencesDialog dialog;
	private RenderPanel renderPanel;
	private List<IPreferencesController> controllers;

	public PreferencesController(Frame parent, boolean modal, ViewManager viewManager)
	{
		dialog = new PreferencesDialog(parent, modal);
		this.controllers = Lists.newArrayList();
		this.renderPanel = viewManager.getCurrentView().getRenderer().getRenderWindowPanel();
				
		controllers.add(new PreferencesSectionProxyController());
		controllers.add(new PreferencesSectionColorMapController());
		controllers.add(new PreferencesSectionColorsController());
		controllers.add(new PreferencesSectionPickToleranceController());
		controllers.add(new PreferencesSectionWindowSizeController(renderPanel.getComponent().getSize().width));
		
		dialog.setPreferenceSections(controllers);
		
		dialog.getApplyButton().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				applyButtonActionPerformed(evt);
			}
		});

		dialog.getCloseButton().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				closeButtonActionPerformed(evt);
			}
		});
	}
	
	private void applyButtonActionPerformed(ActionEvent evt)
	{
		// In addition, save in preferences file for future use
		Map<String, String> preferencesMap = Preferences.getInstance().getMap();
		dialog.getSelectedController().updateProperties(preferencesMap);

		Preferences.getInstance().put(preferencesMap);
	}

	private void closeButtonActionPerformed(ActionEvent evt)
	{
		dialog.setVisible(false);
	}
	
	public PreferencesDialog getDialog()
	{
		return dialog;
	}
}
