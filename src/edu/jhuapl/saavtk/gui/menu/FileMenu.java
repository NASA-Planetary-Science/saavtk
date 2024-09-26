package edu.jhuapl.saavtk.gui.menu;

import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitHandler;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.preferences.PreferencesController;
import edu.jhuapl.saavtk.gui.dialog.preferences.PreferencesDialog;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure;
import edu.jhuapl.saavtk.model.structure.esri.LineStructure;
import edu.jhuapl.saavtk.model.structure.esri.PointStructure;
import edu.jhuapl.saavtk.model.structure.esri.ShapefileUtil;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.io.StructureLegacyUtil;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.ses.jsqrl.api.Serializer;
import edu.jhuapl.ses.jsqrl.impl.gson.Serializers;
import vtk.vtkOBJExporter;
import vtk.vtkOBJReader;
import vtk.vtkPolyData;
import vtk.vtkProp;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class FileMenu extends JMenu
{
	private static final long serialVersionUID = 1L;
	private final ImmutableList<?> fileExtensions;
	private final ViewManager rootPanel;
	private PreferencesDialog preferencesDialog;
	public JFrame frame;

	public FileMenu(ViewManager rootPanel)
	{
		this(rootPanel, ImmutableList.of());
	}

	public FileMenu(ViewManager rootPanel, Iterable<?> fileExtensions)
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

		mi = new JMenuItem(new SaveSceneAsOBJAction());
		this.add(mi);

		JMenu convertMenu = new JMenu("Convert...");
		this.add(convertMenu);
		mi = new JMenuItem(new BatchConvertEsriToSbmtAction());
		convertMenu.add(mi);

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
		}
        else
        {
        	try
			{
				Desktop.getDesktop().setPreferencesHandler(new PreferencesHandler() {
                    @Override
                    public void handlePreferences(java.awt.desktop.PreferencesEvent e) {
                        showPreferences();
                    }
                });
				Desktop.getDesktop().setQuitHandler(new QuitHandler() {
                    @Override
						public void handleQuitRequestWith(java.awt.desktop.QuitEvent e, java.awt.desktop.QuitResponse r) {
                        exitTool();
                    }
                });
			}
			catch (SecurityException e)
			{
				e.printStackTrace();
			}
        }
	}

	public void showPreferences()
	{
		if (preferencesDialog == null)
		{
			PreferencesController controller = new PreferencesController(null, false, rootPanel);
			preferencesDialog = controller.getDialog();
//			preferencesDialog.setViewManager(rootPanel);
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
			RenderIoUtil.saveToFile(rootPanel.getCurrentView().getRenderer());
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
			if (file == null)
				return;

			try
			{
				vtkPolyData vTmpPD = rootPanel.getCurrentView().getModelManager().getPolyhedralModel().getSmallBodyPolyData();
				PolyDataUtil.saveShapeModelAsPLT(vTmpPD, file);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred exporting the shape model.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}

	private class SaveSceneAsOBJAction extends AbstractAction
	{
		public SaveSceneAsOBJAction()
		{
			super("Export Scene to OBJs");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			File file = CustomFileChooser.showSaveDialog(null, "Export Scene to OBJ", "export.obj");

			if (file != null)
			{

				String fileprefix = FilenameUtils.removeExtension(file.getAbsolutePath());
				vtkJoglPanelComponent renderPanel = new vtkJoglPanelComponent();
				renderPanel.getRenderWindow().OffScreenRenderingOn();
				vtkOBJExporter exporter = new vtkOBJExporter();
				exporter.SetRenderWindow(renderPanel.getRenderWindow());
				vtkOBJReader importer = new vtkOBJReader();

				List<vtkProp> actors = rootPanel.getCurrentView().getRenderer().getAllActors();
				for (vtkProp actor : actors)
				{
					if (actor.GetVisibility() == 1)
					{
						renderPanel.getRenderer().AddViewProp(actor);
						renderPanel.Render();
						exporter.SetFilePrefix(fileprefix + "_" + actors.indexOf(actor));
						exporter.Update();
						renderPanel.getRenderer().RemoveViewProp(actor);
						importer.SetFileName(fileprefix + "_" + actors.indexOf(actor) + ".obj");
						importer.Update();
						if (importer.GetOutput().GetNumberOfPoints() == 0)
						{
							FileUtils.deleteQuietly(new File(fileprefix + "_" + actors.indexOf(actor) + ".obj"));
							FileUtils.deleteQuietly(new File(fileprefix + "_" + actors.indexOf(actor) + ".mtl"));
						}
					}
				}
				renderPanel.Delete();
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
			if (file == null)
				return;

			try
			{
				vtkPolyData vTmpPD = rootPanel.getCurrentView().getModelManager().getPolyhedralModel().getSmallBodyPolyData();
				PolyDataUtil.saveShapeModelAsOBJ(vTmpPD, file);
			}
			catch (Exception e1)
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
			if (file == null)
				return;

			try
			{
				vtkPolyData vTmpPD = rootPanel.getCurrentView().getModelManager().getPolyhedralModel().getSmallBodyPolyData();
				PolyDataUtil.saveShapeModelAsSTL(vTmpPD, file);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occurred exporting the shape model.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
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
					JOptionPane.showOptionDialog(frame, "Do you wish to clear your local data cache? \nIf you do, all remotely loaded data will need to be reloaded " + "from the server the next time you wish to view it. \nThis may take a few moments.", "Clear cache", 1, 3, null, null, null);
			if (option == 0)
			{
				Configuration.clearCache();
			}
			else
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
		}
		catch (IOException e1)
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
		}
		catch (IOException e)
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

			FileDialog chooser = new FileDialog((Frame) null, "Output ESRI shapefile directory...", FileDialog.SAVE);
			if (files[0].getParentFile() != null)
				chooser.setDirectory(files[0].getParentFile().getAbsolutePath());
			chooser.setFile("shp");
			chooser.setFilenameFilter(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name)
				{
					return dir.isDirectory();
				}
			});
			chooser.setVisible(true);
			File[] ofiles = chooser.getFiles(); // there will only ever be 0 or 1 elements in this array, for the present use case, but we need the absolute path of the selected file, so getFile() which returns a String is inadequate
			if (ofiles == null || ofiles.length == 0)
				return;
			Path opath = Paths.get(ofiles[0].getAbsolutePath());
			opath.toFile().mkdirs();

			PolyhedralModel body = rootPanel.getCurrentView().getModelManager().getPolyhedralModel();
			for (int i = 0; i < files.length; i++)
			{
				String fname = files[i].getName().toLowerCase();
				String oname = FilenameUtils.removeExtension(files[i].getName()) + ".shp";
				System.out.println(fname + "  -->  " + opath.resolve(oname));
				AnyStructureManager model;
				if (fname.endsWith("circles"))
					try
					{
						model = StructureLegacyUtil.loadStructureManagerFromFile(files[i], body);
						ShapefileUtil.writeEllipseStructures(Lists.newArrayList(EllipseStructure.fromSbmtStructure(model)), opath.resolve(oname));

					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
					catch (Exception ex)
					{
						//ex.printStackTrace();
					}
				else if (fname.endsWith("ellipses"))
					try
					{
						model = StructureLegacyUtil.loadStructureManagerFromFile(files[i], body);
						ShapefileUtil.writeEllipseStructures(Lists.newArrayList(EllipseStructure.fromSbmtStructure(model)), opath.resolve(oname));
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
					catch (Exception ex)
					{
						//ex.printStackTrace();
					}
				else if (fname.endsWith("points"))
					try
					{
						model = StructureLegacyUtil.loadStructureManagerFromFile(files[i], body);
						ShapefileUtil.writePointStructures(Lists.newArrayList(PointStructure.fromSbmtStructure(model)), opath.resolve(oname));

					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				else if (fname.endsWith(".xml")) // try to read .xml as polygons and if no polygons are found try to read as lines
				{
					try
					{
						model = StructureLegacyUtil.loadStructureManagerFromFile(files[i], body);
						ShapefileUtil.writeLineStructures(LineStructure.fromSbmtStructure(model), opath.resolve(oname));

					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
					catch (Exception ex)
					{
						try
						{
							model = StructureLegacyUtil.loadStructureManagerFromFile(files[i], body);
							ShapefileUtil.writeLineStructures(LineStructure.fromSbmtStructure(model), opath.resolve(oname));
						}
						catch (Exception e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}

				else if (fname.endsWith("polygons")) // user can employ the .polygons extension instead of .xml... this if clause also catches the case where the file itself is named "polygons"
					try
					{
						model = StructureLegacyUtil.loadStructureManagerFromFile(files[i], body);
						ShapefileUtil.writeLineStructures(LineStructure.fromSbmtStructure(model), opath.resolve(oname));
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
					catch (Exception ex)
					{
						//ex.printStackTrace();
					}
				else if (fname.endsWith("paths")) // user can employ the .paths extension instead of .xml... this if clause also catches the case where the file itself is named "polygons"
					try
					{
						model = StructureLegacyUtil.loadStructureManagerFromFile(files[i], body);
						ShapefileUtil.writeLineStructures(LineStructure.fromSbmtStructure(model), opath.resolve(oname));
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
					catch (Exception ex)
					{
						//ex.printStackTrace();
					}
				else
				{
					try
					{
						throw new IOException("Unable to determine content of " + fname);
					}
					catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

		}

	}
}
