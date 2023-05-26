package edu.jhuapl.saavtk.structure.gui;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.io.XmlLoadUtil;
import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

/**
 * ItemHandler used to process {@link Structure}
 *
 * @author lopeznr1
 */
public class StructureItemHandler<G1 extends Structure> extends BasicItemHandler<G1, LookUp>
{
	// Ref vars
	private final StructureManager<G1> refManager;

	/**
	 * Standard Constructor
	 */
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
				return ((PolyLine) aItem).getPathLength();
			case Area:
				return ((Polygon) aItem).getSurfaceArea();
			case VertexCount:
				return ((PolyLine) aItem).getControlPoints().size();

			// Specific to round-edge items
			case Angle:
				return ((Ellipse) aItem).getAngle();
			case Diameter:
				return ((Ellipse) aItem).getRadius() * 2.0;
			case Flattening:
				return ((Ellipse) aItem).getFlattening();

			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(G1 aItem, LookUp aEnum, Object aValue)
	{
		List<G1> itemL = ImmutableList.of(aItem);
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
	 *
	 * @return
	 */
	private Object getTypeString(G1 aItem)
	{
		if (aItem instanceof Polygon)
			return XmlLoadUtil.RAW_TYPE_POLYGON;
		else if (aItem instanceof PolyLine)
			return XmlLoadUtil.RAW_TYPE_PATH;

		if (aItem instanceof Ellipse == false)
			return "unknown";

		Ellipse tmpItem = (Ellipse) aItem;
		Mode tmpMode = tmpItem.getMode();
		return tmpMode.getLabel().toLowerCase();
	}

}
