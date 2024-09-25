package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.file.IndexableTuple;

class StandardColoringData extends LoadableColoringData
{
    /**
     * Create and return a {@link StandardColoringData} instance with the specified
     * properties. The returned object will automatically load and cache the data
     * and the range the first time the data or the range are requested via either
     * the {@link #getData()} or {@link #getDefaultRange()} methods. It follows that
     * these methods might throw exceptions even if this factory method returns a
     * validly constructed instance of {@link StandardColoringData}. The returned
     * object's {@link #clear()} method will discard and free up any loaded objects.
     * <p>
     * This implementation uses the {@link FileCache} facility to locate and
     * download the {@link File} based on the file identifier. It follows that if
     * this method executes successfully, the returned file most likely does exist,
     * but nonetheless it is safest to confirm with the {@link File#isFile()} method
     * before attempting to open it.
     * <p>
     * Also, note that, since this method *may* cause the file to be downloaded, it
     * may take time for a large file. Thread accordingly.
     * 
     * @param name the name of the coloring, e.g., "Slope"
     * @param units the units of the coloring, e.g., "degrees"
     * @param numberElements the number of facets/elements of the coloring, which
     *            must match the {@link PolyhedralModel} on which the coloring is
     *            displayed.
     * @param fieldNames the names of the fields within the coloring. Typically
     *            there is either one name (usually the same as the name of the
     *            coloring) for a scalar coloring, or 3 for a vector coloring. In
     *            any case, the field names should reflect the names of columns in
     *            any corresponding data files.
     * @param hasNulls if true, the minimum value in the input
     *            {@link IndexableTuple} will be treated as
     *            null/indef/NaN/undefined.
     * @param fileId the file identifier (relative path or a URL) that locates the
     *            coloring data file in the file cache
     * @param ioHandler the {@link ColoringDataIO} object that will be used to
     *            load/save this object's coloring data from/to a file
     * @return the coloring data object
     */
    static StandardColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, String fileId, ColoringDataIO ioHandler)
    {
        Preconditions.checkNotNull(ioHandler);

        return new StandardColoringData(name, units, numberElements, ImmutableList.copyOf(fieldNames), hasNulls, ioHandler, fileId) {

            @Override
            protected File fetchFile() {
                return FileCache.getFileFromServer(fileId);
            }

        };
    }

    /**
     * Create and return a {@link StandardColoringData} instance with the specified
     * properties. The returned object will automatically load and cache the data
     * and the range the first time the data or the range are requested via either
     * the {@link #getData()} or {@link #getDefaultRange()} methods. It follows that
     * these methods might throw exceptions even if this factory method returns a
     * validly constructed instance of {@link StandardColoringData}. The returned
     * object's {@link #clear()} method will discard and free up any loaded objects.
     * <p>
     * This implementation derives the file identifier from the full path of the
     * specified {@link File}. No check is performed to see if the file exists, so
     * it is safest to confirm with the {@link File#isFile()} method before
     * attempting to open it.
     * 
     * @param name the name of the coloring, e.g., "Slope"
     * @param units the units of the coloring, e.g., "degrees"
     * @param numberElements the number of facets/elements of the coloring, which
     *            must match the {@link PolyhedralModel} on which the coloring is
     *            displayed.
     * @param fieldNames the names of the fields within the coloring. Typically
     *            there is either one name (usually the same as the name of the
     *            coloring) for a scalar coloring, or 3 for a vector coloring. In
     *            any case, the field names should reflect the names of columns in
     *            any corresponding data files.
     * @param hasNulls if true, the minimum value in the input
     *            {@link IndexableTuple} will be treated as
     *            null/indef/NaN/undefined.
     * @param file the file object locating the file in the file system
     * @param ioHandler the {@link ColoringDataIO} object that will be used to
     *            load/save this object's coloring data from/to a file
     * @return the coloring data object
     */
    static StandardColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, File file, ColoringDataIO ioHandler)
    {
        Preconditions.checkNotNull(ioHandler);

        String fileId = getFileIdFromFile(file);

        return new StandardColoringData(name, units, numberElements, ImmutableList.copyOf(fieldNames), hasNulls, ioHandler, fileId);
    }

    /**
     * Create and return a {@link StandardColoringData} instance whose properties
     * are extracted from the specified {@link Metadata} object.
     * <p>
     * The returned object will automatically load and cache the data and the range
     * the first time the data or the range are requested via either the
     * {@link #getData()} or {@link #getDefaultRange()} methods. It follows that
     * these methods might throw exceptions even if this factory method returns a
     * validly constructed instance of {@link StandardColoringData}. The returned
     * object's {@link #clear()} method will discard and free up any loaded objects.
     * <p>
     * The metadata currently does not identify the file format, so this method
     * requires another argument to specify the {@link ColoringDataIO}. This could
     * be changed in the future.
     * 
     * @param metadata the metadata describing this coloring and identifying its
     *            file
     * @param ioHandler the {@link ColoringDataIO} object to be used for
     *            loading/saving this coloring object.
     * @return the coloring data object
     */
    static StandardColoringData of(Metadata metadata, ColoringDataIO ioHandler)
    {
        Preconditions.checkNotNull(metadata);

        String name = metadata.get(NAME);
        String units = metadata.get(UNITS);
        int numberElements = metadata.get(NUMBER_ELEMENTS);
        ImmutableList<String> fieldNames = ImmutableList.copyOf(metadata.get(ELEMENT_NAMES));
        boolean hasNulls = metadata.get(HAS_NULLS);
        String fileId = metadata.get(FILE_ID);

        return of(name, units, numberElements, fieldNames, hasNulls, fileId, ioHandler);
    }

    /**
     * Create and return a {@link StandardColoringData} instance whose properties
     * and data are copied from the specified input {@link ColoringData} object, and
     * augmented with the specified file identifier and {@link ColoringDataIO}
     * object. This method effectively associates a coloring that may have been
     * created on the fly with a file on disk. If the provided source coloring
     * already associates with a file on disk, the new object will be "re-pointed"
     * to the specified file identifier.
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
     * @param sourceData the input {@link StandardColoringData} object to be
     *            (mostly) copied
     * @param fileId the identifier for the new coloring data file
     * @param ioHandler the {@link ColoringDataIO} object used to load/save the
     *            coloring data from/to disk
     * @return the coloring data object
     */
    static StandardColoringData of(ColoringData sourceData, String fileId, ColoringDataIO ioHandler)
    {
        Preconditions.checkNotNull(sourceData);

        StandardColoringData dest = of(sourceData.getName(), sourceData.getUnits(), sourceData.getNumberElements(), sourceData.getFieldNames(), sourceData.hasNulls(), fileId, ioHandler);

        dest.copyData(sourceData);

        return dest;
    }

    /**
     * Create and return a {@link StandardColoringData} instance whose properties
     * and data are copied from the specified input {@link ColoringData} object, and
     * augmented with the specified file identifier and {@link ColoringDataIO}
     * object. This method effectively associates a coloring that may have been
     * created on the fly with a file on disk. If the provided source coloring
     * already associates with a file on disk, the new object will be "re-pointed"
     * to the specified file identifier.
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
     * @param sourceData the input {@link StandardColoringData} object to be
     *            (mostly) copied
     * @param file the file to use for loading/saving operations. The file
     *            identifier is derived from the absolute path of the file.
     * @param ioHandler the {@link ColoringDataIO} object used to load/save the
     *            coloring data from/to disk
     * @return the coloring data object
     */
    static StandardColoringData of(ColoringData sourceData, File file, ColoringDataIO ioHandler)
    {
        Preconditions.checkNotNull(sourceData);

        StandardColoringData dest = of(sourceData.getName(), sourceData.getUnits(), sourceData.getNumberElements(), sourceData.getFieldNames(), sourceData.hasNulls(), file, ioHandler);

        dest.copyData(sourceData);

        return dest;
    }

    private final ColoringDataIO ioHandler;
    private final AtomicReference<IndexableTuple> dataReference;
    private final AtomicReference<double[]> rangeReference;

    protected StandardColoringData(String name, String units, int numberElements, ImmutableList<String> fieldNames, boolean hasNulls, ColoringDataIO ioHandler, String fileId)
    {
        super(name, units, numberElements, fieldNames, hasNulls, fileId);

        this.ioHandler = ioHandler;
        this.dataReference = new AtomicReference<>();
        this.rangeReference = new AtomicReference<>();
    }

    @Override
    protected final ColoringDataIO getIOHandler()
    {
        return ioHandler;
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
    public final int hashCode()
    {
        return LoadableColoringData.hashCode(this);
    }

    @Override
    public final boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }

        if (object instanceof StandardColoringData)
        {
            return LoadableColoringData.equals(this, (LoadableColoringData) object);
        }

        return false;
    }
}
