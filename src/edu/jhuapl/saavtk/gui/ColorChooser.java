package edu.jhuapl.saavtk.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JColorChooser;

public class ColorChooser
{
    private static Color lastColorChosen = null;

    static public Color showColorChooser(Component parent)
    {
        return showColorChooser(parent, null);
    }

    static public Color showColorChooser(Component parent, int[] initialColor)
    {
        Color color = null;
        if (initialColor != null && initialColor.length >= 3)
            color = new Color(initialColor[0], initialColor[1], initialColor[2]);
        else if (lastColorChosen != null)
            color = lastColorChosen;
        else
            color = Color.MAGENTA;

        lastColorChosen = JColorChooser.showDialog(parent, "Color Chooser Dialog", color);

        return lastColorChosen;
    }
}
