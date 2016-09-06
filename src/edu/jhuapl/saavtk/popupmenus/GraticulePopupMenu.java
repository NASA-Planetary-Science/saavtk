package edu.jhuapl.saavtk.popupmenus;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import vtk.vtkProp;
import edu.jhuapl.saavtk.gui.ColorChooser;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;

public class GraticulePopupMenu extends PopupMenu
{
    private Graticule graticule;
    private Component invoker;
    private JMenuItem colorMenuItem;
    private JMenuItem thicknessMenuItem;

    public GraticulePopupMenu(ModelManager modelManager,
            Component invoker)
    {
        this.graticule = (Graticule)modelManager.getModel(ModelNames.GRATICULE);
        this.invoker = invoker;

        colorMenuItem = new JMenuItem(new ChangeColorAction());
        colorMenuItem.setText("Change Color...");
        this.add(colorMenuItem);

        thicknessMenuItem = new JMenuItem(new ChangeThicknessAction());
        thicknessMenuItem.setText("Change Line Width...");
        this.add(thicknessMenuItem);
    }

    public class ChangeColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent arg0)
        {
            Color color = graticule.getColor();
            int[] initialColor = {color.getRed(), color.getGreen(), color.getBlue()};

            color = ColorChooser.showColorChooser(invoker, initialColor);

            if (color == null)
                return;

            graticule.setColor(color);
        }
    }

    public class ChangeThicknessAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent arg0)
        {
            SpinnerNumberModel sModel = new SpinnerNumberModel(graticule.getLineWidth(), 1.0, 100.0, 1.0);
            JSpinner spinner = new JSpinner(sModel);

            int option = JOptionPane.showOptionDialog(
                    invoker,
                    spinner,
                    "Enter valid number",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, null, null);

            if (option == JOptionPane.OK_OPTION)
            {
                graticule.setLineWidth((Double)spinner.getValue());
            }
        }
    }


    @Override
    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        show(e.getComponent(), e.getX(), e.getY());
    }

}
