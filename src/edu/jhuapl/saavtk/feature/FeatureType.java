package edu.jhuapl.saavtk.feature;

/**
 * Immutable class that defines a single feature property.
 * <P>
 * A FeatureType typically refers to a physical quality attribute.
 *
 * @author lopeznr1
 */
public class FeatureType
{
	// Constants
	/** FeatureType that defines the "invalid" physical attribute. */
	public static final FeatureType Invalid = new FeatureType("Invalid", "---", Float.NaN);

	// Attributes
	private final String name;
	private final String unit;
	private final double scale;

	/** Standard Constructor */
	public FeatureType(String aName, String aUnit, double aScale)
	{
		name = aName;
		unit = aUnit;
		scale = aScale;
	}

	public String getName()
	{
		return name;
	}

	public String getUnit()
	{
		return unit;
	}

	public double getScale()
	{
		return scale;
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
		FeatureType other = (FeatureType) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(scale) != Double.doubleToLongBits(other.scale))
			return false;
		if (unit == null)
		{
			if (other.unit != null)
				return false;
		}
		else if (!unit.equals(other.unit))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(scale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

}
