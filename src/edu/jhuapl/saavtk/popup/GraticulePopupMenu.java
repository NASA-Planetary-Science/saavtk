package edu.jhuapl.saavtk.popup;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.Properties;

public class GraticulePopupMenu extends PopupMenu
{
    private Graticule graticule;
    private Component invoker;
    private JMenuItem colorMenuItem;
    private JMenuItem thicknessMenuItem;
    private JMenuItem setLatLongSpacing;
    private ColorChooser colorChooser;
    private double[] bounds = new double[2];
    private double[] factors = {1, 2, 3, 4, 5, 6, 9, 10, 12, 15, 18, 20, 30, 36, 45, 60, 90, 180};
    private boolean tooSmall = false;

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

        setLatLongSpacing = new JMenuItem(new ChangeLatLongSpacingAction());
        setLatLongSpacing.setText("Change Grid Spacing...");
        this.add(setLatLongSpacing);

        this.colorChooser = ColorChooser.of(graticule != null ? graticule.getColor() : Color.BLACK);
    }

    public class ChangeColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent arg0)
        {
        	colorChooser.setColor(graticule.getColor());

        	Color color = colorChooser.showColorDialog(invoker);

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

    public class ChangeLatLongSpacingAction extends AbstractAction
    {
		@Override
		public void actionPerformed(ActionEvent e) {
            SpinnerNumberModel longitudeModel = new SpinnerNumberModel(graticule.getLongitudeSpacing(), 0., 180.0, 1.0);
            JSpinner longitudeSpinner = new JSpinner(longitudeModel);

            SpinnerNumberModel latitudeModel = new SpinnerNumberModel(graticule.getLatitudeSpacing(), 0., 90.0, 1.0);
            JSpinner latitudeSpinner = new JSpinner(latitudeModel);

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new GridLayout(2, 1));
            topPanel.add(new JLabel("<html>Note: a fine grid spacing may<br>take a long time to render."));
            JPanel panel = new JPanel();
            topPanel.add(panel);
            panel.setLayout(new GridLayout(2, 2));
            panel.add(new JLabel("Longitude Spacing"));
            panel.add(longitudeSpinner);
            panel.add(new JLabel("Latitude Spacing"));
            panel.add(latitudeSpinner);

            int option = JOptionPane.showOptionDialog(
                    invoker,
                    topPanel,
                    "Enter valid longitude/latitude spacing",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, null, null);

            if (option == JOptionPane.OK_OPTION)
            {
            	if(180 % (Double)longitudeSpinner.getValue() != 0) 
            	{
                	findBounds((Double)longitudeSpinner.getValue());
//                	System.out.println(" Bound: " + (((bounds[1] - bounds[0]) / 2.0) + bounds[0]));
                	if( (((bounds[1] - bounds[0]) / 2.0) + bounds[0]) >= (Double)longitudeSpinner.getValue())
                	{
                		longitudeSpinner.setValue(bounds[0]);
                	} 
                	else 
                	{
                		longitudeSpinner.setValue(bounds[1]);
                	}
            		if (!tooSmall)
            			JOptionPane.showMessageDialog(invoker, "Value not factor of 180. Nearest factor (" + longitudeSpinner.getValue() + ") will be chosen.", "Longitude Value Warning", JOptionPane.WARNING_MESSAGE);
            		else
            			JOptionPane.showMessageDialog(invoker, "Value must be greater than 0.01. Spacing set to 0.01", "Longitude Value Warning", JOptionPane.WARNING_MESSAGE);
            	}
            	
                graticule.setLongitudeSpacing((Double)longitudeSpinner.getValue());
                graticule.setLatitudeSpacing((Double)latitudeSpinner.getValue());
                graticule.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_RESOLUTION_CHANGED, null, null));
            }
		}
    }

    @Override
    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        show(e.getComponent(), e.getX(), e.getY());
    }
    
    private void findBounds(double input)
    {
    	if (input>=1) 
    	{
			tooSmall = false;
			int size = factors.length;
			for (int i = 0; i < size; i++) {
				if (factors[i] > input) {
					bounds[1] = factors[i];
					bounds[0] = factors[i - 1];
//					System.out.println(bounds[0] + "   " + bounds[1]);
					break;
				}
			} 
		}
    	else if (input>=.1)
		{
    		tooSmall = false;
			input=Math.round(input * 100.0) / 100.0;
//			System.out.println(input);
			bounds[0] = input;
			bounds[1] = input;
			
			while((180.0 / bounds[0]) % 2 != 0)
			{
				bounds[0]-=.01;
				bounds[0]=Math.round(bounds[0] * 100.0) / 100.0;
//				System.out.println("Lower: " + bounds[0]);
			}
			
			while((180.0 / bounds[1]) % 2 != 0)
			{
				bounds[1]+=.01;
				bounds[1]=Math.round(bounds[1] * 100.0) / 100.0;
//				System.out.println("Upper: " + bounds[1]);
			}
		}
    	else
    	{
			bounds[0] = .1;
			bounds[1] = .1;
			tooSmall = true;
    	}

    }
}
