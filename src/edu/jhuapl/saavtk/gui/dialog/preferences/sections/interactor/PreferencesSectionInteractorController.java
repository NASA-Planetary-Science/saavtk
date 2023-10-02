package edu.jhuapl.saavtk.gui.dialog.preferences.sections.interactor;

import java.util.Map;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;

public class PreferencesSectionInteractorController implements IPreferencesController
{
	PreferencesSectionInteractorUI interactorUI;
	PreferencesSectionInteractor interactorModel;

	public PreferencesSectionInteractorController()
	{
		interactorModel = PreferencesSectionInteractor.getInstance();
		interactorUI = new PreferencesSectionInteractorUI();
		interactorUI.getJoystickRadioButton().setEnabled(false);
		interactorUI.getTrackballRadioButton().setEnabled(false);
	}

//	@Override
//	public void applyToView(View v)
//	{
//		Renderer renderer = v.getRenderer();
//		if (renderer == null)
//			return;
//		/*
//		 * if (joystickRadioButton.isSelected())
//		 * renderer.setDefaultInteractorStyleType(InteractorStyleType.JOYSTICK_CAMERA);
//		 * else
//		 * renderer.setDefaultInteractorStyleType(InteractorStyleType.TRACKBALL_CAMERA);
//		 */
//	}

	@Override
	public JPanel getView()
	{
		return interactorUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		View view = ViewManager.getGlobalViewManager().getCurrentView();
		
		return interactorModel.updateProperties(newPropertiesList);
	}
	
	@Override
	public String getPreferenceName()
	{
		return "Renderer Interactor Mode";
	}

//  if (joystickRadioButton.isSelected())
	// preferencesMap.put(Preferences.INTERACTOR_STYLE_TYPE,
	// InteractorStyleType.JOYSTICK_CAMERA.toString());
	// else
	// preferencesMap.put(Preferences.INTERACTOR_STYLE_TYPE,
	// InteractorStyleType.TRACKBALL_CAMERA.toString());

	/*
	 * if (renderer.getDefaultInteractorStyleType() ==
	 * Renderer.InteractorStyleType.JOYSTICK_CAMERA)
	 * joystickRadioButton.setSelected(true); else
	 * trackballRadioButton.setSelected(true);
	 */

}
