package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.awt.Container;

/**
 * Collection of AWT/Swing utilities.
 */
public class GuiUtil
{
	/**
	 * Utility method to recursively change the enable state of all Components
	 * contained by the specified Container.
	 * 
	 * @param aContainer The Container of interest.
	 * @param aBool      Boolean used to define the enable state.
	 */
	public static void setEnabled(Container aContainer, boolean aBool)
	{
		for (Component aComp : aContainer.getComponents())
		{
			aComp.setEnabled(aBool);
			if (aComp instanceof Container)
				setEnabled((Container) aComp, aBool);
		}
	}

}
