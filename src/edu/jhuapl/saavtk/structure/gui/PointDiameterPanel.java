package edu.jhuapl.saavtk.structure.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.primitives.Doubles;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Custom GUI component which provides a UI used to control the point size of
 * the associated PointModel.
 */
public class PointDiameterPanel extends JPanel implements ChangeListener
{
	// State vars
	private final PointModel refPointModel;

	// GUI vars
	private final JSpinner spinner;

	/**
	 * Standard Constructor
	 *
	 * @param aPointModel The PointModel that will be controlled by this UI
	 *                    component.
	 */
	public PointDiameterPanel(PolyhedralModel aSmallBody, PointModel aPointModel)
	{
		refPointModel = aPointModel;

		// Compute point size constraints
		var defVal = EllipseUtil.getDefRadius(aSmallBody) * 2.0;
		var minVal = EllipseUtil.getMinRadius(aSmallBody) * 2.0;
		var maxVal = EllipseUtil.getMaxRadius(aSmallBody) * 2.0;
		var stepSize = computeStepSize(defVal);

		// Retrieve the current value and clamp to the size constraints
		var currVal = 2.0 * refPointModel.getDefaultRadius();
		currVal = Doubles.constrainToRange(currVal, minVal, maxVal);

		setLayout(new MigLayout("", "0[][]", "0[]0"));

		var model = new SpinnerNumberModel(currVal, minVal, maxVal, stepSize);
		spinner = new JSpinner(model);
		spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00000"));
		spinner.addChangeListener(this);
		var radiusLabel = new JLabel("Diameter:");
		radiusLabel.setLabelFor(spinner);
		add(radiusLabel);
		add(spinner);

		var kmLabel = new JLabel("km");
		add(kmLabel, "wrap 0");
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		var tmpRadius = ((Number) spinner.getValue()).doubleValue() / 2.0;
		refPointModel.setDefaultRadius(tmpRadius);

		var tmpItemL = refPointModel.getAllItems();
		for (var aItem : tmpItemL)
			aItem.setRadius(tmpRadius);

		refPointModel.notifyItemsMutated(tmpItemL);
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
