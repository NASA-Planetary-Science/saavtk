package edu.jhuapl.saavtk.gui.dialog.preferences.sections.axes;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;
import edu.jhuapl.saavtk.gui.render.Renderer;

public class PreferencesSectionAxesController implements IPreferencesController
{
	PreferencesSectionAxes axesModel;
	PreferencesSectionAxesUI axesUI;

	public PreferencesSectionAxesController()
	{
		super();
		this.axesModel = PreferencesSectionAxes.getInstance();
		this.axesUI = new PreferencesSectionAxesUI();

//      showAxesCheckBox.setSelected(renderer.getShowOrientationAxes());
//      interactiveCheckBox.setSelected(renderer.getOrientationAxesInteractive());
		
		/*
		 * updateColorLabel(axesPanel.getxColor(), xAxisColorLabel);
		 * updateColorLabel(axesPanel.getyColor(), yAxisColorLabel);
		 * updateColorLabel(axesPanel.getzColor(), zAxisColorLabel);
		 *
		 * updateColorLabel(axesPanel.getFontColor(), fontColorLabel);
		 *
		 * axesSizeSpinner.setValue(axesPanel.getShaftlength());
		 * axesLineWidthSpinner.setValue(axesPanel.getLinewidth());
		 * axesFontSpinner.setValue(axesPanel.getFontsize());
		 * axesConeLengthSpinner.setValue(axesPanel.getConelength());
		 * axesConeRadiusSpinner.setValue(axesPanel.getConeradius());
		 */
	}

//	@Override
//	public void applyToView(View v)
//	{
//		Renderer renderer = v.getRenderer();
//		if (renderer == null)
//			return;
//        /*
//         * axesPanel.setxColor(getColorInstanceFromLabel(xAxisColorLabel));
//         * axesPanel.setyColor(getColorInstanceFromLabel(yAxisColorLabel));
//         * axesPanel.setzColor(getColorInstanceFromLabel(zAxisColorLabel));
//         * axesPanel.setFontColor(getColorInstanceFromLabel(fontColorLabel));
//         * axesPanel.setConelength((Double)axesConeLengthSpinner.getValue());
//         * axesPanel.setConeradius((Double)axesConeRadiusSpinner.getValue());
//         * axesPanel.setFontsize((Integer)axesFontSpinner.getValue());
//         * axesPanel.setLinewidth((Double)axesLineWidthSpinner.getValue());
//         * axesPanel.setShaftlength((Double)axesSizeSpinner.getValue());
//         */
////        RenderPanel renderPanel = v.getRenderer().getRenderWindowPanel();
////        AxesPanel axesPanel = renderPanel.getAxesPanel();
////        axesPanel.getRenderer().SetBackground(rgbArr[0] / 255.0, rgbArr[1] / 255.0, rgbArr[2] / 255.0);
////        axesPanel.Render();
//		
////      renderer.setShowOrientationAxes(showAxesCheckBox.isSelected());
////      renderer.setOrientationAxesInteractive(interactiveCheckBox.isSelected());
//	}

	@Override
	public JPanel getView()
	{
		return axesUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		return axesModel.updateProperties(newPropertiesList);
	}
	
	@Override
	public String getPreferenceName()
	{
		return "Renderer Axes";
	}

//  private void xAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xAxisColorButtonActionPerformed
//  showColorChooser(xAxisColorLabel);
//}//GEN-LAST:event_xAxisColorButtonActionPerformed
//
//private void yAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yAxisColorButtonActionPerformed
//  showColorChooser(yAxisColorLabel);
//}//GEN-LAST:event_yAxisColorButtonActionPerformed
//
//private void zAxisColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zAxisColorButtonActionPerformed
//  showColorChooser(zAxisColorLabel);
//}//GEN-LAST:event_zAxisColorButtonActionPerformed

//  preferencesMap.put(Preferences.AXES_XAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(xAxisColorLabel))));
//  preferencesMap.put(Preferences.AXES_YAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(yAxisColorLabel))));
//  preferencesMap.put(Preferences.AXES_ZAXIS_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(zAxisColorLabel))));
//  preferencesMap.put(Preferences.AXES_CONE_LENGTH, ((Integer)axesConeLengthSpinner.getValue()).toString());
//  preferencesMap.put(Preferences.AXES_CONE_RADIUS, ((Integer)axesConeRadiusSpinner.getValue()).toString());
//  preferencesMap.put(Preferences.AXES_FONT_SIZE, ((Integer)axesFontSpinner.getValue()).toString());
//  preferencesMap.put(Preferences.AXES_FONT_COLOR, Joiner.on(",").join(Ints.asList(getColorFromLabel(fontColorLabel))));
//  preferencesMap.put(Preferences.AXES_LINE_WIDTH, ((Integer)axesLineWidthSpinner.getValue()).toString());
//  preferencesMap.put(Preferences.AXES_SIZE, ((Integer)axesSizeSpinner.getValue()).toString());

//  preferencesMap.put(Preferences.SHOW_AXES, ((Boolean)showAxesCheckBox.isSelected()).toString());
//  preferencesMap.put(Preferences.INTERACTIVE_AXES, ((Boolean)interactiveCheckBox.isSelected()).toString());
}
