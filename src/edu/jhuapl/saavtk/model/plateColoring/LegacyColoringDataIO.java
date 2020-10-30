package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.file.FieldNotFoundException;
import edu.jhuapl.saavtk.util.file.IndexableTuple;
import edu.jhuapl.saavtk.util.file.DataFileReader.IncorrectFileFormatException;

/**
 * 
 * @author James Peachey
 *
 */
abstract class LegacyColoringDataIO implements ColoringDataIO
{
    static LegacyColoringDataIO of(String coloringName)
    {
        Preconditions.checkNotNull(coloringName);

        return new LegacyColoringDataIO() {
            @Override
            public IndexableTuple loadColoringData(File file) throws IOException
            {
                Preconditions.checkNotNull(file);
                Preconditions.checkArgument(file.isFile(), "File named " + file + " does not identify an existing file");

                IndexableTuple indexable;

                if (coloringName.toLowerCase().contains("error"))
                {
                    // Try first for a vector.
                    indexable = tryLoadFitsTuplesOnly(file, FitsColumnId.VECTOR_ERROR.getColumnNumbers());
                    if (indexable == null)
                    {
                        indexable = tryLoadFitsTuplesOnly(file, FitsColumnId.SCALAR_ERROR.getColumnNumbers());
                    }
                }
                else
                {
                    // Try first for a vector.
                    indexable = tryLoadTuples(file, FitsColumnId.VECTOR.getColumnNumbers(), CsvColumnId.VECTOR.getColumnNumbers());
                    if (indexable == null)
                    {
                        indexable = tryLoadTuples(file, FitsColumnId.SCALAR.getColumnNumbers(), CsvColumnId.SCALAR.getColumnNumbers());
                    }
                }

                if (indexable == null)
                {
                    throw new IOException("Could not load coloring " + coloringName + " as vector or scalar data in file " + file);
                }

                return indexable;
            }

        };
    }

    static LegacyColoringDataIO of(Iterable<Integer> fitsColumnNumbers, Iterable<Integer> csvColumnNumbers)
    {
        Preconditions.checkNotNull(fitsColumnNumbers);
        Preconditions.checkNotNull(csvColumnNumbers);

        return new LegacyColoringDataIO() {

            @Override
            public IndexableTuple loadColoringData(File file) throws IOException
            {
                return tryLoadTuples(file, fitsColumnNumbers, csvColumnNumbers);
            }

        };
    }

    private LegacyColoringDataIO()
    {
        super();
    }

    @Override
    public void saveColoringData(IndexableTuple tuples, File file) throws IOException
    {
        Preconditions.checkNotNull(tuples);
        Preconditions.checkNotNull(file);

        ColoringDataIO ioHandler = new VtkColoringDataIO();
        ioHandler.saveColoringData(tuples, file);
    }

    /**
     * Re-declare this to get the throws clause correct but otherwise leave
     * abstract.
     */
    @Override
    public abstract IndexableTuple loadColoringData(File file) throws IOException;

    protected IndexableTuple tryLoadFitsTuplesOnly(File file, Iterable<Integer> columnNumbers) throws IOException
    {
        ColoringDataIO ioHandler = new FitsColoringDataIO(1, ImmutableList.copyOf(columnNumbers));

        try
        {
            return ioHandler.loadColoringData(file);
        }
        catch (IncorrectFileFormatException e)
        {
            throw new IOException(e);
        }
        catch (FieldNotFoundException e)
        {
            // Return null to tell caller this exception was caught. Caller can/should
            // check for null and try other options.
            return null;
        }
    }

    protected IndexableTuple tryLoadTuples(File file, Iterable<Integer> fitsColumnNumbers, Iterable<Integer> csvColumnNumbers) throws IOException
    {
        ImmutableList<Integer> fitsColumnList = ImmutableList.copyOf(fitsColumnNumbers);
        ImmutableList<Integer> csvColumnList = ImmutableList.copyOf(csvColumnNumbers);

        Preconditions.checkArgument(fitsColumnList.size() == csvColumnList.size(), "Cannot ask for different numbers of columns depending on the file type");

        ImmutableList<ColoringDataIO> ioHandlers = ImmutableList.of( //
                new FitsColoringDataIO(1, ImmutableList.copyOf(fitsColumnNumbers)), //
                new VtkColoringDataIO(), //
                new CsvColoringDataIO(ImmutableList.copyOf(csvColumnNumbers)) //
        );

        try
        {
            for (ColoringDataIO ioHandler : ioHandlers)
            {
                try
                {
                    IndexableTuple tuples = ioHandler.loadColoringData(file);

                    // Extra sanity check needed here because the VTK loader ignores column numbers.
                    if (tuples.getNumberFields() > fitsColumnList.size())
                    {
                        throw new IOException("Attempted to coloring with " + tuples.getNumberFields() + //
                                " fields, but only " + fitsColumnList.size() + //
                                " fields in file " + file);
                    }
                    return tuples;
                }
                catch (IncorrectFileFormatException e)
                {
                    // Ignore this exception and hope one of the other handlers can read this file.
                }
            }
            // If execution reaches this point, all the handlers threw the "incorrect
            // format" exception.
            throw new IOException("Unable to load coloring data from file " + file + "; tried FITS, VTK and CSV formats");
        }
        catch (FieldNotFoundException e)
        {
            // Return null to tell caller this exception was caught. Caller can/should
            // check for null and try other options.
            return null;
        }
    }

    protected enum FitsColumnId
    {
        SCALAR(4), //
        SCALAR_ERROR(5), //
        VECTOR(4, 6, 8), //
        VECTOR_ERROR(5, 7, 9), //
        ;

        private final ImmutableList<Integer> columnNumbers;

        private FitsColumnId(int columnNumber)
        {
            this.columnNumbers = ImmutableList.of(columnNumber);
        }

        private FitsColumnId(int xColumnNumber, int yColumnNumber, int zColumnNumber)
        {
            this.columnNumbers = ImmutableList.of(xColumnNumber, yColumnNumber, zColumnNumber);
        }

        public ImmutableList<Integer> getColumnNumbers()
        {
            return columnNumbers;
        }

    }

    protected enum CsvColumnId
    {
        SCALAR(0), //
        VECTOR(0, 1, 2), //
        ;

        private final ImmutableList<Integer> columnNumbers;

        private CsvColumnId(int columnNumber)
        {
            this.columnNumbers = ImmutableList.of(columnNumber);
        }

        private CsvColumnId(int xColumnNumber, int yColumnNumber, int zColumnNumber)
        {
            this.columnNumbers = ImmutableList.of(xColumnNumber, yColumnNumber, zColumnNumber);
        }

        public ImmutableList<Integer> getColumnNumbers()
        {
            return columnNumbers;
        }

    }

}
