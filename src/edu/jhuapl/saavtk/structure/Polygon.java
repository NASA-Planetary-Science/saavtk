package edu.jhuapl.saavtk.structure;

import java.util.List;

import edu.jhuapl.saavtk.util.LatLon;

/**
 * Mutable {@link Structure} that defines a polygon.
 *
 * @author lopeznr1
 */
public class Polygon extends PolyLine implements ClosedShape
{
	// State vars
	private boolean showInterior;

	/** Standard Constructor */
	public Polygon(int aId, Object aSource, List<LatLon> aLatLonL)
	{
		super(aId, aSource, aLatLonL);

		showInterior = false;
	}

	@Override
	public boolean getShowInterior()
	{
		return showInterior;
	}

	@Override
	public double getSurfaceArea()
	{
		return getRenderState().surfaceArea();
	}

	@Override
	public StructureType getType()
	{
		return StructureType.Polygon;
	}

	@Override
	public void setShowInterior(boolean aBool)
	{
		showInterior = aBool;
	}

	@Override
	public boolean isClosed()
	{
		return true;
	}

}
