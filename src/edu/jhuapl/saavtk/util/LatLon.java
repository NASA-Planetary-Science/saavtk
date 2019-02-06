package edu.jhuapl.saavtk.util;

import com.google.common.base.Preconditions;

/**
 * Note it is unspecified whether lat and lon are in degrees or radians.
 */
public class LatLon
{
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

	public LatLon(double lat, double lon, double rad)
	{
		this.lat = lat;
		this.lon = lon;
		this.rad = rad;
	}

	public LatLon(double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
		this.rad = 1.0;
	}

	public LatLon()
	{
		this.lat = 0.0;
		this.lon = 0.0;
		this.rad = 1.0;
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
	protected Object clone()
	{
		return new LatLon(lat, lon, rad);
	}
}
