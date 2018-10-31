package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineSegment;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.model.structure.esri.ShapefileUtil;
import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkAppendPolyData;
import vtk.vtkPolyDataWriter;

public class StructuresImporter
{
	
	public static void importFromShapefile(StructureModel model, Path shapeFile, GenericPolyhedralModel body) throws IOException
	{
		if (model instanceof PointModel)
		{
			importFromShapefile((PointModel)model, shapeFile);
			return;
		}
		if (model instanceof LineModel)
		{
			importFromShapeFile((LineModel)model, shapeFile);
			return;
		}
		if (model instanceof AbstractEllipsePolygonModel)
		{
			importFromShapeFile((AbstractEllipsePolygonModel)model, shapeFile, body);
			return;
		}
	}

	public static void importFromShapefile(PointModel model, Path shapeFile) throws IOException
	{
		Collection<PointStructure> sc=ShapefileUtil.readPointStructures(shapeFile);
		for (PointStructure s : sc)
		{
			Color c=s.getPointStyle().getColor();
			int[] color=new int[]{c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()};
			String label=s.getLabel();
			if (label==null)
				label="";
			//
			model.addNewStructure(s.getCentroid());
			int id=model.getNumberOfStructures()-1;
			model.setStructureColor(id, color);
			model.setStructureLabel(id, label);
		}
	}
	
	public static void importFromShapeFile(LineModel model, Path shapeFile) throws IOException
	{
		Collection<LineStructure> sc=ShapefileUtil.readLineStructures(shapeFile);
		for (LineStructure s : sc)
		{
			//
			Color c=s.getStyle().getLineColor();
			int[] color=new int[]{c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()};
			double w=s.getStyle().getLineWidth();
			String label=s.getLabel();
			if (label==null)
				label="";
			//
			model.addNewStructure();
			int id=model.getNumberOfStructures()-1;
			Line line=(Line)model.getStructure(id);
			for (int i=0; i<s.getNumberOfSegments(); i++)
			{
				LineSegment seg=s.getSegment(i);
				line.xyzPointList.add(new Point3D(seg.getStart()));
				if (i==s.getNumberOfSegments()-1)
					line.xyzPointList.add(new Point3D(seg.getEnd()));
			}
			model.setStructureColor(id, color);
			model.setStructureLabel(id, label);
			model.setLineWidth(w);
		}
	}
	
	public static void importFromShapeFile(AbstractEllipsePolygonModel model, Path shapeFile, GenericPolyhedralModel body) throws IOException
	{
		Collection<EllipseStructure> sc=ShapefileUtil.readEllipseStructures(shapeFile, body);
		for (EllipseStructure s : sc)
		{
			Color c=s.getStyle().getLineColor();
			int[] color=new int[]{c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()};
			double w=s.getStyle().getLineWidth();
			String label=s.getLabel();
			if (label==null)
				label="";
			//
//			double[][] startPoints=new double[s.getNumberOfSegments()][3];
//			double[][] endPoints=new double[s.getNumberOfSegments()][3];
//			for (int i=0; i<s.getNumberOfSegments(); i++)
//			{
//				startPoints[i]=s.getSegment(i).getStart();
//				endPoints[i]=s.getSegment(i).getEnd();
//			}
			//
			double[] center=s.getParameters().center;
			double majorRadius=s.getParameters().majorRadius;
			double flattening=s.getParameters().flattening;
			double angle=s.getParameters().angle;
			//
			model.addNewStructure(center, majorRadius, flattening, angle);
			int id=model.getNumberOfStructures()-1;
			model.setStructureColor(id, color);
			model.setStructureLabel(id, label);
			model.setLineWidth(w);
		}
		
	/*	vtkAppendPolyData append=new vtkAppendPolyData();
		for (int i=0; i<model.getNumberOfStructures(); i++)
			append.AddInputData(model.getPolygons().get(i).boundaryPolyData);
		append.Update();
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetInputData(append.GetOutput());
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.Write();
		
		vtkAppendPolyData append2=new vtkAppendPolyData();
		for (int i=0; i<model.getNumberOfStructures(); i++)
			append2.AddInputData(model.getPolygons().get(i).interiorPolyData);
		append2.Update();

		vtkPolyDataWriter writer2=new vtkPolyDataWriter();
		writer2.SetInputData(append2.GetOutput());
		writer2.SetFileName("/Users/zimmemi1/Desktop/test2.vtk");
		writer2.SetFileTypeToBinary();
		writer2.Write();*/
}
	
}
