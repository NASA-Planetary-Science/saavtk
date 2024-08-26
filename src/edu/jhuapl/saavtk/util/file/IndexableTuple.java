package edu.jhuapl.saavtk.util.file;

/**
 * Extends {@link Indexable}<{@link Tuple}> and adds methods to get the number
 * of fields in each {@link Tuple} as well as the names of the fields and their
 * units. Each {@link Tuple} is assumed to have the same number of fields, each
 * of which is assumed to have the properties given by the methods on this
 * interface.
 * <p>
 * Because the interface is by nature a two-step look-up: first get the
 * {@link Tuple}, then get the value within the {@link Tuple}, the most
 * efficient implementation would generally be one that iterates over the
 * {@link IndexableTuple} first, calls the {{@link #get(int)} once for each
 * tuple index, and then processes all the fields in each {@link Tuple}.
 * 
 * @author James Peachey
 */
public interface IndexableTuple extends DataObject, Indexable<Tuple>
{
    /**
     * Return the number of fields in each {@link Tuple}. Must be non-negative.
     * 
     * @return the number of fields
     */
    int getNumberFields();

    /**
     * Return the name of the field associated with the specified index.
     * Implementations must return a non-empty string for each field.
     * 
     * @param fieldIndex the field index
     * @return the name of the field
     * @throws IndexOutOfBoundsException if the specified index is outside the
     *             half-open range [0, numberFields), where numberFields is the
     *             value returned by the {{@link #getNumberFields()} method.
     */
    String getName(int fieldIndex);

    /**
     * Return the units of the field associated with the specified index.
     * Implementations must return a non-null string for each field (but the string
     * may be empty).
     * 
     * @param fieldIndex the field index
     * @return the units of the field
     * @throws IndexOutOfBoundsException if the specified index is outside the
     *             half-open range [0, numberFields), where numberFields is the
     *             value returned by the {{@link #getNumberFields()} method.
     */
    String getUnits(int fieldIndex);

    /**
     * Return the size of (the number of {@link Tuple}s in) this collection.
     */
    @Override
    int size();

    /**
     * Return the {@link Tuple} associated with the specified index.
     * 
     * @throws IndexOutOfBoundsException if the specified index is outside the
     *             half-open range [0, numberTuples), where numberTuples is the
     *             value returned by the {{@link #size()} method.
     */
    @Override
    Tuple get(int tupleIndex);

}
