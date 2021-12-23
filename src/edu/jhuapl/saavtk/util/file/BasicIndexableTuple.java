package edu.jhuapl.saavtk.util.file;

import crucible.core.data.list.indexable.Indexable;

/**
 * A basic abstract implementation of {@link IndexableTuple} that leaves
 * abstract the way that values are retrieved. Extend this to adapt any type of
 * double-indexed collection of doubles (double[][], List<List<Double>> etc. to
 * the {@link IndexableTuple} interface.
 * 
 * @author James Peachey
 */
public abstract class BasicIndexableTuple implements IndexableTuple
{

    /**
     * A basic abstract implementation of {@link Tuple} that leaves abstract the way
     * that values are retrieved. Intended to collaborate with its parent
     * {@link BasicIndexableTuple} implementation.
     */
    public abstract class BasicTuple implements Tuple
    {
        protected BasicTuple()
        {
            super();
        }

        /**
         * Return the value associated with field identified by the specified index.
         * This is normally called by the {{@link #get(int)} method after checking the
         * range, so implementations of this method do not need to check the range of
         * the index.
         * 
         * @param fieldIndex the index of the field to retrieve
         * @return the value of the field
         */
        protected abstract double getValue(int fieldIndex);

        @Override
        public int size()
        {
            return getNumberFields();
        }

        @Override
        public double get(int fieldIndex)
        {
            checkFieldIndex(fieldIndex);

            return getValue(fieldIndex);
        }

        /**
         * Check the specified index to ensure it is in the range of this tuple fields.
         * 
         * @param fieldIndex the index of the field to check
         * @throws IndexOutOfBoundsException if the specified index is negative or
         *             exceeds the number of fields as returned by the {{@link #size()}
         *             method
         */
        protected void checkFieldIndex(int fieldIndex)
        {
            if (fieldIndex < 0 || fieldIndex >= size())
            {
                throw new IndexOutOfBoundsException( //
                        "Cannot get value for index " + fieldIndex + //
                                "; this index must be in the half-open range [0, " + size() + ")" //
                );
            }
        }
    }

    protected BasicIndexableTuple()
    {
        super();
    }

    protected abstract Indexable<String> getNames();

    protected abstract Indexable<String> getUnits();

    /**
     * Internal method that is called by {@link #get(int)} to provide the
     * {@link Tuple} associated with the specified index. The {@link #get(int)}
     * method first checks the range, so this method may safely assume the argument
     * is in bounds.
     * 
     * @param tupleIndex the index identifying the {@link Tuple} to return
     * @return the {@link Tuple}
     */
    protected abstract Tuple createTuple(int tupleIndex);

    @Override
    public int getNumberFields()
    {
        return getNames().size();
    }

    @Override
    public String getName(int fieldIndex)
    {
        return getNames().get(fieldIndex);
    }

    @Override
    public String getUnits(int fieldIndex)
    {
        return getUnits().get(fieldIndex);
    }

    @Override
    public Tuple get(int tupleIndex)
    {
        checkTupleIndex(tupleIndex);

        return createTuple(tupleIndex);
    }

    protected void checkTupleIndex(int tupleIndex)
    {
        if (tupleIndex < 0 || tupleIndex >= size())
        {
            throw new IndexOutOfBoundsException( //
                    "Cannot get tuple for index " + tupleIndex + //
                            "; this index must be in the half-open range [0, " + size() + ")" //
            );
        }
    }

}
