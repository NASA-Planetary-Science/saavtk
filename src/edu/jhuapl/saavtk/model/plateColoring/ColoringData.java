package edu.jhuapl.saavtk.model.plateColoring;

import java.util.List;

import edu.jhuapl.saavtk.util.file.IndexableTuple;

/**
 * General interface to coloring data.
 * 
 * @author James Peachey
 *
 */
public interface ColoringData
{
    /**
     * Return the name of this coloring, used to identify and label the coloring.
     * Implementations may not return a null or blank string.
     * 
     * @return the name
     */
    String getName();

    /**
     * Return a string providing the units of the coloring data, used for labeling
     * the coloring. All colorings in the tuple are required to have the same units.
     * Implementations may not return a null string.
     * 
     * @return the units
     */
    String getUnits();

    /**
     * Return the number of elements (facets) in the coloring. Implementations must
     * return an integer greater than 0. For a coloring to be used successfully, it
     * must have the same number of facets as the model it is coloring.
     * 
     * @return the number of elements (facets)
     */
    int getNumberElements();

    /**
     * Return a list of strings identifying the fields associated with each element
     * (facet). For scalar-valued coloring data, this is typically a list with a
     * single string equal to the name of the coloring returned by
     * {@link #getName()}. For higher order colorings these names typically reflect
     * column titles in a coloring data file. For example, for 3-d vector data this
     * would give the names of the (X, Y, Z) columns.
     * <p>
     * Implementations may not return null, nor an empty list. The number of
     * elements must match the rank of the data expected to be returned by the
     * {@link #getData()} method.
     * 
     * @return the list of field names
     */
    List<String> getFieldNames();

    /**
     * Return a flag indicating whether the coloring might contain
     * invalid/null-valued data.
     * 
     * @return true if any of the data in the returned data array may contain a null
     *         value, false otherwise.
     */
    boolean hasNulls();

    /**
     * Return the coloring data as an {@link IndexableTuple}. Implementations must
     * return a valid data object with the same number of tuples as the number of
     * elements returned by the {@link #getNumberElements()} method.
     * 
     * @return the coloring data
     */
    IndexableTuple getData();

    /**
     * Return the default/intrinsic/maximum range of data values. Implementations
     * must return a 2-element array in ascending order.
     * 
     * @return the default data range
     */
    double[] getDefaultRange();

    /**
     * For colorings with a serialized form, implementations of this method *may*
     * discard any/all data asscoiated with this coloring, allowing resident
     * resources to be garbage-collected. Subsequenct calls to {@link #getData()}
     * would require the data to be reloaded. For colorings that have no serialized
     * form, this method should have no effect.
     */
    void clear();

}
