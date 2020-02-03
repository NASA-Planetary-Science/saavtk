package edu.jhuapl.saavtk.structure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.LatLon;

/**
 * Mutable {@link Structure} that defines a polyline.
 *
 * @author lopeznr1
 */
public class PolyLine implements Structure
{
	// Attributes
	private final int id;
	private final Object source;

	// State vars
	private String name;
	private String label;

	private ImmutableList<LatLon> controlPointL;

	private Color color;
	private FontAttr labelFA;
	private boolean visible;

	private double pathLength;
	private String shapeModelId;

	/**
	 * Standard Constructor
	 */
	public PolyLine(int aId, Object aSource, List<LatLon> aControlPointL)
	{
		id = aId;
		source = aSource;

		name = "default";
		label = "";

		controlPointL = ImmutableList.copyOf(aControlPointL);

		color = Color.MAGENTA;
		labelFA = FontAttr.Default;
		visible = true;

		pathLength = 0;
		shapeModelId = null;
	}

	/**
	 * Returns the control points that define this PolyLine's geometry.
	 */
	public ImmutableList<LatLon> getControlPoints()
	{
		return controlPointL;
	}

	/**
	 * Returns the length of the line as projected onto the surface.
	 * <P>
	 * Note this is not the distance between all of the control points but rather
	 * the distance of the line as projected on the relevant surface.
	 */
	public double getPathLength()
	{
		return pathLength;
	}

	/**
	 * Returns true if this PolyLine should be closed).
	 */
	public boolean isClosed()
	{
		return false;
	}

	/**
	 * Adds a control point at the specified index. All controls at that index or
	 * later will be shifted to the right.
	 */
	public void addControlPoint(int aIdx, LatLon aControlPoint)
	{
		List<LatLon> tmpL = new ArrayList<>(controlPointL);
		tmpL.add(aIdx, aControlPoint);

		controlPointL = ImmutableList.copyOf(tmpL);
	}

	/**
	 * Removes the control point at the specified index.
	 */
	public void delControlPoint(int aIdx)
	{
		List<LatLon> tmpL = new ArrayList<>(controlPointL);
		tmpL.remove(aIdx);

		controlPointL = ImmutableList.copyOf(tmpL);
	}

	/**
	 * Replaces the control point at the specified index with aControlPoint.
	 */
	public void setControlPoint(int aIdx, LatLon aControlPoint)
	{
		List<LatLon> tmpL = new ArrayList<>(controlPointL);
		tmpL.set(aIdx, aControlPoint);

		controlPointL = ImmutableList.copyOf(tmpL);
	}

	/**
	 * Replaces all of the current control points with those provided in the list.
	 */
	public void setControlPoints(List<LatLon> aControlPointL)
	{
		controlPointL = ImmutableList.copyOf(aControlPointL);
	}

	/**
	 * Sets in the path length assoicated with this Line.
	 */
	public void setPathLength(double aPathLength)
	{
		pathLength = aPathLength;
	}

	@Override
	public int getId()
	{
		return id;
	}

	@Override
	public Color getColor()
	{
		return color;
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public FontAttr getLabelFontAttr()
	{
		return labelFA;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getShapeModelId()
	{
		return shapeModelId;
	}

	@Override
	public Object getSource()
	{
		return source;
	}

	@Override
	public boolean getVisible()
	{
		return visible;
	}

	@Override
	public void setColor(Color aColor)
	{
		color = aColor;
	}

	@Override
	public void setLabel(String aLabel)
	{
		label = aLabel;
	}

	@Override
	public void setLabelFontAttr(FontAttr aAttr)
	{
		labelFA = aAttr;
	}

	@Override
	public void setName(String aName)
	{
		name = aName;
	}

	@Override
	public void setShapeModelId(String aShapeModelId)
	{
		shapeModelId = aShapeModelId;
	}

	@Override
	public void setVisible(boolean aBool)
	{
		visible = aBool;
	}

}
