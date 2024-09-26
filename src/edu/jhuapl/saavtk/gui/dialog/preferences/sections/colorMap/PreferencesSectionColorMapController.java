package edu.jhuapl.saavtk.gui.dialog.preferences.sections.colorMap;

import java.util.Map;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;
import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesSectionColorMapController implements IPreferencesController
{
	PreferencesSectionColorMapUI colorMapUI;
	PreferencesSectionDefColorMap colorModel;

	public PreferencesSectionColorMapController()
	{
		this.colorMapUI = new PreferencesSectionColorMapUI();
		this.colorModel = PreferencesSectionDefColorMap.getInstance();
	    colorMapUI.getDefaultColorMapSelection().setSelectedItem(Colormaps.getCurrentColormapName());

	}

	@Override
	public JPanel getView()
	{
		return colorMapUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		Object colormapSelection = colorMapUI.getDefaultColorMapSelection().getSelectedItem();

		// Ensure colormapSelection is a (possibly null) string:
		if (colormapSelection != null)
		{
			colormapSelection = colormapSelection.toString();
		}

		Colormaps.setCurrentColormapName((String) colormapSelection);
		newPropertiesList.put(Preferences.DEFAULT_COLORMAP_NAME, (String) colorMapUI.getDefaultColorMapSelection().getSelectedItem());
		return colorModel.updateProperties(newPropertiesList);
	}
	
	@Override
	public String getPreferenceName()
	{
		return "Color Map";
	}

}
