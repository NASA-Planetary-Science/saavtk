package edu.jhuapl.saavtk.view.light;

/**
 * Enum that defines various light source types.
 */
public enum LightingType
{
	/** Enum for the no light source. Note the scene will be black. */
	NONE,

	/** Light source that is positioned at the camera. */
	HEADLIGHT,

	/** Light source that maps to the VTK version of light kit. */
	LIGHT_KIT,

	/** Light source that is positioned at a specific location in the scene. */
	FIXEDLIGHT
}