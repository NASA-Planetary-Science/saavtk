package edu.jhuapl.saavtk.grid;

/**
 * Record that defines the attributes associated with the spacing of the lat /
 * lon lines associated with a grid.
 * <p>
 * Note that the units of latitude and longitude are specified in degrees.
 *
 * @author lopeznr1
 */
public record LatLonSpacing(double latSpacing, double lonSpacing)
{
	// Constants
	/** Invalid {@link LatLonSpacing} */
	public static final LatLonSpacing Invalid = new LatLonSpacing(Double.NaN, Double.NaN);

	/** Default {@link LatLonSpacing} */
	public static final LatLonSpacing Default = new LatLonSpacing(10.0, 10.0);

	/**
	 * Returns a {@link GridAttr} with the specified latitude spacing.
	 */
	public LatLonSpacing withLatSpacing(double aLatSpacing)
	{
		return new LatLonSpacing(aLatSpacing, lonSpacing);
	}

	/**
	 * Returns a {@link GridAttr} with the specified longitude spacing.
	 */
	public LatLonSpacing withLonSpacing(double aLonSpacing)
	{
		return new LatLonSpacing(latSpacing, aLonSpacing);
	}

}
