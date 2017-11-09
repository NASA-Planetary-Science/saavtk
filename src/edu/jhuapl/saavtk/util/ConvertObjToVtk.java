package edu.jhuapl.saavtk.util;

import org.apache.commons.io.FilenameUtils;

import vtk.vtkNativeLibrary;
import vtk.vtkOBJReader;
import vtk.vtkPolyDataWriter;

public class ConvertObjToVtk
{
    static { vtkNativeLibrary.LoadAllNativeLibraries(); }

    public static void main(String[] args)
    {
        String objFile=args[0];
        System.out.print("Reading "+objFile+"... ");

        vtkOBJReader reader=new vtkOBJReader();
        reader.SetFileName(objFile);
        reader.Update();

        String vtkFile=FilenameUtils.removeExtension(objFile)+".vtk";
        System.out.print("Writing to "+vtkFile+"... ");
        vtkPolyDataWriter writer=new vtkPolyDataWriter();
        writer.SetFileName(vtkFile);
        writer.SetFileTypeToBinary();
        writer.SetInputData(reader.GetOutput());
        writer.Write();

        System.out.println("Done.");
    }
}
