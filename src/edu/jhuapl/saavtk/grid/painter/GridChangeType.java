package edu.jhuapl.saavtk.grid.painter;

import edu.jhuapl.saavtk.color.painter.ColorBarPainter;

/**
 * Enum that describes the type of configuration change on the
 * {@link ColorBarPainter}.
 *
 * @author lopeznr1
 */
public enum GridChangeType
{
	/** Configuration associated with bothe the grid and lable has changed. */
	All,

	/** Configuration associated with the grid has changed. */
	Grid,

	/** Configuration associated with the labels has changed. */
	Label,

}
