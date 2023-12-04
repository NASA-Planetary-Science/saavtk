package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineSegment;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.ShapefileUtil;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.StructureType;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

@Deprecated // ... for the moment... see AbstractStructureMappingControlPanel... nested class LoadEsriShapeFileAction
public class StructuresImporter
{
	public static void importFromShapeFile(LineModel<PolyLine> aLineManager, Path aFile, GenericPolyhedralModel aBody)
			throws IOException
	{
		int nextId = StructureMiscUtil.calcNextId(aLineManager);
		List<PolyLine> fullL = new ArrayList<>(aLineManager.getAllItems());

		// Load the the ERSI lines
		Collection<LineStructure> loadL = ShapefileUtil.readLineStructures(aFile);

		// Convert the ESRI lines to SBMT lines
		for (LineStructure aErsiLine : loadL)
		{
			Color color = aErsiLine.getLineStyle().getColor();
			double w = aErsiLine.getLineStyle().getWidth();
			String label = aErsiLine.getLabel();
			if (label == null)
				label = "";

			// Synthesize the control points from the provided 3D points
			//
			// Note: The logic below extracts the (LatLon) control points from the provided
			// 3D positions. It is not clear what the 3D positions actually represent. It is
			// not believed this logic has been tested (in current form or previous form).
			// -lopeznr1 2019Oct15
			List<LatLon> controlPointL = new ArrayList<>();
			for (int i = 0; i < aErsiLine.getNumberOfSegments(); i++)
			{
				// List<LineSegment> subsegments=forceCylindricalProjection(aEsri.getSegment(i),
				// body, Math.sqrt(aBody.getMeanCellArea()));
				// for (int j=0; j<subsegments.size(); j++)
				{
					LineSegment seg = aErsiLine.getSegment(i);
					Vector3D begPt = seg.getStart();
					Vector3D endPt = seg.getEnd();

					// Transform the 3D point to LatLon
					LatLon begLL = MathUtil.reclat(begPt.toArray());
					LatLon endLL = MathUtil.reclat(endPt.toArray());

					// Store the control point(s)
					controlPointL.add(begLL);
					if (i == aErsiLine.getNumberOfSegments() - 1)
						controlPointL.add(endLL);
				}
			}

			// Synthesize the (SBMT) line
			PolyLine tmpLine = new PolyLine(nextId, null, controlPointL);
			tmpLine.setColor(color);
			tmpLine.setLabel(label);
			// TODO: Currently (2019Oct15) there is no support for individual line widths
//			tmpLine.setLineWidth(w);
			nextId++;

			fullL.add(tmpLine);
		}

		aLineManager.setAllItems(fullL);
	}

	public static void importFromShapeFile(AnyStructureManager aEllipseManager, Path shapeFile,
			GenericPolyhedralModel body) throws IOException
	{
		Collection<EllipseStructure> sc = ShapefileUtil.readEllipseStructures(shapeFile, body);
		for (EllipseStructure s : sc)
		{
			Color color = s.getLineStyle().getColor();
//			double w = s.getLineStyle().getWidth();
			String label = s.getLabel();
			if (label == null)
				label = "";
//
//			double[][] startPoints=new double[s.getNumberOfSegments()][3];
//			double[][] endPoints=new double[s.getNumberOfSegments()][3];
//			for (int i=0; i<s.getNumberOfSegments(); i++)
//			{
//				startPoints[i]=s.getSegment(i).getStart();
//				endPoints[i]=s.getSegment(i).getEnd();
//			}
//
			Vector3D center = s.getParameters().center;
			double majorRadius = s.getParameters().majorRadius;
			double flattening = s.getParameters().flattening;
			double angle = s.getParameters().angle;

			int id = aEllipseManager.getNumItems() - 1;
			var tmpItem = new Ellipse(id, null, StructureType.Ellipse, center, majorRadius, angle, flattening, color);
			tmpItem.setLabel(label);
			aEllipseManager.addItem(tmpItem);


			aEllipseManager.setColor(tmpItem, color);
			aEllipseManager.setLabel(tmpItem, label);
//			aEllipseManager.setLineWidth(w);
		}

//			vtkAppendPolyData append=new vtkAppendPolyData();
//			for (int i=0; i<model.getNumItems(); i++)
//				append.AddInputData(model.getPolygons().get(i).boundaryPolyData);
//			append.Update();
//
//			vtkPolyDataWriter writer=new vtkPolyDataWriter();
//			writer.SetInputData(append.GetOutput());
//			writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
//			writer.SetFileTypeToBinary();
//			writer.Write();
//
//			vtkAppendPolyData append2=new vtkAppendPolyData();
//			for (int i=0; i<model.getNumItems(); i++)
//				append2.AddInputData(model.getPolygons().get(i).interiorPolyData);
//			append2.Update();
//
//			vtkPolyDataWriter writer2=new vtkPolyDataWriter();
//			writer2.SetInputData(append2.GetOutput());
//			writer2.SetFileName("/Users/zimmemi1/Desktop/test2.vtk");
//			writer2.SetFileTypeToBinary();
//			writer2.Write();
	}

}
