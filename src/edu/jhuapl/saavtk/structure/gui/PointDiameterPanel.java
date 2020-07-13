package edu.jhuapl.saavtk.structure.gui;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import net.miginfocom.swing.MigLayout;

/**
 * Custom GUI component which provides a UI used to control the point size of
 * the associated PointModel.
 */
public class PointDiameterPanel extends JPanel implements ChangeListener
{
	// State vars
	private final PointModel pointModel;

	// GUI vars
	private final JSpinner spinner;

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
		double currVal = diameter;
		double minVal = 0.00001;
		double maxVal = 100.0 * diameter;
		double step = computeStepSize(diameter);

		// TODO: Fix this hack to resolve ticket: #2034
		// TODO: This defect appears as early as 2019Mar19
		// TODO: This logic should be checked to ensure no new defects are introduced
		if (minVal > maxVal)
		{
			double oldMinVal = minVal;
			minVal = maxVal;
			maxVal = oldMinVal;
		}
		if (currVal < minVal)
			minVal = currVal;
		if (currVal > maxVal)
			maxVal = currVal;

		SpinnerModel model = new SpinnerNumberModel(currVal, minVal, maxVal, step);

		spinner = new JSpinner(model);
		spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00000"));
		spinner.addChangeListener(this);
		radiusLabel.setLabelFor(spinner);
		add(spinner);

		JLabel kmLabel = new JLabel("km");
		add(kmLabel, "wrap 0");
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		double tmpRadius = ((Number) spinner.getValue()).doubleValue() / 2.0;
		pointModel.setDefaultRadius(tmpRadius);

		List<Ellipse> tmpItemL = pointModel.getAllItems();
		for (Ellipse aItem : tmpItemL)
			aItem.setRadius(tmpRadius);

		pointModel.notifyItemsMutated(tmpItemL);
	}

	/**
	 * Helper method to determine the step size to use for the spinner.
	 */
	private double computeStepSize(double aDiameter)
	{
		double step = 1.;
		while (aDiameter < 1.)
		{
			aDiameter *= 10.;
			step *= .1;
		}
		return step;
	}

}
