package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.file.IndexableTuple;
import edu.jhuapl.saavtk.util.file.Tuple;
import vtk.vtkDataArray;
import vtk.vtkDoubleArray;

/**
 * Static utility methods useful for manipulating coloring-related abstractions.
 * 
 * @author James Peachey
 *
 */
public class ColoringDataUtils
{

    /**
     * This will also copy a null to a null.
     * @param source
     * @return
     */
    public static IndexableTuple copy(IndexableTuple source)
    {
        IndexableTuple dest;
        if (source instanceof VtkArrayIndexable)
        {
            vtkDataArray arrayCopy = new vtkDoubleArray();
            copyIndexableToVtkArray(source, arrayCopy);
            dest = createIndexableFromVtkArray(arrayCopy);
        }
        else
        {
            // Other known implementations are assumed to be immutable.
            dest = source;
        }

        return dest;
    }

    /**
     * For specified {@link IndexableTuple} object, if its implementation was
     * provided by a previous call to
     * {@link ColoringDataUtils#createIndexableFromVtkArray(vtkDataArray)}, this
     * method calls the Delete() method of the underlying VTK data object to free
     * the associated memory that VTK allocated.
     * <p>
     * After this method is called, an {@link IllegalStateException} will be thrown
     * if any further attempt is made to access elements of data belonging to the
     * specified {@link IndexableTuple}.
     * <p>
     * It is safe to call this method for any object, and safe to call it multiple
     * times for the same object, regardless of the object's implementation.
     * 
     * @param tuples
     */
    public static void deleteVtkData(IndexableTuple tuples)
    {
        if (tuples instanceof VtkArrayIndexable)
        {
            ((VtkArrayIndexable) tuples).deleteVtkData();
        }
    }

    /**
     * Copy data elements from the specified source {@link IndexableTuple} into the
     * specified destination {@link vtkDataArray}.
     * 
     * @param tuples the source {@link IndexableTuple}
     * @param vtkArray the destination {@link vtkDataArray}
     */
    public static void copyIndexableToVtkArray(IndexableTuple tuples, vtkDataArray vtkArray)
    {
        Preconditions.checkNotNull(tuples);
        Preconditions.checkNotNull(vtkArray);

        if (tuples instanceof ColoringDataUtils.VtkArrayIndexable)
        {
            // Optimization in this case: use VTK intrinsic method to copy the array
            // associated with the input Indexable.
            vtkDataArray inputArray = ((ColoringDataUtils.VtkArrayIndexable) tuples).getVtkArray();
//            long startTime = System.currentTimeMillis();
            vtkArray.DeepCopy(inputArray);
//            System.err.println("Time to copy was " + (System.currentTimeMillis() - startTime));
        }
        else
        {
            int numberFields = tuples.getNumberFields();
            vtkArray.SetNumberOfComponents(numberFields);

            int numberRecords = tuples.size();
            vtkArray.SetNumberOfTuples(numberRecords);

            for (int recordIndex = 0; recordIndex < numberRecords; ++recordIndex)
            {
                for (int fieldIndex = 0; fieldIndex < numberFields; ++fieldIndex)
                {
                    vtkArray.SetComponent(recordIndex, fieldIndex, tuples.get(recordIndex).get(fieldIndex));
                }
            }
        }
    }

    /**
     * Convert the specified source {@link vtkDataArray} to an implementation of the
     * {@link IndexableTuple} interface, which provides a view of the source data.
     * 
     * @param vtkArray the source {@link vtkDataArray}
     * @return the {@link IndexableTuple}
     */
    public static IndexableTuple createIndexableFromVtkArray(vtkDataArray vtkArray)
    {
        Preconditions.checkNotNull(vtkArray);

        // This looks clunky, but it is a performant unpacking of VTK's interface that
        // determines the dimensions of the source array one time, and sets up the
        // output object to call the correct variation of GetTuple accordingly.
        if (vtkArray.GetNumberOfComponents() == 1)
        {
            return new ColoringDataUtils.VtkArrayIndexable(vtkArray) {

                @Override
                public Tuple get(int tupleIndex)
                {
                    checkTupleIndex(tupleIndex);

                    return new VtkArrayTuple(tupleIndex) {

                        @Override
                        public double[] get()
                        {
                            return new double[] { getVtkArray().GetTuple1(tupleIndex) };
                        }

                    };
                }

            };
        }
        else if (vtkArray.GetNumberOfComponents() == 2)
        {
            return new ColoringDataUtils.VtkArrayIndexable(vtkArray) {

                @Override
                public Tuple get(int tupleIndex)
                {
                    checkTupleIndex(tupleIndex);

                    return new VtkArrayTuple(tupleIndex) {

                        @Override
                        public double[] get()
                        {
                            return getVtkArray().GetTuple2(tupleIndex);
                        }

                    };
                }

            };
        }
        else if (vtkArray.GetNumberOfComponents() == 3)
        {
            return new ColoringDataUtils.VtkArrayIndexable(vtkArray) {

                @Override
                public Tuple get(int tupleIndex)
                {
                    checkTupleIndex(tupleIndex);

                    return new VtkArrayTuple(tupleIndex) {

                        @Override
                        public double[] get()
                        {
                            return getVtkArray().GetTuple3(tupleIndex);
                        }

                    };
                }

            };
        }
        else if (vtkArray.GetNumberOfComponents() == 4)
        {
            return new ColoringDataUtils.VtkArrayIndexable(vtkArray) {

                @Override
                public Tuple get(int tupleIndex)
                {
                    checkTupleIndex(tupleIndex);

                    return new VtkArrayTuple(tupleIndex) {

                        @Override
                        public double[] get()
                        {
                            return getVtkArray().GetTuple4(tupleIndex);
                        }

                    };
                }

            };
        }
        else if (vtkArray.GetNumberOfComponents() == 6)
        {
            return new ColoringDataUtils.VtkArrayIndexable(vtkArray) {

                @Override
                public Tuple get(int tupleIndex)
                {
                    checkTupleIndex(tupleIndex);

                    return new VtkArrayTuple(tupleIndex) {

                        @Override
                        public double[] get()
                        {
                            return getVtkArray().GetTuple6(tupleIndex);
                        }

                    };
                }

            };
        }
        else if (vtkArray.GetNumberOfComponents() == 9)
        {
            return new ColoringDataUtils.VtkArrayIndexable(vtkArray) {

                @Override
                public Tuple get(int tupleIndex)
                {
                    checkTupleIndex(tupleIndex);

                    return new VtkArrayTuple(tupleIndex) {

                        @Override
                        public double[] get()
                        {
                            return getVtkArray().GetTuple9(tupleIndex);
                        }

                    };
                }

            };
        }
        else
        {
            // This case probably never arises, but may as well cover it.
            return new ColoringDataUtils.VtkArrayIndexable(vtkArray) {

                @Override
                public Tuple get(int tupleIndex)
                {
                    checkTupleIndex(tupleIndex);

                    return new VtkArrayTuple(tupleIndex) {

                        @Override
                        public double[] get()
                        {
                            vtkDataArray vtkArray = getVtkArray();

                            double[] array = new double[vtkArray.GetNumberOfComponents()];
                            for (int fieldIndex = 0; fieldIndex < array.length; ++fieldIndex)
                            {
                                array[fieldIndex] = vtkArray.GetComponent(tupleIndex, fieldIndex);
                            }

                            return array;
                        }

                    };
                }

            };
        }
    }

    /**
     * Create the parent directory of the provided file. Creates all parts of the path needed.
     * Does nothing if the parent directory already exists.
     * 
     * @param file the file whose parent to create
     * @see {@link Files#createDirectories(java.nio.file.Path, java.nio.file.attribute.FileAttribute...)}
     *      for exceptions thrown
     */
    public static void createParentDirectory(File file) throws IOException
    {
        File parent = file.getParentFile();
        if (parent != null)
        {
            if (!parent.isDirectory())
            {
                Files.createDirectories(parent.toPath());
            }
        }

    }
    
    /**
     * Implementation of {@link IndexableTuple} that is backed by a
     * {@link vtkDataArray}.
     */
    private static abstract class VtkArrayIndexable implements IndexableTuple
    {

        protected abstract class VtkArrayTuple implements Tuple
        {
            private final int tupleIndex;

            protected VtkArrayTuple(int tupleIndex)
            {
                super();
                this.tupleIndex = tupleIndex;
            }

            @Override
            public int size()
            {
                return getNumberFields();
            }

            @Override
            public double get(int fieldIndex)
            {
                checkFieldIndex(fieldIndex);

                return getVtkArray().GetComponent(tupleIndex, fieldIndex);
            }

        }

        private vtkDataArray vtkArray;

        protected VtkArrayIndexable(vtkDataArray vtkArray)
        {
            super();
            this.vtkArray = vtkArray;
        }

        protected vtkDataArray getVtkArray()
        {
            Preconditions.checkState(vtkArray != null, "VTK-backed data array has been deleted; cannot access it further");

            return vtkArray;
        }

        protected void deleteVtkData()
        {
            if (vtkArray != null)
            {
                vtkArray.Delete();
            }
            vtkArray = null;
        }

        @Override
        public int getNumberFields()
        {
            return getVtkArray().GetNumberOfComponents();
        }

        @Override
        public String getName(int fieldIndex)
        {
            checkFieldIndex(fieldIndex);

            vtkDataArray vtkArray = getVtkArray();

            String name = vtkArray.HasAComponentName() ? vtkArray.GetComponentName(fieldIndex) : null;
            if (name == null || name.equals(""))
            {
                name = Integer.toString(fieldIndex);
            }

            return name;
        }

        @Override
        public String getUnits(int fieldIndex)
        {
            return "";
        }

        @Override
        public int size()
        {
            return (int)getVtkArray().GetNumberOfTuples();
        }

        protected void checkTupleIndex(int tupleIndex)
        {
            if (tupleIndex < 0 || tupleIndex > size())
            {
                throw new IndexOutOfBoundsException("tupleIndex = " + tupleIndex + " is out of half-open range [0, " + size() + ")");
            }
        }

        protected void checkFieldIndex(int fieldIndex)
        {
            if (fieldIndex < 0 || fieldIndex > getNumberFields())
            {
                throw new IndexOutOfBoundsException("fieldIndex = " + fieldIndex + " is out of half-open range [0, " + getNumberFields() + ")");
            }
        }

    }

    private ColoringDataUtils()
    {
        throw new AssertionError("Static-only class");
    }

}
