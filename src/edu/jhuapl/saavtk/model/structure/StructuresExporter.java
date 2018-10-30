package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.StructureModel.Structure;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.EllipsePolygon;
import edu.jhuapl.saavtk.model.structure.geotools.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.geotools.EllipseStructure.Parameters;
import edu.jhuapl.saavtk.model.structure.geotools.LineSegment;
import edu.jhuapl.saavtk.model.structure.geotools.LineStructure;
import edu.jhuapl.saavtk.model.structure.geotools.LineStyle;
import edu.jhuapl.saavtk.model.structure.geotools.PointStructure;
import edu.jhuapl.saavtk.model.structure.geotools.PointStyle;
import edu.jhuapl.saavtk.model.structure.geotools.ShapefileUtil;
import edu.jhuapl.saavtk.model.structure.geotools.VtkFileUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyDataWriter;

public class StructuresExporter
{

	public static void exportToShapefile(StructureModel model, Path shapeFile) throws IOException
	{
		if (model instanceof PointModel)
		{
			exportToShapefile((PointModel) model, shapeFile);
			return;
		}
		if (model instanceof LineModel || model instanceof PolygonModel)
		{
			exportToShapefile((LineModel) model, shapeFile);
			return;
		}
		if (model instanceof AbstractEllipsePolygonModel)
		{
			exportToShapefile((AbstractEllipsePolygonModel) model, shapeFile);
			return;
		}
	}

	public static void exportToShapefile(PointModel model, Path shapeFile) throws IOException
	{
		List<PointStructure> structuresToWrite = Lists.newArrayList();
		int[] ids = model.getSelectedStructures();
		if (ids.length == 0)
		{
			ids = new int[model.getNumberOfStructures()];
			for (int i = 0; i < ids.length; i++)
				ids[i] = i;
		}
		for (int i = 0; i < ids.length; i++)
		{
			Structure s = model.getStructure(ids[i]);
			int[] c = s.getColor();
			double sz = model.getStructureSize(ids[i]);
			PointStyle style = new PointStyle(new Color(c[0], c[1], c[2]), sz);
			String label = s.getLabel();
			double[] location = model.getStructureCenter(ids[i]);
			structuresToWrite.add(new PointStructure(location, style, label));
		}
		ShapefileUtil.writePointStructures(structuresToWrite, shapeFile);
	}

	public static void exportToShapefile(LineModel model, Path shapeFile) throws IOException//, boolean closed) throws IOException
	{
		List<LineStructure> structuresToWrite = Lists.newArrayList();
		int[] ids = model.getSelectedStructures();
		if (ids.length == 0)
		{
			ids = new int[model.getNumberOfStructures()];
			for (int i = 0; i < ids.length; i++)
				ids[i] = i;
		}
		for (int i = 0; i < ids.length; i++)
		{
			Line line = model.getLines().get(ids[i]);
			if (line.xyzPointList.size()<2)
				continue;
			int[] c = line.getColor();
			double w = model.getLineWidth();
			LineStyle style = new LineStyle(new Color(c[0], c[1], c[2]), w);
			
			//int nSegments=closed?line.xyzPointList.size():(line.xyzPointList.size()-1);
			int nSegments=line.xyzPointList.size()-1;
				
			LineSegment[] segments=new LineSegment[nSegments];
			for (int j = 0; j < nSegments; j++)
			{
				int jp=j+1;
				double[] start=line.xyzPointList.get(j).xyz;
				double[] end=line.xyzPointList.get(jp).xyz;
				segments[j]=new LineSegment(start, end);
			}
			String label = line.getLabel();
			structuresToWrite.add(new LineStructure(segments, style, label));
		}
		ShapefileUtil.writeLineStructures(structuresToWrite, shapeFile);
	}

	public static void exportToShapefile(AbstractEllipsePolygonModel model, Path shapeFile) throws IOException
	{
		ShapefileUtil.writeEllipseStructures(EllipseStructure.fromSbmtStructure(model), shapeFile);
	}

	
    public static String generateDefaultShapefileName(PolyhedralModel body, String modelName)
    {
    	
    	return body.getModelName().replaceAll(" ", "_").replaceAll("/", "-")+"."+modelName+".shp"; 
    }

    public static void exportToVtkFile(LineModel model, Path vtkFile)
    {
		List<LineStructure> structuresToWrite = Lists.newArrayList();
		int[] ids = model.getSelectedStructures();
		if (ids.length == 0)
		{
			ids = new int[model.getNumberOfStructures()];
			for (int i = 0; i < ids.length; i++)
				ids[i] = i;
		}
		for (int i = 0; i < ids.length; i++)
		{
			Line line = model.getLines().get(ids[i]);
			if (line.xyzPointList.size()<2)
				continue;
			int[] c = line.getColor();
			double w = model.getLineWidth();
			LineStyle style = new LineStyle(new Color(c[0], c[1], c[2]), w);
			
			//int nSegments=closed?line.xyzPointList.size():(line.xyzPointList.size()-1);
			int nSegments=line.xyzPointList.size()-1;
				
			LineSegment[] segments=new LineSegment[nSegments];
			for (int j = 0; j < nSegments; j++)
			{
				int jp=j+1;
				double[] start=line.xyzPointList.get(j).xyz;
				double[] end=line.xyzPointList.get(jp).xyz;
				segments[j]=new LineSegment(start, end);
			}
			String label = line.getLabel();
			
			
			Vector3D[] controlPoints=new Vector3D[line.controlPoints.size()];
			for (int j=0; j<line.controlPoints.size(); j++)
			{
				controlPoints[j]=new Vector3D(MathUtil.latrec(line.controlPoints.get(j)));
			}
			
			structuresToWrite.add(new LineStructure(segments, style, label, controlPoints));
			
			VtkFileUtil.writeLineStructures(structuresToWrite, vtkFile);
		}
		
    }
    
}
