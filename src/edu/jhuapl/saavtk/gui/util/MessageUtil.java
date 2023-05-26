package edu.jhuapl.saavtk.gui.util;

import java.util.Collection;

/**
 * Collection of utility methods used to adjust messages. The following
 * functionality is supported:
 * <ul>
 * <li>Transition to plural form
 * </ul>
 *
 * @author lopeznr1
 */
public class MessageUtil
{
	/**
	 * Given a specific message will return the plural form (or the singular form).
	 * <P>
	 * This method works by adding a trailing char 's' (or 'ies' if appropriate) to
	 * transition a message from singular to plural form.
	 *
	 * @param aMsg      The message of interest.
	 * @param aIsPlural If set to true then will be transformed to the plural form
	 *                  otherwise no changes will be made.
	 * @return
	 */
	public static String toPluralForm(String aMsg, boolean aIsPlural)
	{
		// Bail if the message should be kept in the singular form
		if (aIsPlural == false)
			return aMsg;

		// Special case for words ending in y (Example: boy / city ---> boys / cities)
		if (aMsg.length() > 2 && aMsg.endsWith("y") == true)
		{
			int lastIdx = aMsg.length() - 1;
			char secondToLastChar = aMsg.toUpperCase().charAt(lastIdx - 1);
			if ("AEIOU".contains("" + secondToLastChar) == false)
				return aMsg.substring(0, lastIdx) + "ies";
		}

		// Standard case
		return aMsg + 's';
	}

	/**
	 * Given a specific message will return the plural form (or the singular form).
	 * <P>
	 * The determination of whether the message should be shown in the plural form
	 * is decided by how many items are in the collection. If there are multiple
	 * items in the collection aItemC then the message will be shown in the plural
	 * form.
	 * <P>
	 * Also see: {@link #toPluralForm(String, boolean)}
	 *
	 * @param aMsg   The message of interest.
	 * @param aItemC The collection of interest.
	 * @return
	 */
	public static String toPluralForm(String aMsg, Collection<?> aItemC)
	{
		// Determine if the message should be in the plural form
		boolean isPlural = aItemC.size() > 1;

		// Delegate
		return toPluralForm(aMsg, isPlural);
	}

}
