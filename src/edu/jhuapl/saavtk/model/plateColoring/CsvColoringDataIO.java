package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.file.CsvFileReader;
import edu.jhuapl.saavtk.util.file.FieldNotFoundException;
import edu.jhuapl.saavtk.util.file.IndexableTuple;

/**
 * Implementation of {@link ColoringDataIO} for CSV files.
 * @author James Peachey
 *
 */
final class CsvColoringDataIO implements ColoringDataIO
{
    private final ImmutableList<Integer> columnNumbers;

    CsvColoringDataIO(ImmutableList<Integer> columnNumbers)
    {
        super();

        this.columnNumbers = columnNumbers;
    }

    @Override
    public IndexableTuple loadColoringData(File file) throws IOException, FieldNotFoundException
    {
        Preconditions.checkNotNull(file);

        return CsvFileReader.of().readTuples(file, columnNumbers);
    }

    @Override
    public void saveColoringData(IndexableTuple tuples, File file) throws IOException
    {
        throw new UnsupportedOperationException("Not yet implemented: saving coloring data to a CSV file");
    }

}
