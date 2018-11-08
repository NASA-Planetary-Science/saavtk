package edu.jhuapl.saavtk.model.structure.esri;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.structure.PointModel;

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

	public static List<PointStructure> fromSbmtStructure(PointModel crappySbmtPointModel)
	{
		List<PointStructure> ps=Lists.newArrayList();
		for (int i=0; i<crappySbmtPointModel.getNumberOfStructures(); i++)
		{
			ps.add(new PointStructure(new Vector3D(crappySbmtPointModel.getStructureCenter(i))));
		}
		return ps;
	}
	
}
