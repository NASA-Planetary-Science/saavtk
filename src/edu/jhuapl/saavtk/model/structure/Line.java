package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.settings.api.Configurable;
import crucible.crust.settings.api.ControlKey;
import crucible.crust.settings.api.SettableStored;
import crucible.crust.settings.api.Stored;
import crucible.crust.settings.api.Versionable;
import crucible.crust.settings.impl.ConfigurableFactory;
import crucible.crust.settings.impl.KeyedFactory;
import crucible.crust.settings.impl.SettableStoredFactory;
import crucible.crust.settings.impl.StoredFactory;
import crucible.crust.settings.impl.Version;
import crucible.crust.settings.impl.metadata.KeyValueCollectionMetadataManager;
import edu.jhuapl.saavtk.structure.FontAttr;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.io.StructureLegacyUtil;
import edu.jhuapl.saavtk.util.LatLon;

public class Line
{
	public static final ControlKey<SettableStored<Integer>> ID = SettableStoredFactory.key("id");
	public static final ControlKey<SettableStored<String>> NAME = SettableStoredFactory.key("name");
	public static final ControlKey<Stored<List<LatLon>>> VERTICES = StoredFactory.key("vertices");
	public static final ControlKey<SettableStored<int[]>> COLOR = SettableStoredFactory.key("color");
	public static final ControlKey<SettableStored<String>> LABEL = SettableStoredFactory.key("label");
	public static final ControlKey<SettableStored<int[]>> LABEL_COLOR = SettableStoredFactory.key("labelColor");
	public static final ControlKey<SettableStored<Integer>> LABEL_FONT_SIZE = SettableStoredFactory.key("labelFontSize");
	public static final ControlKey<SettableStored<Boolean>> HIDDEN = SettableStoredFactory.key("hidden");
	public static final ControlKey<SettableStored<Boolean>> LABEL_HIDDEN = SettableStoredFactory.key("labelHidden");

	private static final Versionable CONFIGURATION_VERSION = Version.of(1, 0);
	private static final SettableStoredFactory settableValues = SettableStoredFactory.instance();

	protected static Configurable formConfigurationFor(PolyLine aLine)
	{
		int[] intArr;

		KeyedFactory.Builder<Object> builder = KeyedFactory.instance().builder();

		builder.put(ID, settableValues.of(aLine.getId()));
		builder.put(NAME, settableValues.of(aLine.getName()));
		// Note: it is correct to use settableValues to instantiate the setting for
		// VERTICES. This is because the list of controlPoints is final but mutable. If
		// one used just "Values", the set of vertices would not be saved in the file
		// because it would not be considered "stateful".
		builder.put(VERTICES, settableValues.of(aLine.getControlPoints()));
		intArr = StructureLegacyUtil.convertColorToRgba(aLine.getColor());
		builder.put(COLOR, settableValues.of(intArr));
		builder.put(LABEL, settableValues.of(aLine.getLabel()));

		FontAttr tmpFA = aLine.getLabelFontAttr();
		intArr = StructureLegacyUtil.convertColorToRgba(tmpFA.getColor());
		builder.put(LABEL_COLOR, settableValues.of(intArr));
		builder.put(LABEL_FONT_SIZE, settableValues.of(tmpFA.getSize()));
		builder.put(HIDDEN, settableValues.of(!aLine.getVisible()));
		builder.put(LABEL_HIDDEN, settableValues.of(!tmpFA.getIsVisible()));

		return ConfigurableFactory.instance().of(CONFIGURATION_VERSION, builder.build());
	}

	private static final Key<PolyLine> LINE_STRUCTURE_PROXY_KEY = Key.of("Line (structure)");
	private static boolean proxyInitialized = false;

	public static void initializeSerializationProxy()
	{
		if (!proxyInitialized)
		{
			LatLon.initializeSerializationProxy();

			InstanceGetter.defaultInstanceGetter().register(LINE_STRUCTURE_PROXY_KEY, source -> {
				int id = source.get(Key.of(ID.getId()));
				PolyLine result = new PolyLine(id, null, new ArrayList<>());
				unpackMetadata(source, result);

				return result;
			}, PolyLine.class, line -> {
				Configurable configuration = formConfigurationFor(line);
				return KeyValueCollectionMetadataManager.of(configuration.getVersion(), configuration).store();
			});

			proxyInitialized = true;
		}
	}

	protected static void unpackMetadata(Metadata source, PolyLine line)
	{
		int[] intArr;

		line.setName(source.get(Key.of(NAME.getId())));
		intArr = source.get(Key.of(COLOR.getId()));
		line.setColor(StructureLegacyUtil.convertRgbaToColor(intArr));

		List<LatLon> sourceControlPoints = source.get(Key.of(VERTICES.getId()));
		line.setControlPoints(ImmutableList.copyOf(sourceControlPoints));

		line.setLabel(source.get(Key.of(LABEL.getId())));
		intArr = source.get(Key.of(LABEL_COLOR.getId()));
		Color labelColor = StructureLegacyUtil.convertRgbaToColor(intArr);
		int labelSize = source.get(Key.of(LABEL_FONT_SIZE.getId()));
		boolean visible = source.get(Key.of(HIDDEN.getId()));
		line.setVisible(!visible);
		boolean labelIsVisible = source.get(Key.of(LABEL_HIDDEN.getId()));
		labelIsVisible = !labelIsVisible;

		line.setLabelFontAttr(new FontAttr("Plain", labelColor, labelSize, labelIsVisible));
	}
}
