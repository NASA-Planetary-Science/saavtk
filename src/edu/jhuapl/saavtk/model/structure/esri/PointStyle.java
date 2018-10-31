package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;

public class PointStyle
{
	Color color;
	double size;
	
	public PointStyle()
	{
		color=Color.BLUE;
		size=1;
	}
	
	public PointStyle(Color color, double size)
	{
		this.color=color;
		this.size=size;
	}

	public Color getColor()
	{
		return color;
	}

	public double getSize()
	{
		return size;
	}	
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"{color="+color+",size="+size+"}";
	}
	
}
