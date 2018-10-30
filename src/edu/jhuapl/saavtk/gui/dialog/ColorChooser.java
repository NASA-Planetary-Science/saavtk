package edu.jhuapl.saavtk.gui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.randelshofer.quaqua.colorchooser.SwatchesChooser;


public class ColorChooser
{
    private static Color lastColorChosen = null;

    static public Color showColorChooser(Component parent)
    {
        return showColorChooser(parent, null);
    }

    static public Color showColorChooser(Component parent, int[] initialColor)
    {
//        Color color = null;
//        if (initialColor != null && initialColor.length >= 3)
//            color = new Color(initialColor[0], initialColor[1], initialColor[2]);
//        else if (lastColorChosen != null)
//            color = lastColorChosen;
//        else
//            color = Color.MAGENTA;

        lastColorChosen = new ColorChooser(Color.blue).showColorDialog(parent);//JColorChooser.showDialog(parent, "Color Chooser Dialog", color);

        return lastColorChosen;
    }

    static public ColorChooser of(Color initialColor) {
    	return new ColorChooser(initialColor);
    }

    private final JColorChooser chooser;
	private Color currentColor;

    private ColorChooser(Color initialColor) {
    	this.chooser = new JColorChooser(initialColor);
    	this.currentColor = initialColor;
    	chooser.setPreviewPanel(new ColorMeterPanel(chooser));
    }

    public Color showColorDialog(Component parent)
    {
    	ActionListener ok = new ActionListener() {

			@Override
			public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
				currentColor = chooser.getColor();
			}
    		
    	};

    	ActionListener cancel = new ActionListener() {

			@Override
			public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
			}
    		
    	};

    	JDialog dialog = JColorChooser.createDialog(parent, "Color Chooser Dialog", true, chooser, ok, cancel);
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
