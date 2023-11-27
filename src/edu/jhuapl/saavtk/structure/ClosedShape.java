package edu.jhuapl.saavtk.structure;

/**
 * Interface that defines methods specific to an object that are mathematically a closed shape.
 *
 * @author lopeznr1
 */
public interface ClosedShape
{
	/**
	 * Returns true if the object's interior is utilized.
	 */
	public boolean getShowInterior();

	/**
	 * Returns the surface area of the object.
	 */
	public double getSurfaceArea();

	/**
	 * Sets whether the interior should be shown.
	 */
	public void setShowInterior(boolean aBool);

}
