package edu.jhuapl.saavtk.gui;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * User interface input used to capture an individual numerical input (type:
 * double).
 * <P>
 * Unlike JTextField, users of this class should not use getText() / setText()
 * but rather getValue() / setValue() methods. Also it should not be necessary
 * to register DocumentListeners - rather an ActionListener should be
 * sufficient.
 * <P>
 * This class is modeled after the Glum library's GNumberField but without the
 * support of Unit and custom Documents.
 */
public class GNumberField extends JTextField implements DocumentListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// State vars
	protected double currValue, minValue, maxValue;
	protected boolean isMutating;

	// Gui vars
	protected Color failColor, passColor;
	protected Document myDocument;
	protected NumberFormat myFormat;

	/**
	 * Constructor
	 * 
	 * @param aListener An ActionListener that will be notified when ever the user
	 *                  makes any input changes.
	 * @param aMinValue The minimum allowed value (inclusive).
	 * @param aMaxValue The maximum allowed value (inclusive).
	 */
	public GNumberField(ActionListener aListener, double aMinValue, double aMaxValue)
	{
		super("", 0);

		minValue = aMinValue;
		maxValue = aMaxValue;
		currValue = minValue;
		isMutating = false;

		failColor = Color.RED.darker();
		passColor = getForeground();

		myDocument = new PlainDocument();
		super.setDocument(myDocument);
		myFormat = new DecimalFormat("#.###");

		// Register the ActionListener
		if (aListener != null)
			addActionListener(aListener);

		// Register for events of interest
		myDocument.addDocumentListener(this);
	}

	/**
	 * Constructor
	 * 
	 * @param aListener An ActionListener that will be notified when ever the user
	 *                  makes any input changes.
	 */
	public GNumberField(ActionListener aListener)
	{
		this(aListener, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Sets the currently stored model value to Double.NaN. Also clears out the
	 * input area. This method will not trigger an ActionEvent.
	 */
	public void clearValue()
	{
		currValue = Double.NaN;

		myDocument.removeDocumentListener(this);
		setText("");
		setCaretPosition(0);
		myDocument.addDocumentListener(this);
	}

	/**
	 * Returns the currently stored model value
	 */
	public double getValue()
	{
		return currValue;
	}

	/**
	 * Returns the currently stored model value as an integer. If the modelValue is
	 * NaN, then errorVal will be returned. The values MaxInt, MinInt are returned
	 * for Infinity.
	 */
	public int getValueAsInt(int errorVal)
	{
		if (Double.isNaN(currValue) == true)
			return errorVal;

		return (int) currValue;
	}

	/**
	 * Returns whether the current input is valid
	 */
	public boolean isValidInput()
	{
		double modelVal;

		// Ensure we have valid input
		modelVal = transformToModel(this.getText());
		if (Double.isNaN(modelVal) == true)
			return false;

		// Ensure the value is within range
		if (modelVal < minValue || modelVal > maxValue)
			return false;

		return true;
	}

	/**
	 * Sets in the NumberFormat used to format numerical input.
	 */
	public void setFormat(NumberFormat aFormat)
	{
		myFormat = aFormat;
	}

	/**
	 * Sets in the Color used to change foreground text when ever invalid input is
	 * entered.
	 */
	public void setFailColor(Color aColor)
	{
		failColor = aColor;
	}

	/**
	 * Takes in a model value and will display it with respect to the active unit.
	 * This method will not trigger an ActionEvent.
	 * <P>
	 * Note this method will do nothing if the UI is being "mutated" when this
	 * method is called.
	 */
	public void setValue(final double aValue)
	{
		// Bail if we are being mutated. The alternative is to throw an exception like:
		// throw new IllegalStateException("Attempt to mutate in notification");
		if (isMutating == true)
			return;

		// Bail if the value has not changed. We do this so that user
		// entered input will not change if the model value has not changed.
		double ulp = Math.ulp(aValue);
		boolean ignoreInput = true;
		ignoreInput &= Double.isNaN(ulp) == false;
		ignoreInput &= Double.isFinite(ulp) == true;
		ignoreInput &= Math.abs(currValue - aValue) < ulp;
		if (ignoreInput == true)
			return;

		// Simple edit if we are not currently being mutated
		forceTF(aValue);
		updateGui();
	}

	@Override
	public void changedUpdate(DocumentEvent aEvent)
	{
		syncValue(aEvent);
	}

	@Override
	public void insertUpdate(DocumentEvent aEvent)
	{
		syncValue(aEvent);
	}

	@Override
	public void removeUpdate(DocumentEvent aEvent)
	{
		syncValue(aEvent);
	}

	/**
	 * Updates the internal model value and will update the display wrt to the
	 * active unit.
	 */
	protected void forceTF(double aValue)
	{
		// Save off the new model value, and check the validity
		currValue = aValue;
		if (currValue < minValue || currValue > maxValue)
			currValue = Double.NaN;
//			throw new RuntimeException("Programmatic input is invalid. Is unit compatible? Input: " + aValue);

		// Invalid values shall just clear the text field and bail
		if (Double.isNaN(currValue) == true)
		{
			clearValue();
			return;
		}

		// Convert from model value to text
		String tmpStr = transformToString(currValue);

		// Update the GUI internals
		myDocument.removeDocumentListener(this);
		setText(tmpStr);
		setCaretPosition(0);
		myDocument.addDocumentListener(this);
	}

	/**
	 * Keeps the "model" value conceptually linked to the GUI component. It will
	 * also trigger the actionEventListeners.
	 */
	protected void syncValue(DocumentEvent aEvent)
	{
		// Mark ourself as mutating
		isMutating = true;

		// Convert the string to the model value
		currValue = transformToModel(this.getText());

		// If the value is not in range then, it is invalid
		if (currValue < minValue || currValue > maxValue)
			currValue = Double.NaN;

		// Notify our listeners and update the GUI
		updateGui();
		fireActionPerformed();

		// We are no longer mutating
		isMutating = false;
	}

	/**
	 * Helper method to update the GUI to reflect the current state of the
	 * NumberField
	 */
	protected void updateGui()
	{
		Color tmpColor = passColor;
		if (isValidInput() == false)
			tmpColor = failColor;

		setForeground(tmpColor);
	}

	/**
	 * Helper method that will take a given value and convert it to a string.
	 * 
	 * @param aValue
	 */
	private String transformToString(double aValue)
	{
		return myFormat.format(aValue);
	}

	/**
	 * Helper method that will take a String and convert it to the equivalent
	 * numerical value. On failure Double.NaN will be returned.
	 * 
	 * @param aValue
	 */
	private double transformToModel(String aStr)
	{
		try
		{
			return Double.parseDouble(aStr);
		}
		catch (NumberFormatException aExp)
		{
			return Double.NaN;
		}
	}

}
