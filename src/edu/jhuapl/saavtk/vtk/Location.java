package edu.jhuapl.saavtk.vtk;

/**
 * Immutable object that contains rectangular position and dimension.
 *
 * @author lopeznr1
 */
public class Location
{
	// Attributes
	private final double posX;
	private final double posY;
	private final double dimX;
	private final double dimY;

	/** Standard Constructor */
	public Location(double aPosX, double aPosY, double aDimX, double aDimY)
	{
		posX = aPosX;
		posY = aPosY;
		dimX = aDimX;
		dimY = aDimY;
	}

	public double getDimX()
	{
		return dimX;
	}

	public double getDimY()
	{
		return dimY;
	}

	public double getPosX()
	{
		return posX;
	}

	public double getPosY()
	{
		return posY;
	}

	/**
	 * Returns true if the specified point is within bounds.
	 */
	public boolean isInside(double aPX, double aPY)
	{
		if (aPX < posX || aPX > aPX + dimX)
			return false;

		if (aPY < posY || aPY > aPY + dimY)
			return false;

		return true;
	}

	/**
	 * Returns true if the location is valid. In order for a location to be
	 * "considered" valid the following must be true:
	 * <UL>
	 * <LI>All numeric values must not be NaN.
	 * <LI>All numeric values must be finite.
	 * <LI>Dimension values must not be negative.
	 * </UL>
	 */
	public boolean isValid()
	{
		boolean isValid = true;
		isValid &= Double.isNaN(posX) == false;
		isValid &= Double.isNaN(posY) == false;
		isValid &= Double.isNaN(dimX) == false;
		isValid &= Double.isNaN(dimY) == false;
		isValid &= Double.isFinite(posX) == true;
		isValid &= Double.isFinite(posY) == true;
		isValid &= Double.isFinite(dimX) == true;
		isValid &= Double.isFinite(dimY) == true;
		isValid &= dimX >= 0;
		isValid &= dimY >= 0;

		return isValid;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(dimX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(dimY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(posX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(posY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (Double.doubleToLongBits(dimX) != Double.doubleToLongBits(other.dimX))
			return false;
		if (Double.doubleToLongBits(dimY) != Double.doubleToLongBits(other.dimY))
			return false;
		if (Double.doubleToLongBits(posX) != Double.doubleToLongBits(other.posX))
			return false;
		if (Double.doubleToLongBits(posY) != Double.doubleToLongBits(other.posY))
			return false;
		return true;
	}

}
