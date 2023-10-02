package edu.jhuapl.saavtk.gui.dialog.preferences.sections.windowSize;

import java.util.Map;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesSection;

public class PreferencesSectionWindowSize implements IPreferencesSection
{
	
	private static PreferencesSectionWindowSize ref = null;

    public static PreferencesSectionWindowSize getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionWindowSize();
        return ref;
    }
	

	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		return true;
	}

}
