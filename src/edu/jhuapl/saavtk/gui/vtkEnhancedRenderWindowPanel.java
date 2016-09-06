package edu.jhuapl.saavtk.gui;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import vtk.vtkBMPWriter;
import vtk.vtkJPEGWriter;
import vtk.vtkPNGWriter;
import vtk.vtkPNMWriter;
import vtk.vtkPostScriptWriter;
import vtk.vtkRenderWindowPanel;
import vtk.vtkTIFFWriter;
import vtk.vtkWindowToImageFilter;

/**
 * The purpose of this class is to add mouse wheel support to vtkRenderWindowPanel
 * which for some reason appears to be missing. It also prevents the render window
 * from accepting focus when the mouse hovers over it, which for some reason is
 * the default in the base class.
 */
public class vtkEnhancedRenderWindowPanel extends vtkRenderWindowPanel
                                                implements
                                                MouseWheelListener
{
    public vtkEnhancedRenderWindowPanel()
    {
        addMouseWheelListener(this);
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        ctrlPressed = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 1
                : 0;
        shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK ? 1
                : 0;

        iren.SetEventInformationFlipY(e.getX(), e.getY(), ctrlPressed,
                shiftPressed, '0', 0, "0");

        Lock();
        if (e.getWheelRotation() > 0)
            iren.MouseWheelBackwardEvent();
        else
            iren.MouseWheelForwardEvent();
        UnLock();
    }

    /**
     * The following function is overridden since for some reason, one of the base classes
     * of this class which implements this method causes the render window to always accept focus
     * whenever the mouse hovers over the window. We don't want this behavior so override it
     * with an empty function.
     */
    public void mouseEntered(MouseEvent e)
    {
        // do nothing
    }

    public void saveToFile(File file)
    {
        if (file != null)
        {
            lock();
            vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
            windowToImage.SetInput(GetRenderWindow());

            String filename = file.getAbsolutePath();
            if (filename.toLowerCase().endsWith("bmp"))
            {
                vtkBMPWriter writer = new vtkBMPWriter();
                writer.SetFileName(filename);
                writer.SetInputConnection(windowToImage.GetOutputPort());
                writer.Write();
            }
            else if (filename.toLowerCase().endsWith("jpg") ||
                    filename.toLowerCase().endsWith("jpeg"))
            {
                vtkJPEGWriter writer = new vtkJPEGWriter();
                writer.SetFileName(filename);
                writer.SetInputConnection(windowToImage.GetOutputPort());
                writer.Write();
            }
            else if (filename.toLowerCase().endsWith("png"))
            {
                vtkPNGWriter writer = new vtkPNGWriter();
                writer.SetFileName(filename);
                writer.SetInputConnection(windowToImage.GetOutputPort());
                writer.Write();
            }
            else if (filename.toLowerCase().endsWith("pnm"))
            {
                vtkPNMWriter writer = new vtkPNMWriter();
                writer.SetFileName(filename);
                writer.SetInputConnection(windowToImage.GetOutputPort());
                writer.Write();
            }
            else if (filename.toLowerCase().endsWith("ps"))
            {
                vtkPostScriptWriter writer = new vtkPostScriptWriter();
                writer.SetFileName(filename);
                writer.SetInputConnection(windowToImage.GetOutputPort());
                writer.Write();
            }
            else if (filename.toLowerCase().endsWith("tif") ||
                    filename.toLowerCase().endsWith("tiff"))
            {
                vtkTIFFWriter writer = new vtkTIFFWriter();
                writer.SetFileName(filename);
                writer.SetInputConnection(windowToImage.GetOutputPort());
                writer.SetCompressionToNoCompression();
                writer.Write();
            }
            unlock();
        }
    }
}
