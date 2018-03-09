package edu.jhuapl.saavtk.gui.panel;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.popup.StructuresPopupMenu;

public class PointsMappingControlPanel extends
        AbstractStructureMappingControlPanel implements ChangeListener
{
    private JSpinner spinner;
    private PointModel pointModel;

    public PointsMappingControlPanel(
            ModelManager modelManager,
            PickManager pickManager,
            Component parent)
    {
        super(modelManager,
                (StructureModel)modelManager.getModel(ModelNames.POINT_STRUCTURES),
                pickManager,
                PickManager.PickMode.POINT_DRAW,
                (StructuresPopupMenu)pickManager.getPopupManager().getPopup((StructureModel)modelManager.getModel(ModelNames.POINT_STRUCTURES)),
                false);

        pointModel = (PointModel)modelManager.getModel(ModelNames.POINT_STRUCTURES);

        double diameter = 2.0 * pointModel.getDefaultRadius();

        JPanel panel = new JPanel();

        JLabel radiusLabel = new JLabel("Diameter");
        panel.add(radiusLabel);

        double max = 100.0 * diameter;
        double step = computeStepSize(diameter);

        SpinnerModel model = new SpinnerNumberModel(diameter, //initial value
                0.00001,
                max,
                step);

        spinner = new JSpinner(model) {
            @Override
            protected JComponent createEditor( SpinnerModel model )
            {
              return new NumberEditor(this, "0.00000");
            }
          };
        spinner.addChangeListener(this);
        radiusLabel.setLabelFor(spinner);
        panel.add(spinner);

        JLabel kmLabel = new JLabel("km");
        panel.add(kmLabel);

        add(panel, "span");
    }

	public void stateChanged(ChangeEvent e)
    {
        Number val = (Number)spinner.getValue();
        pointModel.setDefaultRadius(val.doubleValue()/2.0);
        pointModel.changeRadiusOfAllPolygons(val.doubleValue()/2.0);
    }

	private double computeStepSize(double diameter) {
		double step = 1.;
		while (diameter < 1.)
		{
			diameter *= 10.;
			step *= .1;
		}
		return step;
	}
	
}
