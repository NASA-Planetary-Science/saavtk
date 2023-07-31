package edu.jhuapl.saavtk.gui.dialog;

import java.util.LinkedHashMap;

public interface IPreferencesSection {

	// Updates section's properties with new values provided by input list
	public boolean updateProperties(LinkedHashMap<String, String> newPropertiesList);

}
