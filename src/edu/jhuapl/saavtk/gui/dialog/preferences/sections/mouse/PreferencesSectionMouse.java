package edu.jhuapl.saavtk.gui.dialog.preferences.sections.mouse;

import java.util.Map;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesSection;

public class PreferencesSectionMouse implements IPreferencesSection
{
	private static PreferencesSectionMouse ref = null;

    public static PreferencesSectionMouse getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionMouse();
        return ref;
    }

	@Override
	public boolean updateProperties(Map<String, String>newPropertiesList)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
