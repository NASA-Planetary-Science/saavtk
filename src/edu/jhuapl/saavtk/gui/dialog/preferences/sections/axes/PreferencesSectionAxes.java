package edu.jhuapl.saavtk.gui.dialog.preferences.sections.axes;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesSection;

public class PreferencesSectionAxes implements IPreferencesSection
{

	private static PreferencesSectionAxes ref = null;

    public static PreferencesSectionAxes getInstance()
    {
        if (ref == null)
            ref = new PreferencesSectionAxes();
        return ref;
    }

	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
