package edu.jhuapl.saavtk.model.structure;

import java.util.ArrayList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.util.LatLon;

public class Polygon
{
    private static SettableMetadata formMetadataFor(edu.jhuapl.saavtk.structure.Polygon aPolygon)
    {
        SettableMetadata metadata = Line.formMetadataFor(aPolygon);

        metadata.put(AREA_KEY, aPolygon.getSurfaceArea());
        metadata.put(SHOW_INTERIOR_KEY, aPolygon.getShowInterior());

        return metadata;
    }

    private static final Key<edu.jhuapl.saavtk.structure.Polygon> POLYGON_STRUCTURE_PROXY_KEY = Key.of("Polygon");
    private static boolean proxyInitialized = false;

    public static void initializeSerializationProxy()
    {
        if (!proxyInitialized)
        {
            LatLon.initializeSerializationProxy();

            InstanceGetter.defaultInstanceGetter().register(POLYGON_STRUCTURE_PROXY_KEY, source -> {
                int id = source.get(Line.ID);
                edu.jhuapl.saavtk.structure.Polygon result = new edu.jhuapl.saavtk.structure.Polygon(id, null, new ArrayList<>());
                unpackMetadata(source, result);

                return result;
            }, edu.jhuapl.saavtk.structure.Polygon.class, polygon -> {
                SettableMetadata metadata = formMetadataFor(polygon);

                return FixedMetadata.of(metadata);
            });

            proxyInitialized = true;
        }
    }

    public static final Key<Double> AREA_KEY = Key.of("area");
    public static final Key<Boolean> SHOW_INTERIOR_KEY = Key.of("showInterior");

    protected static void unpackMetadata(Metadata source, edu.jhuapl.saavtk.structure.Polygon polygon)
    {
        Line.unpackMetadata(source, polygon);

        if (source.hasKey(AREA_KEY))
            polygon.setSurfaceArea(source.get(AREA_KEY));

        if (source.hasKey(SHOW_INTERIOR_KEY))
            polygon.setShowInterior(source.get(SHOW_INTERIOR_KEY));
    }
}
