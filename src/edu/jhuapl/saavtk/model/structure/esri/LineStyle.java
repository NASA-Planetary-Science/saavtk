package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;

public class LineStyle
{
	Color color;
	double width;
	
	public LineStyle()
	{
		color=Color.BLUE;
		width=1;
	}
	
	public LineStyle(Color color, double width)
	{
		this.color=color;
		this.width=width;
	}

	

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public double getWidth()
	{
		return width;
	}

	public void setWidth(double width)
	{
		this.width = width;
	}

	@Override
	public String toString()
	{
		return "LineStyle [color=" + color + ", width=" + width + "]";
	}
}
