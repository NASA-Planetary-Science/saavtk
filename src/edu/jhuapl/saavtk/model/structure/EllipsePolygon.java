package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.io.StructureLegacyUtil;
import edu.jhuapl.saavtk.vtk.font.FontAttr;

public class EllipsePolygon
{
	private static final Key<Ellipse> ELLIPSE_POLYGON_KEY = Key.of("ellipsePolygon");
	private static final Key<Integer> NUMBER_SIDES_KEY = Key.of("numberSides");
	private static final Key<String> TYPE_KEY = Key.of("type");
	private static final Key<int[]> COLOR_KEY = Key.of("color");
	private static final Key<String> MODE_KEY = Key.of("mode");
	private static final Key<Integer> ID_KEY = Key.of("id");
	private static final Key<String> LABEL_KEY = Key.of("label");
	private static final Key<int[]> LABEL_COLOR_KEY = Key.of("labelColor");
	private static final Key<Integer> LABEL_FONT_SIZE_KEY = Key.of("labelFontSize");
	private static final Key<String> NAME_KEY = Key.of("name");
	private static final Key<double[]> CENTER_KEY = Key.of("center");
	private static final Key<Double> RADIUS_KEY = Key.of("radius");
	private static final Key<Double> FLATTENING_KEY = Key.of("flattening");
	private static final Key<Double> ANGLE_KEY = Key.of("angle");
	private static final Key<Boolean> HIDDEN_KEY = Key.of("hidden");
	private static final Key<Boolean> LABEL_HIDDEN_KEY = Key.of("labelHidden");

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(ELLIPSE_POLYGON_KEY, (source) -> {
//			int numberSides = source.get(NUMBER_SIDES_KEY);
//			String type = source.get(TYPE_KEY);
			Color color = StructureLegacyUtil.convertRgbaToColor(source.get(COLOR_KEY));
			Mode mode = Mode.valueOf(source.get(MODE_KEY));
			int id = source.get(ID_KEY);
			String label = source.get(LABEL_KEY);
			Vector3D center = new Vector3D(source.get(CENTER_KEY));
			double radius = source.get(RADIUS_KEY);
			double flattening = source.get(FLATTENING_KEY);
			double angle = source.get(ANGLE_KEY);

			Ellipse result = new Ellipse(id, null, mode, center, radius, angle, flattening, color, label);

			result.setName(source.get(NAME_KEY));
			result.setVisible(!source.get(HIDDEN_KEY));
			boolean labelIsVisible = !source.get(LABEL_HIDDEN_KEY);
			Color labelColor = StructureLegacyUtil.convertRgbaToColor(source.get(LABEL_COLOR_KEY));
			int labelSize = source.get(LABEL_FONT_SIZE_KEY);
			result.setLabelFontAttr(new FontAttr("Plain", labelColor, labelSize, labelIsVisible));

			return result;
		}, Ellipse.class, polygon -> {
			SettableMetadata result = SettableMetadata.of(Version.of(1, 0));

			int numberOfSides = 20;
			String typeStr = "ellipse";
			if (polygon.getMode() == Mode.CIRCLE_MODE)
			{
				typeStr = "circle";
			}
			else if (polygon.getMode() == Mode.POINT_MODE)
			{
				numberOfSides = 4;
				typeStr = "point";
			}

			result.put(NUMBER_SIDES_KEY, numberOfSides);
			result.put(TYPE_KEY, typeStr);
			result.put(COLOR_KEY, StructureLegacyUtil.convertColorToRgba(polygon.getColor()));
			result.put(MODE_KEY, polygon.getMode().name());
			result.put(ID_KEY, polygon.getId());
			result.put(LABEL_KEY, polygon.getLabel());

			result.put(NAME_KEY, polygon.getName());
			result.put(CENTER_KEY, polygon.getCenter().toArray());
			result.put(RADIUS_KEY, polygon.getRadius());
			result.put(FLATTENING_KEY, polygon.getFlattening());
			result.put(ANGLE_KEY, polygon.getAngle());
			result.put(HIDDEN_KEY, !polygon.getVisible());
			result.put(LABEL_HIDDEN_KEY, !polygon.getLabelFontAttr().getIsVisible());
			result.put(LABEL_COLOR_KEY, StructureLegacyUtil.convertColorToRgba(polygon.getLabelFontAttr().getColor()));
			result.put(LABEL_FONT_SIZE_KEY, polygon.getLabelFontAttr().getSize());

			return result;
		});
	}

}
