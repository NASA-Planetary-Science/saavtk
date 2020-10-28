package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.util.file.IndexableTuple;

/**
 * This class is not exactly deprecated, but its use should not be expanded, nor
 * should its inner behaviors be made more public. The reason is that, although
 * it provides a fairly general implementation of {@link LoadableColoringData},
 * its handling of the different inputs needed for CSV v. FITS v. VTK files is a
 * bit clunky and monolithic.
 * 
 * @author James Peachey
 *
 */
abstract class LegacyColoringData extends LoadableColoringData
{

    /**
     * Create and return a {@link LegacyColoringData} instance with the specified
     * properties. The returned object will automatically load and cache the data
     * and the range the first time the data or the range are requested via either
     * the {@link #getData()} or {@link #getDefaultRange()} methods. It follows that
     * these methods might throw exceptions even if this factory method returns a
     * validly constructed instance of {@link LegacyColoringData}. The returned
     * object's {@link #clear()} method will discard and free up any loaded objects.
     * 
     * @param name
     * @param units
     * @param numberElements
     * @param fieldNames
     * @param hasNulls
     * @param fileId
     * @param columnIdentifiers
     * @return
     */
    static LegacyColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, String fileId, Iterable<?> columnIdentifiers)
    {
        LegacyColoringDataIO ioHandler = LegacyColoringDataIO.of(name);

        ImmutableList<?> columnIdList = columnIdentifiers != null ? ImmutableList.copyOf(columnIdentifiers) : null;

        AtomicReference<IndexableTuple> dataReference = new AtomicReference<>();
        AtomicReference<double[]> rangeReference = new AtomicReference<>();

        return new LegacyColoringData(name, units, numberElements, ImmutableList.copyOf(fieldNames), hasNulls, fileId, columnIdList) {

            @Override
            protected ColoringDataIO getIOHandler()
            {
                return ioHandler;
            }

            @Override
            protected AtomicReference<IndexableTuple> getDataReference()
            {
                return dataReference;
            }

            @Override
            protected AtomicReference<double[]> getRangeReference()
            {
                return rangeReference;
            }

            @Override
            public int hashCode()
            {
                final int prime = 31;
                int result = 1;
                result = prime * LoadableColoringData.hashCode(this);
                result = prime * Objects.hashCode(getColumnIdentifiers());

                return result;
            }

            @Override
            public boolean equals(Object object)
            {
                if (this == object)
                {
                    return true;
                }

                if (object instanceof LegacyColoringData)
                {
                    LegacyColoringData other = (LegacyColoringData) object;

                    return LoadableColoringData.equals(this, other) && getColumnIdentifiers().equals(other.getColumnIdentifiers());
                }

                return false;
            }
        };
    }

    /**
     * Create and return a {@link LegacyColoringData} instance whose properties are
     * extracted from the specified {@link Metadata} object.
     * <p>
     * The returned object will automatically load and cache the data and the range
     * the first time the data or the range are requested via either the
     * {@link #getData()} or {@link #getDefaultRange()} methods. It follows that
     * these methods might throw exceptions even if this factory method returns a
     * validly constructed instance of {@link LegacyColoringData}. The returned
     * object's {@link #clear()} method will discard and free up any loaded objects.
     * <p>
     * The metadata currently does not identify the file format, so this method
     * requires another argument to specify the {@link ColoringDataIO}. This could
     * be changed in the future.
     * 
     * @param metadata
     * @return
     */
    static LegacyColoringData of(Metadata metadata)
    {
        LoadableColoringData tmpData = of(metadata, DummyHandler);

        ImmutableList<?> columnIdentifiers = (metadata.hasKey(COLUMN_IDS)) ? ImmutableList.copyOf(metadata.get(COLUMN_IDS)) : null;

        return of(tmpData.getName(), tmpData.getUnits(), tmpData.getNumberElements(), tmpData.getFieldNames(), tmpData.hasNulls(), tmpData.getFileId(), columnIdentifiers);
    }

    /**
     * Create and return a {@link LegacyColoringData} instance whose properties and
     * data are copied from the specified input {@link ColoringData} object, and
     * augmented with the specified file identifier and column identifiers. This
     * method effectively associates a coloring that may have been created on the
     * fly with a file on disk. If the provided source coloring already associates
     * with a file on disk, the new object will be "re-pointed" to the specified
     * file identifier.
     * <p>
     * Most properties from the source object are simply reused, but the source
     * object's {@link IndexableTuple} data returned by its
     * {@link ColoringData#getData()} method is copied. Thus, this method will force
     * the source object to load its data if it hasn't already. The new instance
     * will use this copy and recompute the range.
     * <p>
     * This method only creates the coloring data object -- it does not copy the
     * data file on disk. However, the caller may follow this with an explicit
     * command to copy the file, or save the newly created coloring data object.
     * 
     * @param sourceData
     * @param fileId
     * @param columnIdentifiers
     * @return
     */
    static LegacyColoringData of(ColoringData sourceData, String fileId, Iterable<?> columnIdentifiers)
    {
        Preconditions.checkNotNull(sourceData);
        Preconditions.checkNotNull(fileId);

        LegacyColoringData dest = of(sourceData.getName(), sourceData.getUnits(), sourceData.getNumberElements(), sourceData.getFieldNames(), sourceData.hasNulls(), fileId, columnIdentifiers);

        dest.copyData(sourceData);

        return dest;
    }

    private final ImmutableList<?> columnIds;

    private LegacyColoringData(String name, String units, int numberElements, ImmutableList<String> fieldNames, boolean hasNulls, String fileId, ImmutableList<?> columnIds)
    {
        super(name, units, numberElements, fieldNames, hasNulls, fileId);

        this.columnIds = columnIds;
    }

    public ImmutableList<?> getColumnIdentifiers()
    {
        return columnIds;
    }

    @Override
    protected IndexableTuple provideData()
    {
        File file = getFile();

        @SuppressWarnings("unchecked")
        List<Integer> columnIdentifiers = (List<Integer>) getColumnIdentifiers();
        try
        {
            LegacyColoringDataIO ioHandler = //
                    columnIdentifiers == null || columnIdentifiers.isEmpty() ? //
                            LegacyColoringDataIO.of(getName()) : //
                            LegacyColoringDataIO.of(columnIdentifiers, columnIdentifiers);
            return ioHandler.loadColoringData(file);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to load coloring data from file " + file, e);
        }
    }

    @Override
    public FixedMetadata getMetadata()
    {
        SettableMetadata metadata = SettableMetadata.of(COLORING_DATA_VERSION);
        Metadata superMetadata = super.getMetadata();
        for (Key<?> wildCardKey : superMetadata.getKeys())
        {
            @SuppressWarnings("unchecked")
            Key<Object> key = (Key<Object>) wildCardKey;

            metadata.put(key, superMetadata.get(key));
        }
        metadata.put(COLUMN_IDS, getColumnIdentifiers());

        return FixedMetadata.of(metadata);
    }

    // Metadata constants.
    static final Version COLORING_DATA_VERSION = Version.of(1, 1);
    static final Key<List<?>> COLUMN_IDS = Key.of("Column identifiers"); // [ "Slope" ] or [ 5, 7, 9 ]

}
