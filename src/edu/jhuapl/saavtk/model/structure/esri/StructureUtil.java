package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;
import java.util.Arrays;

public class StructureUtil
{
	public static double[] centroid(double[][] points)
	{
		double[] centroid=new double[3];
		for (int i=0; i<points.length; i++)
		{
			centroid[0]+=points[i][0];
			centroid[1]+=points[i][1];
			centroid[2]+=points[i][2];
		}
		centroid[0]/=(double)points.length;
		centroid[1]/=(double)points.length;
		centroid[2]/=(double)points.length;
		return centroid;
	}
	
	public static double[] centroidClosedPoly(double[][] points)
	{
		double[][] subset=new double[points.length-1][];
		for (int i=0; i<points.length-1; i++)
			subset[i]=points[i];
		return centroid(subset);
	}
	
	public static double[] centroid(LineSegment[] segments)
	{
		double[] centroid=new double[3];
		for (int i=0; i<segments.length; i++)
		{
			centroid[0]+=segments[i].start[0];
			centroid[1]+=segments[i].start[1];
			centroid[2]+=segments[i].start[2];
			centroid[0]+=segments[i].end[0];
			centroid[1]+=segments[i].end[1];
			centroid[2]+=segments[i].end[2];
		}
		centroid[0]/=segments.length*2;
		centroid[1]/=segments.length*2;
		centroid[2]/=segments.length*2;
		return centroid;
	}
	
	public static double[] random()
	{
		return new double[]{2*Math.random()-1,2*Math.random()-1,2*Math.random()-1};
	}
	
	public static double[][] random(int n)
	{
		double[][] pts=new double[n][];
		for (int i=0; i<n; i++)
			pts[i]=random();
		return pts;
	}
	
	public static String toString(double[][] pts)
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
