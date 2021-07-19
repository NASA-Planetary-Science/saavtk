package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.geotools.feature.DefaultFeatureCollection;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.FeatureUtil;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.StructurePanel;
import edu.jhuapl.saavtk.structure.io.BennuStructuresEsriIO;

/**
 * Object that defines the behavior associated with this action.
 * <P>
 * This class was originally part of the single monolithic file:
 * edu.jhuaple.saavtk.gui.panel.AbstractStructureMappingControlPanel.
 * <P>
 * Subclasses that were implementations of {@link Action} have been refactored
 * to the package edu.jhuapl.saavtk.structure.gui.action on ~2019Sep09.
 */
public class SaveEsriShapeFileAction<G1 extends Structure> extends AbstractAction
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final StructurePanel<G1> refParent;
	private final StructureManager<G1> refStructureManager;
	private final ModelManager refModelManager;

	public SaveEsriShapeFileAction(StructurePanel<G1> aParent, StructureManager<G1> aManager, ModelManager aModelManager)
	{
		super("ESRI Shapefile Datastore...");

		refParent = aParent;
		refStructureManager = aManager;
		refModelManager = aModelManager;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String fileMenuTitle = null;
		if (refStructureManager instanceof PointModel)
			fileMenuTitle = "Export points to ESRI shapefile...";
		else if (refStructureManager instanceof LineModel)
			fileMenuTitle = "Export paths as ESRI lines...";
		else if (refStructureManager instanceof PolygonModel)
			fileMenuTitle = "Export polygons to ESRI shapefile...";
		else if (refStructureManager instanceof EllipseModel)
			fileMenuTitle = "Export ellipses as ESRI polygons...";
		else if (refStructureManager instanceof CircleModel)
			fileMenuTitle = "Export circles as ESRI polygons...";
		else
			fileMenuTitle = "Datastore filename";
		File file = CustomFileChooser.showSaveDialog(refParent, fileMenuTitle, "myDataStore", "shp");
		if (file == null)
			return;
		if (!file.getName().endsWith(".shp"))
			file = new File(file.getName() + ".shp");

		if (refStructureManager == refModelManager.getModel(ModelNames.ELLIPSE_STRUCTURES).get(0))
		{
			List<EllipseStructure> ellipses = EllipseStructure.fromSbmtStructure(
					(AbstractEllipsePolygonModel) refModelManager.getModel(ModelNames.ELLIPSE_STRUCTURES).get(0));
			DefaultFeatureCollection ellipseFeatures = new DefaultFeatureCollection();
			for (int i = 0; i < ellipses.size(); i++)
				ellipseFeatures.add(FeatureUtil.createFeatureFrom(ellipses.get(i)));
			BennuStructuresEsriIO.write(file.toPath(), ellipseFeatures, FeatureUtil.ellipseType);
		}
		else if (refStructureManager == refModelManager.getModel(ModelNames.CIRCLE_STRUCTURES).get(0))
		{
			List<EllipseStructure> circles = EllipseStructure
					.fromSbmtStructure((AbstractEllipsePolygonModel) refModelManager.getModel(ModelNames.CIRCLE_STRUCTURES).get(0));
			DefaultFeatureCollection circleFeatures = new DefaultFeatureCollection();
			for (int i = 0; i < circles.size(); i++)
				circleFeatures.add(FeatureUtil.createFeatureFrom(circles.get(i)));
			BennuStructuresEsriIO.write(file.toPath(), circleFeatures, FeatureUtil.ellipseType);
		}
		else if (refStructureManager == refModelManager.getModel(ModelNames.POINT_STRUCTURES).get(0))
		{
			List<EllipseStructure> ellipseRepresentations = EllipseStructure
					.fromSbmtStructure((AbstractEllipsePolygonModel) refModelManager.getModel(ModelNames.POINT_STRUCTURES).get(0));
			DefaultFeatureCollection pointFeatures = new DefaultFeatureCollection();
			for (int i = 0; i < ellipseRepresentations.size(); i++)
			{
				pointFeatures
						.add(FeatureUtil.createFeatureFrom(new PointStructure(ellipseRepresentations.get(i).getCentroid())));
				// System.out.println(ellipseRepresentations.get(i).getCentroid());
			}
			BennuStructuresEsriIO.write(file.toPath(), pointFeatures, FeatureUtil.pointType);

		}
		else if (refStructureManager == refModelManager.getModel(ModelNames.LINE_STRUCTURES).get(0))
		{
			List<LineStructure> lines = LineStructure
					.fromSbmtStructure((LineModel<?>) refModelManager.getModel(ModelNames.LINE_STRUCTURES).get(0));

			DefaultFeatureCollection lineFeatures = new DefaultFeatureCollection();
			for (int i = 0; i < lines.size(); i++)
				lineFeatures.add(FeatureUtil.createFeatureFrom(lines.get(i)));
			BennuStructuresEsriIO.write(file.toPath(), lineFeatures, FeatureUtil.lineType);

			/*
			 * DefaultFeatureCollection controlPointLineFeatures = new
			 * DefaultFeatureCollection(); for (int i = 0; i < lines.size(); i++) {
			 * List<LineSegment> segments = Lists.newArrayList(); for (int j = 0; j <
			 * lines.get(i).getNumberOfControlPoints() - 1; j++) { double[] p1 =
			 * lines.get(i).getControlPoint(j).toArray(); double[] p2 =
			 * lines.get(i).getControlPoint(j + 1).toArray(); segments.add(new
			 * LineSegment(p1, p2)); }
			 * controlPointLineFeatures.add(FeatureUtil.createFeatureFrom(new
			 * LineStructure(segments))); } BennuStructuresEsriIO.write(Paths.get(prefix +
			 * ".paths-ctrlpts.shp"), controlPointLineFeatures, FeatureUtil.lineType);
			 */

		}
		else if (refStructureManager == refModelManager.getModel(ModelNames.POLYGON_STRUCTURES).get(0))
		{
			List<LineStructure> lines = LineStructure
					.fromSbmtStructure((PolygonModel) refModelManager.getModel(ModelNames.POLYGON_STRUCTURES).get(0));

			DefaultFeatureCollection lineFeatures = new DefaultFeatureCollection();
			for (int i = 0; i < lines.size(); i++)
				lineFeatures.add(FeatureUtil.createFeatureFrom(lines.get(i)));
			BennuStructuresEsriIO.write(file.toPath(), lineFeatures, FeatureUtil.lineType);

			/*
			 * DefaultFeatureCollection controlPointLineFeatures = new
			 * DefaultFeatureCollection(); for (int i = 0; i < lines.size(); i++) {
			 * List<LineSegment> segments = Lists.newArrayList(); for (int j = 0; j <
			 * lines.get(i).getNumberOfControlPoints(); j++) { if (j <
			 * lines.get(i).getNumberOfControlPoints() - 1) { double[] p1 =
			 * lines.get(i).getControlPoint(j).toArray(); double[] p2 =
			 * lines.get(i).getControlPoint(j + 1).toArray(); segments.add(new
			 * LineSegment(p1, p2)); } else { double[] p1 =
			 * lines.get(i).getControlPoint(j).toArray(); double[] p2 =
			 * lines.get(i).getControlPoint(0).toArray(); segments.add(new LineSegment(p1,
			 * p2));
			 *
			 * } } controlPointLineFeatures.add(FeatureUtil.createFeatureFrom(new
			 * LineStructure(segments))); } BennuStructuresEsriIO.write(Paths.get(prefix +
			 * ".polygons-ctrlpts.shp"), controlPointLineFeatures, FeatureUtil.lineType);
			 */

		}

	}
}
