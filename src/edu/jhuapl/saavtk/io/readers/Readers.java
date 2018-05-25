package edu.jhuapl.saavtk.io.readers;

import java.io.File;

import vtk.vtkPolyData;

public class Readers {

	public static String OBJ_EXT = ".obj";
	public static String STL_EXT = ".stl";
	public static String PLY_EXT = ".ply";
	
	private Readers() {}
	
	public static PolyDataReader get(File file) {
		PolyDataReader reader = null;
		String fileName = file.getName().toLowerCase();
		if (fileName.endsWith(OBJ_EXT)) {
			reader = new ObjReader();
		} else if (fileName.endsWith(STL_EXT)) {
			reader = new StlReader();
		}/* else if (fileName.endsWith(PLY_EXT)) {
			reader = new PlyReader();
		}*/
		reader.SetFileName(file.getAbsolutePath());
		return reader;
	}
	
	public static vtkPolyData read(File file) {
		PolyDataReader reader = Readers.get(file);
		reader.Update();
		return reader.GetOutput();
	}
}
