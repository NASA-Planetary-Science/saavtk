package edu.jhuapl.saavtk.model.plateColoring;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.file.IndexableTuple;
import vtk.vtkDataArray;

/**
 * A basic abstract implementation of {@link ColoringData} that uses
 * {@link AtomicReference}s to cache its data in the form of a
 * format-independent {@link IndexableTuple}. The data's range is similarly
 * cached.
 * <p>
 * This class also adds an abstract method {{@link #provideData()}, through
 * which implementations may provide the data that is cached. In this way, the
 * data may be, for example, generated on the fly or loaded from a file.
 * <p>
 * When extending this class, please also review (and possibly reimplement) the
 * method {@link #clear()}.
 * 
 * @author James Peachey
 *
 */
public abstract class BasicColoringData implements ColoringData
{
    protected static final float VtkMinFloatValue = -1.0e38f;
    protected static final float VtkMaxFloatValue = 1.0e38f;

    /**
     * Create and return a {@link BasicColoringData} instance with the specified
     * properties. The returned object permanently caches the specified
     * {@link IndexableTuple} argument. The returned object's {@link #clear()}
     * method has no effect.
     * <p>
     * The returned instance may not be loaded or saved by a {@link ColoringDataIO}
     * object, nor may it be represented as metadata. As-is, it is suitable only for
     * use within a running program. However, the object can be enhanced to make it
     * loadable/savable, and/or represented as metadata by other classes. See
     * {@link LoadableColoringData} and classes that use it for more information.
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
     * @param tuples the coloring data in {@link IndexableTuple} form
     * @return the coloring data object in {@link BasicColoringData} form
     */
    static BasicColoringData of(String name, String units, int numberElements, Iterable<String> fieldNames, boolean hasNulls, IndexableTuple tuples)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(units);
        Preconditions.checkNotNull(fieldNames);

        ImmutableList<String> fieldNameList = ImmutableList.copyOf(fieldNames);

        checkArguments(tuples, numberElements, fieldNameList.size());

        AtomicReference<IndexableTuple> dataReference = new AtomicReference<>();
        AtomicReference<double[]> rangeReference = new AtomicReference<>();

        return new BasicColoringData() {

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
            public List<String> getFieldNames()
            {
                return fieldNameList;
            }

            @Override
            public boolean hasNulls()
            {
                return hasNulls;
            }

            /**
             * This implementation has no associated file or other means to retrieve the
             * data after a clear operation, so override this to take no action.
             */
            @Override
            public void clear()
            {

            }

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
            protected IndexableTuple provideData()
            {
                return tuples;
            }

            @Override
            public int hashCode()
            {
                return hashCode(this);
            }

            @Override
            public boolean equals(Object object)
            {
                if (this == object)
                {
                    return true;
                }

                if (object instanceof BasicColoringData)
                {
                    return equals(this, (BasicColoringData) object);
                }

                return false;
            }
        };
    }

    /**
     * Create and return a {@link BasicColoringData} instance that is copied from a
     * source {@link ColoringData} object. Most properties from the source object
     * are simply reused, but the source object's {@link IndexableTuple} data
     * returned by its {@link ColoringData#getData()} method is copied. Thus, this
     * method will force the source object to load its data if it hasn't already.
     * The new instance will use this copy and recompute the range.
     * <p>
     * The returned instance may not be loaded or saved by a {@link ColoringDataIO}
     * object, nor may it be represented as metadata. As-is, it is suitable only for
     * use within a running program. However, the object can be enhanced to make it
     * loadable/savable, and/or represented as metadata by other classes. See
     * {@link LoadableColoringData} and classes that use it for more information.
     * 
     * @param sourceData the input source {@link ColoringData} object
     * @return the copied {@link BasicColoringData} object.
     */
    static BasicColoringData of(ColoringData sourceData)
    {
        Preconditions.checkNotNull(sourceData);
        IndexableTuple destTuples = ColoringDataUtils.copy(sourceData.getData());

        return of(sourceData.getName(), sourceData.getUnits(), sourceData.getNumberElements(), sourceData.getFieldNames(), sourceData.hasNulls(), destTuples);
    }

    protected BasicColoringData()
    {
        super();
    }

    protected abstract AtomicReference<IndexableTuple> getDataReference();

    protected abstract AtomicReference<double[]> getRangeReference();

    protected abstract IndexableTuple provideData();

    /**
     * Call the {@link #load()} method to load the data and compute their range,
     * then extract and return the data.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public IndexableTuple getData()
    {
        load();

        return getDataReference().get();
    }

    /**
     * Call the {@link #load()} method to load the data and compute their range,
     * then extract and return the data's range.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public double[] getDefaultRange()
    {
        load();

        return getRangeReference().get();
    }

    /**
     * The base implementation does reset the cached values for the data and data
     * range. In addition, if the data ({@link IndexableTuple}) is backed by a
     * {@link vtkDataArray}, that array will be deleted using its
     * {@link vtkObject#Delete()} method.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        IndexableTuple data = getDataReference().getAndSet(null);
        getRangeReference().set(null);
        ColoringDataUtils.deleteVtkData(data);
    }

    /**
     * Call the {@link #provideData()} method as needed to provide the
     * {@link IndexableTuple}. The base implementation also computes the data range
     * and caches both the data and their range. It is safe to call the base
     * implementation any number of times. It will only actually do anything the
     * first time it is called, or the first time it is called after calling the
     * {@link #clear()} method.
     * <p>
     * If overriding this method, it is very important to review and possibly
     * override the {@link #clear()} method!
     */
    protected void load()
    {
        AtomicReference<IndexableTuple> dataReference = getDataReference();

        IndexableTuple data = dataReference.get();

        if (data == null)
        {
            // Provide the data.
            data = provideData();

            int numberTuples = getNumberElements();
            int numberFields = getFieldNames().size();

            checkArguments(data, numberTuples, numberFields);

            // The purpose of the rest of this block is to compute the range of the data.

            // Compute lowest and next-lowest value by starting with the maximum (for a
            // float) and lowering it.
            double lowestValue = VtkMaxFloatValue;
            double nextLowestValue = lowestValue;

            // Compute maximum by starting with the minimum (-maximum for a float) and
            // raising it.
            double maximum = VtkMinFloatValue;

            // Iterate over every value and consider it as a candidate for min and/or max.
            for (int tupleIndex = 0; tupleIndex < numberTuples; ++tupleIndex)
            {
                for (int fieldIndex = 0; fieldIndex < numberFields; ++fieldIndex)
                {
                    double value = data.get(tupleIndex).get(fieldIndex);

                    // Only valid values should be considered for minimum or maximum.
                    if (isValidFloat(value))
                    {
                        if (value < lowestValue)
                        {
                            lowestValue = value;
                        }
                        else if (value < nextLowestValue)
                        {
                            nextLowestValue = value;
                        }
                        if (value > maximum)
                        {
                            maximum = value;
                        }
                    }
                    else
                    {
                        // TODO: flag nulls, arrange to report those somehow. Was this for VTK
                        // version. Probably add an isNull to the Tuple interface, make a set of null
                        // coordinates to use as the backing implementation. Clients need to
                        // check for nulls. This is better than tampering with the data like
                        // the version below:
//                            tuples.SetComponent(tupleIndex, fieldIndex, invalidFloatValue());
                    }
                }
            }

            // At this point, we have refined values for min and max, but need a final check
            // in case the data values collectively did not define a valid range.
            if (!isValidFloat(lowestValue) || !isValidFloat(maximum) || maximum < lowestValue)
            {
                throw new RuntimeException("Coloring " + getName() + " has invalid range (" + lowestValue + ", " + maximum + ")");
            }

            // The minimum value is either the next-lowest value or the lowest value. Use
            // the next-lowest if it was defined and the hasNulls flag is set.
            double minimum = hasNulls() && nextLowestValue > lowestValue ? nextLowestValue : lowestValue;

            // Cache the data and computed range.
            dataReference.set(data);
            getRangeReference().set(new double[] { minimum, maximum });
        }
    }

    protected void copyData(ColoringData sourceData)
    {
        // Copy data and range. This will force a load if it hasn't happened yet.
        IndexableTuple destTuples = ColoringDataUtils.copy(sourceData.getData());

        double[] srcRange = sourceData.getDefaultRange();
        double[] destRange = Arrays.copyOf(srcRange, srcRange.length);

        getDataReference().set(destTuples);
        getRangeReference().set(destRange);
    }

    protected static void checkArguments(IndexableTuple data, int numberElements, int numberComponents)
    {
        // Data validity checks: must be non-null, and have the right number of elements
        // (facets) and the right rank (scalar = 1, vector = 3).
        Preconditions.checkNotNull(data, "Unable to provide coloring data from null VTK array");

        Preconditions.checkArgument(numberElements == data.size(), //
                "Unable to provide coloring data with " + data.size() + //
                        " values (facets), not the expected number, " + numberElements);

        Preconditions.checkArgument(numberComponents == data.getNumberFields(), //
                "Unable to provide coloring data with rank " + data.getNumberFields() + //
                        ", not expected rank " + numberComponents);
    }

    /**
     * Return a flag that classifies a value as "valid" (or not) as a float. In the
     * base implementation, a value must be finite (and not NaN) to be considered
     * valid. In addition, the value must also lie in the CLOSED range
     * [{@link #VtkMinFloatValue}, {@link #VtkMaxFloatValue}].
     * <p>
     * Override this method to use different criteria for detecting invalid values.
     * 
     * @param value the value to be classified
     * @return true if the value should be considered to be a valid float, false
     *         otherwise.
     */
    protected boolean isValidFloat(double value)
    {
        return Double.isFinite(value) && value >= VtkMinFloatValue && value <= VtkMaxFloatValue;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object object);

    protected static int hashCode(BasicColoringData data)
    {
        return Objects.hash(data.getName(), data.getUnits(), data.getNumberElements(), data.getFieldNames(), data.hasNulls());
    }

    protected static boolean equals(BasicColoringData data1, BasicColoringData data2)
    {
        return data1.getName().equals(data2.getName()) && //
                data1.getUnits().equals(data2.getUnits()) && //
                data1.getNumberElements() == data2.getNumberElements() && //
                data1.getFieldNames().equals(data2.getFieldNames()) && //
                data1.hasNulls() == data2.hasNulls();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(getName());
        append(builder, getUnits());

        return builder.toString();
    }

    protected final void append(StringBuilder builder, String toAppend)
    {
        if (toAppend != null && toAppend.matches(".*\\S.*"))
        {
            builder.append(", ");
            builder.append(toAppend);
        }
    }

}
