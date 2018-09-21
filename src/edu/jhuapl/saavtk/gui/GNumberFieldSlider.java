package edu.jhuapl.saavtk.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * User interface component that combines a {@link GNumberField} and a
 * {@link GSlider} into a single unified componet.
 */
public class GNumberFieldSlider extends JPanel implements ActionListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Gui vars
	private JLabel valueL;
	private GNumberField valueNF;
	private GSlider valueS;

	// State vars
	private ActionListener refLister;
	private double minVal;
	private double maxVal;

	public GNumberFieldSlider(ActionListener aListener, String aLabel, double aMinVal, double aMaxVal)
	{
		refLister = aListener;
		minVal = aMinVal;
		maxVal = aMaxVal;

		buildGui(aLabel);
	}
	
	/**
	 * Returns the label component used in the composite GUI control.
	 */
	public JLabel getLabelComponent()
	{
		return valueL;
	}

	/**
	 * Returns whether the current input is valid
	 */
	public boolean isValidInput()
	{
		// Delegate
		return valueNF.isValidInput();
	}

	/**
	 * Returns the selected value.
	 */
	public double getValue()
	{
		return valueNF.getValue();
	}

	/**
	 * Sets in the selected value. Note no events will be fired.
	 */
	public void setValue(double aVal)
	{
		valueNF.setValue(aVal);
		valueS.setModelValue(aVal);
	}

	/**
	 * Set the enable state of the UI component.
	 */
	public void setEnabled(boolean aBool)
	{
		GuiUtil.setEnabled(this, aBool);
	}

	/**
	 * Sets in the number of columns for the associated GTextField.
	 */
	public void setNumColumns(int aNumColumns)
	{
		valueNF.setColumns(aNumColumns);
	}

	/**
	 * Sets in the number of steps associated with the slider.
	 */
	public void setNumSteps(int aNumSteps)
	{
		valueS.setNumSteps(aNumSteps);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == valueNF)
		{
			if (valueNF.isValidInput() == false)
				return;

			int tmpVal = valueNF.getValueAsInt(-1);
			valueS.setModelValue(tmpVal);
		}
		else if (source == valueS)
		{
			int tmpVal = (int) valueS.getModelValue();
			valueNF.setValue(tmpVal);
		}

		ActionEvent tmpEvent = new ActionEvent(this, 0, "");
		refLister.actionPerformed(tmpEvent);
	}

	/**
	 * Helper method that builds the unified GUI
	 */
	private void buildGui(String aLabel)
	{
		valueL = new JLabel(aLabel, JLabel.RIGHT);
		valueNF = new GNumberField(this, minVal, maxVal);
		valueS = new GSlider(this, minVal, maxVal);

		setLayout(new MigLayout("", "0[]0", "0[]0"));
		add(valueL, "");
		add(valueNF, "");
		add(valueS, "growx");
	}

}
