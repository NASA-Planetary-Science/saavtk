package edu.jhuapl.saavtk.popup;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import vtk.vtkProp;

public abstract class PopupMenu extends JPopupMenu
{
    abstract public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition);
}
