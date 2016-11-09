package edu.jhuapl.saavtk.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import vtk.vtkPolyData;

public class STLtoOBJConverter {

	public static void convert(File stlFile, File objFile) throws Exception {
		vtkPolyData polydata = PolyDataUtil.loadSTLShapeModel(stlFile.getAbsolutePath());
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(objFile);
			STLtoOBJConverter.convert(polydata, fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	public static void convert(vtkPolyData polyData, OutputStream outputStream) throws IOException {
		PolyDataUtil.saveShapeModelAsOBJ(polyData, outputStream);
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			throw new RuntimeException("Need to pass in the STL file you want to convert");
		}
		
		String fileName = args[0];
		if (!fileName.toUpperCase().endsWith(".STL")) {
			throw new RuntimeException("The file must be a STL file and end with either .stl or .STL");
		}
		
		NativeLibraryLoader.loadVtkLibraries();
		
		File stlFile = new File(fileName);
		String objFilename = fileName.substring(0, fileName.length()-4) + ".obj";
		File objFile = new File(objFilename);
 		
		STLtoOBJConverter.convert(stlFile, objFile);
	}
}
