package edu.jhuapl.saavtk.model.structure.esri;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.structure.Ellipse;

public class PointStructure implements Structure
{
	Vector3D location;
	PointStyle pointStyle;
	String label;

	public PointStructure(Vector3D location)
	{
		this.location = location;
		pointStyle=new PointStyle();
		label="";
	}

	public PointStyle getPointStyle()
	{
		return pointStyle;
	}

	public void setPointStyle(PointStyle pointStyle)
	{
		this.pointStyle = pointStyle;
	}

	@Override
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

	public static List<PointStructure> fromSbmtStructure(PointModel aPointManager)
	{
		List<PointStructure> retL = new ArrayList<>();
		for (Ellipse aItem : aPointManager.getAllItems())
		{
			retL.add(new PointStructure(aPointManager.getCenter(aItem)));
		}
		return retL;
	}

}
