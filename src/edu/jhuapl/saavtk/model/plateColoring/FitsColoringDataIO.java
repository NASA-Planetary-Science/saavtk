package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.file.DataFileReader.IncorrectFileFormatException;
import edu.jhuapl.saavtk.util.file.FieldNotFoundException;
import edu.jhuapl.saavtk.util.file.FitsFileReader;
import edu.jhuapl.saavtk.util.file.IndexableTuple;

/**
 * Implementation of {@link ColoringDataIO} for FITS files.
 * 
 * @author James Peachey
 *
 */
final class FitsColoringDataIO implements ColoringDataIO
{
    private final int tableId;
    private final ImmutableList<Integer> columnNumbers;

    FitsColoringDataIO(int tableId, ImmutableList<Integer> columnNumbers)
    {
        super();

        this.tableId = tableId;
        this.columnNumbers = columnNumbers;
    }

    @Override
    public IndexableTuple loadColoringData(File file) throws IOException, IncorrectFileFormatException, FieldNotFoundException
    {
        Preconditions.checkNotNull(file);

        return FitsFileReader.of().readTuples(file, tableId, columnNumbers);
    }

    @Override
    public void saveColoringData(IndexableTuple tuples, File file) throws IOException
    {
        throw new UnsupportedOperationException("Not yet implemented: saving coloring data to a FITS file");
    }

}
