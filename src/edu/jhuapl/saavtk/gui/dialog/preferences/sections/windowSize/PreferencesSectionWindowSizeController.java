package edu.jhuapl.saavtk.gui.dialog.preferences.sections.windowSize;

import java.util.Map;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;

public class PreferencesSectionWindowSizeController implements IPreferencesController
{
	PreferencesSectionWindowSizeUI windowSizeUI;
	PreferencesSectionWindowSize windowModel;
	int renderPanelWidth;

	public PreferencesSectionWindowSizeController(int renderPanelWidth)
	{
		super();
		this.windowSizeUI = new PreferencesSectionWindowSizeUI();
		this.windowModel = PreferencesSectionWindowSize.getInstance();
		this.renderPanelWidth = renderPanelWidth;
		
		 MainWindow window = MainWindow.getMainWindow();
         windowSizeUI.getWindowWidthTextField().setValue(window.getSize().width);
         windowSizeUI.getWindowHeightTextField().setValue(window.getSize().height);

         windowSizeUI.getPanelWidthTextField().setValue(window.getSize().width - ViewManager.getGlobalViewManager().getCurrentView().getMainSplitPane().getDividerLocation());
	}

	@Override
	public JPanel getView()
	{
		return windowSizeUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{		
		View view = ViewManager.getGlobalViewManager().getCurrentView();
		MainWindow window = MainWindow.getMainWindow();
		int dividerLocation = (int) windowSizeUI.getWindowWidthTextField().getValue() - (int) windowSizeUI.getPanelWidthTextField().getValue();
		view.getMainSplitPane().setDividerLocation(dividerLocation);
		
		window.setSize((int) windowSizeUI.getPanelWidthTextField().getValue() + view.getMainSplitPane().getDividerLocation() + 27,
				(int) (windowSizeUI.getWindowHeightTextField().getValue()) + 128);

		return windowModel.updateProperties(newPropertiesList);
	}

	@Override
	public String getPreferenceName()
	{
		return "Window Size";
	}
}
