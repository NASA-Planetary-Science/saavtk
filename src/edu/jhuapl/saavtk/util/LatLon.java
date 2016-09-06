package edu.jhuapl.saavtk.util;

/**
 * Note it is unspecified whether lat and lon are in degrees or radians.
 */
public class LatLon
{
    public double lat;
    public double lon;
    public double rad;

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

    public LatLon(LatLon other)
    {
        this.lat = other.lat;
        this.lon = other.lon;
        this.rad = other.rad;
    }

    public String toString()
    {
        return "lat: " + lat + " lon: " + lon + " rad: " + rad;
    }

    /**
     * Assuming this instance is in radians, return a new instance converted to degrees.
     * @return
     */
    public LatLon toDegrees()
    {
        return new LatLon(lat*180.0/Math.PI, lon*180.0/Math.PI, rad);
    }

    /**
     * Assuming this instance is in degrees, return a new instance converted to radians.
     * @return
     */
    public LatLon toRadians()
    {
        return new LatLon(lat*Math.PI/180.0, lon*Math.PI/180.0, rad);
    }

    /**
     * Given two LatLons WITH EQUAL RADII, find a new LatLon that
     * equally bisects the two given LatLons.
     *
     * The algorithm used is as follows.
     *
     * 1. Convert the 2 points to xyz using latLonToRec.
     * 2. Find the midpoint between the 2 points.
     * 3. Convert this midpoint back into LatLon using recToLatLon.
     * 4. Set the radius of this LatLon to that of the 2 points.
     *
     * @param ll1
     * @return
     */
    static public LatLon midpoint(LatLon ll1, LatLon ll2)
    {
        double[] xyz1 = MathUtil.latrec(ll1);
        double[] xyz2 = MathUtil.latrec(ll2);

        double[] midxyz = {(xyz1[0]+xyz2[0])/2.0,(xyz1[1]+xyz2[1])/2.0,(xyz1[2]+xyz2[2])/2.0};

        LatLon midll = MathUtil.reclat(midxyz);

        midll.rad = ll1.rad;

        return midll;
    }

    @Override
    protected Object clone()
    {
        return new LatLon(lat, lon, rad);
    }
}
