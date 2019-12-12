package edu.jhuapl.saavtk.structure;

import edu.jhuapl.saavtk.model.structure.LineModel;

/**
 * Enum that describes the mode of a PolyLine.
 * <P>
 * The class was sourced from {@link LineModel}. It should eventually be removed
 * due to the defective design.
 *
 * @author lopeznr1
 */
public enum PolyLineMode
{
	/** Defines a PolyLine with no constraints on its configuration. */
	DEFAULT,

	/** Defines a PolyLine where there are only 2 points. */
	PROFILE,

	/** Defines a PolyLine where the first point and last point are the same. */
	CLOSED
}