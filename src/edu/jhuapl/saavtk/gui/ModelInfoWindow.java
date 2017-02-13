package edu.jhuapl.saavtk.gui;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import vtk.vtkPropPicker;

import edu.jhuapl.saavtk.gui.jogl.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.Model;

public abstract class ModelInfoWindow extends JFrame implements PropertyChangeListener
{
    public ModelInfoWindow()
    {
        ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/sbmt/data/eros.png"));
        setIconImage(erosIcon.getImage());
    }

    public abstract Model getModel();

    /**
     * Get the collection model which directly manages the model returned by getModel.
     * E.g. for MSI Images this would MSIImageCollection
     *
     * @return
     */
    public abstract Model getCollectionModel();

    protected int doPick(MouseEvent e, vtkPropPicker picker, vtksbmtJoglCanvas renWin)
    {
        int pickSucceeded = 0;
        try
        {
            renWin.getComponent().getContext().makeCurrent();
            renWin.getVTKLock().lock();
            // Note that on some displays, such as a retina display, the height used by
            // OpenGL is different than the height used by Java. Therefore we need
            // scale the mouse coordinates to get the right position for OpenGL.
//            double openGlHeight = renWin.getComponent().getSurfaceHeight();
            double openGlHeight = renWin.getComponent().getHeight();
            double javaHeight = renWin.getComponent().getHeight();
            double scale = openGlHeight / javaHeight;
            pickSucceeded = picker.Pick(scale*e.getX(), scale*(javaHeight-e.getY()-1), 0.0, renWin.getRenderer());
            renWin.getVTKLock().unlock();
        }
        finally
        {
            renWin.getComponent().getContext().release();
        }

        return pickSucceeded;
    }
}
