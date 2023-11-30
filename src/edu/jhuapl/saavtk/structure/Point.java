package edu.jhuapl.saavtk.structure;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.structure.vtk.RenderState;
import edu.jhuapl.saavtk.vtk.font.FontAttr;

/**
 * Mutable {@link Structure} that defines a point.
 *
 * @author lopeznr1
 */
public class Point implements Structure
{
	// Attributes
	private final int id;
	private final Object source;

	// State vars
	private Vector3D center;
	private Color color;
	private FontAttr labelFA;
	private boolean visible;

	private String name;
	private String label;

	private String shapeModelId;

	// Render vars
	private RenderState renderState;

	/** Standard Constructor */
	public Point(int aId, Object aSource, Vector3D aCenter, Color aColor)
	{
		id = aId;
		source = aSource;

		center = aCenter;
		color = aColor;

		name = "default";
		label = "";

		labelFA = FontAttr.Default;
		visible = true;

		shapeModelId = null;

		renderState = RenderState.Invalid;
	}

	public Vector3D getCenter()
	{
		return center;
	}

	public void setCenter(Vector3D aCenter)
	{
		center = aCenter;
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
	public StructureType getType()
	{
		return StructureType.Point;
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
