package edu.jhuapl.saavtk.gui.menu;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import crucible.crust.metadata.api.Serializer;
import crucible.crust.metadata.impl.gson.Serializers;
import edu.jhuapl.saavtk.gui.OSXAdapter;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.CameraDialog;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.PreferencesDialog;
import edu.jhuapl.saavtk.gui.panel.AbstractStructureMappingControlPanel;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.model.structure.esri.ShapefileUtil;
import edu.jhuapl.saavtk.model.structure.esri.StructureUtil;
import edu.jhuapl.saavtk.util.Configuration;

public class FileMenu extends JMenu
{
	private static final long serialVersionUID = 1L;
	private final ImmutableList<String> fileExtensions;
	private final ViewManager rootPanel;
	private PreferencesDialog preferencesDialog;
	public JFrame frame;

	public FileMenu(ViewManager rootPanel)
	{
		this(rootPanel, ImmutableList.of());
	}

	public FileMenu(ViewManager rootPanel, Iterable<String> fileExtensions)
	{
		super("File");
		this.fileExtensions = ImmutableList.copyOf(fileExtensions);
		this.rootPanel = rootPanel;

		JMenuItem mi = new JMenuItem(new OpenSession());
		this.add(mi);
		mi = new JMenuItem(new SaveSessionAs());
		this.add(mi);
		mi = new JMenuItem(new SaveImageAction());
		this.add(mi);
		mi = new JMenuItem(new Save6AxesViewsAction());
		this.add(mi);
		JMenu saveShapeModelMenu = new JMenu("Export Shape Model to...");
		this.add(saveShapeModelMenu);
		mi = new JMenuItem(new SaveShapeModelAsPLTAction());
		saveShapeModelMenu.add(mi);
		mi = new JMenuItem(new SaveShapeModelAsOBJAction());
		saveShapeModelMenu.add(mi);
		mi = new JMenuItem(new SaveShapeModelAsSTLAction());
		saveShapeModelMenu.add(mi);

		JMenu convertMenu=new JMenu("Convert...");
		this.add(convertMenu);
		mi = new JMenuItem(new BatchConvertEsriToSbmtAction());
		convertMenu.add(mi);

		mi = new JMenuItem(new ShowCameraOrientationAction());
		this.add(mi);

		// mi = new JMenuItem(new CopyToClipboardAction());
		// this.add(mi);
		// mi = new JCheckBoxMenuItem(new ShowSimpleCylindricalProjectionAction());
		// this.add(mi);
		if (Configuration.useFileCache())
		{
			mi = new JMenuItem(new ClearCacheAction());
			this.add(mi);
		}

		// On macs the exit action is in the Application menu not the file menu
		if (!Configuration.isMac())
		{
			mi = new JMenuItem(new PreferencesAction());
			this.add(mi);

			this.addSeparator();

			mi = new JMenuItem(new ExitAction());
			this.add(mi);
		} else
		{
			try
			{
				OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("showPreferences", (Class[]) null));
				OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("exitTool", (Class[]) null));
			} catch (SecurityException e)
			{
				e.printStackTrace();
			} catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void showPreferences()
	{
		if (preferencesDialog == null)
		{
			preferencesDialog = new PreferencesDialog(null, false);
			preferencesDialog.setViewManager(rootPanel);
		}

		preferencesDialog.setLocationRelativeTo(rootPanel);
		preferencesDialog.setVisible(true);
	}

	public void exitTool()
	{
		System.exit(0);
	}

	private class OpenSession extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public OpenSession()
		{
			super("Open Session");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
		{
			File file = CustomFileChooser.showOpenDialog(FileMenu.this, "Open a previously saved session", fileExtensions);
			if (file != null)
			{
				loadFrom(file);
			}
		}
	}

	//	private class SaveSession extends AbstractAction
	//	{
	//		private static final long serialVersionUID = 1L;
	//
	//		public SaveSession()
	//		{
	//			super("Save Session");
	//		}
	//
	//		@Override
	//		public void actionPerformed(ActionEvent e)
	//		{
	//			// TODO Auto-generated method stub
	//		}
	//	}

	private class SaveSessionAs extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public SaveSessionAs()
		{
			super("Save Session As...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
		{
			File file = CustomFileChooser.showSaveDialog(FileMenu.this, "Save Current Session", "MySession", "sbmt");
			if (file != null)
			{
				saveTo(file);
			}
		}
	}

	private class SaveImageAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public SaveImageAction()
		{
			super("Export to Image...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			rootPanel.getCurrentView().getRenderer().saveToFile();
		}
	}

	private class Save6AxesViewsAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public Save6AxesViewsAction()
		{
			super("Export Six Views along Axes to Images...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			rootPanel.getCurrentView().getRenderer().save6ViewsToFile();
		}
	}

	private class SaveShapeModelAsPLTAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public SaveShapeModelAsPLTAction()
		{
			super("PLT (Gaskell Format)");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to PLT (Gaskell Format)", "model.plt");

			try
			{
				if (file != null)
					rootPanel.getCurrentView().getModelManager().getPolyhedralModel().saveAsPLT(file);
			} catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred exporting the shape model.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}

	private class SaveShapeModelAsOBJAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public SaveShapeModelAsOBJAction()
		{
			super("OBJ");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to OBJ", "model.obj");

			try
			{
				if (file != null)
					rootPanel.getCurrentView().getModelManager().getPolyhedralModel().saveAsOBJ(file);
			} catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred exporting the shape model.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}

	private class SaveShapeModelAsSTLAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public SaveShapeModelAsSTLAction()
		{
			super("STL");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to STL", "model.stl");

			try
			{
				if (file != null)
					rootPanel.getCurrentView().getModelManager().getPolyhedralModel().saveAsSTL(file);
			} catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred exporting the shape model.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}

	private class ShowCameraOrientationAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		private final CameraDialog refCameraDialog;

		public ShowCameraOrientationAction()
		{
			super("Camera...");

			refCameraDialog = new CameraDialog();
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
		{
			refCameraDialog.setRenderer(rootPanel.getCurrentView().getRenderer());
			refCameraDialog.setVisible(true);
		}
	}

	//	private class CopyToClipboardAction extends AbstractAction
	//	{
	//		private static final long serialVersionUID = 1L;
	//
	//		public CopyToClipboardAction()
	//		{
	//			super("Copy to Clipboard...");
	//		}
	//
	//		@Override
	//		public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
	//		{
	//			// TODO Actually implement this.
	//		}
	//	}
	//
	//	private class ShowSimpleCylindricalProjectionAction extends AbstractAction
	//	{
	//		private static final long serialVersionUID = 1L;
	//
	//		public ShowSimpleCylindricalProjectionAction()
	//		{
	//			super("Render using Simple Cylindrical Projection (Experimental)");
	//		}
	//
	//		@Override
	//		public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
	//		{
	//			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) e.getSource();
	//			rootPanel.getCurrentView().getRenderer().set2DMode(mi.isSelected());
	//		}
	//	}

	private class PreferencesAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public PreferencesAction()
		{
			super("Preferences...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			showPreferences();
		}
	}

	private class ClearCacheAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public ClearCacheAction()
		{
			super("Clear Cache");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			int option =
					JOptionPane.showOptionDialog(frame, "Do you wish to clear your local data cache? \nIf you do, all remotely loaded data will need to be reloaded "
							+ "from the server the next time you wish to view it. \nThis may take a few moments.", "Clear cache", 1, 3, null, null, null);			if (option == 0)
			{
				Configuration.clearCache();
			} else
			{
				return;
			}
		}

	}

	private class ExitAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public ExitAction()
		{
			super("Exit");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			exitTool();
		}
	}

	private void saveTo(File file)
	{
		Serializer serializer = Serializers.getDefault();
		try
		{
			serializer.save(file);
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void loadFrom(File file)
	{
		Serializer serializer = Serializers.getDefault();
		try
		{
			serializer.load(file);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class BatchConvertEsriToSbmtAction extends AbstractAction
	{

		public BatchConvertEsriToSbmtAction()
		{
			super("SBMT structures files to ESRI shape files");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			
			File[] files = CustomFileChooser.showOpenDialog(FileMenu.this, "Input SBMT structure files...", null, true);
			if (files == null)
				return;
			
			FileDialog chooser=new FileDialog((Frame)null, "Output ESRI shapefile directory...", FileDialog.SAVE);
			if (files[0].getParentFile()!=null)
				chooser.setDirectory(files[0].getParentFile().getAbsolutePath());
			chooser.setFile("shp");
			chooser.setFilenameFilter(new FilenameFilter()
			{
				
				@Override
				public boolean accept(File dir, String name)
				{
					return dir.isDirectory();
				}
			});
			chooser.setVisible(true);
			File[] ofiles=chooser.getFiles();	// there will only ever be 0 or 1 elements in this array, for the present use case, but we need the absolute path of the selected file, so getFile() which returns a String is inadequate
			if (ofiles==null || ofiles.length==0)
				return;
			Path opath=Paths.get(ofiles[0].getAbsolutePath());
			opath.toFile().mkdirs();

			PolyhedralModel body = rootPanel.getCurrentView().getModelManager().getPolyhedralModel();
			for (int i = 0; i < files.length; i++)
			{
				String fname = files[i].getName().toLowerCase();
				String oname = FilenameUtils.removeExtension(files[i].getName()) + ".shp";
				System.out.println(fname+"  -->  "+opath.resolve(oname));
				StructureModel model = null;
				if (fname.endsWith("circles"))
					try
					{
						model = AbstractStructureMappingControlPanel.loadStructuresFromFile(files[i], ModelNames.CIRCLE_STRUCTURES, body);
						ShapefileUtil.writeEllipseStructures(Lists.newArrayList(EllipseStructure.fromSbmtStructure((AbstractEllipsePolygonModel) model)), opath.resolve(oname));

					} catch (IOException ex)
					{
						ex.printStackTrace();
					} catch (Exception ex)
					{
						//ex.printStackTrace();
					}
				else if (fname.endsWith("ellipses"))
					try
					{
						model = AbstractStructureMappingControlPanel.loadStructuresFromFile(files[i], ModelNames.ELLIPSE_STRUCTURES, body);
						ShapefileUtil.writeEllipseStructures(Lists.newArrayList(EllipseStructure.fromSbmtStructure((AbstractEllipsePolygonModel) model)), opath.resolve(oname));
					} catch (IOException ex)
					{
						ex.printStackTrace();
					} catch (Exception ex)
					{
						//ex.printStackTrace();
					}
				else if (fname.endsWith("points"))
					try
					{
						model = AbstractStructureMappingControlPanel.loadStructuresFromFile(files[i], ModelNames.POINT_STRUCTURES, body);
						ShapefileUtil.writePointStructures(Lists.newArrayList(PointStructure.fromSbmtStructure((PointModel) model)), opath.resolve(oname));

					} catch (IOException ex)
					{
						ex.printStackTrace();
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}

				else if (fname.endsWith(".xml")) // try to read .xml as polygons and if no polygons are found try to read as lines
				{
					try
					{
						model = AbstractStructureMappingControlPanel.loadStructuresFromFile(files[i], ModelNames.POLYGON_STRUCTURES, body);
						ShapefileUtil.writeLineStructures(LineStructure.fromSbmtStructure((LineModel) model), opath.resolve(oname));

					} catch (IOException ex)
					{
						ex.printStackTrace();
					} catch (Exception ex)
					{
						try
						{
							model = AbstractStructureMappingControlPanel.loadStructuresFromFile(files[i], ModelNames.LINE_STRUCTURES, body);
							ShapefileUtil.writeLineStructures(LineStructure.fromSbmtStructure((LineModel) model), opath.resolve(oname));
						} catch (Exception e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}

				else if (fname.endsWith("polygons")) // user can employ the .polygons extension instead of .xml... this if clause also catches the case where the file itself is named "polygons"
					try
					{
						model = AbstractStructureMappingControlPanel.loadStructuresFromFile(files[i], ModelNames.POLYGON_STRUCTURES, body);
						ShapefileUtil.writeLineStructures(LineStructure.fromSbmtStructure((LineModel) model), opath.resolve(oname));
					} catch (IOException ex)
					{
						ex.printStackTrace();
					} catch (Exception ex)
					{
						//ex.printStackTrace();
					}
				else if (fname.endsWith("paths")) // user can employ the .paths extension instead of .xml... this if clause also catches the case where the file itself is named "polygons"
					try
					{
						model = AbstractStructureMappingControlPanel.loadStructuresFromFile(files[i], ModelNames.LINE_STRUCTURES, body);
						ShapefileUtil.writeLineStructures(LineStructure.fromSbmtStructure((LineModel) model), opath.resolve(oname));
					} catch (IOException ex)
					{
						ex.printStackTrace();
					} catch (Exception ex)
					{
						//ex.printStackTrace();
					}
				else
				{
					try
					{
						throw new IOException("Unable to determine content of " + fname);
					} catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

		}

	}
}
