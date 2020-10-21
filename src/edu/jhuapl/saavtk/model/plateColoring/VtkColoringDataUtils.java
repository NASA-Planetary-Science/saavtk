package edu.jhuapl.saavtk.model.plateColoring;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.file.IndexableTuple;
import edu.jhuapl.saavtk.util.file.Tuple;
import vtk.vtkDataArray;

/**
 * Static utility methods useful for converting coloring-related abstractions to
 * VTK abstractions.
 * 
 * @author James Peachey
 *
 */
public class VtkColoringDataUtils
{

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
                return getNumberCells();
            }
    
            @Override
            public String getAsString(int cellIndex)
            {
                return Double.toString(get(cellIndex));
            }
    
            @Override
            public double get(int cellIndex)
            {
                checkCellIndex(cellIndex);
    
                return getVtkArray().GetComponent(tupleIndex, cellIndex);
            }
    
        }
    
        private final vtkDataArray vtkArray;
    
        protected VtkArrayIndexable(vtkDataArray vtkArray)
        {
            super();
            this.vtkArray = vtkArray;
        }
    
        protected vtkDataArray getVtkArray()
        {
            return vtkArray;
        }
    
        @Override
        public int getNumberCells()
        {
            return getVtkArray().GetNumberOfComponents();
        }
    
        @Override
        public String getName(int cellIndex)
        {
            checkCellIndex(cellIndex);
    
            vtkDataArray vtkArray = getVtkArray();
    
            String name = vtkArray.HasAComponentName() ? vtkArray.GetComponentName(cellIndex) : null;
            if (name == null || name.equals(""))
            {
                name = Integer.toString(cellIndex);
            }
    
            return name;
        }
    
        @Override
        public String getUnits(int cellIndex)
        {
            return "";
        }
    
        @Override
        public int size()
        {
            return getVtkArray().GetNumberOfTuples();
        }
    
        protected void checkTupleIndex(int tupleIndex)
        {
            if (tupleIndex < 0 || tupleIndex > size())
            {
                throw new IndexOutOfBoundsException("cellIndex = " + tupleIndex + " is out of half-open range [0, " + size() + ")");
            }
        }
    
        protected void checkCellIndex(int cellIndex)
        {
            if (cellIndex < 0 || cellIndex > getNumberCells())
            {
                throw new IndexOutOfBoundsException("cellIndex = " + cellIndex + " is out of half-open range [0, " + getNumberCells() + ")");
            }
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

        if (tuples instanceof VtkColoringDataUtils.VtkArrayIndexable)
        {
            // Optimization in this case: use VTK intrinsic method to copy the array
            // associated with the input Indexable.
            vtkDataArray inputArray = ((VtkColoringDataUtils.VtkArrayIndexable) tuples).getVtkArray();
//            long startTime = System.currentTimeMillis();
            vtkArray.DeepCopy(inputArray);
//            System.err.println("Time to copy was " + (System.currentTimeMillis() - startTime));
        }
        else
        {
            int numberCells = tuples.getNumberCells();
            vtkArray.SetNumberOfComponents(numberCells);

            int numberRecords = tuples.size();
            vtkArray.SetNumberOfTuples(numberRecords);

            for (int recordIndex = 0; recordIndex < numberRecords; ++recordIndex)
            {
                for (int cellIndex = 0; cellIndex < numberCells; ++cellIndex)
                {
                    vtkArray.SetComponent(recordIndex, cellIndex, tuples.get(recordIndex).get(cellIndex));
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
        // This looks clunky, but it is a performant unpacking of VTK's interface that
        // determines the dimensions of the source array one time, and sets up the
        // output object to call the correct variation of GetTuple accordingly.
        if (vtkArray.GetNumberOfComponents() == 1)
        {
            return new VtkColoringDataUtils.VtkArrayIndexable(vtkArray) {

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
            return new VtkColoringDataUtils.VtkArrayIndexable(vtkArray) {

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
            return new VtkColoringDataUtils.VtkArrayIndexable(vtkArray) {

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
            return new VtkColoringDataUtils.VtkArrayIndexable(vtkArray) {

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
            return new VtkColoringDataUtils.VtkArrayIndexable(vtkArray) {

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
            return new VtkColoringDataUtils.VtkArrayIndexable(vtkArray) {

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
            return new VtkColoringDataUtils.VtkArrayIndexable(vtkArray) {

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
                            for (int cellIndex = 0; cellIndex < array.length; ++cellIndex)
                            {
                                array[cellIndex] = vtkArray.GetComponent(tupleIndex, cellIndex);
                            }

                            return array;
                        }

                    };
                }

            };
        }
    }

}
