package edu.jhuapl.saavtk.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.ColorCellRenderer;
import edu.jhuapl.saavtk.gui.GNumberFieldSlider;
import edu.jhuapl.saavtk.gui.IconUtil;
import edu.jhuapl.saavtk.gui.ProfilePlot;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.NormalOffsetChangerDialog;
import edu.jhuapl.saavtk.gui.funk.PopupButton;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.model.structure.StructuresExporter;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.FeatureUtil;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.popup.StructuresPopupMenu;
import edu.jhuapl.saavtk.util.ColorIcon;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.ProgressListener;
import edu.jhuapl.saavtk.util.Properties;
import glum.gui.TableUtil;
import net.miginfocom.swing.MigLayout;

public class AbstractStructureMappingControlPanel extends JPanel implements ActionListener, PropertyChangeListener, TableModelListener, ListSelectionListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// State vars
	private ModelManager modelManager;
	private File structuresFile;
	private StructureModel structureModel;
	private PickManager pickManager;
	private PickManager.PickMode pickMode;

	// GUI vars
	private JLabel structuresFileL;
	private JTable structuresTable;
	private StructuresPopupMenu structuresPopupMenu;
	private JFrame profileWindow;

	// GUI vars
	private NormalOffsetChangerDialog changeOffsetDialog;
	private JLabel tableHeadL;
	private JButton selectAllB, selectInvertB, selectNoneB;
	private JButton createB, deleteB;
	private JToggleButton editB;
	private JLabel labelTitleL, structTitleL;
	private JButton structColorB, structHideB, structShowB;
	private JButton labelColorB, labelHideB, labelShowB;
	private JButton changeOffsetB;
	private GNumberFieldSlider fontSizeNFS;
	private GNumberFieldSlider lineWidthNFS;

	private StructuresLoadingTask task;
	private ProgressMonitor structuresLoadingProgressMonitor;

	private PopupButton saveB;
	private PopupButton loadB;

	public static StructureModel loadStructuresFromFile(File file, ModelNames name, PolyhedralModel body) throws Exception
	{
		StructureModel model = null;
		switch (name)
		{
		case CIRCLE_STRUCTURES:
			model = new CircleModel(body);
			break;
		case ELLIPSE_STRUCTURES:
			model = new EllipseModel(body);
			break;
		case POINT_STRUCTURES:
			model = new PointModel(body);
			break;
		case POLYGON_STRUCTURES:
			model = new PolygonModel(body);
			break;
		case LINE_STRUCTURES:
			model = new LineModel(body);
			break;
		default:
			throw new Error(name.name() + " is not a valid structures type");
		}

		model.loadModel(file, false, null);
		if (model.getNumberOfStructures() == 0)
			throw new Exception("No valid " + name.name() + " found");
		return model;
	}

	class LoadSbmtStructuresFileAction extends AbstractAction
	{
		public LoadSbmtStructuresFileAction()
		{
			super("SBMT Structures File...");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			File file = CustomFileChooser.showOpenDialog(AbstractStructureMappingControlPanel.this, "Select File");
			if (file == null)
				return;

			try
			{
				// If there are already structures, ask user if they want to
				// append or overwrite them
				boolean append = false;
				if (structureModel.getNumberOfStructures() > 0)
				{
					Object[] options = { "Append", "Replace" };
					int n = JOptionPane.showOptionDialog(AbstractStructureMappingControlPanel.this,
							"Would you like to append to or replace the existing structures?", "Append or Replace?",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					append = (n == 0 ? true : false);
				}
				structuresLoadingProgressMonitor = new ProgressMonitor(null, "Loading Structures...", "", 0, 100);
				structuresLoadingProgressMonitor.setProgress(0);

				task = new StructuresLoadingTask(file, append);
				task.addPropertyChangeListener(AbstractStructureMappingControlPanel.this);
				task.execute();

			}
			catch (Exception aExp)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(AbstractStructureMappingControlPanel.this),
						"There was an error reading the file.", "Error", JOptionPane.ERROR_MESSAGE);

				aExp.printStackTrace();
			}
		}
	}

	class SaveSbmtStructuresFileAction extends AbstractAction
	{
		public SaveSbmtStructuresFileAction()
		{
			super("SBMT Structures File...");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			File file = structuresFile;
			if (file != null)
			{
				// File already exists, use it as the default filename
				file = CustomFileChooser.showSaveDialog(AbstractStructureMappingControlPanel.this, "Select File", file.getName());
			}
			else
			{
				// We don't have a default filename to provide
				file = CustomFileChooser.showSaveDialog(AbstractStructureMappingControlPanel.this, "Select File");
			}

			if (file != null)
			{
				try
				{
					structureModel.saveModel(file);
					structuresFileL.setText(file.getName());
					structuresFileL.setToolTipText(file.getAbsolutePath());
					structuresFile = file;
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(AbstractStructureMappingControlPanel.this), "There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

					ex.printStackTrace();
				}
			}

		}
	}

	class SaveEsriShapeFileAction extends AbstractAction
	{
		public SaveEsriShapeFileAction()
		{
			super("ESRI Shapefile Datastore...");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			String fileMenuTitle = null;
			if (structureModel instanceof PointModel)
				fileMenuTitle = "Export points to ESRI shapefile...";
			else if (structureModel instanceof LineModel)
				fileMenuTitle = "Export paths as ESRI lines...";
			else if (structureModel instanceof PolygonModel)
				fileMenuTitle = "Export polygons to ESRI shapefile...";
			else if (structureModel instanceof EllipseModel)
				fileMenuTitle = "Export ellipses as ESRI polygons...";
			else if (structureModel instanceof CircleModel)
				fileMenuTitle = "Export circles as ESRI polygons...";
			else
				fileMenuTitle = "Datastore filename";
			File file = CustomFileChooser.showSaveDialog(AbstractStructureMappingControlPanel.this, fileMenuTitle, "myDataStore", "shp");
			if (file == null)
				return;
			if (!file.getName().endsWith(".shp"))
				file = new File(file.getName() + ".shp");

			if (structureModel == modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES))
			{
				List<EllipseStructure> ellipses = EllipseStructure.fromSbmtStructure((AbstractEllipsePolygonModel) modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES));
				DefaultFeatureCollection ellipseFeatures = new DefaultFeatureCollection();
				for (int i = 0; i < ellipses.size(); i++)
					ellipseFeatures.add(FeatureUtil.createFeatureFrom(ellipses.get(i)));
				BennuStructuresEsriIO.write(file.toPath(), ellipseFeatures, FeatureUtil.ellipseType);
			}
			else if (structureModel == modelManager.getModel(ModelNames.CIRCLE_STRUCTURES))
			{
				List<EllipseStructure> circles = EllipseStructure.fromSbmtStructure((AbstractEllipsePolygonModel) modelManager.getModel(ModelNames.CIRCLE_STRUCTURES));
				DefaultFeatureCollection circleFeatures = new DefaultFeatureCollection();
				for (int i = 0; i < circles.size(); i++)
					circleFeatures.add(FeatureUtil.createFeatureFrom(circles.get(i)));
				BennuStructuresEsriIO.write(file.toPath(), circleFeatures, FeatureUtil.ellipseType);
			}
			else if (structureModel == modelManager.getModel(ModelNames.POINT_STRUCTURES))
			{
				List<EllipseStructure> ellipseRepresentations = EllipseStructure.fromSbmtStructure((AbstractEllipsePolygonModel) modelManager.getModel(ModelNames.POINT_STRUCTURES));
				DefaultFeatureCollection pointFeatures = new DefaultFeatureCollection();
				for (int i = 0; i < ellipseRepresentations.size(); i++)
				{
					pointFeatures.add(FeatureUtil.createFeatureFrom(new PointStructure(ellipseRepresentations.get(i).getCentroid())));
					// System.out.println(ellipseRepresentations.get(i).getCentroid());
				}
				BennuStructuresEsriIO.write(file.toPath(), pointFeatures, FeatureUtil.pointType);

			}
			else if (structureModel == modelManager.getModel(ModelNames.LINE_STRUCTURES))
			{
				List<LineStructure> lines = LineStructure.fromSbmtStructure((LineModel) modelManager.getModel(ModelNames.LINE_STRUCTURES));

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
			else if (structureModel == modelManager.getModel(ModelNames.POLYGON_STRUCTURES))
			{
				List<LineStructure> lines = LineStructure.fromSbmtStructure((PolygonModel) modelManager.getModel(ModelNames.POLYGON_STRUCTURES));

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

	protected class SaveVtkFileAction extends AbstractAction
	{

		public SaveVtkFileAction()
		{
			super("VTK Polydata...");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			JCheckBox multipleFileCB = new JCheckBox("Save as multiple files");
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(multipleFileCB, BorderLayout.EAST);
			File file = CustomFileChooser.showSaveDialogWithCustomSouthComponent(null, "Save Structure (VTK)", "structures.vtk", "vtk", panel);
			if (file != null)
			{
				try
				{
					StructuresExporter.exportToVtkFile((LineModel) structureModel, file.toPath(), multipleFileCB.isSelected());
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(null, "Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		}

	}

	class LoadEsriShapeFileAction extends AbstractAction
	{
		public LoadEsriShapeFileAction()
		{
			super("ESRI Shapefile Datastore...");
		}

		protected void updateStatusBar(Feature f, int m, int mtot)
		{
			statusBar.setLeftText("Loading " + f.getDefaultGeometryProperty().getClass().getSimpleName() + " [" + (m + 1) + "/" + mtot + "]");
			statusBar.repaint();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			String fileMenuTitle = null;
			if (structureModel instanceof PointModel)
				fileMenuTitle = "Points from shapefile...";
			else if (structureModel instanceof LineModel)
				fileMenuTitle = "Path from shapefile...";
			else if (structureModel instanceof PolygonModel)
				fileMenuTitle = "Polygon from shapefile...";
			else
				fileMenuTitle = "Datastore filename";
			File[] files = CustomFileChooser.showOpenDialog(AbstractStructureMappingControlPanel.this, fileMenuTitle, Lists.newArrayList("shp"), true);
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
				//            //String prefix = FilenameUtils.getFullPath(file.toString()) + FilenameUtils.getBaseName(file.toString());
				//            String prefix = FilenameUtils.getFullPath(file.toString()) + FilenameUtils.getName(file.toString());
				//            //System.out.println(prefix);
				//            int idx1 = prefix.lastIndexOf('.');
				//            prefix = prefix.substring(0, idx1);
				//            int idx2 = prefix.lastIndexOf('.');
				//            if (idx2<0)
				//                {
				//                     int result=JOptionPane.showConfirmDialog(null, "The file \""+file.toString()+"\" does not conform to the SBMT shapefile naming convention.\nOpen file-renaming tool?");
				//                     if (result==JOptionPane.YES_OPTION)
				//                     {
				//                         SBMTShapefileRenamer renamingPanel=new SBMTShapefileRenamer(file.getAbsolutePath());
				//                         result=JOptionPane.showConfirmDialog(null, renamingPanel, "Non-conforming shapefile name", JOptionPane.OK_CANCEL_OPTION);
				//                         if (result==JOptionPane.OK_OPTION)
				//                         {
				//                             prefix=renamingPanel.rename();
				//                             System.out.println(prefix);
				//                             idx1 = prefix.lastIndexOf('.');
				//                             prefix = prefix.substring(0, idx1);
				//                             idx2 = prefix.lastIndexOf('.');
				//                         }
				//                         else
				//                             return;
				//                     }
				//                     else
				//                         return;
				//                };
				//            prefix = prefix.substring(0, idx2);
				//            //System.out.println("prefix=" +prefix);

				//            if (structureModel == modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES))
				//            { 
				//                if (filePath.toFile().exists())
				//                {
				//                    FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.ellipseType);
				//                    FeatureIterator<Feature> it = features.features();
				//                    while (it.hasNext())
				//                    {
				//                        Feature f = it.next();
				//                        EllipseStructure es = FeatureUtil.createEllipseStructureFrom((SimpleFeature) f, (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));
				//                        EllipseModel model = (EllipseModel) modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);
				//                        model.addNewStructure(es.getCentroid(), es.getParameters().majorRadius, es.getParameters().flattening, es.getParameters().angle);
				//                    }
				//                    it.close();
				//                }
				//            }
				//            else if (structureModel == modelManager.getModel(ModelNames.CIRCLE_STRUCTURES))
				//            {
				//                if (filePath.toFile().exists())
				//                {
				//
				//                    FeatureCollection features = BennuStructuresEsriIO.read(filePath, FeatureUtil.ellipseType);
				//                    FeatureIterator<Feature> it = features.features();
				//                    while (it.hasNext())
				//                    {
				//                        Feature f = it.next();
				//                        EllipseStructure es = FeatureUtil.createEllipseStructureFrom((SimpleFeature) f, (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));
				//                        CircleModel model = (CircleModel) modelManager.getModel(ModelNames.CIRCLE_STRUCTURES);
				//                        model.addNewStructure(es.getCentroid(), es.getParameters().majorRadius, es.getParameters().flattening, es.getParameters().angle);
				//                    }
				//                    it.close();
				//                }
				//            }
				if (structureModel == modelManager.getModel(ModelNames.POINT_STRUCTURES))
				{
					if (filePath.toFile().exists())
					{
						PointModel model = (PointModel) modelManager.getModel(ModelNames.POINT_STRUCTURES);
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
							PointStructure ps = FeatureUtil.createPointStructureFrom((SimpleFeature) flist.get(m), (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));
							model.addNewStructure(ps.getCentroid().toArray());
						}
						model.activateStructure(-1);
					}
				}
				else if (structureModel == modelManager.getModel(ModelNames.LINE_STRUCTURES))
				{
					if (filePath.toFile().exists())
					{
						LineModel model = (LineModel) modelManager.getModel(ModelNames.LINE_STRUCTURES);
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
							LineStructure ls = FeatureUtil.createLineStructureFrom((SimpleFeature) f, (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));
							model.addNewStructure();
							model.activateStructure(model.getNumberOfStructures() - 1);
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
								GenericPolyhedralModel body = (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY);
								double[] intersectPoint = new double[3];
								body.getPointAndCellIdFromLatLon(latlon.lat, latlon.lon, intersectPoint);
								model.insertVertexIntoActivatedStructure(intersectPoint);
							}

						}
						it.close();
						model.activateStructure(-1);
					}
				}
				else if (structureModel == modelManager.getModel(ModelNames.POLYGON_STRUCTURES))
				{
					// System.out.println(filePath+" "+filePath.toFile().exists());
					if (filePath.toFile().exists())
					{
						PolygonModel model = (PolygonModel) modelManager.getModel(ModelNames.POLYGON_STRUCTURES);
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
							LineStructure ls = FeatureUtil.createLineStructureFrom((SimpleFeature) f, (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));
							model.addNewStructure();
							model.activateStructure(model.getNumberOfStructures() - 1);
							for (int i = 0; i < ls.getNumberOfSegments(); i++)
							{
								double[] pt;
								if (i == ls.getNumberOfSegments())
									pt = ls.getSegment(i - 1).getEnd().toArray();
								else
									pt = ls.getSegment(i).getStart().toArray();
								LatLon latlon = MathUtil.reclat(pt);
								GenericPolyhedralModel body = (GenericPolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY);
								double[] intersectPoint = new double[3];
								body.getPointAndCellIdFromLatLon(latlon.lat, latlon.lon, intersectPoint);
								model.insertVertexIntoActivatedStructure(intersectPoint);
							}

						}
						it.close();
						model.activateStructure(-1);

					}
				}
			}
		}
	}

	StatusBar statusBar;

	public AbstractStructureMappingControlPanel(final ModelManager modelManager, ModelNames aModelType, final PickManager pickManager, final PickManager.PickMode pickMode, final StatusBar statusBar)
	{
		this.modelManager = modelManager;
		structureModel = (StructureModel) modelManager.getModel(aModelType);
		this.pickManager = pickManager;
		this.pickMode = pickMode;
		structuresPopupMenu = (StructuresPopupMenu) pickManager.getPopupManager().getPopup(structureModel);
		this.statusBar = statusBar;

		changeOffsetDialog = null;

		loadB = new PopupButton("Load...");
		saveB = new PopupButton("Save...");

		loadB.getPopup().add(new JMenuItem(new LoadSbmtStructuresFileAction()));
		JMenuItem esriLoadMI = loadB.getPopup().add(new JMenuItem(new LoadEsriShapeFileAction()));
		if (!(structureModel instanceof PointModel) && !(structureModel instanceof LineModel)) // don't let user import esri shapes as ellipses or circles; these require extra information in order to be upgraded (downgraded?) to "SBMT" structures ... instead they can use the polygons tab
		{
			esriLoadMI.setEnabled(false);
			esriLoadMI.setToolTipText("ESRI circles and ellipses can be imported using the Polygons tab");
		}

		saveB.getPopup().add(new JMenuItem(new SaveSbmtStructuresFileAction()));
		saveB.getPopup().add(new JMenuItem(new SaveEsriShapeFileAction()));
		saveB.getPopup().add(new JMenuItem(new SaveVtkFileAction()));

		structureModel.addPropertyChangeListener(this);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e)
			{
				setEditingEnabled(false);
			}
		});

		pickManager.getDefaultPicker().addPropertyChangeListener(this);

		setLayout(new MigLayout("", "", "[]"));

		JLabel fileNameL = new JLabel("File: ");
		structuresFileL = new JLabel("<no file loaded>");
		add(fileNameL, "span,split");
		add(structuresFileL, "growx,pushx,w 100:100:");
		add(loadB, "sg g0");
		add(saveB, "sg g0,wrap");

		// Table header
		selectInvertB = new JButton(IconUtil.loadIcon("resources/icons/itemSelectInvert.png"));
		selectInvertB.addActionListener(this);
		selectInvertB.setToolTipText("Invert Selection");

		selectNoneB = new JButton(IconUtil.loadIcon("resources/icons/itemSelectNone.png"));
		selectNoneB.addActionListener(this);
		selectNoneB.setToolTipText("Clear Selection");

		selectAllB = new JButton(IconUtil.loadIcon("resources/icons/itemSelectAll.png"));
		selectAllB.addActionListener(this);
		selectAllB.setToolTipText("Select All");
		
		tableHeadL = new JLabel("Structures: 0");
		add(tableHeadL, "growx,span,split");
		add(selectInvertB, "w 24!,h 24!");
		add(selectNoneB, "w 24!,h 24!");
		add(selectAllB, "w 24!,h 24!,wrap 2");

		// Table content
		String[] columnNames = { "Id", "Type", "Name", "Details", "Color", "Label" };
		// "Hide Label",
		// "Hide Structure"

		structuresTable = new JTable(new StructuresTableModel(columnNames));
		structuresTable.setAutoCreateRowSorter(true);
		structuresTable.setBorder(BorderFactory.createTitledBorder(""));
		structuresTable.setColumnSelectionAllowed(false);
		structuresTable.setRowSelectionAllowed(true);
		structuresTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		structuresTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		structuresTable.setDefaultRenderer(String.class, new StringRenderer());
		structuresTable.setDefaultRenderer(Color.class, new ColorCellRenderer(true));
		structuresTable.getColumnModel().getColumn(5).setCellRenderer(new StructureLabelRenderer());
		// structuresTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		structuresTable.getModel().addTableModelListener(this);
		structuresTable.getSelectionModel().addListSelectionListener(this);
		structuresTable.addMouseListener(new TableMouseHandler());
		structuresTable.getTableHeader().setReorderingAllowed(false);

		structuresTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e)
			{
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
					doDeleteSelectedStructures();
			}
		});

		/*
		 * structuresTable.getColumnModel().getColumn(6).setPreferredWidth(31);
		 * structuresTable.getColumnModel().getColumn(7).setPreferredWidth(31);
		 * structuresTable.getColumnModel().getColumn(6).setResizable(false);
		 * structuresTable.getColumnModel().getColumn(7).setResizable(false);
		 */

		JScrollPane tableScrollPane = new JScrollPane(structuresTable);
		add(tableScrollPane, "growx,growy,pushy,span,wrap");

		createB = new JButton("New");
		createB.addActionListener(this);
		createB.setVisible(structureModel.supportsActivation());
		deleteB = new JButton("Delete");
		deleteB.addActionListener(this);
		editB = new JToggleButton("Edit");
		editB.addActionListener(this);
		add(createB, "sg g1,span,split");
		add(editB, "sg g1");
		add(deleteB, "sg g1,wrap");

		labelTitleL = new JLabel("Labels:", JLabel.RIGHT);
		labelColorB = new JButton("");
		labelColorB.addActionListener(this);
		labelHideB = new JButton("Hide");
		labelHideB.addActionListener(this);
		labelShowB = new JButton("Show");
		labelShowB.addActionListener(this);
		add(labelTitleL, "span,split,sg g2");
		add(labelHideB, "sg g3");
		add(labelShowB, "sg g3");
		add(labelColorB, "gapy 0,sgy g3,wrap");

		structTitleL = new JLabel("Structs:", JLabel.RIGHT);
		structColorB = new JButton("");
		structColorB.addActionListener(this);
		structHideB = new JButton("Hide");
		structHideB.addActionListener(this);
		structShowB = new JButton("Show");
		structShowB.addActionListener(this);
		add(structTitleL, "span,split,sg g2");
		add(structHideB, "sg g3");
		add(structShowB, "sg g3");
		add(structColorB, "gapy 0,sgy g3,wrap");

		changeOffsetB = new JButton("Change Normal Offset...");
		changeOffsetB.addActionListener(this);
		changeOffsetB.setToolTipText("<html>Structures displayed on a shape model need to be shifted slightly away from<br>" + "the shape model in the direction normal to the plates as otherwise they will<br>"
				+ "interfere with the shape model itself and may not be visible. Click this<br>" + "button to show a dialog that will allow you to explicitely set the offset<br>" + "amount in meters.</html>");
		add(changeOffsetB, "sg g5,span,split");

		JButton openProfilePlotButton = new JButton("Show Profile Plot...");
		if (pickMode == PickManager.PickMode.LINE_DRAW)
		{
			// Only add profile plots for line structures
			ProfilePlot profilePlot = new ProfilePlot((LineModel) modelManager.getModel(ModelNames.LINE_STRUCTURES), (PolyhedralModel) modelManager.getModel(ModelNames.SMALL_BODY));

			// Create a new frame/window with profile
			profileWindow = new JFrame();
			ImageIcon icon = new ImageIcon(getClass().getResource("/edu/jhuapl/saavtk/data/black-sphere.png"));
			profileWindow.setTitle("Profile Plot");
			profileWindow.setIconImage(icon.getImage());
			profileWindow.getContentPane().add(profilePlot.getChartPanel());
			profileWindow.setSize(600, 400);
			profileWindow.setVisible(false);

			openProfilePlotButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					profileWindow.setVisible(true);
				}
			});
		}
		else
		{
			openProfilePlotButton.setVisible(false);
		}
		add(openProfilePlotButton, "sg g5,wrap");

		fontSizeNFS = new GNumberFieldSlider(this, "Font Size:", 8, 120);
		fontSizeNFS.setNumSteps(67);
		fontSizeNFS.setNumColumns(3);
		add(fontSizeNFS, "growx,sg g6,span,wrap");

		lineWidthNFS = new GNumberFieldSlider(this, "Line Width:", 1, 100);
		lineWidthNFS.setNumSteps(100);
		lineWidthNFS.setNumColumns(3);

		// Create the pointDiameterPanel if the PickMode == POINT_DRAW
		PointDiameterPanel pointDiameterPanel = null;
		if (pickMode == PickMode.POINT_DRAW)
		{
			PointModel pointModel = (PointModel) modelManager.getModel(ModelNames.POINT_STRUCTURES);
			pointDiameterPanel = new PointDiameterPanel(pointModel);
		}

		// Either the lineWidthNFS or the pointDiameterPanel will be visible
		if (pointDiameterPanel != null)
			add(pointDiameterPanel, "span");
		//			add(pointDiameterPanel, "growx,sg g5,span,wrap");
		else
			add(lineWidthNFS, "growx,sg g6,span");

		// Hack to force fontSizeNFS label to have the same size as lineWidthNFS
		fontSizeNFS.getLabelComponent().setPreferredSize(lineWidthNFS.getLabelComponent().getPreferredSize());

		updateControlGui();

		updateModelDisplay();
	}

	class StructuresLoadingTask extends SwingWorker<Void, Void>
	{
		private final File file;
		private final boolean append;

		public StructuresLoadingTask(File file, boolean append)
		{
			this.file = file;
			this.append = append;
		}

		@Override
		protected Void doInBackground() throws Exception
		{
			structureModel.loadModel(file, append, new ProgressListener() {
				@Override
				public void setProgress(int progress)
				{
					task.setProgress(progress);
				}

			});
			structuresFileL.setText(file.getName());
			structuresFileL.setToolTipText(file.getAbsolutePath());
			structuresFile = file;
			return null;
		}

		@Override
		protected void done()
		{
			// TODO Auto-generated method stub
			super.done();
		}

	}

	private int[] getSelectedRows()
	{
		int[] selectedRows = structuresTable.getSelectedRows();
		int[] sortedSelectedRows = new int[selectedRows.length];
		int i = 0;
		for (int row : selectedRows)
		{
			sortedSelectedRows[i++] = structuresTable.getRowSorter().convertRowIndexToModel(row);
		}
		return sortedSelectedRows;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		Object source = actionEvent.getSource();

		if (source == createB)
		{
			// Ensure all structures are visible (in case any are hidden)
			structureModel.setVisible(true);

			structureModel.addNewStructure();
			pickManager.setPickMode(pickMode);
			editB.setSelected(true);
			updateStructureTable();

			int numStructures = structuresTable.getRowCount();
			if (numStructures > 0)
				structuresTable.setRowSelectionInterval(numStructures - 1, numStructures - 1);
		}
		else if (source == editB)
		{
			setEditingEnabled(editB.isSelected());
		}
		else if (source == selectAllB)
		{
			int numRows = structuresTable.getRowCount();
			if (numRows == 0)
				return;
			structuresTable.setRowSelectionInterval(0, numRows - 1);
		}
		else if (source == selectNoneB)
		{
			structuresTable.clearSelection();
		}
		else if (source == selectInvertB)
		{
			TableUtil.invertSelection(structuresTable, this);
		}
		else if (source == deleteB)
		{
			doDeleteSelectedStructures();
		}
		else if (source == labelColorB)
		{
			int[] idxArr = getSelectedRows();

			Color defColor = structureModel.getLabelColor(idxArr[0]);
			Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;

			structureModel.setLabelColor(idxArr, tmpColor);
		}
		else if (source == labelHideB)
		{
			int[] idxArr = getSelectedRows();
			structureModel.setLabelVisible(idxArr, false);
		}
		else if (source == labelShowB)
		{
			int[] idxArr = getSelectedRows();
			structureModel.setLabelVisible(idxArr, true);
		}
		else if (source == structColorB)
		{
			int[] idxArr = getSelectedRows();

			Color defColor = structureModel.getStructureColor(idxArr[0]);
			Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;

			structureModel.setStructureColor(idxArr, tmpColor);
		}
		else if (source == structHideB)
		{
			int[] idxArr = getSelectedRows();
			structureModel.setStructureVisible(idxArr, false);
		}
		else if (source == structShowB)
		{
			int[] idxArr = getSelectedRows();
			structureModel.setStructureVisible(idxArr, true);
		}
		else if (source == changeOffsetB)
		{
			// Lazy init
			if (changeOffsetDialog == null)
			{
				changeOffsetDialog = new NormalOffsetChangerDialog(structureModel);
				changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(this));
			}

			changeOffsetDialog.setVisible(true);
		}
		else if (source == fontSizeNFS)
		{
			doUpdateFontSize();
		}
		else if (source == lineWidthNFS)
		{
			doUpdateLineWidth();
		}

		updateControlGui();
	}

	/**
	 * Helper method to delete the selected structures
	 */
	private void doDeleteSelectedStructures()
	{
		int[] idxArr = getSelectedRows();

		// Request confirmation when deleting multiple items
		if (idxArr.length > 0)
		{
			String countStr = "" + idxArr.length;
			String infoMsg = "Are you sure you want to delete " + countStr + " structures?";
			int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this), infoMsg, "Confirm Deletion", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.NO_OPTION)
				return;
		}

		// Remove the structures
		structuresTable.getSelectionModel().removeListSelectionListener(this);
		structureModel.removeStructures(idxArr);
		structuresTable.getSelectionModel().addListSelectionListener(this);

		// Update internal state vars
		pickManager.setPickMode(PickManager.PickMode.DEFAULT);
		structureModel.activateStructure(-1);

		// Update GUI
		updateControlGui();
		editB.setSelected(false);
	}

	/**
	 * Helper method to update the (selected) models to reflect the user selected
	 * font size.
	 */
	private void doUpdateFontSize()
	{
		if (fontSizeNFS.isValidInput() == false)
			return;

		// Retrieve the fontSize
		int fontSize = (int) fontSizeNFS.getValue();

		// Update the relevant structures
		int[] idxArr = getSelectedRows();
		structureModel.setLabelFontSize(idxArr, fontSize);
	}

	/**
	 * Helper method to update the specified structures to reflect the user selected
	 * line width.
	 */
	private void doUpdateLineWidth()
	{
		if (lineWidthNFS.isValidInput() == false)
			return;

		// Retrieve the lineWidth
		int lineWidth = (int) lineWidthNFS.getValue();

// TODO: Currently individual (sub)structures can not have different line widths from
// TODO: their parent (super) structure. In the future that may be a request
// Update the relevant structures
//		int[] idxArr = structuresTable.getSelectedRows();
//		for (int aIdx : idxArr)
//			structureModel.setLineWidth(aIdx, lineWidth);
		structureModel.setLineWidth(lineWidth);
	}

	/**
	 * Helper method to update the colored icons/text for labelIconB and structIconB
	 */
	private void updateColoredButtons()
	{
		// These values may need to be fiddled with if there are sizing issues
		int iconW = (int) (structHideB.getWidth() * 0.80);
		int iconH = (int) (structHideB.getHeight() * 0.50);

		// Update the label / struct colors
		Icon labelIcon = null;
		Icon structIcon = null;

		int[] idxArr = structureModel.getSelectedStructures();
		boolean isEnabled = idxArr.length > 0;
		if (isEnabled == true)
		{
			// Determine label color attributes
			boolean isMixed = false;
			Color tmpColor = structureModel.getLabelColor(idxArr[0]);
			for (int aIdx : idxArr)
				isMixed |= Objects.equals(tmpColor, structureModel.getLabelColor(aIdx)) == false;

			if (isMixed == false)
				labelIcon = new ColorIcon(tmpColor, Color.BLACK, iconW, iconH);
			else
				labelIcon = new ColorIcon(Color.LIGHT_GRAY, Color.GRAY, iconW, iconH);

			// Determine structure color attributes
			isMixed = false;
			tmpColor = structureModel.getStructureColor(idxArr[0]);
			for (int aIdx : idxArr)
				isMixed |= Objects.equals(tmpColor, structureModel.getStructureColor(aIdx)) == false;

			if (isMixed == false)
				structIcon = new ColorIcon(tmpColor, Color.BLACK, iconW, iconH);
			else
				structIcon = new ColorIcon(Color.LIGHT_GRAY, Color.GRAY, iconW, iconH);
		}
		else
		{
			Color tmpColor = new Color(0, 0, 0, 0);
			labelIcon = new ColorIcon(tmpColor, tmpColor, iconW, iconH);
			structIcon = labelIcon;
		}

		labelColorB.setIcon(labelIcon);
		structColorB.setIcon(structIcon);
	}

	/**
	 * Helper method to update control UI. Triggered when selection has changed.
	 */
	public void updateControlGui()
	{
		// Update various buttons
		int cntFullItems = structureModel.getNumberOfStructures();
		boolean isEnabled = cntFullItems > 0;
		selectInvertB.setEnabled(isEnabled);

		int[] pickArr = structureModel.getSelectedStructures();
		int cntPickItems = pickArr.length;
		isEnabled = cntPickItems > 0;
		selectNoneB.setEnabled(isEnabled);
		labelColorB.setEnabled(isEnabled);
		structColorB.setEnabled(isEnabled);

		isEnabled = cntFullItems > 0 && cntPickItems < cntFullItems;
		selectAllB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0 && editB.isSelected() == false;
		deleteB.setEnabled(isEnabled);
		labelTitleL.setEnabled(isEnabled);
		labelHideB.setEnabled(isEnabled);
		labelShowB.setEnabled(isEnabled);
//		labelColorB.setEnabled(isEnabled);
		structTitleL.setEnabled(isEnabled);
		structHideB.setEnabled(isEnabled);
		structShowB.setEnabled(isEnabled);
//		structColorB.setEnabled(isEnabled);
		updateColoredButtons();

		isEnabled = cntPickItems > 0;
		fontSizeNFS.setEnabled(isEnabled);

		int fontSize = -1;
		if (cntPickItems > 0)
			fontSize = structureModel.getLabelFontSize(pickArr[0]);
		fontSizeNFS.setValue(fontSize);

		double lineWidth = -1;
		lineWidth = structureModel.getLineWidth();
// TODO: Enable if lineWidths are customizable on a per (sub)structure basis
//		if (idxArr.length > 0)
//			lineWidth = structureModel.getLineWidth();
		lineWidthNFS.setValue(lineWidth);

		// Table title
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		String infoStr = "Structures: " + cntFormat.format(cntFullItems);
		if (cntPickItems > 0)
			infoStr += "  (Selected: " + cntFormat.format(cntPickItems) + ")";

		tableHeadL.setText(infoStr);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			((DefaultTableModel) structuresTable.getModel()).setRowCount(structureModel.getNumberOfStructures());

			updateModelDisplay();
			updateControlGui();
		}
		else if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
		{
			// If we're editing, say, a path, return immediately.
			if (structureModel.supportsActivation() && editB.isSelected())
			{
				return;
			}

			PickEvent e = (PickEvent) evt.getNewValue();
			if (modelManager.getModel(e.getPickedProp()) == structureModel)
			{
				int idx = structureModel.getStructureIndexFromCellId(e.getPickedCellId(), e.getPickedProp());
				idx = structuresTable.getRowSorter().convertRowIndexToView(idx);
				//idx is 29 for orpheus - need to convert to sorted index and choose that below
				if (PickUtil.isPopupTrigger(e.getMouseEvent()))
				{
					// If the item right-clicked on is not selected, then deselect all the
					// other items and select the item right-clicked on.
					if (!structuresTable.isRowSelected(idx))
					{
						structuresTable.setRowSelectionInterval(idx, idx);
					}
				}
				else
				{
					int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
					if (((e.getMouseEvent().getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) || ((e.getMouseEvent().getModifiers() & keyMask) == keyMask))
						structuresTable.addRowSelectionInterval(idx, idx);
					else
						structuresTable.setRowSelectionInterval(idx, idx);
				}

				structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
			}
			else
			{
				int count = structuresTable.getRowCount();
				if (count > 0)
					structuresTable.removeRowSelectionInterval(0, count - 1);
			}
		}
		else if (Properties.STRUCTURE_ADDED.equals(evt.getPropertyName()))
		{
			int idx = structureModel.getNumberOfStructures() - 1;
			structuresTable.setRowSelectionInterval(idx, idx);
			structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
		}
		else if (Properties.STRUCTURE_REMOVED.equals(evt.getPropertyName()))
		{
			int idx = (Integer) evt.getNewValue();

			updateStructureTable();

			int numStructures = structuresTable.getRowCount();
			if (numStructures > 0)
			{
				if (idx > numStructures - 1)
				{
					structuresTable.setRowSelectionInterval(numStructures - 1, numStructures - 1);
					structuresTable.scrollRectToVisible(structuresTable.getCellRect(numStructures - 1, 0, true));
				}
				else
				{
					structuresTable.setRowSelectionInterval(idx, idx);
					structuresTable.scrollRectToVisible(structuresTable.getCellRect(idx, 0, true));
				}
			}
		}
		else if (Properties.ALL_STRUCTURES_REMOVED.equals(evt.getPropertyName()) || Properties.COLOR_CHANGED.equals(evt.getPropertyName()))
		{
			updateStructureTable();
		}
		else if ("progress" == evt.getPropertyName())
		{
			int progress = (Integer) evt.getNewValue();
			structuresLoadingProgressMonitor.setProgress(progress);
			String message =
					String.format("Completed %d%%.\n", progress);
			structuresLoadingProgressMonitor.setNote(message);
			if (structuresLoadingProgressMonitor.isCanceled() || task.isDone())
			{
				if (structuresLoadingProgressMonitor.isCanceled())
				{
					task.cancel(true);
				}
			}
		}
	}

	private void updateModelDisplay()
	{
		updateStructureTable();

		if (structureModel.supportsActivation())
		{
			int idx = structureModel.getActivatedStructureIndex();
			if (idx >= 0)
			{
				idx = structuresTable.getRowSorter().convertRowIndexToModel(idx);
				pickManager.setPickMode(pickMode);
				if (!editB.isSelected())
					editB.setSelected(true);
				structuresTable.setRowSelectionInterval(idx, idx);
				structuresTable.setEnabled(false);
			}
			else
			{
				// Don't change the picker if this tab is not in view since
				// it's possible we could be in the middle of drawing other
				// objects.
				if (isVisible())
					pickManager.setPickMode(PickManager.PickMode.DEFAULT);
				if (editB.isSelected())
					editB.setSelected(false);
				structuresTable.setEnabled(true);
			}
		}

	}

	private void updateStructureTable()
	{
		int numStructures = structureModel.getNumberOfStructures();
		List<? extends SortKey> sortKeys = structuresTable.getRowSorter().getSortKeys();
		((DefaultTableModel) structuresTable.getModel()).setRowCount(numStructures);
		for (int i = 0; i < numStructures; ++i)
		{
			StructureModel.Structure structure = structureModel.getStructure(i);
			int[] c = structure.getColor();
			((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getId(), i, 0);
			((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getType(), i, 1);
			((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getName(), i, 2);
			((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getInfo(), i, 3);
			((DefaultTableModel) structuresTable.getModel()).setValueAt(new Color(c[0], c[1], c[2]), i, 4);
			((DefaultTableModel) structuresTable.getModel()).setValueAt(structure.getLabel(), i, 5);
			// structuresTable.setValueAt(structure.getLabelHidden(), i, 6);
			// structuresTable.setValueAt(structure.getHidden(), i, 6);
		}
		structuresTable.getRowSorter().setSortKeys(sortKeys);
		updateColoredButtons();
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		// Retrieve the model colum index
		int colNum = e.getColumn();
		//		System.out.println("AbstractStructureMappingControlPanel: tableChanged: row " + e.getFirstRow());
		if (colNum == 2)
		{
			int row = e.getFirstRow();
			int col = e.getColumn();
			StructureModel.Structure structure = structureModel.getStructure(row);
			//            String name = (String) structuresTable.getValueAt(row, col);
			String name = (String) ((DefaultTableModel) structuresTable.getModel()).getValueAt(row, col);
			if (name != null && !name.equals(structure.getName()))
			{
				structure.setName(name);
			}
		}
		if (colNum == 5)
		{
			int row = e.getFirstRow();
			int col = e.getColumn();
			StructureModel.Structure structure = structureModel.getStructure(row);
			String label = (String) ((DefaultTableModel) structuresTable.getModel()).getValueAt(row, col);
			//            String label = (String) structuresTable.getValueAt(row, col);
			if (label != null && !label.equals(structure.getLabel()))
			{
				structure.setLabel(label);
				structureModel.setStructureLabel(row, label);
			}
		}
		/*
		 * if(colNum ==6) { int row = e.getFirstRow(); int col = e.getColumn();
		 * StructureModel.Structure structure = structureModel.getStructure(row);
		 * Boolean hidden = (Boolean)structuresTable.getValueAt(row, col);
		 * if(structuresTable.getValueAt(row,
		 * col-1)!=null&&!(structuresTable.getValueAt(row, col-1)).equals("")) {
		 * structure.setHidden(hidden); //structureModel. } } if(colNum==7) { int row =
		 * e.getFirstRow(); int col = e.getColumn(); if(structuresTable.getValueAt(row,
		 * col-1)!=null&&!(structuresTable.getValueAt(row, col-2)).equals("")) { Boolean
		 * labelHidden = (Boolean)structuresTable.getValueAt(row, col-1); Boolean hidden
		 * = (Boolean)structuresTable.getValueAt(row, col); int [] loc = {row};
		 * structureModel.setStructuresHidden(loc, hidden, labelHidden, false); } }
		 */
	}

	@Override
	public void valueChanged(ListSelectionEvent aEvent)
	{
		if (getSelectedRows().length == 0)
			structuresTable.clearSelection();
		int[] selectedRows = getSelectedRows();
		structureModel.selectStructures(selectedRows);
		updateControlGui();
	}

	public void setEditingEnabled(boolean enable)
	{
		if (enable)
		{
			structureModel.setVisible(true); // in case user hid everything, make it visible again

			if (!editB.isSelected())
				editB.setSelected(true);
		}
		else
		{
			if (editB.isSelected())
				editB.setSelected(false);
		}

		if (structureModel.supportsActivation())
		{
			int idx = structuresTable.getSelectedRow();

			if (enable)
			{
				if (idx >= 0)
				{
					idx = structuresTable.getRowSorter().convertRowIndexToModel(idx);
					pickManager.setPickMode(pickMode);
					structureModel.activateStructure(idx);
				}
				else
				{
					editB.setSelected(false);
				}
			}
			else
			{
				pickManager.setPickMode(PickManager.PickMode.DEFAULT);
				structureModel.activateStructure(-1);
			}

			// The item in the table might get deselected so select it again here.
			int numStructures = structuresTable.getRowCount();
			if (idx >= 0 && idx < numStructures)
			{
				idx = structuresTable.getRowSorter().convertRowIndexToModel(idx);
				structuresTable.setRowSelectionInterval(idx, idx);
			}
		}
		else
		{
			if (enable)
			{
				pickManager.setPickMode(pickMode);
			}
			else
			{
				pickManager.setPickMode(PickManager.PickMode.DEFAULT);
			}
		}

		updateControlGui();
	}

	private void structuresTableMaybeShowPopup(MouseEvent e)
	{
		if (e.isPopupTrigger() && !editB.isSelected())
		{
			int index = structuresTable.rowAtPoint(e.getPoint());

			if (index >= 0)
			{
				// If the item right-clicked on is not selected, then deselect all the
				// other items and select the item right-clicked on.
				if (!structuresTable.isRowSelected(index))
				{
					structuresTable.setRowSelectionInterval(index, index);
				}

				structuresPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	class FileIOButtonListener extends MouseAdapter
	{
		JPopupMenu menu;

		public FileIOButtonListener(JPopupMenu menu)
		{
			this.menu = menu;
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			if (e.getSource() == menu)
				menu.setVisible(false);
		}

		private void maybeShowPopup(MouseEvent e)
		{
			// if (e.isPopupTrigger())
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	class StringRenderer extends DefaultTableCellRenderer
	{
		private Color selectionForeground;

		public StringRenderer()
		{
			UIDefaults defaults = UIManager.getDefaults();
			selectionForeground = defaults.getColor("Table.selectionForeground");
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (row >= structureModel.getNumberOfStructures())
				return c;
			int sortedRow = structuresTable.getRowSorter().convertRowIndexToModel(row);
			if (structureModel.isStructureVisible(sortedRow) == false)
			{
				c.setForeground(Color.GRAY);
			}
			else
			{
				if (isSelected)
					c.setForeground(selectionForeground);
				else
					c.setForeground(Color.BLACK);
			}

			return c;
		}
	}

	class StructureLabelRenderer extends DefaultTableCellRenderer
	{
		private Color selectionForeground;

		public StructureLabelRenderer()
		{
			UIDefaults defaults = UIManager.getDefaults();
			selectionForeground = defaults.getColor("Table.selectionForeground");
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (row >= structureModel.getNumberOfStructures())
				return c;
			int sortedRow = structuresTable.getRowSorter().convertRowIndexToModel(row);
			boolean isVisible = structureModel.isStructureVisible(sortedRow) && structureModel.isLabelVisible(sortedRow);
			if (isVisible == false)
				c.setForeground(Color.GRAY);
			else if (isSelected == true)
				c.setForeground(selectionForeground);
			else
				c.setForeground(Color.BLACK);

			return c;
		}
	}

	class StructuresTableModel extends DefaultTableModel
	{
		public StructuresTableModel(String[] columnNames)
		{
			super(columnNames, 0);
		}

		@Override
		public boolean isCellEditable(int row, int column)
		{
			if (column == 2 || column == 5) // || column ==6 || column ==7)
				return true;
			else
				return false;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			if (columnIndex == 0)
				return Integer.class;
			else if (columnIndex == 4)
				return Color.class;
			// else if(columnIndex == 6||columnIndex == 7)
			// return Boolean.class;
			else
				return String.class;
		}
	}

	class TableMouseHandler extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			int row = structuresTable.rowAtPoint(e.getPoint());
			int col = structuresTable.columnAtPoint(e.getPoint());
			int sortedRow = structuresTable.getRowSorter().convertRowIndexToModel(row);
			if (e.getClickCount() == 2 && row >= 0 && col == 4)
			{
				Color color = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(structuresTable), structureModel.getStructure(sortedRow).getColor());

				if (color == null)
					return;

				int[] c = new int[4];
				c[0] = color.getRed();
				c[1] = color.getGreen();
				c[2] = color.getBlue();
				c[3] = color.getAlpha();

				structureModel.setStructureColor(sortedRow, c);
			}
		}

		@Override
		public void mousePressed(MouseEvent evt)
		{
			structuresTableMaybeShowPopup(evt);
		}

		@Override
		public void mouseReleased(MouseEvent evt)
		{
			structuresTableMaybeShowPopup(evt);
		}
	}

}
