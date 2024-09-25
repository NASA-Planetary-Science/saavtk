package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;

import com.google.common.base.Preconditions;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.saavtk.util.file.IndexableTuple;
import vtk.vtkDataArray;

/**
 * All-static class with methods for creating various implementations of
 * {@link ColoringData} and {@link LoadableColoringData}. Except as noted below,
 * for all factory methods that return {@link ColoringData}, the following are
 * true:
 * <ul>
 * <li>The returned instance will have loaded its data already in the form of an
 * {@link IndexableTuple}. The {@link ColoringData#clear()} method will not do
 * anything, and there is no way to "reload" the data.
 * <li>The returned instance may not directly be loaded or saved by a
 * {@link ColoringDataIO} object, nor may it be represented as metadata. As-is,
 * it is suitable only for use within a running program. However, the object can
 * be enhanced to make it loadable/savable, and/or represented as metadata by
 * other classes, using other factory methods (see below).
 * <li>If a factory method finishes execution and returns an instance, all
 * methods of that instance should execute successfully, as described for
 * {@link ColoringData}.
 * </ul>
 * Except as noted below, for all factory methods that return
 * {@link LoadableColoringData}, the following are true:
 * <ul>
 * <li>The returned instance will not have loaded its data. When the data are
 * first accessed using either {@link ColoringData#getData()} or
 * {@link ColoringData#getDefaultRange()}, the coloring object will attempt to
 * load the data from its associated file. The {@link ColoringData#clear()}
 * method will discard any loaded data and delete any associated
 * {@link vtkDataArray} objects. The first subsequent call that accesses the
 * data will cause the data to be reloaded.
 * <li>In keeping with the point above, the returned instance will also not
 * automatically save its data to its associated file.
 * <li>The returned instance may be saved to its file. Calling the save method
 * multiple times will result in multiple writes to that file. Existing copies
 * of the file will be overwritten without warning. The returned instance will
 * only load its data once, the first time it is needed, unless the
 * {@link ColoringData#clear()} method is called; after that the data would be
 * reloaded.
 * <li>The {@link LoadableColoringData#getMetadata()} method will return
 * metadata that should suffice to recreate the coloring data object. However,
 * the metadata currently does not include the file format, so it may be
 * necessary to specify how to load coloring data by specifying a
 * {@link ColoringDataIO} I/O handler object.
 * <li>If a factory method finishes execution and returns an instance, all
 * methods of that instance that do not access the actual coloring data should
 * execute successfully, but any attempt to access the coloring data may throw
 * an exception if the data cannot be loaded.
 * <p>
 * None of the methods is thread-safe currently, but they could be made thread
 * safe fairly easily.
 * </ul>
 * 
 * @author James Peachey
 */
public class ColoringDataFactory
{

    /**
     * Create a {@link ColoringData} object from {@link IndexableTuple} data and
     * other specified properties.
     * <p>
     * See the description of {@link ColoringDataFactory} for further details about
     * the properties of the returned instance.
     * 
     * @param name
     * @param units
     * @param numberElements
     * @param fieldNames
     * @param hasNulls
     * @param tuples the coloring data as an {@link IndexableTuple}
     * @return
     */
    public static ColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, IndexableTuple tuples)
    {
        return BasicColoringData.of(name, units, numberElements, fieldNames, hasNulls, tuples);
    }

    /**
     * Create a {@link ColoringData} object from {@link vtkDataArray} data and other
     * specified properties.
     * <p>
     * See the description of {@link ColoringDataFactory} for further details about
     * the properties of the returned instance.
     * 
     * @param name
     * @param units
     * @param numberElements
     * @param fieldNames
     * @param hasNulls
     * @param vtkArray the coloring data as a {@link vtkDataArray}
     * @return
     */
    public static ColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, vtkDataArray vtkArray)
    {
        IndexableTuple tuples = ColoringDataUtils.createIndexableFromVtkArray(vtkArray);

        return BasicColoringData.of(name, units, numberElements, fieldNames, hasNulls, tuples);
    }

    /**
     * Create a {@link LoadableColoringData} object that will auto-load its data
     * from a disk file, choosing columns automatically based on conventions
     * established for Osiris-REx coloring FITS files.
     * <p>
     * See the description of {@link ColoringDataFactory} for further details about
     * the properties of the returned instance.
     * 
     * @param name
     * @param units
     * @param numberElements
     * @param fieldNames
     * @param hasNulls
     * @param fileId
     * @return
     */
    public static LoadableColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, String fileId)
    {
        Iterable<?> columnIdentifiers = null;

        return LegacyColoringData.of(name, units, numberElements, fieldNames, hasNulls, fileId, columnIdentifiers);
    }

    /**
     * Create a {@link LoadableColoringData} object that will auto-load its data
     * from a disk file, using explicitly specified column identifiers (currently
     * these must be integer column numbers).
     * <p>
     * See the description of {@link ColoringDataFactory} for further details about
     * the properties of the returned instance.
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
    public static LoadableColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, String fileId, Iterable<?> columnIdentifiers)
    {
        Preconditions.checkNotNull(columnIdentifiers);

        return LegacyColoringData.of(name, units, numberElements, fieldNames, hasNulls, fileId, columnIdentifiers);
    }

    /**
     * Return a {@link LoadableColoringData} object that was created from metadata.
     * <p>
     * This method returns a "legacy" implementation of
     * {@link LoadableColoringData}. If the metadata does not specify column
     * identifiers, or if the column identifiers field is null, columns will be
     * chosen automatically based on conventions established for Osiris-REx coloring
     * FITS files.
     * <p>
     * See the description of {@link ColoringDataFactory} for further details about
     * the properties of the returned instance.
     * 
     * @param metadata
     * @return
     */
    public static LoadableColoringData of(Metadata metadata)
    {
        return LegacyColoringData.of(metadata);
    }

    /**
     * Return a {@link LoadableColoringData} object that was created by copying
     * properties and/or data from the specified source data object, overriding only
     * the location of the file used to load/save data. For all types of input
     * coloring data, this method <b>DOES</b> force any data on disk to be loaded by
     * the source data object. However, this method does <b>NOT</b> automatically
     * save the copied data on behalf of the returned instance.
     * <p>
     * This method tries to come as close as it can to creating an object that
     * behaves just like the source object, except that it is guaranteed to support
     * load/save operations and points to the specified file identifier.
     * <p>
     * See the description of {@link ColoringDataFactory} for further details about
     * the properties of the returned instance.
     * 
     * @param sourceData
     * @param fileId
     * @return
     */
    public static LoadableColoringData of(ColoringData sourceData, String fileId)
    {
        Preconditions.checkNotNull(sourceData);
        Preconditions.checkNotNull(fileId);

        if (sourceData instanceof LegacyColoringData)
        {
            return LegacyColoringData.of(sourceData, fileId, ((LegacyColoringData) sourceData).getColumnIdentifiers());
        }
        else if (sourceData instanceof LoadableColoringData)
        {
            return StandardColoringData.of(sourceData, fileId, ((LoadableColoringData) sourceData).getIOHandler());
        }
        else
        {
            return StandardColoringData.of(sourceData, fileId, LegacyColoringDataIO.of(sourceData.getName()));
        }
    }

    public static LoadableColoringData of(ColoringData sourceData, String fileId, Iterable<?> columnIdentifiers)
    {
        Preconditions.checkNotNull(sourceData);
        Preconditions.checkNotNull(fileId);
        Preconditions.checkNotNull(columnIdentifiers);

        return LegacyColoringData.of(sourceData, fileId, columnIdentifiers);
    }

    /**
     * 
     * @param sourceData
     * @param file
     * @return
     */
    public static LoadableColoringData of(ColoringData sourceData, File file)
    {
        Preconditions.checkNotNull(sourceData);
        Preconditions.checkNotNull(file);

        if (sourceData instanceof LegacyColoringData)
        {
            return LegacyColoringData.of(sourceData, file, ((LegacyColoringData) sourceData).getColumnIdentifiers());
        }
        else if (sourceData instanceof LoadableColoringData)
        {
            return StandardColoringData.of(sourceData, file, ((LoadableColoringData) sourceData).getIOHandler());
        }
        else
        {
            return StandardColoringData.of(sourceData, file, LegacyColoringDataIO.of(sourceData.getName()));
        }
    }

    static Metadata getMetadata(ColoringData data)
    {
        Preconditions.checkArgument(data instanceof LoadableColoringData, "Cannot get metadata from non-serializable coloring data object");

        return ((LoadableColoringData) data).getMetadata();
    }

    private ColoringDataFactory()
    {
        throw new AssertionError("This class is pure static");
    }
}
