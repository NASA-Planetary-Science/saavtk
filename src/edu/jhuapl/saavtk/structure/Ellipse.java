package edu.jhuapl.saavtk.structure;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.vtk.font.FontAttr;

/**
 * Mutable {@link Structure} that defines an ellipse.
 *
 * @author lopeznr1
 */
public class Ellipse implements Structure
{
	// Attributes
	private final int id;
	private final Object source;
	private final Mode mode;

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

	/**
	 * Standard Constructor
	 */
	public Ellipse(int aId, Object aSource, Mode aMode, Vector3D aCenter, double aRadius, double aAngle,
			double aFlattening, Color aColor, String aLabel)
	{
		id = aId;
		source = aSource;
		mode = aMode;

		center = aCenter;
		radius = aRadius;
		angle = aAngle;
		flattening = aFlattening;

		name = "default";
		label = aLabel;

		color = aColor;
		labelFA = FontAttr.Default;
		visible = true;

		shapeModelId = null;
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

	public Mode getMode()
	{
		return mode;
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
