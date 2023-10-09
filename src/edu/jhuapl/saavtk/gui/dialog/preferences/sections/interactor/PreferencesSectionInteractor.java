package edu.jhuapl.saavtk.gui.dialog.preferences.sections.interactor;

import java.util.Map;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesSection;

public class PreferencesSectionInteractor implements IPreferencesSection
{
	
	private static PreferencesSectionInteractor ref = null;

    public static PreferencesSectionInteractor getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionInteractor();
        return ref;
    }

	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
