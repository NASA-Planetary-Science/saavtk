package edu.jhuapl.saavtk.structure;

import java.util.List;

import edu.jhuapl.saavtk.util.LatLon;

/**
 * Mutable {@link Structure} that defines a polygon.
 *
 * @author lopeznr1
 */
public class Polygon extends PolyLine
{
	// State vars
	private boolean showInterior;
	private double surfaceArea;

	/**
	 * Standard Constructor
	 */
	public Polygon(int aId, Object aSource, List<LatLon> aLatLonL)
	{
		super(aId, aSource, aLatLonL);

		showInterior = false;
		surfaceArea = 0.0;
	}

	public boolean getShowInterior()
	{
		return showInterior;
	}

	public double getSurfaceArea()
	{
		return surfaceArea;
	}

	/**
	 * Sets whether the interior should be shown.
	 */
	public void setShowInterior(boolean aBool)
	{
		showInterior = aBool;
	}

	/**
	 * Sets whether the surface area should be shown.
	 */
	public void setSurfaceArea(double aValue)
	{
		surfaceArea = aValue;
	}

	@Override
	public boolean isClosed()
	{
		return true;
	}

}
