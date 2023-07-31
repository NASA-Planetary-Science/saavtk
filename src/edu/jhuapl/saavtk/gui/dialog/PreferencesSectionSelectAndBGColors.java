package edu.jhuapl.saavtk.gui.dialog;

import java.util.LinkedHashMap;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesSectionSelectAndBGColors implements IPreferencesSection {
	
	private LinkedHashMap<String, String> prefs;
	private Preferences prefsInstance;
	private String selectionColor;
	private String backgroundColor;
	
	private static PreferencesSectionSelectAndBGColors ref = null;

    public static PreferencesSectionSelectAndBGColors getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionSelectAndBGColors();
        return ref;
    }
	
	private PreferencesSectionSelectAndBGColors() {
		super();
		prefs = new LinkedHashMap<String, String>();
		prefsInstance = Preferences.getInstance();
		selectionColor = prefsInstance.get(Preferences.SELECTION_COLOR);
		backgroundColor = prefsInstance.get(Preferences.BACKGROUND_COLOR);
	}

	@Override
	public boolean updateProperties(LinkedHashMap<String, String> newPropertiesList) {
		selectionColor = newPropertiesList.get(Preferences.SELECTION_COLOR);
		backgroundColor = newPropertiesList.get(Preferences.BACKGROUND_COLOR);
		prefs.put(Preferences.SELECTION_COLOR, selectionColor);
		prefs.put(Preferences.BACKGROUND_COLOR, backgroundColor);
		prefsInstance.put(prefs);
		return true;
	}

}
