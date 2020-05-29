package edu.jhuapl.saavtk.gui.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.pick.Picker;

/**
 * {@link AbstractAction} that provides the logic for the configure pick
 * tolerance action.
 */
public class PickToleranceAction extends AbstractAction implements ChangeListener
{
	// Ref vars
	private final ViewManager refViewManager;

	// Gui vars
	private final JSlider slider;

	/**
	 * Standard Constructor
	 */
	public PickToleranceAction(ViewManager aViewManager)
	{
		super("Set pick accuracy...");

		refViewManager = aViewManager;

		slider = new JSlider(0, 100);
		slider.setValue(convertPickToleranceToSliderValue(Picker.DEFAULT_PICK_TOLERANCE));
		slider.addChangeListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		JLabel label = new JLabel("Pick accuracy");
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.WEST);
		panel.add(slider, BorderLayout.CENTER);

		JFrame frame = new JFrame();
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
	}

	public int convertPickToleranceToSliderValue(double tol)
	{
		return (int) (1
				- (tol - Picker.MINIMUM_PICK_TOLERANCE) / (Picker.MAXIMUM_PICK_TOLERANCE - Picker.MINIMUM_PICK_TOLERANCE)
						* (slider.getMaximum() - slider.getMinimum()))
				+ slider.getMinimum();
	}

	public double convertSliderValueToPickTolerance(int val)
	{
		return Picker.MAXIMUM_PICK_TOLERANCE
				- (double) (val - slider.getMinimum()) / (double) (slider.getMaximum() - slider.getMinimum())
						* (Picker.MAXIMUM_PICK_TOLERANCE - Picker.MINIMUM_PICK_TOLERANCE);
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		refViewManager.getCurrentView().getPickManager()
				.setPickTolerance(convertSliderValueToPickTolerance(slider.getValue()));
	}

}