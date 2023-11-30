package edu.jhuapl.saavtk.structure;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.structure.vtk.RenderState;
import edu.jhuapl.saavtk.vtk.font.FontAttr;

/**
 * Mutable {@link Structure} that defines an ellipse.
 *
 * @author lopeznr1
 */
public class Ellipse implements Structure, ClosedShape
{
	// Attributes
	private final int id;
	private final Object source;
	private final StructureType type;

	// State vars
	private Vector3D center;
	private double radius; // or semimajor axis
	private double angle;
	private double flattening; // ratio of semiminor axis to semimajor axis

	private Color color;
	private FontAttr labelFA;
	private boolean visible;

	private String name;
	private String label;

	private String shapeModelId;
	private boolean showInterior;

	// Render vars
	private RenderState renderState;

	/** Standard Constructor */
	public Ellipse(int aId, Object aSource, StructureType aType, Vector3D aCenter, double aRadius, double aAngle,
			double aFlattening, Color aColor)
	{
		id = aId;
		source = aSource;
		type = aType;

		center = aCenter;
		radius = aRadius;
		angle = aAngle;
		flattening = aFlattening;

		name = "default";
		label = "";

		color = aColor;
		labelFA = FontAttr.Default;
		visible = true;

		shapeModelId = null;
		showInterior = false;

		renderState = RenderState.Invalid;
	}

	/** Simplified Constructor: Circle */
	public Ellipse(int aId, Object aSource, Vector3D aCenter, double aRadius, Color aColor)
	{
		this(aId, aSource, StructureType.Circle, aCenter, aRadius, 0.0, 1.0, aColor);
	}

	public double getAngle()
	{
		return angle;
	}

	public Vector3D getCenter()
	{
		return center;
	}

	public double getFlattening()
	{
		return flattening;
	}

	public double getRadius()
	{
		return radius;
	}

	public void setAngle(double aAngle)
	{
		angle = aAngle;
	}

	public void setCenter(Vector3D aCenter)
	{
		center = aCenter;
	}

	public void setFlattening(double aFlattening)
	{
		flattening = aFlattening;
	}

	public void setRadius(double aRadius)
	{
		radius = aRadius;
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
	public boolean getShowInterior()
	{
		return showInterior;
	}

	@Override
	public double getSurfaceArea()
	{
		return renderState.surfaceArea();
	}

	@Override
	public StructureType getType()
	{
		return type;
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
	public void setShowInterior(boolean aBool)
	{
		showInterior = aBool;
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
