package edu.jhuapl.saavtk.gui.util;

import java.awt.Color;

/**
 * Collection of utility methods that provide a singular location for the
 * definition, management, and retrieval of (application) colors.
 *
 * @author lopeznr1
 */
public class Colors
{
	// Constants
	private static final Color failFG = Color.RED.darker();
	private static final Color passFG = Color.BLACK;

	/**
	 * Returns the foreground color that should we used for UI elements in an
	 * erroneous state.
	 */
	public static Color getFailFG()
	{
		return failFG;
	}

	/**
	 * Returns the foreground color that should we used for UI elements in a valid
	 * state.
	 */
	public static Color getPassFG()
	{
		return passFG;
	}

}
