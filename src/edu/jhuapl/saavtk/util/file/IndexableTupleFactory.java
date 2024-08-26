package edu.jhuapl.saavtk.util.file;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;

/**
 * Factory that adapts input collection-or-array-type objects to provide
 * implmentations of {@link IndexableTuple}. This class has several method
 * overrides for creating {@link IndexableTuple} objects conveniently with or
 * without a set of explicit names/units for the fields encapsulated by the
 * {@link Tuple}s.
 * 
 * @author James Peachey
 */
public class IndexableTupleFactory
{

    /**
     * Return a factory that treats the specified 2-dimensional array as a table
     * with a single row of columns.
     * 
     * @param rowOfColumns the input table/array
     * @return a factory that can adapt this table to create an
     *         {@link IndexableTuple}
     */
    public static IndexableTupleFactory ofColumns(double[][] rowOfColumns)
    {
        Preconditions.checkNotNull(rowOfColumns);

        int numberTuples = rowOfColumns.length > 0 ? rowOfColumns[0].length : 0;
        int numberFields = rowOfColumns.length;

        return new IndexableTupleFactory(numberTuples, numberFields, (tupleIndex, fieldIndex) -> {
            return rowOfColumns[fieldIndex][tupleIndex];
        });
    }

    /**
     * Return a factory that treats the specified 2-dimensional array as a table
     * with a single column of rows.
     * <p>
     * The {@link IndexableTuple} created by this factory method should provide the
     * fastest overall performance, because 1) it does not need to auto-box/unbox
     * big-D Doubles and 2) it does not need to copy the tuple array when returning
     * values. However, this means it is possible to modify the created object's
     * internal data by modifying the array returned by the {@link Tuple#get()}
     * method.
     * 
     * @param rowOfColumns the input table/array
     * @return a factory that can adapt this table to create an
     *         {@link IndexableTuple}
     */
    public static IndexableTupleFactory ofRows(double[][] columnOfRows)
    {
        Preconditions.checkNotNull(columnOfRows);

        int numberTuples = columnOfRows.length;
        int numberFields = columnOfRows.length > 0 ? columnOfRows[0].length : 0;

        return new IndexableTupleFactory(numberTuples, numberFields, (tupleIndex, fieldIndex) -> {
            return columnOfRows[tupleIndex][fieldIndex];
        }) {
            @Override
            public IndexableTuple create(Indexable<String> names, Indexable<String> units)
            {
                check(names, units);

                return new StandardIndexableTuple(names, units) {

                    @Override
                    protected Tuple createTuple(int tupleIndex)
                    {
                        return new StandardTuple(tupleIndex) {

                            @Override
                            public double[] get()
                            {
                                return columnOfRows[tupleIndex];
                            }
                        };
                    }
                };
            }
        };
    }

    /**
     * Return a factory that treats the specified {@link List}<{@link List}<Double>>
     * as a table with a single row of columns.
     * 
     * @param rowOfColumns the input table (list-of-lists)
     * @return a factory that can adapt this table to create an
     *         {@link IndexableTuple}
     */
    public static IndexableTupleFactory ofColumns(List<? extends List<Double>> rowOfColumns)
    {
        Preconditions.checkNotNull(rowOfColumns);

        int numberTuples = rowOfColumns.size() > 0 ? rowOfColumns.get(0).size() : 0;
        int numberFields = rowOfColumns.size();

        return new IndexableTupleFactory(numberTuples, numberFields, (tupleIndex, fieldIndex) -> {
            return rowOfColumns.get(fieldIndex).get(tupleIndex);
        });
    }

    /**
     * Return a factory that treats the specified {@link List}<{@link List}<Double>>
     * as a table with a single column of rows.
     * 
     * @param rowOfColumns the input table (list-of-lists)
     * @return a factory that can adapt this table to create an
     *         {@link IndexableTuple}
     */
    public static IndexableTupleFactory ofRows(List<? extends List<Double>> columnOfRows)
    {
        Preconditions.checkNotNull(columnOfRows);

        int numberTuples = columnOfRows.size();
        int numberFields = columnOfRows.size() > 0 ? columnOfRows.get(0).size() : 0;

        return new IndexableTupleFactory(numberTuples, numberFields, (tupleIndex, fieldIndex) -> {
            return columnOfRows.get(tupleIndex).get(fieldIndex);
        }) {
            @Override
            public IndexableTuple create(Indexable<String> names, Indexable<String> units)
            {
                check(names, units);

                return new StandardIndexableTuple(names, units) {

                    @Override
                    protected Tuple createTuple(int tupleIndex)
                    {
                        return new StandardTuple(tupleIndex) {

                            @Override
                            public double[] get()
                            {
                                return Doubles.toArray(columnOfRows.get(tupleIndex));
                            }
                        };
                    }
                };
            }

        };
    }

    protected final int numberTuples;
    protected final int numberFields;
    protected final FunctionalLookup lookup;

    protected IndexableTupleFactory(int numberTuples, int numberFields, FunctionalLookup lookup)
    {
        this.numberTuples = numberTuples;
        this.numberFields = numberFields;
        this.lookup = lookup;
    }

    /**
     * Create an {@link IndexableTuple} whose field names are simply the index of
     * each field converted to a string, and whose units are all blank strings.
     * <p>
     * The number of fields in each {@link Tuple} and the number of {@link Tuple}s
     * are determined completely by the dimensions of the input data object that was
     * provided to the factory's constructor.
     * 
     * @return the {@link IndexableTuple}
     */
    public IndexableTuple create()
    {
        return create(indexToString(numberFields), allBlank(numberFields));
    }

    /**
     * Create an {@link IndexableTuple} whose field names are specified, and whose
     * units are all blank strings.
     * <p>
     * The number of fields in each {@link Tuple} must agree with the number of
     * names specified. The number of {@link Tuple}s isdetermined completely by the
     * dimensions of the input data object that was provided to the factory's
     * constructor.
     * 
     * @param names the names for the fields
     * @return the {@link IndexableTuple}
     */
    public IndexableTuple create(Indexable<String> names)
    {
        return create(names, allBlank(numberFields));
    }

    /**
     * Create an {@link IndexableTuple} whose field names and units are specified.
     * <p>
     * The number of fields in each {@link Tuple} must agree with the number of
     * names and units specified. The number of {@link Tuple}s isdetermined
     * completely by the dimensions of the input data object that was provided to
     * the factory's constructor.
     * 
     * @param names the names for the fields
     * @return the {@link IndexableTuple}
     */
    public IndexableTuple create(Indexable<String> names, Indexable<String> units)
    {
        check(names, units);

        return new StandardIndexableTuple(names, units);
    }

    protected void check(Indexable<String> names, Indexable<String> units)
    {
        Preconditions.checkNotNull(names);
        Preconditions.checkNotNull(units);
        Preconditions.checkArgument(names.size() == units.size());
        Preconditions.checkArgument(names.size() == numberFields);
    }

    /**
     * Make an all-blank {@link Indexable} of the specified size.
     * 
     * @param size number of elements in the output indexable
     * @return the {@link Indexable}
     */
    protected Indexable<String> allBlank(int size)
    {
        return new Indexable<String>() {

            @Override
            public int size()
            {
                return size;
            }

            @Override
            public String get(int tupleIndex)
            {
                if (tupleIndex < 0 || tupleIndex >= size)
                {
                    throw new IndexOutOfBoundsException();
                }

                return "";
            }

        };
    }

    /**
     * Make an {@link Indexable} of the specified size in which each indexed string
     * just contains that index converted to a string.
     * 
     * @param size number of elements in the output indexable
     * @return the {@link Indexable}
     */
    protected Indexable<String> indexToString(int size)
    {
        return new Indexable<String>() {

            @Override
            public int size()
            {
                return size;
            }

            @Override
            public String get(int tupleIndex)
            {
                if (tupleIndex < 0 || tupleIndex >= size)
                {
                    throw new IndexOutOfBoundsException();
                }

                return Integer.toString(tupleIndex);
            }

        };
    }

    /**
     * Internal interface that encapsulates the look-up of the value of one field in
     * a {@link Tuple}. Used by other internals to simplify factory creation.
     */
    @FunctionalInterface
    protected interface FunctionalLookup
    {
        /**
         * Return the value associated with the specified field and {@link Tuple}.
         * 
         * @param tupleIndex the index identifying the {@link Tuple} within the
         *            {@link IndexableTuple}
         * @param fieldIndex the index identifying the field within the {@link Tuple}
         * @return the value
         * @throws IndexOutOfBoundsException if either index is out of bounds. See the
         *             documentation for {@link IndexableTuple} for details about the
         *             index bounds.
         */
        double get(int tupleIndex, int fieldIndex);
    }

    /**
     * Extension of {@link BasicIndexableTuple} that uses {@link StandardTuple} in
     * the base implementation.
     */
    protected class StandardIndexableTuple extends BasicIndexableTuple
    {

        /**
         * Extension of {@link BasicTuple} that uses {@link FunctionalLookup} to provide
         * values for its {@link Tuple}s.
         */
        protected class StandardTuple extends BasicTuple
        {
            protected final int tupleIndex;

            protected StandardTuple(int tupleIndex)
            {
                this.tupleIndex = tupleIndex;

            }

            @Override
            protected double getValue(int fieldIndex)
            {
                return lookup.get(tupleIndex, fieldIndex);
            }

        }

        protected final Indexable<String> names;
        protected final Indexable<String> units;

        protected StandardIndexableTuple(Indexable<String> names, Indexable<String> units)
        {
            super();

            this.names = names;
            this.units = units;

        }

        @Override
        public int size()
        {
            return numberTuples;
        }

        @Override
        protected Indexable<String> getNames()
        {
            return names;
        }

        @Override
        protected Indexable<String> getUnits()
        {
            return units;
        }

        /**
         * This method may be overridden to provide more efficient implementations,
         * depending on the underlying data structure being adapted by this
         * {@link IndexableTupleFactory}.
         * <p>
         * {@inheritDoc}
         */
        @Override
        protected Tuple createTuple(int tupleIndex)
        {
            return new StandardTuple(tupleIndex);
        }

    }

    public static void main(String[] args)
    {
        double[][] array = new double[][] { //
                new double[] { 0., 1., 2., 3. }, //
                new double[] { 4., 5., 6., 7. }, //
                new double[] { 8., 9., 10., 11. }
        };

        ImmutableList<ImmutableList<Double>> list = ImmutableList.of( //
                ImmutableList.of(0., 1., 2., 3.), //
                ImmutableList.of(4., 5., 6., 7.), //
                ImmutableList.of(8., 9., 10., 11.) //
        );

        printTuples(ofRows(array).create());
        System.err.println();
        printTuples(ofColumns(array).create());

        System.err.println();

        printTuples(ofRows(list).create());
        System.err.println();
        printTuples(ofColumns(list).create());
    }

    protected static void printTuples(IndexableTuple tuples)
    {
        String delimiter = "";
        for (int fieldIndex = 0; fieldIndex < tuples.getNumberFields(); ++fieldIndex)
        {
            System.err.print(delimiter);
            delimiter = ",\t";

            System.err.print(tuples.getName(fieldIndex));
            System.err.print(" (");
            System.err.print(tuples.getUnits(fieldIndex));
            System.err.print(")");

        }
        System.err.println();

        for (int tupleIndex = 0; tupleIndex < tuples.size(); ++tupleIndex)
        {
            delimiter = "";
            for (int fieldIndex = 0; fieldIndex < tuples.getNumberFields(); ++fieldIndex)
            {
                System.err.print(delimiter);
                delimiter = ",\t";

                System.err.print(tuples.get(tupleIndex).get(fieldIndex));

            }

            System.err.println();
        }
    }

}
