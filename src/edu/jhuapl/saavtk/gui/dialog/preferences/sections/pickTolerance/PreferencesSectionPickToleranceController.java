package edu.jhuapl.saavtk.gui.dialog.preferences.sections.pickTolerance;

import java.util.Map;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesSectionPickToleranceController implements IPreferencesController
{
	PreferencesSectionPickTolerance toleranceModel;
	PreferencesSectionPickToleranceUI toleranceUI;
    private static final double MAX_TOLERANCE = 0.01;
	
	public PreferencesSectionPickToleranceController()
	{
		this.toleranceModel = PreferencesSectionPickTolerance.getInstance();
		this.toleranceUI = new PreferencesSectionPickToleranceUI();
		
		PickManager pickManager = ViewManager.getGlobalViewManager().getCurrentView().getPickManager();
        int value = getSliderValueFromTolerance(pickManager.getPickTolerance());
        toleranceUI.getPickToleranceSlider().setValue(value);
	}
	
    private double getToleranceFromSliderValue(int value)
    {
        return MAX_TOLERANCE * value / toleranceUI.getPickToleranceSlider().getMaximum();
    }

    private int getSliderValueFromTolerance(double tolerance)
    {
        return (int) (toleranceUI.getPickToleranceSlider().getMaximum()
                * tolerance / MAX_TOLERANCE);
    }

	@Override
	public JPanel getView()
	{
		return toleranceUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		PickManager pickManager = ViewManager.getGlobalViewManager().getCurrentView().getPickManager();
        double tolerance = getToleranceFromSliderValue(toleranceUI.getPickToleranceSlider().getValue());
        pickManager.setPickTolerance(tolerance);
		newPropertiesList.put(Preferences.PICK_TOLERANCE,
				Double.valueOf(getToleranceFromSliderValue(toleranceUI.getPickToleranceSlider().getValue())).toString());		
		return toleranceModel.updateProperties(newPropertiesList);
	}
	
	@Override
	public String getPreferenceName()
	{
		return "Pick Tolerance";
	}
	
}
