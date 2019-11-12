package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineSegment;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineStyle;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStyle;
import edu.jhuapl.saavtk.model.structure.esri.ShapefileUtil;
import edu.jhuapl.saavtk.model.structure.esri.VtkFileUtil;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.util.MathUtil;

public class StructuresExporter
{

	public static void exportToShapefile(StructureManager<?> model, Path shapeFile) throws IOException
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

	public static void exportToShapefile(PointModel aPointManager, Path aShapeFile) throws IOException
	{
		List<PointStructure> structuresToWrite = new ArrayList<>();

		// Determine the structures to be exported
		Collection<EllipsePolygon> tmpC = aPointManager.getSelectedItems();
		if (tmpC.isEmpty() == true)
			tmpC = aPointManager.getAllItems();

		// Export the structures
		for (EllipsePolygon aItem : tmpC)
		{
			Color c = aItem.getColor();
			double sz = aPointManager.getStructureSize(aItem);
			PointStyle style = new PointStyle(c, sz);
			String label = aItem.getLabel();
			double[] location = aPointManager.getStructureCenter(aItem);
			PointStructure ps=new PointStructure(new Vector3D(location));
			ps.setPointStyle(style);
			ps.setLabel(label);
			structuresToWrite.add(ps);
		}
		ShapefileUtil.writePointStructures(structuresToWrite, aShapeFile);
	}

	public static void exportToShapefile(LineModel<Line> aLineManager, Path aShapeFile) throws IOException//, boolean closed) throws IOException
	{
		List<LineStructure> structuresToWrite = new ArrayList<>();

		// Determine the structures to be exported
		Collection<Line> tmpC = aLineManager.getSelectedItems();
		if (tmpC.isEmpty() == true)
			tmpC = aLineManager.getAllItems();

		// Export the structures
		for (Line aItem : tmpC)
		{
			if (aItem.xyzPointList.size()<2)
				continue;

			Color c = aItem.getColor();
			double w = aLineManager.getLineWidth();
			LineStyle style = new LineStyle(c, w);

			//int nSegments=closed?line.xyzPointList.size():(line.xyzPointList.size()-1);
			int nSegments=aItem.xyzPointList.size()-1;

			List<LineSegment> segments=Lists.newArrayList();
			for (int j = 0; j < nSegments; j++)
			{
				int jp=j+1;
				double[] start=aItem.xyzPointList.get(j).xyz;
				double[] end=aItem.xyzPointList.get(jp).xyz;
				segments.add(new LineSegment(new Vector3D(start), new Vector3D(end)));
			}
			String label = aItem.getLabel();
			LineStructure ls=new LineStructure(segments);
			ls.setLineStyle(style);
			ls.setLabel(label);
			structuresToWrite.add(ls);
		}
		ShapefileUtil.writeLineStructures(structuresToWrite, aShapeFile);
	}

	public static void exportToShapefile(AbstractEllipsePolygonModel model, Path shapeFile) throws IOException
	{
		ShapefileUtil.writeEllipseStructures(EllipseStructure.fromSbmtStructure(model), shapeFile);
	}


    public static String generateDefaultShapefileName(PolyhedralModel body, String modelName)
    {

    	return body.getModelName().replaceAll(" ", "_").replaceAll("/", "-")+"."+modelName+".shp";
    }

    public static void exportToVtkFile(PointModel aPointManager, Path vtkFile, boolean multipleFiles)
    {
 		// Determine the structures to be exported
 		Collection<EllipsePolygon> tmpC = aPointManager.getSelectedItems();
 		if (tmpC.isEmpty() == true)
 			tmpC = aPointManager.getAllItems();

    	Map<Integer, PointStructure> structuresToWrite = new HashMap<>();

    	// Export the structures
 		for (EllipsePolygon aItem : tmpC)
		{
			double[] center=aPointManager.getStructureCenter(aItem);
			Color color=aPointManager.getStructureColor(aItem);
			// TODO: finish this
		}
    }

    public static void exportToVtkFile(LineModel<Line> aLineManager, Path vtkFile, boolean multipleFiles)
    {
 		// Determine the structures to be exported
 		Collection<Line> tmpC = aLineManager.getSelectedItems();
 		if (tmpC.isEmpty() == true)
 			tmpC = aLineManager.getAllItems();

		Map<Integer, LineStructure> structuresToWrite = new HashMap<>();

 		// Export the structures
 		for (Line aItem : tmpC)
 		{
			if (aItem.xyzPointList.size()<2)
				continue;
			Color c = aItem.getColor();
			double w = aLineManager.getLineWidth();
			LineStyle style = new LineStyle(c, w);

			//int nSegments=closed?line.xyzPointList.size():(line.xyzPointList.size()-1);
			int nSegments=aItem.xyzPointList.size()-1;

			List<LineSegment> segments=Lists.newArrayList();
			for (int j = 0; j < nSegments; j++)
			{
				int jp=j+1;
				double[] start=aItem.xyzPointList.get(j).xyz;
				double[] end=aItem.xyzPointList.get(jp).xyz;
				segments.add(new LineSegment(new Vector3D(start), new Vector3D(end)));
			}
			String label = aItem.getLabel();


			List<Vector3D> controlPoints=Lists.newArrayList();
			for (int j=0; j<aItem.getControlPoints().size(); j++)
			{
				controlPoints.add(new Vector3D(MathUtil.latrec(aItem.getControlPoints().get(j))));
			}

			LineStructure ls=new LineStructure(segments, controlPoints);
			ls.setLineStyle(style);
			ls.setLabel(label);

			structuresToWrite.put(aItem.getId(), ls);
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
