package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.io.StructureLegacyUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.vtk.font.FontAttr;

public class Line
{
    public static final Key<Integer> ID = Key.of("id");
    public static final Key<String> NAME = Key.of("name");
    public static final Key<List<LatLon>> VERTICES = Key.of("vertices");
    public static final Key<int[]> COLOR = Key.of("color");
    public static final Key<String> LABEL = Key.of("label");
    public static final Key<int[]> LABEL_COLOR = Key.of("labelColor");
    public static final Key<Integer> LABEL_FONT_SIZE = Key.of("labelFontSize");
    public static final Key<Boolean> HIDDEN = Key.of("hidden");
    public static final Key<Boolean> LABEL_HIDDEN = Key.of("labelHidden");

    private static final Version METADATA_VERSION = Version.of(1, 0);

    protected static SettableMetadata formMetadataFor(PolyLine aLine)
    {
        int[] intArr;

        SettableMetadata metadata = SettableMetadata.of(METADATA_VERSION);

        metadata.put(ID, aLine.getId());
        metadata.put(NAME, aLine.getName());
        // Note: it is correct to use settableValues to instantiate the setting for
        // VERTICES. This is because the list of controlPoints is final but mutable. If
        // one used just "Values", the set of vertices would not be saved in the file
        // because it would not be considered "stateful".
        metadata.put(VERTICES, aLine.getControlPoints());
        intArr = StructureLegacyUtil.convertColorToRgba(aLine.getColor());
        metadata.put(COLOR, intArr);
        metadata.put(LABEL, aLine.getLabel());

        FontAttr tmpFA = aLine.getLabelFontAttr();
        intArr = StructureLegacyUtil.convertColorToRgba(tmpFA.getColor());
        metadata.put(LABEL_COLOR, intArr);
        metadata.put(LABEL_FONT_SIZE, tmpFA.getSize());
        metadata.put(HIDDEN, !aLine.getVisible());
        metadata.put(LABEL_HIDDEN, !tmpFA.getIsVisible());

        return metadata;
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
                Metadata metadata = formMetadataFor(line);

                return FixedMetadata.of(metadata);
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
