package edu.jhuapl.saavtk.gui.dialog.preferences;

import java.util.Map;

public interface IPreferencesSection {

	// Updates section's properties with new values provided by input list
	public boolean updateProperties(Map<String, String> newPropertiesList);

}
