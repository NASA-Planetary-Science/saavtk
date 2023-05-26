package edu.jhuapl.saavtk.color.painter;

/**
 * Enum that describes the type of configuration change on the
 * {@link ColorBarPainter}.
 *
 * @author lopeznr1
 */
public enum ColorBarChangeType
{
	/** Configuration associated with the background has changed. */
	Background,

	/** Configuration associated with the color map has changed. */
	ColorMap,

	/** Configuration associated with the layout has changed. */
	Layout,

	/** Configuration associated with the location has changed. */
	Location,

	/** Configuration associated with the label has changed. */
	Label,

	/** Configuration associated with the title has changed. */
	Title

}
