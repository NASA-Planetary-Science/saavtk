package edu.jhuapl.saavtk.model.plateColoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.common.base.Preconditions;

import vtk.vtkCellArray;
import vtk.vtkFloatArray;
import vtk.vtkPolyData;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkVertex;

final class VtkColoringDataIOOrig // implements ColoringDataIO
{
    private static final int NumVertices = 3 * 4096;

    VtkColoringDataIOOrig()
    {
        super();
    }

    // Original signature relied on float array as the currency for plate colorings.
    // Now using IndexableTuple.
    public vtkFloatArray loadColoringData(File file) throws IOException
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

        vtkFloatArray array = (vtkFloatArray) polyData.GetCellData().GetArray(0);
        if (array == null)
        {
            throw new IOException("Unable to load coloring data from poly data in file " + file.toString());
        }

        return array;
    }

    // Original signature relied on float array as the currency for plate colorings.
    // Now using IndexableTuple.
    public void saveColoringData(vtkFloatArray array, File file)
    {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(array);

        file.delete();

        try
        {
            vtkPolyData polyData = new vtkPolyData();
            polyData.GetCellData().AddArray(array);

            vtkCellArray verts = new vtkCellArray();
            polyData.SetVerts(verts);

            for (int index = 0; index < array.GetNumberOfTuples(); index++)
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
