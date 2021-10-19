package edu.jhuapl.saavtk.coloring;

/**
 * Enum that defines the different types of methods for coloring a small body.
 *
 * @author lopeznr1
 */
public enum ColoringMode
{
	/** Mode where the object is colored by a single color. */
	Plain,

	/**
	 * Mode where the color of an object will be a function of a (plate color)
	 * feature and a corresponding a color table configuration.
	 */
	PlateColorStandard,

	/**
	 * Mode where each individual color channel (red, green, blue) will have a value
	 * specific to some (plate color) feature.
	 */
	PlateColorRGB,

}
