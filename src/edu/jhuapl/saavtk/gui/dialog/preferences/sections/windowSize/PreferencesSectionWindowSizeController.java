package edu.jhuapl.saavtk.gui.dialog.preferences.sections.windowSize;

import java.awt.Dimension;
import java.awt.Toolkit;
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

         windowSizeUI.getPanelHeightTextField().setText("" + (window.getSize().height-100));
         windowSizeUI.getPanelWidthTextField().setValue(window.getSize().width - ViewManager.getGlobalViewManager().getCurrentView().getMainSplitPane().getDividerLocation()-11);
	}

	@Override
	public JPanel getView()
	{
		return windowSizeUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{		
		windowSizeUI.getErrorLabel().setText("");
		String errors = checkForErrors();
		if (!errors.isEmpty())
		{
			windowSizeUI.getErrorLabel().setText(errors);
			return false;
		}
		
		View view = ViewManager.getGlobalViewManager().getCurrentView();
		MainWindow window = MainWindow.getMainWindow();
		int dividerLocation = (int) windowSizeUI.getWindowWidthTextField().getValue() - (int) windowSizeUI.getPanelWidthTextField().getValue();
		view.getMainSplitPane().setDividerLocation(dividerLocation);
		
		window.setSize((int) windowSizeUI.getPanelWidthTextField().getValue() + view.getMainSplitPane().getDividerLocation() + 11,
				(int) (windowSizeUI.getWindowHeightTextField().getValue())/* + 128*/);

		windowSizeUI.getPanelHeightTextField().setText("" + ((int) (windowSizeUI.getWindowHeightTextField().getValue()) - 100));
		return windowModel.updateProperties(newPropertiesList);
	}
	
	private String checkForErrors()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		String errorMessage = "";
		if ((int)windowSizeUI.getWindowHeightTextField().getValue() > screenSize.getHeight())
			errorMessage = "App Window height cannot exceed screen height (" + screenSize.getHeight() + ")";
		else if ((int)windowSizeUI.getWindowWidthTextField().getValue() > screenSize.getWidth())
			errorMessage = "App Window width cannot exceed screen width (" + screenSize.getWidth() + ")";
		else if ((int)windowSizeUI.getPanelWidthTextField().getValue() > (int)windowSizeUI.getWindowWidthTextField().getValue())
			errorMessage = "Renderer width cannot exceed App Window width (" + (int)windowSizeUI.getWindowWidthTextField().getValue() + ")";
		else if ((int)windowSizeUI.getWindowHeightTextField().getValue() < 600)
			errorMessage = "App Window height must be 600 pixels or greater";
		
		return errorMessage;
	}

	@Override
	public String getPreferenceName()
	{
		return "Window Size";
	}
}
