package edu.jhuapl.saavtk.model.structure.esri;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

public class LineSegment
{
	Vector3D start;
	Vector3D end;
	
	public LineSegment(Vector3D start, Vector3D end)
	{
		this.start=start;
		this.end=end;
	}
	
	public Vector3D getStart()
	{
		return start;
	}
	
	public Vector3D getEnd()
	{
		return end;
	}
	
	public Vector3D getMidpoint()
	{
		return start.add(end).scalarMultiply(0.5);
	}
	
	public List<LineSegment> split()
	{
		Vector3D midpoint=getMidpoint();
		return Lists.newArrayList(new LineSegment(start, midpoint), new LineSegment(midpoint, end));
	}
	
	public double getLength()
	{
		return end.subtract(start).getNorm();
	}

	@Override
	public String toString()
	{
		return "LineSegment [start=" + start + ", end=" + end + ", getLength()=" + getLength() + "]";
	}
	
	
}
