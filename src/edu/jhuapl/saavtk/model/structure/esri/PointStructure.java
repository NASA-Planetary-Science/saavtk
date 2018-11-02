package edu.jhuapl.saavtk.model.structure.esri;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class PointStructure implements Structure
{
	Vector3D location;
	PointStyle pointStyle;
	String label;

	public PointStructure(Vector3D location)
	{
		this.location = location;
	}

	public PointStyle getPointStyle()
	{
		return pointStyle;
	}

	public void setPointStyle(PointStyle pointStyle)
	{
		this.pointStyle = pointStyle;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public Vector3D getCentroid()
	{
		return location;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{centroid=" + location + ",label=" + label + ",style=" + pointStyle + "}";
	}

}
