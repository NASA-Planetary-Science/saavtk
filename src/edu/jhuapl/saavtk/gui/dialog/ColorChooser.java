package edu.jhuapl.saavtk.gui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JDialog;

public class ColorChooser
{
	private static Color lastColorChosen = null;

	static public Color showColorChooser(Component parent)
	{
		return showColorChooser(parent, (Color) null);
	}

	static public Color showColorChooser(Component parent, int[] aColorArr)
	{
		Color tmpColor = null;
		if (aColorArr != null && aColorArr.length >= 3)
			tmpColor = new Color(aColorArr[0], aColorArr[1], aColorArr[2]);

		// Delegate
		return showColorChooser(parent, tmpColor);
	}

	static public Color showColorChooser(Component parent, Color aColor)
	{
		Color color = null;
		if (aColor != null)
			color = aColor;
		else if (lastColorChosen != null)
			color = lastColorChosen;
		else
			color = Color.MAGENTA;

		lastColorChosen = new ColorChooser(color).showColorDialog(parent);// JColorChooser.showDialog(parent, "Color
																								// Chooser Dialog", color);

		return lastColorChosen;
	}

	static public ColorChooser of(Color initialColor)
	{
		return new ColorChooser(initialColor);
	}

	private final JColorChooser chooser;
	private Color currentColor;

	private ColorChooser(Color initialColor)
	{
		this.chooser = new JColorChooser(initialColor);
		this.currentColor = initialColor;
		chooser.setPreviewPanel(new ColorMeterPanel(chooser));
	}

	public Color showColorDialog(Component parent)
	{
		ActionListener acceptAL = (aEvent) -> {
			currentColor = chooser.getColor();
		};

		ActionListener cancelAL = (aEvent) -> {
			currentColor = null;
		};

		JDialog dialog = JColorChooser.createDialog(parent, "Color Chooser Dialog", true, chooser, acceptAL, cancelAL);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		return currentColor;
	}

	public Color getColor()
	{
		return currentColor;
	}

	public void setColor(Color color)
	{
		if (color != null)
		{
			currentColor = color;
		}
	}

}
