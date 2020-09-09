package edu.jhuapl.saavtk.color.table;

/**
 * Immutable class that defines the attributes associated with a
 * {@link ColorTable}.
 * <P>
 * A color map is defined by the following:
 * <UL>
 * <LI>Reference {@link ColorTable}
 * <LI>Minimum value that is mapped to the color table
 * <LI>Maximum value that is mapped to the color table
 * <LI>Number of distinct color levels
 * <LI>Flag that defines if a log scale will be used for color mapping
 * </UL>
 *
 * @author lopeznr1
 */
public class ColorMapAttr
{
	// Constants
	/** The "invalid" color map attribute */
	public static final ColorMapAttr Invalid = new ColorMapAttr(ColorTable.Invalid, Double.NaN, Double.NaN, 2, false);

	// Attributes
	private final ColorTable refColorTable;
	private final double minVal;
	private final double maxVal;
	private final int numLevels;
	private final boolean isLogScale;

	/** Standard Constructor */
	public ColorMapAttr(ColorTable aColorTable, double aMinVal, double aMaxVal, int aNumLevels, boolean aIsLogScale)
	{
		refColorTable = aColorTable;
		minVal = aMinVal;
		maxVal = aMaxVal;
		numLevels = aNumLevels;
		isLogScale = aIsLogScale;
	}

	/**
	 * The reference {@link ColorTable}.
	 */
	public ColorTable getColorTable()
	{
		return refColorTable;
	}

	/**
	 * Minimum value that is to be mapped.
	 */
	public double getMinVal()
	{
		return minVal;
	}

	/**
	 * Maximum value that is to be mapped.
	 */
	public double getMaxVal()
	{
		return maxVal;
	}

	/**
	 * The number of distinct color levels.
	 */
	public int getNumLevels()
	{
		return numLevels;
	}

	/**
	 * Returns true if the values will be mapped on a log scale.
	 */
	public boolean getIsLogScale()
	{
		return isLogScale;
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
		ColorMapAttr other = (ColorMapAttr) obj;
		if (refColorTable == null)
		{
			if (other.refColorTable != null)
				return false;
		}
		else if (!refColorTable.equals(other.refColorTable))
			return false;
		if (isLogScale != other.isLogScale)
			return false;
		if (Double.doubleToLongBits(maxVal) != Double.doubleToLongBits(other.maxVal))
			return false;
		if (Double.doubleToLongBits(minVal) != Double.doubleToLongBits(other.minVal))
			return false;
		if (numLevels != other.numLevels)
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((refColorTable == null) ? 0 : refColorTable.hashCode());
		result = prime * result + (isLogScale ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(maxVal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minVal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + numLevels;
		return result;
	}

}
