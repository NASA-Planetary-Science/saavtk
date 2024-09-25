package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.FixedMetadata;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;
import edu.jhuapl.saavtk.util.FileCache;
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
class LegacyColoringData extends LoadableColoringData
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
     * @param fileId the file identifier (relative path or a URL) that locates the
     *            coloring data file in the file cache
     * @param columnIdentifiers
     * @return
     */
    static LegacyColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, String fileId, Iterable<?> columnIdentifiers)
    {
        LegacyColoringDataIO ioHandler = LegacyColoringDataIO.of(name);

        ImmutableList<?> columnIdList = columnIdentifiers != null ? ImmutableList.copyOf(columnIdentifiers) : null;

        return new LegacyColoringData(name, units, numberElements, ImmutableList.copyOf(fieldNames), hasNulls, columnIdList, ioHandler, fileId) {

            @Override
            protected File fetchFile() {
                return FileCache.getFileFromServer(fileId);
            }

        };
    }

    /**
     * Create and return a {@link LegacyColoringData} instance with the specified
     * properties. The returned object will automatically load and cache the data
     * and the range the first time the data or the range are requested via either
     * the {@link #getData()} or {@link #getDefaultRange()} methods. It follows that
     * these methods might throw exceptions even if this factory method returns a
     * validly constructed instance of {@link LegacyColoringData}. The returned
     * object's {@link #clear()} method will discard and free up any loaded objects.
     * <p>
     * This implementation derives the file identifier from the full path of the
     * specified {@link File}. No check is performed to see if the file exists, so
     * it is safest to confirm with the {@link File#isFile()} method before
     * attempting to open it.
     * 
     * @param name
     * @param units
     * @param numberElements
     * @param fieldNames
     * @param hasNulls
     * @param file the file object locating the file in the file system
     * @param columnIdentifiers
     * @return
     */
    static LegacyColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, File file, Iterable<?> columnIdentifiers)
    {
        LegacyColoringDataIO ioHandler = LegacyColoringDataIO.of(name);

        ImmutableList<?> columnIdList = columnIdentifiers != null ? ImmutableList.copyOf(columnIdentifiers) : null;

        String fileId = getFileIdFromFile(file);

        return new LegacyColoringData(name, units, numberElements, ImmutableList.copyOf(fieldNames), hasNulls, columnIdList, ioHandler, fileId);
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
        LoadableColoringData tmpData = StandardColoringData.of(metadata, DummyHandler);

        ImmutableList<?> columnIdList = null;
        if (metadata.hasKey(COLUMN_IDS))
        {
            List<?> columnIds = metadata.get(COLUMN_IDS);
            if (columnIds != null)
            {
                columnIdList = ImmutableList.copyOf(columnIds);
            }
        }

        return of(tmpData.getName(), tmpData.getUnits(), tmpData.getNumberElements(), tmpData.getFieldNames(), tmpData.hasNulls(), tmpData.getFileId(), columnIdList);
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
     * @param file
     * @param columnIdentifiers
     * @return
     */
    static LegacyColoringData of(ColoringData sourceData, File file, Iterable<?> columnIdentifiers)
    {
        Preconditions.checkNotNull(sourceData);
        Preconditions.checkNotNull(file);

        LegacyColoringData dest = of(sourceData.getName(), sourceData.getUnits(), sourceData.getNumberElements(), sourceData.getFieldNames(), sourceData.hasNulls(), file, columnIdentifiers);

        dest.copyData(sourceData);

        return dest;
    }

    private final ImmutableList<?> columnIds;
    private final ColoringDataIO ioHandler;
    private final AtomicReference<IndexableTuple> dataReference;
    private final AtomicReference<double[]> rangeReference;

    protected LegacyColoringData(String name, String units, int numberElements, ImmutableList<String> fieldNames, boolean hasNulls, ImmutableList<?> columnIds, ColoringDataIO ioHandler, String fileId)
    {
        super(name, units, numberElements, fieldNames, hasNulls, fileId);

        this.columnIds = columnIds;
        this.ioHandler = ioHandler;
        this.dataReference = new AtomicReference<>();
        this.rangeReference = new AtomicReference<>();
    }

    @Override
    protected IndexableTuple provideData()
    {
        @SuppressWarnings("unchecked")
        List<Integer> columnIdentifiers = (List<Integer>) getColumnIdentifiers();
        try
        {
            File file = fetchFile();
            
            LegacyColoringDataIO ioHandler = //SS
                    columnIdentifiers == null || columnIdentifiers.isEmpty() ? //
                            LegacyColoringDataIO.of(getName()) : //
                            LegacyColoringDataIO.of(columnIdentifiers, columnIdentifiers);
            return ioHandler.loadColoringData(file);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to load coloring data from file " + getFile(), e);
        }
    }

    @Override
    protected final AtomicReference<IndexableTuple> getDataReference()
    {
        return dataReference;
    }

    @Override
    protected final AtomicReference<double[]> getRangeReference()
    {
        return rangeReference;
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

    @Override
    protected final ColoringDataIO getIOHandler()
    {
        return ioHandler;
    }
    
    public final ImmutableList<?> getColumnIdentifiers()
    {
        return columnIds;
    }
    
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * LoadableColoringData.hashCode(this);
        result = prime * Objects.hashCode(getColumnIdentifiers());

        return result;
    }

    @Override
    public final boolean equals(Object object)
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

    // Metadata constants.
    static final Version COLORING_DATA_VERSION = Version.of(1, 1);
    static final Key<List<?>> COLUMN_IDS = Key.of("Column identifiers"); // [ "Slope" ] or [ 5, 7, 9 ]

}
