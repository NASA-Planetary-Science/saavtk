package edu.jhuapl.saavtk.gui.dialog.preferences.sections.font;

import java.util.Map;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesSection;

public class PreferencesSectionFont implements IPreferencesSection
{
	private static PreferencesSectionFont ref = null;

    public static PreferencesSectionFont getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionFont();
        return ref;
    }

	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
