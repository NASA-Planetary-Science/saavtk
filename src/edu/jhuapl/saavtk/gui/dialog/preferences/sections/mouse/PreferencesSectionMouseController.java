package edu.jhuapl.saavtk.gui.dialog.preferences.sections.mouse;

import java.util.Map;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;

public class PreferencesSectionMouseController implements IPreferencesController
{
	PreferencesSectionMouseUI mouseUI;
	PreferencesSectionMouse mouseModel;

	public PreferencesSectionMouseController()
	{
		super();
		this.mouseUI = new PreferencesSectionMouseUI();
		this.mouseModel = PreferencesSectionMouse.getInstance();
	}

	@Override
	public JPanel getView()
	{
		return mouseUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
//        preferencesMap.put(Preferences.MOUSE_WHEEL_MOTION_FACTOR, ((Double)mouseWheelMotionFactorSpinner.getValue()).toString());

		
		return mouseModel.updateProperties(newPropertiesList);
	}
	
	@Override
	public String getPreferenceName()
	{
		return "Mouse Settings";
	}

//  mouseWheelMotionFactorSpinner.setValue(renderer.getMouseWheelMotionFactor());

}
