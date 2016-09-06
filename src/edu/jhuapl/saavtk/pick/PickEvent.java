package edu.jhuapl.saavtk.pick;

import java.awt.event.MouseEvent;

import vtk.vtkProp;

public class PickEvent
{
    private MouseEvent mouseEvent;
    private vtkProp pickedProp;
    private int pickedCellId;
    private double[] pickedPosition;

    public PickEvent(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
    {
        this.mouseEvent = e;
        this.pickedProp = pickedProp;
        this.pickedCellId = pickedCellId;
        this.pickedPosition = pickedPosition;
    }

    public MouseEvent getMouseEvent()
    {
        return mouseEvent;
    }

    public vtkProp getPickedProp()
    {
        return pickedProp;
    }

    public int getPickedCellId()
    {
        return pickedCellId;
    }

    public double[] getPickedPosition()
    {
        return pickedPosition;
    }
}
