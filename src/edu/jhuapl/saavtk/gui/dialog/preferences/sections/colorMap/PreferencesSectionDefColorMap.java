package edu.jhuapl.saavtk.gui.dialog.preferences.sections.colorMap;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesSection;
import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesSectionDefColorMap implements IPreferencesSection {

	private LinkedHashMap<String, String> prefs;
	private Preferences prefsInstance;
	private String defaultColorMapName;
	
	private static PreferencesSectionDefColorMap ref = null;

    public static PreferencesSectionDefColorMap getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionDefColorMap();
        return ref;
    }
	
	private PreferencesSectionDefColorMap() {
		super();
		prefs = new LinkedHashMap<String, String>();
		prefsInstance = Preferences.getInstance();
		defaultColorMapName = (String) prefsInstance.get(Preferences.DEFAULT_COLORMAP_NAME);
	}

	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList) 
	{
		System.out.println("PreferencesSectionDefColorMap: updateProperties: updating");
		defaultColorMapName = newPropertiesList.get(Preferences.DEFAULT_COLORMAP_NAME);
		prefs.put(Preferences.DEFAULT_COLORMAP_NAME, defaultColorMapName);
		prefsInstance.put(prefs);
		return true;
	}

}
