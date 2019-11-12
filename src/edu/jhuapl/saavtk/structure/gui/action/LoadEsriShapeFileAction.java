package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.Polygon;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.model.structure.esri.FeatureUtil;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.StructurePanel;
import edu.jhuapl.saavtk.structure.io.BennuStructuresEsriIO;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

/**
 * Object that defines the behavior associated with this action.
 * <P>
 * This class was originally part of the single monolithic file:
 * edu.jhuaple.saavtk.gui.panel.AbstractStructureMappingControlPanel.
 * <P>
 * Subclasses that were implementations of {@link Action} have been refactored
 * to the package edu.jhuapl.saavtk.structure.gui.action on ~2019Sep09.
 */
public class LoadEsriShapeFileAction<G1 extends Structure> extends AbstractAction
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final StructurePanel<G1> refParent;
	private final StructureManager<G1> refStructureManager;
	private final ModelManager refModelManager;
	private final StatusBar refStatusBar;

	public LoadEsriShapeFileAction(StructurePanel<G1> aParent, StructureManager<G1> aManager, ModelManager aModelManager,
			StatusBar aStatusBar)
	{
		super("ESRI Shapefile Datastore...");

		refParent = aParent;
		refStructureManager = aManager;
		refModelManager = aModelManager;
		refStatusBar = aStatusBar;
	}

	protected void updateStatusBar(Feature f, int m, int mtot)
	{
		refStatusBar.setLeftText(
				"Loading " + f.getDefaultGeometryProperty().getClass().getSimpleName() + " [" + (m + 1) + "/" + mtot + "]");
		refStatusBar.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String fileMenuTitle = null;
		if (refStructureManager instanceof PointModel)
			fileMenuTitle = "Points from shapefile...";
		else if (refStructureManager instanceof LineModel)
			fileMenuTitle = "Path from shapefile...";
		else if (refStructureManager instanceof PolygonModel)
			fileMenuTitle = "Polygon from shapefile...";
		else
			fileMenuTitle = "Datastore filename";
		File[] files = CustomFileChooser.showOpenDialog(refParent, fileMenuTitle, Lists.newArrayList("shp"), true);
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
			Path filePath = file.toPath().toAbsolutePath();
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
			if (refStructureManager == refModelManager.getModel(ModelNames.POINT_STRUCTURES))
			{
				if (filePath.toFile().exists())
				{
					PointModel model = (PointModel) refModelManager.getModel(ModelNames.POINT_STRUCTURES);
					FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.pointType);
					FeatureIterator<Feature> it = features.features();
					List<Feature> flist = Lists.newArrayList();
					while (it.hasNext())
					{
						flist.add(it.next());
					}
					it.close();
					for (int m = 0; m < flist.size(); m++)
					{
						Feature f = flist.get(m);
						updateStatusBar(f, m, flist.size());
						PointStructure ps = FeatureUtil.createPointStructureFrom((SimpleFeature) flist.get(m),
								(GenericPolyhedralModel) refModelManager.getModel(ModelNames.SMALL_BODY));
						model.addNewStructure(ps.getCentroid().toArray());
					}
					model.activateStructure(null);
				}
			}
			else if (refStructureManager == refModelManager.getModel(ModelNames.LINE_STRUCTURES))
			{
				if (filePath.toFile().exists())
				{
					LineModel<Line> model = (LineModel) refModelManager.getModel(ModelNames.LINE_STRUCTURES);
					FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.lineType);
					FeatureIterator<Feature> it = features.features();
					List<Feature> flist = Lists.newArrayList();
					while (it.hasNext())
					{
						flist.add(it.next());
					}
					it.close();
					for (int m = 0; m < flist.size(); m++)
					{
						Feature f = flist.get(m);
						updateStatusBar(f, m, flist.size());
						// System.out.println("reading line structure: "+f);
						LineStructure ls = FeatureUtil.createLineStructureFrom((SimpleFeature) f,
								(GenericPolyhedralModel) refModelManager.getModel(ModelNames.SMALL_BODY));

						Line tmpItem = model.addNewStructure();
						model.activateStructure(tmpItem);
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
							GenericPolyhedralModel body = (GenericPolyhedralModel) refModelManager
									.getModel(ModelNames.SMALL_BODY);
							double[] intersectPoint = new double[3];
							body.getPointAndCellIdFromLatLon(latlon.lat, latlon.lon, intersectPoint);
							model.insertVertexIntoActivatedStructure(intersectPoint);
						}

					}
					it.close();
					model.activateStructure(null);
				}
			}
			else if (refStructureManager == refModelManager.getModel(ModelNames.POLYGON_STRUCTURES))
			{
				// System.out.println(filePath+" "+filePath.toFile().exists());
				if (filePath.toFile().exists())
				{
					PolygonModel model = (PolygonModel) refModelManager.getModel(ModelNames.POLYGON_STRUCTURES);
					FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.lineType);
					FeatureIterator<Feature> it = features.features();
					List<Feature> flist = Lists.newArrayList();
					while (it.hasNext())
					{
						flist.add(it.next());
					}
					it.close();
					for (int m = 0; m < flist.size(); m++)
					{
						Feature f = flist.get(m);
						updateStatusBar(f, m, flist.size());
						LineStructure ls = FeatureUtil.createLineStructureFrom((SimpleFeature) f,
								(GenericPolyhedralModel) refModelManager.getModel(ModelNames.SMALL_BODY));
						Polygon tmpItem = model.addNewStructure();
						model.activateStructure(tmpItem);
						for (int i = 0; i < ls.getNumberOfSegments(); i++)
						{
							double[] pt;
							if (i == ls.getNumberOfSegments())
								pt = ls.getSegment(i - 1).getEnd().toArray();
							else
								pt = ls.getSegment(i).getStart().toArray();
							LatLon latlon = MathUtil.reclat(pt);
							GenericPolyhedralModel body = (GenericPolyhedralModel) refModelManager
									.getModel(ModelNames.SMALL_BODY);
							double[] intersectPoint = new double[3];
							body.getPointAndCellIdFromLatLon(latlon.lat, latlon.lon, intersectPoint);
							model.insertVertexIntoActivatedStructure(intersectPoint);
						}

					}
					it.close();
					model.activateStructure(null);

				}
			}
		}
	}
}
