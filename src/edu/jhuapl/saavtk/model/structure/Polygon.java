package edu.jhuapl.saavtk.model.structure;

import java.util.ArrayList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.settings.api.Configurable;
import crucible.crust.settings.api.ControlKey;
import crucible.crust.settings.api.SettableStored;
import crucible.crust.settings.api.Viewable;
import crucible.crust.settings.impl.ConfigurableFactory;
import crucible.crust.settings.impl.KeyedFactory;
import crucible.crust.settings.impl.SettableStoredFactory;
import crucible.crust.settings.impl.metadata.KeyValueCollectionMetadataManager;
import edu.jhuapl.saavtk.util.LatLon;

public class Polygon
{
    private static Configurable formConfigurationFor(edu.jhuapl.saavtk.structure.Polygon aPolygon)
    {
        Configurable lineConfiguration = Line.formConfigurationFor(aPolygon);

        KeyedFactory.Builder<Object> builder = KeyedFactory.instance().builder();
        for (ControlKey<?> key : lineConfiguration.getKeys())
        {
            @SuppressWarnings("unchecked")
            ControlKey<Viewable> contentKey = (ControlKey<Viewable>) key;
            builder.put(contentKey, lineConfiguration.getItem(contentKey));
        }
        builder.put(AREA_KEY, SettableStoredFactory.instance().of(aPolygon.getSurfaceArea()));
        builder.put(SHOW_INTERIOR_KEY, SettableStoredFactory.instance().of(aPolygon.getShowInterior()));

        return ConfigurableFactory.instance().of(lineConfiguration.getVersion(), builder.build());
    }

    private static final Key<edu.jhuapl.saavtk.structure.Polygon> POLYGON_STRUCTURE_PROXY_KEY = Key.of("Polygon");
    private static boolean proxyInitialized = false;

    public static void initializeSerializationProxy()
    {
        if (!proxyInitialized)
        {
            LatLon.initializeSerializationProxy();

            InstanceGetter.defaultInstanceGetter().register(POLYGON_STRUCTURE_PROXY_KEY, source -> {
                int id = source.get(Key.of(Line.ID.getId()));
                edu.jhuapl.saavtk.structure.Polygon result = new edu.jhuapl.saavtk.structure.Polygon(id, null, new ArrayList<>());
                unpackMetadata(source, result);

                return result;
            }, edu.jhuapl.saavtk.structure.Polygon.class, polygon -> {
                Configurable configuration = formConfigurationFor(polygon);
                return KeyValueCollectionMetadataManager.of(configuration.getVersion(), configuration).store();
            });

            proxyInitialized = true;
        }
    }

    public static final ControlKey<SettableStored<Double>> AREA_KEY = SettableStoredFactory.key("area");
    public static final ControlKey<SettableStored<Boolean>> SHOW_INTERIOR_KEY = SettableStoredFactory.key("showInterior");

    protected static void unpackMetadata(Metadata source, edu.jhuapl.saavtk.structure.Polygon polygon)
    {
        Line.unpackMetadata(source, polygon);

        Key<Double> areaKey = Key.of(AREA_KEY.getId());
        if (source.hasKey(areaKey))
            polygon.setSurfaceArea(source.get(areaKey));

        Key<Boolean> showInteriorKey = Key.of(SHOW_INTERIOR_KEY.getId());
        if (source.hasKey(showInteriorKey))
            polygon.setShowInterior(source.get(showInteriorKey));
    }
}
