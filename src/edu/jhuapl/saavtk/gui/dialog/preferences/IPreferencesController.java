package edu.jhuapl.saavtk.gui.dialog.preferences;

import java.util.Map;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.View;

public interface IPreferencesController
{		
	public JPanel getView();
	
	public boolean updateProperties(Map<String, String> newPropertiesList);
	
	public String getPreferenceName();
}
