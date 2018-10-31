package edu.jhuapl.saavtk.gui.panel.structures;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.model.structure.PointModel;
import net.miginfocom.swing.MigLayout;

/**
 * Custom GUI component which provides a UI used to control the point size of
 * the associated PointModel.
 */
public class PointDiameterPanel extends JPanel implements ChangeListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// State vars
	private final PointModel pointModel;

	// GUI vars
	private JSpinner spinner;

	/**
	 * Standard Constructor
	 * 
	 * @param aPointModel The PointModel that will be controlled by this UI
	 *                    component.
	 */
	public PointDiameterPanel(PointModel aPointModel)
	{
		pointModel = aPointModel;

		setLayout(new MigLayout("", "0[][]", "0[]0"));

		JLabel radiusLabel = new JLabel("Diameter:");
		add(radiusLabel);

		double diameter = 2.0 * pointModel.getDefaultRadius();
		double max = 100.0 * diameter;
		double step = computeStepSize(diameter);
		SpinnerModel model = new SpinnerNumberModel(diameter, 0.00001, max, step);

		spinner = new JSpinner(model);
		spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00000"));
		spinner.addChangeListener(this);
		radiusLabel.setLabelFor(spinner);
		add(spinner);

		JLabel kmLabel = new JLabel("km");
		add(kmLabel, "wrap 0");
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		Number val = (Number) spinner.getValue();
		pointModel.setDefaultRadius(val.doubleValue() / 2.0);
		pointModel.changeRadiusOfAllPolygons(val.doubleValue() / 2.0);
	}

	/**
	 * Helper method to determine the step size to use for the spinner.
	 */
	private double computeStepSize(double diameter)
	{
		double step = 1.;
		while (diameter < 1.)
		{
			diameter *= 10.;
			step *= .1;
		}
		return step;
	}

}
