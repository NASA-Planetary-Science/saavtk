package edu.jhuapl.saavtk.util.file;

/**
 * Encapsulates the idea of a tuple of scalar field values, accessible
 * individually or retrievable as an array. This is similar to
 * {@link Indexable<Double>} but does not extend it so it can return little-d
 * doubles.
 * 
 * @author James Peachey
 *
 */
public interface Tuple
{
    /**
     * Return the size of this tuple (the number of fields).
     * 
     * @return the number of fields
     */
    int size();

    /**
     * Return the value held in the field identified by the specified index.
     * 
     * @param fieldIndex the index
     * @return the value of the field
     * @throws IndexOutOfBoundsException if the specified index is outside the
     *             half-open range [0, numberFields), where numberFields is the
     *             value returned by the {{@link #size()} method.
     */
    double get(int fieldIndex);

    /**
     * Return the whole tuple (all fields) as an array of double values. The default
     * implementation creates a new array, then calls the {{@link #get(int)} method
     * repeatedly to get the values. Consider overriding this when extending this
     * class, as a more efficient option may be possible depending on the underlying
     * concrete data structures.
     * <p>
     * Although the default implementation creates a new array, another
     * implementation might override this to return internal data for efficiency, so
     * it is recommended that callers do not modify the returned array.
     * 
     * @return the array of tuple field values
     */
    default double[] get()
    {
        int size = size();
        double[] array = new double[size];
        for (int index = 0; index < size; ++index)
        {
            array[index] = get(index);
        }

        return array;
    }
}
