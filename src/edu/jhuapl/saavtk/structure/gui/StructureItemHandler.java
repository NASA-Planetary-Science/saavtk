package edu.jhuapl.saavtk.structure.gui;

import java.awt.Color;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.structure.ClosedShape;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.io.XmlLoadUtil;
import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * {@link ItemHandler} used to process {@link Structure}s.
 *
 * @author lopeznr1
 */
public class StructureItemHandler<G1 extends Structure> extends BasicItemHandler<G1, LookUp>
{
	// Ref vars
	private final StructureManager<G1> refManager;

	/** Standard Constructor */
	public StructureItemHandler(StructureManager<G1> aManager, QueryComposer<LookUp> aComposer)
	{
		super(aComposer);

		refManager = aManager;
	}

	@Override
	public Object getColumnValue(G1 aItem, LookUp aEnum)
	{
		switch (aEnum)
		{
			case Id:
				return aItem.getId();
			case Source:
				return aItem.getSource();
			case Type:
				return getTypeString(aItem);
			case IsVisible:
				return aItem.getVisible();
			case Color:
				return aItem.getColor();
			case Name:
				return aItem.getName();
			case Label:
				return aItem.getLabel();

			// Specific to hard-edge items
			case Length:
				if (aItem instanceof PolyLine aPolyLine)
					return aPolyLine.getRenderState().pathLength();
				else if (aItem instanceof Ellipse aEllipse)
					return 2 * Math.PI * aEllipse.getRadius();
				return null;
			case Area:
				if (aItem instanceof ClosedShape aClosedShape)
					return aClosedShape.getSurfaceArea();
				return null;
			case VertexCount:
				if (aItem instanceof PolyLine aPolyLine)
					return aPolyLine.getControlPoints().size();
				return null;

			// Specific to round-edge items
			case Angle:
				if (aItem instanceof Ellipse aEllipse)
					return aEllipse.getAngle();
				return null;
			case Diameter:
				if (aItem instanceof Ellipse aEllipse)
					return aEllipse.getRadius() * 2;
				return null;
			case Flattening:
				if (aItem instanceof Ellipse aEllipse)
					return aEllipse.getFlattening();
				return null;

			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(G1 aItem, LookUp aEnum, Object aValue)
	{
		var itemL = ImmutableList.of(aItem);
		if (aEnum == LookUp.IsVisible)
			refManager.setIsVisible(itemL, (boolean) aValue);
		else if (aEnum == LookUp.Color)
			refManager.setColor(itemL, (Color) aValue);
		else if (aEnum == LookUp.Name)
			aItem.setName((String) aValue);
		else if (aEnum == LookUp.Label)
			refManager.setLabel(aItem, (String) aValue);
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	/**
	 * Returns the string that should be used for the type column.
	 */
	private Object getTypeString(G1 aItem)
	{
		if (aItem instanceof Polygon)
			return XmlLoadUtil.RAW_TYPE_POLYGON;
		else if (aItem instanceof PolyLine)
			return XmlLoadUtil.RAW_TYPE_PATH;
		else if (aItem != null)
			return ("" + aItem.getType()).toLowerCase();

		return null;
	}

}
