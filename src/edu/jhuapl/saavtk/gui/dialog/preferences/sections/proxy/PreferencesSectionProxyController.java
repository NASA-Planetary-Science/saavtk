package edu.jhuapl.saavtk.gui.dialog.preferences.sections.proxy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;
import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesSectionProxyController implements IPreferencesController
{
	PreferencesSectionProxyUI proxyUI;
	PreferencesSectionProxy proxyModel;

	public PreferencesSectionProxyController()
	{
		this.proxyModel = PreferencesSectionProxy.getInstance();
		this.proxyUI = new PreferencesSectionProxyUI();

		proxyUI.getProxyEnableCheckBox().setText("Enable");
		proxyUI.getProxyEnableCheckBox().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
	        	LinkedHashMap<String, String> preferencesMap = new LinkedHashMap<String, String>();

				if (!proxyUI.getProxyEnableCheckBox().isSelected())
				{
					proxyModel.turnOffProxy();
				}
	            updateProperties(preferencesMap);

			}
		});
		
		proxyUI.getProxyHostTextField().setText(Preferences.getInstance().get(Preferences.PROXY_HOST, "ENTER HOST HERE"));
        proxyUI.getProxyPortTextField().setText(Preferences.getInstance().get(Preferences.PROXY_PORT, "ENTER PORT HERE"));
        proxyUI.getProxyEnableCheckBox().setSelected(Preferences.getInstance().getAsBoolean(Preferences.PROXY_ENABLED, false));
	}

	@Override
	public JPanel getView()
	{
		return proxyUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		newPropertiesList.put(Preferences.PROXY_HOST, proxyUI.getProxyHostTextField().getText());
		newPropertiesList.put(Preferences.PROXY_PORT, proxyUI.getProxyPortTextField().getText());
		newPropertiesList.put(Preferences.PROXY_ENABLED, proxyUI.getProxyEnableCheckBox().isSelected()+"");
		return proxyModel.updateProperties(newPropertiesList);
	}
	
	@Override
	public String getPreferenceName()
	{
		return "Proxy Settings";
	}
}
