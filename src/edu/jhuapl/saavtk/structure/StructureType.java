package edu.jhuapl.saavtk.structure;

/**
 * Enumeration that defines the structure type.
 */
public enum StructureType
{
	Point,

	Circle,

	Ellipse,

	Path,

	Polygon;

	/**
	 * Returns a string used as a user display friendly name.
	 * <p>
	 * String should be upper case.
	 */
	public String getLabel()
	{
		return toString();
	}
}
