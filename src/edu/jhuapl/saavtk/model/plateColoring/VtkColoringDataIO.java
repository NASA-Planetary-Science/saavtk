package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.util.file.DataFileReader.IncorrectFileFormatException;
import edu.jhuapl.saavtk.util.file.IndexableTuple;
import vtk.vtkCellArray;
import vtk.vtkDataArray;
import vtk.vtkDoubleArray;
import vtk.vtkPolyData;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkVertex;

/**
* Implementation of {@link ColoringDataIO} for VTK files.
* @author James Peachey
*
*/
final class VtkColoringDataIO implements ColoringDataIO
{

    protected VtkColoringDataIO()
    {
        super();
    }

    @Override
    public IndexableTuple loadColoringData(File file) throws IOException, IncorrectFileFormatException
    {
        Preconditions.checkNotNull(file);

        if (!file.isFile())
        {
            throw new FileNotFoundException(file.toString());
        }

        vtkPolyDataReader reader = new vtkPolyDataReader();
        reader.SetFileName(file.toString());
        reader.Update();

        vtkPolyData polyData = reader.GetOutput();
        if (polyData == null)
        {
            throw new IOException("Unable to load poly data from file " + file.toString());
        }

        vtkDataArray vtkArray = polyData.GetCellData().GetArray(0);
        if (vtkArray == null)
        {
            throw new IncorrectFileFormatException("Unable to load coloring data from poly data in file " + file.toString());
        }

        return ColoringDataUtils.createIndexableFromVtkArray(vtkArray);
    }

    @Override
    public void saveColoringData(IndexableTuple tuples, File file) throws IOException
    {
        Preconditions.checkNotNull(tuples);
        Preconditions.checkNotNull(file);
        
        file.delete();

        try
        {
            vtkDoubleArray vtkArray = new vtkDoubleArray();
            ColoringDataUtils.copyIndexableToVtkArray(tuples, vtkArray);
            
            vtkPolyData polyData = new vtkPolyData();
            polyData.GetCellData().AddArray(vtkArray);

            vtkCellArray verts = new vtkCellArray();
            polyData.SetVerts(verts);

            for (int index = 0; index < vtkArray.GetNumberOfTuples(); index++)
            {
                vtkVertex vert = new vtkVertex();
                vert.GetPointIds().SetId(0, index);
                verts.InsertNextCell(vert);
            }

            vtkPolyDataWriter writer = new vtkPolyDataWriter();
            writer.SetFileName(file.toString());
            writer.SetFileTypeToBinary();
            writer.SetInputData(polyData);
            writer.Write();
        }
        catch (Exception e)
        {
            file.delete();
            throw e;
        }

    }

}
