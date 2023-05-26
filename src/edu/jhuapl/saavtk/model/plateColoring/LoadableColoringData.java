package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;
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

    @Override
    protected IndexableTuple provideData()
    {
        IndexableTuple data;
        try
        {
            data = getIOHandler().loadColoringData(fetchFile());
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

    /**
     * Return the file identifier, that is a string that may be used to identify
     * uniquely the file containing the data associated with this plate coloring.
     * <p>
     * The file identifier is saved in the metadata, rather than the absolute path
     * to the actual file, which makes the metadata portable.
     * 
     * @return the identifier
     */
    public final String getFileId()
    {
        return fileId;
    }

    /**
     * Return the {@link File} associated with the file identifier. This is final to
     * make sure the {@link FileCache} provides the {@link File} object based on the
     * file identifier string. This is necessary even for files that do not
     * originate on the server so that UI components can listen for changes in file
     * accessibility.
     * <p>
     * This only gets the file object; it does not download/create/check for
     * existence etc.
     * 
     * @return the file
     */
    public final File getFile()
    {
        String fileId = getFileId();

        // This method just determines what file would be downloaded, without actually
        // downloading it.
        return FileCache.getState(fileId).getFileState().getFile();
    }

    /**
     * If possible and necessary, ensure the existence of the file required to load
     * the data. Override this to get the file from the cache, for example. The base
     * implementation is a no-op, and in general, despite the name of the method,
     * there is no guarantee that the file returned exists.
     * 
     * @throws IOException if an IO error prevents obtaining the file.
     */
    protected File fetchFile() throws IOException
    {
        return getFile();
    }

    /**
     * Save the coloring data in memory to the disk file specified by the
     * {@link #getFile()} method. Force the file cache to update after a successful
     * save operation.
     * 
     * @throws IOException if unable to write the file.
     */
    public final void save() throws IOException
    {
        getIOHandler().saveColoringData(getData(), getFile());

        FileCache.refreshStateInfo(getFileId());
    }

    protected static String getFileIdFromFile(File file)
    {
        try
        {
            return file.toURI().toURL().toString();
        }
        catch (MalformedURLException e)
        {
            // This really shouldn't happen.
            throw new AssertionError("Unable to create a URL from file named " + file, e);
        }
    }

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

    // Metadata constants.
    protected static final Version COLORING_DATA_VERSION = Version.of(1, 0);
    protected static final Key<String> NAME = Key.of("Coloring name"); // Slope, Gravitational Vector...
    protected static final Key<List<String>> ELEMENT_NAMES = Key.of("Element names"); // [ "Slope" ], [ "G_x", "G_y", "G_z" ]...
    protected static final Key<String> UNITS = Key.of("Coloring units"); // deg, m/s^2...
    protected static final Key<Integer> NUMBER_ELEMENTS = Key.of("Number of elements"); // 49152...
    protected static final Key<Boolean> HAS_NULLS = Key.of("Coloring has nulls"); // If true, range excludes minimum value
    protected static final Key<String> FILE_ID = Key.of("File name"); // Keep as "File name" for backward compatibility

}
