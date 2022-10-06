package edu.jhuapl.saavtk.util.wireframe;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.saavtk.io.readers.Readers;
import edu.jhuapl.saavtk.io.writers.ObjWriter;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkAppendPolyData;
import vtk.vtkCell;
import vtk.vtkPolyData;

public class WireframeConverter {

	private final WireframeStrategy strategy;
	
	public WireframeConverter() {
		this(new CylinderWireframeStrategy());
	}
	
	public WireframeConverter(WireframeStrategy strategy) {
		this.strategy = strategy;
	}
	
	public vtkPolyData convert(vtkPolyData rawPolyData) {
		vtkAppendPolyData appendFilter=new vtkAppendPolyData();

	    for (int i=0; i<rawPolyData.GetNumberOfCells(); i++) {
	    	vtkCell cell=rawPolyData.GetCell(i);
	    	switch ((int)cell.GetNumberOfPoints())
	    	{
	    	case 1:
                appendFilter.AddInputData(strategy.convertPoint(new Point3D(cell.GetPoints().GetPoint(0))));
	    	    break;
	    	default:
	    	    for (int j=0; j<cell.GetNumberOfPoints(); j++)
	    	    {
	    	        int jp=(j+1) % (int)cell.GetNumberOfPoints();
	    	        vtkPolyData line = strategy.convertLine(new Point3D(cell.GetPoints().GetPoint(j)), new Point3D(cell.GetPoints().GetPoint(jp)));
                    appendFilter.AddInputData(line);
	    	    }
	    	    break;
	    	}
	    }
	    appendFilter.Update();
	    
	    return appendFilter.GetOutput();
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			throw new RuntimeException("Need to pass in the STL file you want to convert");
		}
		
		String fileName = args[0];
		
		NativeLibraryLoader.loadVtkLibraries();
		
		File stlFile = new File(fileName);
		String objFilename = fileName.substring(0, fileName.length()-4) + "_wireframe.obj";
		File objFile = new File(objFilename);
		
		vtkPolyData data = Readers.read(stlFile);
		CylinderWireframeStrategy cylinderStrategy = new CylinderWireframeStrategy(0.2);
		vtkPolyData wireframe = new WireframeConverter(cylinderStrategy).convert(data);
		ObjWriter writer = new ObjWriter();
		writer.setInputData(wireframe);
		writer.setOutputFile(objFile);
		writer.write();
	}
}
