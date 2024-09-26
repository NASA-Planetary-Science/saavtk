package edu.jhuapl.saavtk.gui.dialog.preferences.sections.font;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;
import edu.jhuapl.saavtk.util.ColorIcon;

public class PreferencesSectionFontController implements IPreferencesController
{
	PreferencesSectionFontUI fontUI;
	PreferencesSectionFont fontModel;
	
	public PreferencesSectionFontController()
	{
		this.fontUI = new PreferencesSectionFontUI();
		this.fontModel = PreferencesSectionFont.getInstance();
	}
	
    private void fontColorButtonActionPerformed(ActionEvent evt)
    {
        showColorChooser(fontUI.getFontColorLabel());
    }
    
    private void showColorChooser(JLabel label)
	{
		int[] initialColor = getColorFromLabel(label);
		Color color = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(fontUI), initialColor);

		if (color == null)
			return;

		int[] c = new int[3];
		c[0] = color.getRed();
		c[1] = color.getGreen();
		c[2] = color.getBlue();

		updateColorLabel(c, label);
	}

	private int[] getColorFromLabel(JLabel label)
	{
		Color color = ((ColorIcon) label.getIcon()).getColor();
		int[] c = new int[3];
		c[0] = color.getRed();
		c[1] = color.getGreen();
		c[2] = color.getBlue();
		return c;
	}

	private void updateColorLabel(int[] color, JLabel label)
	{
		int[] c = color;
		label.setText("[" + c[0] + "," + c[1] + "," + c[2] + "]");
		label.setIcon(new ColorIcon(new Color(c[0], c[1], c[2])));
	}

	private void updateColorLabel(Color color, JLabel label)
	{
		int[] c = new int[]
		{ color.getRed(), color.getGreen(), color.getBlue() };
		updateColorLabel(c, label);
	}

	@Override
	public JPanel getView()
	{
		return fontUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		return fontModel.updateProperties(newPropertiesList);
	}
	
	@Override
	public String getPreferenceName()
	{
		return "Fonts";
	}
}
