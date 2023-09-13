package edu.jhuapl.saavtk.gui.dialog.preferences.sections.proxy;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesSection;
import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesSectionProxy implements IPreferencesSection {

	private LinkedHashMap<String, String> prefs;
	private Preferences prefsInstance;
	private String host;
	private String port;
	private String enabled;
	
	private static PreferencesSectionProxy ref = null;

    public static PreferencesSectionProxy getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionProxy();
        return ref;
    }
	
	private PreferencesSectionProxy() {
		super();
		prefs = new LinkedHashMap<String, String>();
		prefsInstance = Preferences.getInstance();
		host = (String) prefsInstance.get(Preferences.PROXY_HOST);
		port = (String) prefsInstance.get(Preferences.PROXY_PORT);
		enabled = (String) prefsInstance.get(Preferences.PROXY_ENABLED); 
		prefs.put(Preferences.PROXY_HOST, host);
		prefs.put(Preferences.PROXY_PORT, port);
		prefs.put(Preferences.PROXY_ENABLED, enabled);
		
		if (Boolean.parseBoolean(enabled)) {
			System.setProperty("http.proxyHost", host);
			System.setProperty("http.proxyPort", port);
		}
	}

	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList) 
	{
		host = (String) newPropertiesList.get(Preferences.PROXY_HOST);
		port = (String) newPropertiesList.get(Preferences.PROXY_PORT);
		enabled = newPropertiesList.get(Preferences.PROXY_ENABLED);
		prefs.put(Preferences.PROXY_HOST, host);
		prefs.put(Preferences.PROXY_PORT, port);
		prefs.put(Preferences.PROXY_ENABLED, enabled+"");
		System.setProperty("http.proxyHost", host);
		System.setProperty("http.proxyPort", port);
		prefsInstance.put(prefs);
		return true;
	}

	public boolean turnOffProxy() {
		System.clearProperty("http.proxyHost");
		System.clearProperty("http.proxyPort");
		prefs.put(Preferences.PROXY_ENABLED, "false");
		prefsInstance.put(prefs);
		return false;
	}
	
	// Returns source of truth for proxy preferences
	public LinkedHashMap<String, String> getProperties() {
		return prefs;	
	}

}
