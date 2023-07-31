package edu.jhuapl.saavtk.gui.dialog;

import java.util.LinkedHashMap;

import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesSectionPickTolerance implements IPreferencesSection {

	private LinkedHashMap<String, String> prefs;
	private Preferences prefsInstance;
	private String pickTolerance;
	
	private static PreferencesSectionPickTolerance ref = null;

    public static PreferencesSectionPickTolerance getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionPickTolerance();
        return ref;
    }
	
	private PreferencesSectionPickTolerance() {
		super();
		prefs = new LinkedHashMap<String, String>();
		prefsInstance = Preferences.getInstance();
		pickTolerance = prefsInstance.get(Preferences.PICK_TOLERANCE);
	}

	@Override
	public boolean updateProperties(LinkedHashMap<String, String> newPropertiesList) {
		pickTolerance = newPropertiesList.get(Preferences.PICK_TOLERANCE);
		prefs.put(Preferences.PICK_TOLERANCE, pickTolerance);
		prefsInstance.put(prefs);
		return true;
	}

}
