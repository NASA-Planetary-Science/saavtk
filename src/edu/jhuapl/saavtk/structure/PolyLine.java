package edu.jhuapl.saavtk.structure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.structure.vtk.RenderState;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.vtk.font.FontAttr;

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

	private String shapeModelId;

	// Render vars
	private RenderState renderState;

	/** Standard Constructor */
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

		shapeModelId = null;

		renderState = RenderState.Invalid;
	}

	/**
	 * Returns the control points that define this PolyLine's geometry.
	 */
	public ImmutableList<LatLon> getControlPoints()
	{
		return controlPointL;
	}

	/**
	 * Returns true if this PolyLine should be closed).
	 */
	public boolean isClosed()
	{
		return false;
	}

	/**
	 * Adds a control point at the specified index. All controls at that index or later will be shifted to the right.
	 */
	public void addControlPoint(int aIdx, LatLon aControlPoint)
	{
		var tmpL = new ArrayList<LatLon>(controlPointL);
		tmpL.add(aIdx, aControlPoint);

		controlPointL = ImmutableList.copyOf(tmpL);
	}

	/**
	 * Removes the control point at the specified index.
	 */
	public void delControlPoint(int aIdx)
	{
		var tmpL = new ArrayList<LatLon>(controlPointL);
		tmpL.remove(aIdx);

		controlPointL = ImmutableList.copyOf(tmpL);
	}

	/**
	 * Replaces the control point at the specified index with aControlPoint.
	 */
	public void setControlPoint(int aIdx, LatLon aControlPoint)
	{
		var tmpL = new ArrayList<LatLon>(controlPointL);
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
	public RenderState getRenderState()
	{
		return renderState;
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
	public StructureType getType()
	{
		return StructureType.Path;
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
	public void setRenderState(RenderState aRenderState)
	{
		renderState = aRenderState;
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

	@Override
	public String toString()
	{
		return name;
	}

}
