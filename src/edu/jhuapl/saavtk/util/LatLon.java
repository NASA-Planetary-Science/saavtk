package edu.jhuapl.saavtk.util;

import com.google.common.base.Preconditions;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

/**
 * Immutable object used to store a position specified as latitude, longitude,
 * and radius.
 * <p>
 * Note it is unspecified whether lat and lon are in degrees or radians.
 */
public class LatLon
{
	// Constants
	public static final LatLon NaN = new LatLon(Double.NaN, Double.NaN, Double.NaN);
	public static final LatLon Zero = new LatLon(0.0, 0.0, 0.0);

	// Attributes
	public final double lat;
	public final double lon;
	public final double rad;

	public LatLon(double[] latLonRad)
	{
		Preconditions.checkNotNull(latLonRad);
		Preconditions.checkArgument(latLonRad.length == 3);
		this.lat = latLonRad[0];
		this.lon = latLonRad[1];
		this.rad = latLonRad[2];
	}

	/** Standard Constructor */
	public LatLon(double lat, double lon, double rad)
	{
		this.lat = lat;
		this.lon = lon;
		this.rad = rad;
	}

	/** Simplified Constructor */
	public LatLon(double lat, double lon)
	{
		this(lat, lon, 1.0);
	}

	public double[] get()
	{
		return new double[] { lat, lon, rad };
	}

	@Override
	public String toString()
	{
		return "lat: " + lat + " lon: " + lon + " rad: " + rad;
	}

	/**
	 * Assuming this instance is in radians, return a new instance converted to
	 * degrees.
	 *
	 * @return
	 */
	public LatLon toDegrees()
	{
		return new LatLon(lat * 180.0 / Math.PI, lon * 180.0 / Math.PI, rad);
	}

	/**
	 * Assuming this instance is in degrees, return a new instance converted to
	 * radians.
	 *
	 * @return
	 */
	public LatLon toRadians()
	{
		return new LatLon(lat * Math.PI / 180.0, lon * Math.PI / 180.0, rad);
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
		LatLon other = (LatLon) obj;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
			return false;
		if (Double.doubleToLongBits(rad) != Double.doubleToLongBits(other.rad))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(rad);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	private static final Version METADATA_VERSION = Version.of(1, 0);
	private static final Key<LatLon> LATLON_PROXY_KEY = Key.of("latLon");
	private static final Key<double[]> LAT_LON_RADIUS = Key.of("lat, lon, rad");
	private static boolean proxyInitialized = false;

	public static void initializeSerializationProxy()
	{
		if (!proxyInitialized)
		{
			InstanceGetter.defaultInstanceGetter().register(LATLON_PROXY_KEY, source -> {
				return new LatLon(source.get(LAT_LON_RADIUS));
			}, LatLon.class, latlon -> {
				SettableMetadata metadata = SettableMetadata.of(METADATA_VERSION);
				metadata.put(LAT_LON_RADIUS, latlon.get());
				return metadata;
			});

			proxyInitialized = true;
		}
	}

}
