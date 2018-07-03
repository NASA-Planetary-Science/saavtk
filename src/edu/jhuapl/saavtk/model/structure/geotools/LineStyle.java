package edu.jhuapl.saavtk.model.structure.geotools;

import java.awt.Color;

public class LineStyle
{
	Color lineColor;
	double lineWidth;
	
	public LineStyle()
	{
		lineColor=Color.BLUE;
		lineWidth=1;
	}
	
	public LineStyle(Color lineColor, double lineWidth)
	{
		this.lineColor=lineColor;
		this.lineWidth=lineWidth;
	}

	public Color getLineColor()
	{
		return lineColor;
	}

	public double getLineWidth()
	{
		return lineWidth;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"{linecolor="+lineWidth+",linewidth="+lineWidth+"}";
	}
}
