package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;

@Deprecated		// use LineStyle, PointStyle, and not-yet-implemented FillStyle
public class PatchStyle
{
	LineStyle lineStyle;
	Color fillColor;
	
	public PatchStyle()
	{
		this(Color.LIGHT_GRAY);
	}
	
	public PatchStyle(Color fillColor)
	{
		this(fillColor, new LineStyle());
	}
	
	public PatchStyle(Color fillColor, LineStyle lineStyle)
	{
		this.lineStyle=lineStyle;
		this.fillColor=fillColor;
	}
	
	public Color getFillColor()
	{
		return fillColor;
	}
	
	public LineStyle getLineStyle()
	{
		return lineStyle;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName()+"{linestyle="+lineStyle+",fillcolor="+fillColor+"}";
	}
}
