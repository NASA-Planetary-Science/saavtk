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
import edu.jhuapl.saavtk.model.structure.esri.LineSegment;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineStyle;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStyle;
import edu.jhuapl.saavtk.model.structure.esri.ShapefileUtil;
import edu.jhuapl.saavtk.model.structure.esri.VtkFileUtil;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.util.MathUtil;

public class StructuresExporter
{
	public static void exportPointsToShapefile(AnyStructureManager aPointManager, Collection<Structure> aItemC, Path aShapeFile) throws IOException
	{
		List<PointStructure> structuresToWrite = new ArrayList<>();

		// Export the structures
		for (var aItem : aItemC)
		{
			Color c = aItem.getColor();
			double sz = aPointManager.getDiameter(aItem);
			PointStyle style = new PointStyle(c, sz);
			String label = aItem.getLabel();
			Vector3D location = aPointManager.getCenter(aItem);
			PointStructure ps=new PointStructure(location);
			ps.setPointStyle(style);
			ps.setLabel(label);
			structuresToWrite.add(ps);
		}
		ShapefileUtil.writePointStructures(structuresToWrite, aShapeFile);
	}

	public static void exportToShapefile(LineModel<PolyLine> aLineManager, Path aShapeFile) throws IOException//, boolean closed) throws IOException
	{
		List<LineStructure> structuresToWrite = new ArrayList<>();

		// Determine the structures to be exported
		Collection<PolyLine> tmpC = aLineManager.getSelectedItems();
		if (tmpC.isEmpty() == true)
			tmpC = aLineManager.getAllItems();

		// Export the structures
		for (PolyLine aItem : tmpC)
		{
			List<Vector3D> xyzPointL = aLineManager.getXyzPointsFor(aItem);

			if (xyzPointL.size()<2)
				continue;

			Color c = aItem.getColor();
			double w = aLineManager.getRenderAttr().lineWidth();
			LineStyle style = new LineStyle(c, w);

			//int nSegments=closed?line.xyzPointList.size():(line.xyzPointList.size()-1);
			int nSegments= xyzPointL.size()-1;

			List<LineSegment> segments=Lists.newArrayList();
			for (int j = 0; j < nSegments; j++)
			{
				int jp=j+1;
				Vector3D start=xyzPointL.get(j);
				Vector3D  end=xyzPointL.get(jp);
				segments.add(new LineSegment(start, end));
			}
			String label = aItem.getLabel();
			LineStructure ls=new LineStructure(segments);
			ls.setLineStyle(style);
			ls.setLabel(label);
			structuresToWrite.add(ls);
		}
		ShapefileUtil.writeLineStructures(structuresToWrite, aShapeFile);
	}

    public static String generateDefaultShapefileName(PolyhedralModel body, String modelName)
    {

    	return body.getModelName().replaceAll(" ", "_").replaceAll("/", "-")+"."+modelName+".shp";
    }

    public static void exportToVtkFile(AnyStructureManager aManager, Collection<Structure> aItemC, Path vtkFile, boolean multipleFiles)
    {
		Map<Integer, LineStructure> structuresToWrite = new HashMap<>();

 		// Export the structures
 		for (var aItem : aItemC)
 		{
 			if (aItem instanceof PolyLine == false)
 				return;

 			List<Vector3D> xyzPointL = aManager.getXyzPointsFor(aItem);

			if (xyzPointL.size()<2)
				continue;
			Color c = aItem.getColor();
			double w = aManager.getRenderAttr().lineWidth();
			LineStyle style = new LineStyle(c, w);

			//int nSegments=closed?line.xyzPointList.size():(line.xyzPointList.size()-1);
			int nSegments= xyzPointL.size()-1;

			List<LineSegment> segments=Lists.newArrayList();
			for (int j = 0; j < nSegments; j++)
			{
				int jp=j+1;
				Vector3D start= xyzPointL.get(j);
				Vector3D end= xyzPointL.get(jp);
				segments.add(new LineSegment(start, end));
			}
			String label = aItem.getLabel();


			var controlPoints = new ArrayList<Vector3D>();
			if (aItem instanceof PolyLine aPolyLine)
				for (int j = 0; j < aPolyLine.getControlPoints().size(); j++)
					controlPoints.add(new Vector3D(MathUtil.latrec(aPolyLine.getControlPoints().get(j))));
			else if (aItem instanceof Polygon aPolygon)
				for (int j = 0; j < aPolygon.getControlPoints().size(); j++)
					controlPoints.add(new Vector3D(MathUtil.latrec(aPolygon.getControlPoints().get(j))));
			else
				throw new Error("Unsupported structure: " + aItem);

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
