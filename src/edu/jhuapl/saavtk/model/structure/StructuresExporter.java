package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.StructureModel.Structure;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineSegment;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineStyle;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStyle;
import edu.jhuapl.saavtk.model.structure.esri.ShapefileUtil;
import edu.jhuapl.saavtk.model.structure.esri.VtkFileUtil;
import edu.jhuapl.saavtk.util.MathUtil;

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
			PointStructure ps=new PointStructure(new Vector3D(location));
			ps.setPointStyle(style);
			ps.setLabel(label);
			structuresToWrite.add(ps);
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
				
			List<LineSegment> segments=Lists.newArrayList();
			for (int j = 0; j < nSegments; j++)
			{
				int jp=j+1;
				double[] start=line.xyzPointList.get(j).xyz;
				double[] end=line.xyzPointList.get(jp).xyz;
				segments.add(new LineSegment(new Vector3D(start), new Vector3D(end)));
			}
			String label = line.getLabel();
			LineStructure ls=new LineStructure(segments);
			ls.setLineStyle(style);
			ls.setLabel(label);
			structuresToWrite.add(ls);
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

    public static void exportToVtkFile(PointModel model, Path vtkFile, boolean multipleFiles)
    {
    	Map<Integer, PointStructure> structuresToWrite=Maps.newHashMap();
		int[] ids = model.getSelectedStructures();
		if (ids.length == 0)
		{
			ids = new int[model.getNumberOfStructures()];
			for (int i = 0; i < ids.length; i++)
				ids[i] = i;
		}
		for (int i = 0; i < ids.length; i++)
		{
			double[] center=model.getStructureCenter(ids[i]);
			Color color=model.getStructureColor(ids[i]);
			// TODO: finish this
		}    	
    }

    public static void exportToVtkFile(LineModel model, Path vtkFile, boolean multipleFiles)
    {
		Map<Integer, LineStructure> structuresToWrite = Maps.newHashMap();
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
				
			List<LineSegment> segments=Lists.newArrayList();
			for (int j = 0; j < nSegments; j++)
			{
				int jp=j+1;
				double[] start=line.xyzPointList.get(j).xyz;
				double[] end=line.xyzPointList.get(jp).xyz;
				segments.add(new LineSegment(new Vector3D(start), new Vector3D(end)));
			}
			String label = line.getLabel();
			
			
			List<Vector3D> controlPoints=Lists.newArrayList();
			for (int j=0; j<line.getControlPoints().size(); j++)
			{
				controlPoints.add(new Vector3D(MathUtil.latrec(line.getControlPoints().get(j))));
			}
			
			LineStructure ls=new LineStructure(segments, controlPoints);
			ls.setLineStyle(style);
			ls.setLabel(label);
			structuresToWrite.put(ids[i], ls);
		}
		if (multipleFiles)
		{
			for (int id : structuresToWrite.keySet())
			{
				Path file=Paths.get(vtkFile.toAbsolutePath().toString().replace(".vtk", "-"+id+".vtk"));
				VtkFileUtil.writeLineStructures(Lists.newArrayList(structuresToWrite.get(id)), file);
			}
		}
		else
		{
			VtkFileUtil.writeLineStructures(Lists.newArrayList(structuresToWrite.values()), vtkFile);
		}
			
		
    }
    
}
