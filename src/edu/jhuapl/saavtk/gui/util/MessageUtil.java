package edu.jhuapl.saavtk.gui.util;

import java.util.Collection;

/**
 * Collection of utility methods that provide a singular location for the
 * definition, management, and retrieval of common messages.
 * 
 * @author lopeznr1
 */
public class MessageUtil
{
	/**
	 * Given a specific message will return the plural form or the singular form.
	 * <P>
	 * This method works by adding a trailing char 's' or removing the trailing char
	 * 's' to transition a message from singular to plural form.
	 * 
	 * @param aMsg      The message of interest.
	 * @param aIsPlural If set to true then will be transformed to the plural form
	 *                  otherwise to the singular form.
	 * @return
	 */
	public static String toPluralForm(String aMsg, boolean aIsPlural)
	{
		// Bail if the message is already in the proper form
		boolean isPlural = aMsg.endsWith("s") == true;
		if (isPlural == aIsPlural)
			return aMsg;

		if (aIsPlural == true)
			return aMsg + 's';
		else
			return aMsg.substring(0, aMsg.length() - 1);
	}

	/**
	 * Given a specific message will return the plural form or the singular form.
	 * <P>
	 * The determination of whether the message should be shown in the plural form
	 * is decided by how many items are in the collection. If there are multiple
	 * items in the collection aItemL then the message will be shown in the plural
	 * form.
	 * <P>
	 * Also see: {@link #toPluralForm(String, boolean)}
	 * 
	 * @param aMsg   The message of interest.
	 * @param aItemL The collection of interest.
	 * @return
	 */
	public static String toPluralForm(String aMsg, Collection<?> aItemL)
	{
		// Determine if the message should be in the plural form
		boolean isPlural = aItemL.size() > 1;

		// Delegate
		return toPluralForm(aMsg, isPlural);
	}

}
