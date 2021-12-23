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
	 * Returns the tool tip that should be used for "Reset: aMsg" buttons.
	 *
	 * @param aMsg The value that the control would be reset to.
	 */
	public static String getItemResetMsg(String aMsg)
	{
		return "Reset: " + aMsg;
	}

	/**
	 * Returns the tool tip that should be used for the primary fixed-light button.
	 */
	public static String getLightFixed()
	{
		return "A Fixed Light is a light fixed in space that does not move with the virtual camera. Its intensity and positon can be changed below.";
	}

	/**
	 * Returns the tool tip that should be used for the primary headlight button.
	 */
	public static String getLightHeadlight()
	{
		return "A Headlight is a single light always positioned at the virtual camera. It's intensity can be changed below.";
	}

	/**
	 * Returns the tool tip that should be used for the primary "light kit" button.
	 */
	public static String getLightKit()
	{
		return "A Light Kit is a set of several lights of various strengths positioned to provide suitable illumination for most situations.";
	}

	/**
	 * Returns the tool tip that should be used for "Add Profile" buttons
	 */
	public static String getProfileAdd()
	{
		return "Add Profile";
	}

	/**
	 * Returns the tool tip that should be used for "Delete Profile" buttons
	 */
	public static String getProfileDel()
	{
		return "Delete Profile";
	}

	/**
	 * Returns the tool tip that should be used for "Edit Profile" buttons
	 */
	public static String getProfileEdit()
	{
		return "Edit Profiles";
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
	
	/*
	 * Returns the tool tip that should be used for "Load Items" buttons
	 */
	public static String getItemLoad()
	{
		return "Load";
	}
	
	/*
	 * Returns the tool tip that should be used for "Save Items" buttons
	 */
	public static String getItemSave()
	{
		return "Save";
	}
	
	/*
	 * Returns the tool tip that should be used for "Show Items" buttons
	 */
	public static String getItemShow()
	{
		return "Show";
	}
	
	/*
	 * Returns the tool tip that should be used for "Hide Items" buttons
	 */
	public static String getItemHide()
	{
		return "Hide";
	}
	
	/*
	 * Returns the tool tip that should be used for "Record" buttons
	 */
	public static String getRecord()
	{
		return "Record";
	}
	
	/*
	 * Returns the tool tip that should be used for "Stop" buttons
	 */
	public static String getStop()
	{
		return "Stop";
	}
	
	/*
	 * Returns the tool tip that should be used for "Font" buttons
	 */
	public static String getFont()
	{
		return "Font";
	}
	
	/*
	 * Returns the tool tip that should be used for "Color" buttons
	 */
	public static String getColorImage()
	{
		return "Generate a Color Image";
	}
	 
	/*
	 * Returns the tool tip that should be used for "Layer" buttons
	 */
	public static String getImageCube()
	{
		return "Generate an Image Cube";
	}
	
	/*
	 * Returns the tool tip that should be used for "New" buttons
	 */
	public static String getCustomImage()
	{
		return "Generate a Custom Image";
	}

}
