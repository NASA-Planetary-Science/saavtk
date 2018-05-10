package edu.jhuapl.saavtk2.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.jhuapl.saavtk.util.Point3D;
import edu.jhuapl.saavtk.util.iterator.Iterators;
import vtk.vtkIdList;
import vtk.vtkPolyData;

public class ObjWriter implements PolyDataWriter {

	private vtkPolyData data;
	private File outputFile;
	
	@Override
	public void setInputData(vtkPolyData data) {
		this.data = data;
	}

	@Override
	public void setOutputFile(String fileName) {
		setOutputFile(new File(fileName));
	}

	@Override
	public void setOutputFile(File file) {
		this.outputFile = file;
	}

	@Override
	public void write() throws IOException {
		BufferedWriter writer = null;
		try {
			writer=new BufferedWriter(new FileWriter(outputFile));
			for (Point3D point : Iterators.get(data)) {
				writer.write("v ");
				writer.write(Double.toString(point.xyz[0]));
				writer.write(' ');
				writer.write(Double.toString(point.xyz[1]));
				writer.write(' ');
				writer.write(Double.toString(point.xyz[2]));
				writer.write('\n');
			}
			
			for (int i=0; i<data.GetNumberOfCells(); i++) {
			    vtkIdList ids=data.GetCell(i).GetPointIds();
                int id0=ids.GetId(0)+1;
                int id1=ids.GetId(1)+1;
                int id2=ids.GetId(2)+1;
                writer.write("f "+id0+" "+id1+" "+id2+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
