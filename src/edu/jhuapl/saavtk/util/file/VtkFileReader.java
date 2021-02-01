package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.saavtk.util.file.DataFileInfo.FileFormat;

/**
 * This class is really still under construction. It doesn't work yet to load
 * a plate coloring from a VTK file. Problems are described inline below.
 * 
 * @author James Peachey
 *
 */
public class VtkFileReader extends DataFileReader
{
    private static final VtkFileReader INSTANCE = new VtkFileReader();

    public static VtkFileReader of()
    {
        return INSTANCE;
    }

    public static final FileFormat VTK_FORMAT = new FileFormat() {
        @Override
        public String toString()
        {
            return "VTK";
        }
    };

    /**
     * According to online searches, there is no good way to tell if a VTK file is
     * valid in code without generating a bunch of error messages. Instead, do a
     * simplistic file name check, allowing for optional gzipped format.
     * 
     * @param file the file to check
     */
    @Override
    public void checkFormat(File file) throws IOException, FileFormatException
    {
        String lcFileName = file.getName().toLowerCase();
        if (!lcFileName.endsWith(".vtk") && !lcFileName.endsWith(".vtk.gz"))
        {
            throw new IncorrectFileFormatException("File format check: unable to open as a VTK file " + file.toString());
        }
    }

    /**
     * There seems to be no good way to implement this such that it would work with
     * the DataFileReader design.
     */
    @Override
    public DataFileInfo readFileInfo(File file) throws IOException, FileFormatException
    {
//        if (isFileGzipped(file))
//        {
//            file = gunzip(file);
//        }
//
//        vtkPolyDataReader fileReader = new vtkPolyDataReader();
//        fileReader.SetFileName(file.toString());
//
//        // This doesn't work: IsFileValid seems to return the same value whether or not
//        // the file is a VTK polydata file.
//        if (0 == fileReader.IsFileValid("POLYDATA"))
//        {
//            throw new IncorrectFileFormatException("Unable to open as a VTK file " + file.toString());
//        }
//
//        // This also doesn't work: OpenFile seems to return non-0 (meaning no error
//        // according to VTK docs)
//        // whether or not the file is a VTK polydata file.
//        if (0 == fileReader.OpenVTKFile())
//        {
//            throw new IncorrectFileFormatException("Unable to open as a VTK file " + file.toString());
//        }
//        int numberElements;
//        int numberComponents;
//        fileReader.ReadArray("float", numberElements, numberComponents);
//        vtkPolyData polyData = fileReader.GetOutput();
//        
//        polyData.data
//
//        ImmutableList.Builder<DataObjectInfo> builder = ImmutableList.builder();
//
//        List<String> fields;
//        List<? extends InfoRow> elements;
//        Description description = Description.of(fields, elements);
//        
//        int numberRows;
//        List<? extends ColumnInfo> columnInfo;
//        DataObjectInfo info = TableInfo.of(file.getName(), description, numberRows, columnInfo);
//        
//        builder.add(info);
//        return DataFileInfo.of(file, VTK_FORMAT, builder.build());
        throw new UnsupportedOperationException("VTK file reading is not yet implemented");
    }

}
