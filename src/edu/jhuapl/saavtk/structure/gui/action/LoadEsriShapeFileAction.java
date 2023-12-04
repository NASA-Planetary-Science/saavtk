package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.util.FileExtensionsAndDescriptions;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.esri.FeatureUtil;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Point;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureType;
import edu.jhuapl.saavtk.structure.io.BennuStructuresEsriIO;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

/**
 * Object that defines the behavior associated with this action.
 * <p>
 * This class was originally part of the single monolithic file:
 * edu.jhuaple.saavtk.gui.panel.AbstractStructureMappingControlPanel.
 * <p>
 * Subclasses that were implementations of {@link Action} have been refactored
 * to the package edu.jhuapl.saavtk.structure.gui.action on ~2019Sep09.
 */
public class LoadEsriShapeFileAction<G1 extends Structure> extends AbstractAction
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final Component refParent;
	private final PolyhedralModel refSmallBody;
	private final AnyStructureManager refStructureManager;
	private final StatusNotifier refStatusNotifier;
	private final StructureType refType;

	public LoadEsriShapeFileAction(Component aParent, PolyhedralModel aSmallBody, AnyStructureManager aManager,
			StatusNotifier aStatusNotifier, StructureType aType, String aName)
	{
		super(aName);

		refParent = aParent;
		refSmallBody = aSmallBody;
		refStructureManager = aManager;
		refStatusNotifier = aStatusNotifier;
		refType = aType;
	}

	protected void updateStatusBar(Feature f, int m, int mtot)
	{
		refStatusNotifier.setPriStatus(
				"Loading " + f.getDefaultGeometryProperty().getClass().getSimpleName() + " [" + (m + 1) + "/" + mtot + "]",
				null);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String fileMenuTitle = null;
		if (refType == StructureType.Point)
			fileMenuTitle = "Points from shapefile...";
		else if (refType == StructureType.Path)
			fileMenuTitle = "Path from shapefile...";
		else if (refType == StructureType.Polygon)
			fileMenuTitle = "Polygon from shapefile...";
		else
			fileMenuTitle = "Datastore filename";
		File[] files = CustomFileChooser.showOpenDialog(refParent, fileMenuTitle, Lists.newArrayList(FileExtensionsAndDescriptions.SHAPE), true);
		if (files == null)
			return;

		for (int p = 0; p < files.length; p++)
		{
			File file = files[p];
			if (file == null)
			{
				System.out.println("No file selected, or file not found");
				return;
			}

			// Bail if the file does not exist
			Path filePath = file.toPath().toAbsolutePath();
			if (filePath.toFile().exists() == false)
				return;

//			//String prefix = FilenameUtils.getFullPath(file.toString()) + FilenameUtils.getBaseName(file.toString());
//			String prefix = FilenameUtils.getFullPath(file.toString()) + FilenameUtils.getName(file.toString());
//			//System.out.println(prefix);
//			int idx1 = prefix.lastIndexOf('.');
//			prefix = prefix.substring(0, idx1);
//			int idx2 = prefix.lastIndexOf('.');
//			if (idx2<0)
//				{
//					 int result=JOptionPane.showConfirmDialog(null, "The file \""+file.toString()+"\" does not conform to the SBMT shapefile naming convention.\nOpen file-renaming tool?");
//					 if (result==JOptionPane.YES_OPTION)
//					 {
//						 SBMTShapefileRenamer renamingPanel=new SBMTShapefileRenamer(file.getAbsolutePath());
//						 result=JOptionPane.showConfirmDialog(null, renamingPanel, "Non-conforming shapefile name", JOptionPane.OK_CANCEL_OPTION);
//						 if (result==JOptionPane.OK_OPTION)
//						 {
//							 prefix=renamingPanel.rename();
//							 System.out.println(prefix);
//							 idx1 = prefix.lastIndexOf('.');
//							 prefix = prefix.substring(0, idx1);
//							 idx2 = prefix.lastIndexOf('.');
//						 }
//						 else
//							 return;
//					 }
//					 else
//						 return;
//				};
//			prefix = prefix.substring(0, idx2);
//			//System.out.println("prefix=" +prefix);

//			if (structureModel == modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES))
//			{
//				if (filePath.toFile().exists())
//				{
//					FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.ellipseType);
//					FeatureIterator<Feature> it = features.features();
//					while (it.hasNext())
//					{
//						Feature f = it.next();
//						EllipseStructure es = FeatureUtil.createEllipseStructureFrom((SimpleFeature) f, (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));
//						EllipseModel model = (EllipseModel) modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);
//						model.addNewStructure(es.getCentroid(), es.getParameters().majorRadius, es.getParameters().flattening, es.getParameters().angle);
//					}
//					it.close();
//				}
//			}
//			else if (structureModel == modelManager.getModel(ModelNames.CIRCLE_STRUCTURES))
//			{
//				if (filePath.toFile().exists())
//				{
//
//					FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.ellipseType);
//					FeatureIterator<Feature> it = features.features();
//					while (it.hasNext())
//					{
//						Feature f = it.next();
//						EllipseStructure es = FeatureUtil.createEllipseStructureFrom((SimpleFeature) f, (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));
//						CircleModel model = (CircleModel) modelManager.getModel(ModelNames.CIRCLE_STRUCTURES);
//						model.addNewStructure(es.getCentroid(), es.getParameters().majorRadius, es.getParameters().flattening, es.getParameters().angle);
//					}
//					it.close();
//				}
//			}
			if (refType == StructureType.Point)
			{
				FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.pointType);
				FeatureIterator<Feature> it = features.features();
				List<Feature> flist = Lists.newArrayList();
				while (it.hasNext())
				{
					flist.add(it.next());
				}
				it.close();

				var fullL = new ArrayList<>(refStructureManager.getAllItems());
				var nextId = StructureMiscUtil.calcNextId(refStructureManager);

				for (int m = 0; m < flist.size(); m++)
				{
					Feature f = flist.get(m);
					updateStatusBar(f, m, flist.size());
					PointStructure ps = FeatureUtil.createPointStructureFrom((SimpleFeature) flist.get(m), refSmallBody);

					// Instantiate the Point
					var tmpItem = new Point(nextId, null, ps.getCentroid(), Color.MAGENTA);
					fullL.add(tmpItem);
					nextId++;
				}

				// Install the points
				refStructureManager.setAllItems(fullL);
			}
			else if (refType == StructureType.Path)
			{
				// Load the file
				FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.lineType);
				FeatureIterator<Feature> it = features.features();
				List<Feature> flist = Lists.newArrayList();
				while (it.hasNext())
				{
					flist.add(it.next());
				}
				it.close();

				// Retrieve the LineManager and the current list of installed lines
				// Note that lines are appended to the LineManager rather than replaced
				var fullL = new ArrayList<>(refStructureManager.getAllItems());
				var nextId = StructureMiscUtil.calcNextId(refStructureManager);

				// Transform (ESRI) features to SBMT lines
				for (int m = 0; m < flist.size(); m++)
				{
					Feature f = flist.get(m);
					updateStatusBar(f, m, flist.size());
					// System.out.println("reading line structure: "+f);
					LineStructure ls = FeatureUtil.createLineStructureFrom((SimpleFeature) f, refSmallBody);

					// Synthesize the control points
					List<LatLon> controlPointL = new ArrayList<>();
					for (int i = 0; i <= ls.getNumberOfSegments(); i++)
					{
						// subdivide segment
						double[] pt;
						if (i == ls.getNumberOfSegments())
							pt = ls.getSegment(i - 1).getEnd().toArray();
						else
							pt = ls.getSegment(i).getStart().toArray();
						LatLon latlon = MathUtil.reclat(pt);
						// System.out.println(latlon.lat + " " + latlon.lon);
						double[] intersectPoint = new double[3];
						refSmallBody.getPointAndCellIdFromLatLon(latlon.lat, latlon.lon, intersectPoint);

						LatLon tmpLL = MathUtil.reclat(intersectPoint);
						controlPointL.add(tmpLL);
					}

					// Instantiate the line
					PolyLine tmpItem = new PolyLine(nextId, null, controlPointL);
					fullL.add(tmpItem);
					nextId++;
				}

				// Install the lines
				refStructureManager.setAllItems(fullL);
			}
			else if (refType == StructureType.Polygon)
			{
				// Load the file
				FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.lineType);
				FeatureIterator<Feature> it = features.features();
				List<Feature> flist = Lists.newArrayList();
				while (it.hasNext())
				{
					flist.add(it.next());
				}
				it.close();

				// Retrieve the PolygonManager and the current list of installed lines
				// Note that polygons are appended to the LineManager rather than replaced
				var fullL = new ArrayList<>(refStructureManager.getAllItems());
				var nextId = StructureMiscUtil.calcNextId(refStructureManager);

				// Transform (ESRI) features to SBMT lines
				for (int m = 0; m < flist.size(); m++)
				{
					Feature f = flist.get(m);
					updateStatusBar(f, m, flist.size());
					// System.out.println("reading line structure: "+f);
					LineStructure ls = FeatureUtil.createLineStructureFrom((SimpleFeature) f, refSmallBody);

					// Synthesize the control points
					List<LatLon> controlPointL = new ArrayList<>();
					for (int i = 0; i <= ls.getNumberOfSegments(); i++)
					{
						// subdivide segment
						double[] pt;
						if (i == ls.getNumberOfSegments())
							pt = ls.getSegment(i - 1).getEnd().toArray();
						else
							pt = ls.getSegment(i).getStart().toArray();
						LatLon latlon = MathUtil.reclat(pt);
						// System.out.println(latlon.lat + " " + latlon.lon);
						double[] intersectPoint = new double[3];
						refSmallBody.getPointAndCellIdFromLatLon(latlon.lat, latlon.lon, intersectPoint);

						LatLon tmpLL = MathUtil.reclat(intersectPoint);
						controlPointL.add(tmpLL);
					}

					// Instantiate the polygon
					Polygon tmpItem = new Polygon(nextId, null, controlPointL);
					fullL.add(tmpItem);
					nextId++;
				}

				// Install the polygons
				refStructureManager.setAllItems(fullL);
			}
		}
	}
}
