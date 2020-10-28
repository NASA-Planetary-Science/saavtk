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
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.file.IndexableTuple;

/**
 * Extension of {@link BasicColoringData} that adds an association with a
 * {@link ColoringDataIO} object that may be used to load/save coloring data
 * from/to a file. It also supports returning the properties of each instance in
 * the form of {@link Metadata}.
 * <p>
 * The additional capabilities are provided using methods that are not part of
 * the {@link ColoringData} interface, so downcasting may be necessary when
 * using this class.
 * 
 * @author James Peachey
 *
 */
public abstract class LoadableColoringData extends BasicColoringData
{

    /**
     * Create and return a {@link LoadableColoringData} instance with the specified
     * properties. The returned object will automatically load and cache the data
     * and the range the first time the data or the range are requested via either
     * the {@link #getData()} or {@link #getDefaultRange()} methods. It follows that
     * these methods might throw exceptions even if this factory method returns a
     * validly constructed instance of {@link LoadableColoringData}. The returned
     * object's {@link #clear()} method will discard and free up any loaded objects.
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
     *            coloring data file
     * @param ioHandler the {@link ColoringDataIO} object that will be used to
     *            load/save this object's coloring data from/to a file
     * @return the coloring data object
     */
    static LoadableColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, String fileId, ColoringDataIO ioHandler)
    {
        Preconditions.checkNotNull(ioHandler);

        AtomicReference<IndexableTuple> dataReference = new AtomicReference<>();
        AtomicReference<double[]> rangeReference = new AtomicReference<>();

        return new LoadableColoringData(name, units, numberElements, ImmutableList.copyOf(fieldNames), hasNulls, fileId) {

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
            protected ColoringDataIO getIOHandler()
            {
                return ioHandler;
            }

            @Override
            public int hashCode()
            {
                return LoadableColoringData.hashCode(this);
            }

            @Override
            public boolean equals(Object object)
            {
                if (this == object)
                {
                    return true;
                }

                if (object instanceof LoadableColoringData)
                {
                    return LoadableColoringData.equals(this, (LoadableColoringData) object);
                }

                return false;
            }
        };
    }

    /**
     * Create and return a {@link LoadableColoringData} instance whose properties
     * are extracted from the specified {@link Metadata} object.
     * <p>
     * The returned object will automatically load and cache the data and the range
     * the first time the data or the range are requested via either the
     * {@link #getData()} or {@link #getDefaultRange()} methods. It follows that
     * these methods might throw exceptions even if this factory method returns a
     * validly constructed instance of {@link LoadableColoringData}. The returned
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
    static LoadableColoringData of(Metadata metadata, ColoringDataIO ioHandler)
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
     * Create and return a {@link LoadableColoringData} instance whose properties
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
     * @param sourceData the input {@link LoadableColoringData} object to be
     *            (mostly) copied
     * @param fileId the identifier for the new coloring data file
     * @param ioHandler the {@link ColoringDataIO} object used to load/save the
     *            coloring data from/to disk
     * @return the coloring data object
     */
    static LoadableColoringData of(ColoringData sourceData, String fileId, ColoringDataIO ioHandler)
    {
        Preconditions.checkNotNull(sourceData);

        LoadableColoringData dest = of(sourceData.getName(), sourceData.getUnits(), sourceData.getNumberElements(), sourceData.getFieldNames(), sourceData.hasNulls(), fileId, ioHandler);

        dest.copyData(sourceData);

        return dest;
    }

    private final String name;
    private final String units;
    private final int numberElements;
    private final ImmutableList<String> fieldNames;
    private final boolean hasNulls;
    private final String fileId;

    protected LoadableColoringData(String name, String units, int numberElements, ImmutableList<String> fieldNames, boolean hasNulls, String fileId)
    {
        super();

        this.name = Preconditions.checkNotNull(name);
        this.units = Preconditions.checkNotNull(units);
        this.numberElements = numberElements;
        this.fieldNames = Preconditions.checkNotNull(fieldNames);
        this.hasNulls = hasNulls;
        this.fileId = Preconditions.checkNotNull(fileId);
    }

    public Metadata getMetadata()
    {
        SettableMetadata metadata = SettableMetadata.of(COLORING_DATA_VERSION);
        metadata.put(NAME, getName());
        metadata.put(UNITS, getUnits());
        metadata.put(NUMBER_ELEMENTS, getNumberElements());
        metadata.put(ELEMENT_NAMES, getFieldNames());
        metadata.put(HAS_NULLS, hasNulls());
        metadata.put(FILE_ID, getFileId());

        return FixedMetadata.of(metadata);
    }

    protected abstract ColoringDataIO getIOHandler();

    @Override
    protected IndexableTuple provideData()
    {
        IndexableTuple data;
        try
        {
            data = getIOHandler().loadColoringData(getFile());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return data;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getUnits()
    {
        return units;
    }

    @Override
    public int getNumberElements()
    {
        return numberElements;
    }

    @Override
    public ImmutableList<String> getFieldNames()
    {
        return fieldNames;
    }

    @Override
    public boolean hasNulls()
    {
        return hasNulls;
    }

    /**
     * Return the file identifier, that is a string that may be used to identify
     * uniquely the file containing the data associated with this plate coloring.
     * <p>
     * The file identifier is saved in the metadata, rather than the absolute path
     * to the actual file, which makes the metadata portable.
     * 
     * @return the identifier
     */
    public String getFileId()
    {
        return fileId;
    }

    /**
     * Return the {@link File} associated with the file identifier. The returned
     * object is (obviously) associated with the file system.
     * <p>
     * The base implementation uses the {@link FileCache} facility to locate and
     * download the {@link File}. It follows that if this method executes
     * successfully, the returned file most likely does exist, but nonetheless it is
     * safest to confirm with the {@link File#isFile()} method before attempting to
     * open it.
     * <p>
     * Also, note that, since this method *may* cause the file to be downloaded, it
     * may take time for a large file. Thread accordingly.
     * 
     * @return the file
     */
    public File getFile()
    {
        String fileId = getFileId();

        return FileCache.getFileFromServer(fileId);
    }

    /**
     * Save the coloring data in memory to the disk file specified by the
     * {@link #getFile()} method.
     * 
     * @throws IOException if unable to write the file.
     */
    public void save() throws IOException
    {
        getIOHandler().saveColoringData(getData(), getFile());
    }

    // Metadata constants.
    protected static final Version COLORING_DATA_VERSION = Version.of(1, 0);
    protected static final Key<String> NAME = Key.of("Coloring name"); // Slope, Gravitational Vector...
    protected static final Key<List<String>> ELEMENT_NAMES = Key.of("Element names"); // [ "Slope" ], [ "G_x", "G_y", "G_z" ]...
    protected static final Key<String> UNITS = Key.of("Coloring units"); // deg, m/s^2...
    protected static final Key<Integer> NUMBER_ELEMENTS = Key.of("Number of elements"); // 49152...
    protected static final Key<Boolean> HAS_NULLS = Key.of("Coloring has nulls"); // If true, range excludes minimum value
    protected static final Key<String> FILE_ID = Key.of("File name"); // Keep as "File name" for backward compatibility

    protected static int hashCode(LoadableColoringData data)
    {
        return Objects.hash(data.getName(), data.getUnits(), data.getNumberElements(), data.getFieldNames(), data.hasNulls(), data.getFileId());
    }

    protected static boolean equals(LoadableColoringData data1, LoadableColoringData data2)
    {
        return data1.getName().equals(data2.getName()) && //
                data1.getUnits().equals(data2.getUnits()) && //
                data1.getNumberElements() == data2.getNumberElements() && //
                data1.getFieldNames().equals(data2.getFieldNames()) && //
                data1.hasNulls() == data2.hasNulls() && //
                data1.getFileId().equals(data2.getFileId());
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(getName());
        append(builder, getUnits());
        String fileId = getFileId();
        if (fileId.matches(".*\\.[^/\\\\]*"))
        {
            String fileFormat = fileId.replaceFirst(".*[/\\\\]", "").replaceFirst("[^\\.]*\\.", "");
            fileFormat = fileFormat.replaceFirst("\\.gz$", "").toUpperCase();
            append(builder, fileFormat);
        }
        return builder.toString();
    }

    /**
     * Handler that can't actually load or save any data, but is useful internally
     * for bootstrapping a {@link LoadableColoringData} object instance.
     */
    protected static final ColoringDataIO DummyHandler = new ColoringDataIO() {

        @Override
        public IndexableTuple loadColoringData(File file)
        {
            throw new UnsupportedOperationException("Dummy IO handler cannot actually load data");
        }

        @Override
        public void saveColoringData(IndexableTuple tuples, File file)
        {
            throw new UnsupportedOperationException("Dummy IO handler cannot actually save data");
        }

    };

}
