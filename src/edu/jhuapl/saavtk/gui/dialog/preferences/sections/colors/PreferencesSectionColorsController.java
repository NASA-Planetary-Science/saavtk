package edu.jhuapl.saavtk.gui.dialog.preferences.sections.colors;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.preferences.IPreferencesController;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.util.ColorIcon;
import edu.jhuapl.saavtk.util.Preferences;

public class PreferencesSectionColorsController implements IPreferencesController
{
	PreferencesSectionSelectAndBGColors colorModel;
	PreferencesSectionColorsUI colorUI;
	private ViewManager viewManager;

	public PreferencesSectionColorsController()
	{
		this.colorModel = PreferencesSectionSelectAndBGColors.getInstance();
		this.colorUI = new PreferencesSectionColorsUI();
		this.viewManager = ViewManager.getGlobalViewManager();
		
		colorUI.getBackgroundColorButton().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				backgroundColorButtonActionPerformed(evt);
			}
		});
		
		colorUI.getSelectionColorButton().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				selectionColorButtonActionPerformed(evt);
			}
		});
		
		colorUI.getApplyToAllButton().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				applyToAllButtonActionPerformed(evt);
			}
		});
		
		colorUI.getApplyToCurrentButton().addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				applyToCurrentButtonActionPerformed(evt);
			}
		});
		
		Color color = viewManager.getCurrentView().getModelManager().getCommonData().getSelectionColor();
        updateColorLabel(color, colorUI.getSelectionColorLabel());

        int[] rgbArr = viewManager.getCurrentView().getRenderer().getBackgroundColor();
        updateColorLabel(rgbArr, colorUI.getBackgroundColorLabel());
	}
	
	private void applyToCurrentButtonActionPerformed(ActionEvent evt)
	{
		applyToView(viewManager.getCurrentView());
	}
	
	private void applyToAllButtonActionPerformed(ActionEvent evt)
	{
		List<View> views = viewManager.getAllViews();
		for (View v : views)
		{
			applyToView(v);
		}
	}

	public void applyToView(View v)
	{
		Renderer renderer = v.getRenderer();
		if (renderer == null)
			return;
        Color color = getColorInstanceFromLabel(colorUI.getSelectionColorLabel());
        v.getModelManager().getCommonData().setSelectionColor(color);

        int[] rgbArr = getColorFromLabel(colorUI.getBackgroundColorLabel());
        renderer.setBackgroundColor(rgbArr);

	}
	
    private Color getColorInstanceFromLabel(JLabel label)
    {
        return ((ColorIcon) label.getIcon()).getColor();
    }

	private void selectionColorButtonActionPerformed(ActionEvent evt)
	{
		showColorChooser(colorUI.getSelectionColorLabel());
	}

	private void backgroundColorButtonActionPerformed(ActionEvent evt)
	{
		showColorChooser(colorUI.getBackgroundColorLabel());
	}

	private void showColorChooser(JLabel label)
	{
		int[] initialColor = getColorFromLabel(label);
		Color color = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(colorUI), initialColor);

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
		return colorUI;
	}
	
	@Override
	public boolean updateProperties(Map<String, String> newPropertiesList)
	{
		newPropertiesList.put(Preferences.SELECTION_COLOR,
				Joiner.on(",").join(Ints.asList(getColorFromLabel(colorUI.getSelectionColorLabel()))));
		newPropertiesList.put(Preferences.BACKGROUND_COLOR,
				Joiner.on(",").join(Ints.asList(getColorFromLabel(colorUI.getBackgroundColorLabel()))));
		return colorModel.updateProperties(newPropertiesList);
	}
	
	@Override
	public String getPreferenceName()
	{
		return "Selection and Background Colors";
	}
}
