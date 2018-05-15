package edu.jhuapl.saavtk.io.readers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataInputStream;

import vtk.vtkNativeLibrary;
import vtk.vtkPLYReader;
import vtk.vtkPolyData;
import vtk.vtkUnstructuredGridWriter;

public abstract class PlyReader implements PolyDataReader {

	protected static enum PropertyType
	{
		DOUBLE("double"),UCHAR("uchar");
		
		String str;
		private PropertyType(String str) {
			this.str=str;
		}
		
	}
	
	protected static class Property
	{
		PropertyType type;
		String name;
	}

	String filename;
	int elementCount=-1;
	List<Property> properties=Lists.newArrayList();
	List<double[]> data=Lists.newArrayList();
	
	// assume format is binary_little_endian, e.g. O-REx IPWG format from Dani G
	
	@Override
	public void SetFileName(String filename) {
		this.filename=filename;
	}

	@Override
	public void Update() {
		properties.clear();
		data.clear();
		elementCount=-1;
		readHeader();
		readData();
	}
	
	public static final String PLY_END_HEADER="end_header";
	public static final String PLY_FORMAT_KEYWORD="format";
	public static final String PLY_ELEMENT_KEYWORD="element";
	public static final String PLY_PROPERTY_KEYWORD="property";

	protected void readHeader()
	{
		File file=new File(filename);
		LineIterator it=null;
		try {
			
			it = FileUtils.lineIterator(file);
			
			while (it.hasNext())
			{
				String line=it.nextLine();
				if (line.equals(PLY_END_HEADER))
					break;
				String[] tokens=line.split("\\s+");
				if (tokens[0].equals(PLY_ELEMENT_KEYWORD))
				{
					String elementType=tokens[1];
					if (!elementType.equals("vertex"))
					{
						System.err.println("element type \""+elementType+"\" not supported");
						break;
					}
					elementCount=Integer.valueOf(tokens[2]);
				} else if (tokens[0].equals(PLY_FORMAT_KEYWORD))
				{
					String format=tokens[1];
					if (!format.equals("binary_little_endian"))
					{
						System.err.println("format \""+format+"\" not supported");
						break;
					}
					
				} else if (tokens[0].equals(PLY_PROPERTY_KEYWORD))
				{
					Property prop=new Property();
					prop.name=tokens[2];
					prop.type=null;
					for (PropertyType type : PropertyType.values())
					{
						if (type.str.equals(tokens[1]))
							prop.type=type;
					}
					if (prop.type==null)
					{
						System.err.println("property type \""+tokens[1]+"\" not supported");
						break;
					}
					properties.add(prop);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (it!=null)
				it.close();
		}
	}
	
	protected void readData()
	{
		try {
			DataInputStream stream=new DataInputStream(new BufferedInputStream(new FileInputStream(new File(filename))));
			
			String str=null;
			do
			{
				str=stream.readLine();
			} while (!str.equals("end_header"));

			LittleEndianDataInputStream estream=new LittleEndianDataInputStream(stream);	// readLine() method is not supported on LittleEndianDataInputStream but 
			
			for (int i=0; i<elementCount; i++)
			{
				double[] tuple=new double[properties.size()];
				for (int j=0; j<properties.size(); j++)
				{
					switch (properties.get(j).type)
					{
					case DOUBLE:
						tuple[j]=estream.readDouble();
						break;
					case UCHAR:
						tuple[j]=estream.readUnsignedByte();
						break;
					}
					data.add(tuple);
				}
			}
			estream.close();
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
/*	private final vtkPLYReader reader;
	
	public PlyReader() {
		this.reader = new vtkPLYReader();
	}
	
	@Override
	public void SetFileName(String filename) {
		reader.SetFileName(filename);
	}

	@Override
	public void Update() {
		reader.Update();
	}

	@Override
	public vtkPolyData GetOutput() {
		return reader.GetOutput();
	}*/
	
	
}
