package edu.jhuapl.saavtk.view.light;

import edu.jhuapl.saavtk.util.LatLon;

/**
 * Immutable object that defines a lighting configuration.
 * <p>
 * The following attributes are supported:
 * <ul>
 * <li>{@link LightingType}
 * <li>Position of light in {@link LatLon} (radians)
 * <li>Intensity (percent)
 * </ul>
 *
 * @author lopeznr1
 */
public class LightCfg
{
	// Constants
	/** Default LatLon location for a fixed position light source. */
	private final static LatLon DefaultPositionLL = new LatLon(90, 0, 1.0e+8).toRadians();

	/** The "default" {@link LightCfg}. */
	public final static LightCfg Default = new LightCfg(LightingType.LIGHT_KIT, DefaultPositionLL, 1.00);
	/** The "invalid" {@link LightCfg}. */
	public final static LightCfg Invalid = new LightCfg(LightingType.NONE, LatLon.NaN, Double.NaN);

	// Attributes
	private final LightingType type;
	private final LatLon positionLL;
	private final double intensity;

	/**
	 * Standard Constructor
	 *
	 * @param aType       The light type ({@link LightingType}) to use.
	 * @param aPositionLL The position of the light in radians.
	 * @param aIntensity  The intensity of the light as a percent: [0.0, 1.0]
	 */
	public LightCfg(LightingType aType, LatLon aPositionLL, double aIntensity)
	{
		type = aType;
		positionLL = aPositionLL;
		intensity = aIntensity;
	}

	/**
	 * Returns the light type (model).
	 */
	public LightingType getType()
	{
		return type;
	}

	/**
	 * Returns the position of the (fixed) light.
	 * <p>
	 * The returned {@link LatLon} will be in units of radians.
	 */
	public LatLon getPositionLL()
	{
		return positionLL;
	}

	/**
	 * Returns the intensity as a percent: [0.0 - 1.0]
	 */
	public double getIntensity()
	{
		return intensity;
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
		LightCfg other = (LightCfg) obj;
		if (Double.doubleToLongBits(intensity) != Double.doubleToLongBits(other.intensity))
			return false;
		if (positionLL == null)
		{
			if (other.positionLL != null)
				return false;
		}
		else if (!positionLL.equals(other.positionLL))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(intensity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((positionLL == null) ? 0 : positionLL.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

}