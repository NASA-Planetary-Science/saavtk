package edu.jhuapl.saavtk.gui.coloringData;

import java.io.IOException;

import javax.swing.JFrame;

import edu.jhuapl.saavtk.model.FacetColoringData;


public class ColoringInfoWindow extends JFrame //implements PropertyChangeListener
{
    private FacetColoringData[] coloringData;

    public ColoringInfoWindow(FacetColoringData[] coloringData) throws IOException
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Plate Coloring Statistics");
        this.coloringData = coloringData;

        ColoringInfoPanel panel = new ColoringInfoPanel(coloringData);

        add(panel);
        pack();
        setVisible(true);
    }
}