package edu.jhuapl.saavtk.structure.gui;

import edu.jhuapl.saavtk.structure.Structure;

/**
 * Enums that define the available table columns for items of type
 * {@link Structure}.
 *
 * @author lopeznr1
 */
enum LookUp
{
	Id,

	Source,

	Type,

	IsVisible,

	Color,

	Name,

	Label,

	// Enums specific to hard-edge items
	Length,
	Area,
	VertexCount,

	// Enums specific to round-edge items
	Angle,
	Diameter,
	Flattening,

}
