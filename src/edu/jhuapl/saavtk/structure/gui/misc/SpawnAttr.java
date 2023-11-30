package edu.jhuapl.saavtk.structure.gui.misc;

import java.awt.Color;

/**
 * Record that defines the attributes for a newly created structures.
 * <p>
 * The following attributes are supported:
 * <ul>
 * <li>Color
 * <li>Is interior shown
 * </ul>
 *
 * @author lopeznr1
 */
public record SpawnAttr(Color color, boolean isIntShown)
{
	// Constants
	public static final SpawnAttr Default = new SpawnAttr(Color.MAGENTA, false);

	/** Return a copy of this {@link SpawnAttr} but with the specified color. */
	public SpawnAttr withColor(Color aColor)
	{
		return new SpawnAttr(aColor, isIntShown);
	}

	/** Return a copy of this {@link SpawnAttr} but with the specified isIntShown. */
	public SpawnAttr withIsIntShown(boolean aIsIntShown)
	{
		return new SpawnAttr(color, aIsIntShown);
	}

}
