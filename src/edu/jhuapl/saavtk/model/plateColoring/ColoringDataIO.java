package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.saavtk.util.file.DataFileReader.IncorrectFileFormatException;
import edu.jhuapl.saavtk.util.file.FieldNotFoundException;
import edu.jhuapl.saavtk.util.file.IndexableTuple;

/**
 * Interface for loading/saving plate coloring data.
 * 
 * @author James Peachey
 *
 */
public interface ColoringDataIO
{
    /**
     * Load the coloring data as a {@link IndexableTuple} from the specified
     * {@link File}.
     * 
     * @param file the file from which to load the coloring data
     * @return the {@link IndexableTuple} that provides the coloring data
     * @throws IOException if IOException prevents loading the file
     * @throws IncorrectFileFormatException if the file exists but the
     *             implementation cannot handle files with that format
     * @throws FieldNotFoundException if the file has the correct format but does
     *             not contain the expected fields
     */
    IndexableTuple loadColoringData(File file) throws IOException, IncorrectFileFormatException, FieldNotFoundException;

    /**
     * Save the specified coloring data {@link IndexableTuple} to the specified
     * file.
     * 
     * @param tuples the {@link IndexableTuple} to save
     * @param file the file to which to save it
     * @throws IOException if coloring data cannot be saved to the file
     */
    void saveColoringData(IndexableTuple tuples, File file) throws IOException;
}
