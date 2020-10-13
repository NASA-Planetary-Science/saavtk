package edu.jhuapl.saavtk.gui.util;

import java.text.NumberFormat;

import glum.text.SigFigNumberFormat;

/**
 * Collection of utility methods that provide a singular location for the
 * definition, management, and retrieval of tool tips.
 *
 * @author lopeznr1
 */
public class ToolTipUtil
{
	/**
	 * Returns the tool tip that describes incomplete logic / future functionality.
	 */
	public static String getFutureFunctionality()
	{
		return "Not implemented. Future functionality.";
	}

	/**
	 * Returns the tool tip that should be used for "Add Items" buttons
	 */
	public static String getItemAdd()
	{
		return "Add Items";
	}

	/**
	 * Returns the tool tip that should be used for "Delete Items" buttons
	 */
	public static String getItemDel()
	{
		return "Delete Items";
	}

	/**
	 * Returns the tool tip that should be used for "Edit Items" buttons
	 */
	public static String getItemEdit()
	{
		return "Edit Items";
	}

	/**
	 * Returns the tool tip that should be used for "Edit Items" buttons
	 */
	public static String getItemReset()
	{
		return "Reset";
	}

	/**
	 * Returns the tool tip that should be used for "Reset Min Value" buttons.
	 *
	 * @param aTmpNF The {@link NumberFormat} used to format the value.
	 * @param aValue The value to display.
	 */
	public static String getItemResetMinVal(NumberFormat aTmpNF, double aValue)
	{
		if (Double.isNaN(aValue) == true)
			return "Reset Min: ---";

		if (aTmpNF == null)
			aTmpNF = new SigFigNumberFormat(14);

		return "Reset Min: " + aTmpNF.format(aValue);
	}

	/**
	 * Returns the tool tip that should be used for "Reset Max Value" buttons.
	 *
	 * @param aTmpNF The {@link NumberFormat} used to format the value.
	 * @param aValue The value to display.
	 */
	public static String getItemResetMaxVal(NumberFormat aTmpNF, double aValue)
	{
		if (Double.isNaN(aValue) == true)
			return "Reset Max: ---";

		if (aTmpNF == null)
			aTmpNF = new SigFigNumberFormat(14);

		return "Reset Max: " + aTmpNF.format(aValue);
	}

	/**
	 * Returns the tool tip that should be used for "Select All" buttons
	 */
	public static String getSelectAll()
	{
		return "Select All";
	}

	/**
	 * Returns the tool tip that should be used for "Select Invert" buttons
	 */
	public static String getSelectInvert()
	{
		return "Invert Selection";
	}

	/**
	 * Returns the tool tip that should be used for "Select None" buttons
	 */
	public static String getSelectNone()
	{
		return "Clear Selection";
	}

}
