package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.file.DataFileInfo.FileFormat;
import vtk.vtkPolyDataReader;

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

    @Override
    public void checkFormat(File file) throws IOException, FileFormatException
    {
        if (isFileGzipped(file))
        {
            file = gunzip(file);
        }

        vtkPolyDataReader fileReader = new vtkPolyDataReader();
        fileReader.SetFileName(file.toString());
        fileReader.Update();

        if (0 == fileReader.OpenVTKFile()) {
            throw new IncorrectFileFormatException("File format check: unable to open as a VTK file " + file.toString());
        }
    }

    @Override
    public DataFileInfo readFileInfo(File file) throws IOException, FileFormatException
    {
        if (isFileGzipped(file))
        {
            file = gunzip(file);
        }

        vtkPolyDataReader fileReader = new vtkPolyDataReader();
        fileReader.SetFileName(file.toString());
        fileReader.Update();

        if (0 == fileReader.OpenVTKFile()) {
            throw new IncorrectFileFormatException("Unable to open as a VTK file " + file.toString());
        }

//        int numberElements;
//        int numberComponents;
//        fileReader.ReadArray("float", numberElements, numberComponents);
//        vtkPolyData polyData = fileReader.GetOutput();
//        
//        polyData.data

        ImmutableList.Builder<DataObjectInfo> builder = ImmutableList.builder();

//        List<String> fields;
//        List<? extends InfoRow> elements;
//        Description description = Description.of(fields, elements);
//        
//        int numberRows;
//        List<? extends ColumnInfo> columnInfo;
//        DataObjectInfo info = TableInfo.of(file.getName(), description, numberRows, columnInfo);
//        
//        builder.add(info);
        return DataFileInfo.of(file, VTK_FORMAT, builder.build());
    }

}
