package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

public class StructureUtil
{
	public static Vector3D centroid(List<Vector3D> points)
	{
		Vector3D result=Vector3D.ZERO;
		for (int i=0; i<points.size(); i++)
		{
			result=result.add(points.get(i));
		}
		return result;
	}
	
	public static Vector3D centroidClosedPoly(List<Vector3D> points)
	{
		return centroid(points.subList(0, points.size()-1));
	}
	
	public static Vector3D centroidOfSegments(List<LineSegment> segments)
	{
		Vector3D result=Vector3D.ZERO;
		for (int i=0; i<segments.size(); i++)
		{
			result=result.add(segments.get(i).getStart());
			result=result.add(segments.get(i).getEnd());
		}
		return result.scalarMultiply(1./2./segments.size());
	}
	
	public static Vector3D randomVector3D()
	{
		return new Vector3D(2*Math.random()-1,2*Math.random()-1,2*Math.random()-1);
	}
	
	public static List<Vector3D> random(int n)
	{
		List<Vector3D> pts=Lists.newArrayList();
		for (int i=0; i<n; i++)
			pts.add(randomVector3D());
		return pts;
	}
	
/*	public static String toString(double[][] pts)
	{
		String str="{";
		for (int i=0; i<pts.length; i++)
		{
			str+=toString(pts[i]);
			if (i<pts.length-1)
				str+=",";
		}
		str+="}";
		return str;
	}
	
	public static String toString(double[] pt)
	{
		return "("+pt[0]+" "+pt[1]+" "+pt[2]+")";
	}
*/	
	public static String colorToHex(Color color)
	{
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); // from http://stackoverflow.com/questions/3607858/convert-a-rgb-color-value-to-a-hexadecimal
	}

	
	public static final Color defaultColor=Color.WHITE;
	public static Color hexToColor(String hex) // from http://stackoverflow.com/questions/4129666/how-to-convert-hex-to-rgb-using-java/4129692#4129692
	{
		if (hex==null)
			return defaultColor;
		else
			return new Color(Integer.valueOf(hex.substring(1, 3), 16), Integer.valueOf(hex.substring(3, 5), 16), Integer.valueOf(hex.substring(5, 7), 16));
	}

	
}
