package edu.jhuapl.saavtk.gui.menu;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.OSXAdapter;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.CameraDialog;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.dialog.PreferencesDialog;
import edu.jhuapl.saavtk.state.StateSerializer;
import edu.jhuapl.saavtk.state.gson.GsonFileStateSerializer;
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
		JMenu saveShapeModelMenu = new JMenu("Export Shape Model to");
		this.add(saveShapeModelMenu);
		mi = new JMenuItem(new SaveShapeModelAsPLTAction());
		saveShapeModelMenu.add(mi);
		mi = new JMenuItem(new SaveShapeModelAsOBJAction());
		saveShapeModelMenu.add(mi);
		mi = new JMenuItem(new SaveShapeModelAsSTLAction());
		saveShapeModelMenu.add(mi);

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
		}
		else
		{
			try
			{
				OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("showPreferences", (Class[]) null));
				OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("exitTool", (Class[]) null));
			}
			catch (SecurityException e)
			{
				e.printStackTrace();
			}
			catch (NoSuchMethodException e)
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
		public void actionPerformed(ActionEvent e)
		{
			File file = CustomFileChooser.showOpenDialog(FileMenu.this, "Open a previously saved session", fileExtensions);
			if (file != null)
			{
				loadFrom(file);
			}
		}
	}

	private class SaveSession extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public SaveSession()
		{
			super("Save Session");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// TODO Auto-generated method stub
		}
	}

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
			super("PLT (Gaskell Format)...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to PLT (Gaskell Format)", "model.plt");

			try
			{
				if (file != null)
					rootPanel.getCurrentView().getModelManager().getPolyhedralModel().saveAsPLT(file);
			}
			catch (Exception e1)
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
			super("OBJ...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to OBJ", "model.obj");

			try
			{
				if (file != null)
					rootPanel.getCurrentView().getModelManager().getPolyhedralModel().saveAsOBJ(file);
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
			super("STL...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
		{
			File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model to STL", "model.stl");

			try
			{
				if (file != null)
					rootPanel.getCurrentView().getModelManager().getPolyhedralModel().saveAsSTL(file);
			}
			catch (Exception e1)
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

		public ShowCameraOrientationAction()
		{
			super("Camera...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
		{
			new CameraDialog(rootPanel.getCurrentView().getRenderer()).setVisible(true);
		}
	}

	private class CopyToClipboardAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public CopyToClipboardAction()
		{
			super("Copy to Clipboard...");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
		{
			// TODO Actually implement this.
		}
	}

	private class ShowSimpleCylindricalProjectionAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public ShowSimpleCylindricalProjectionAction()
		{
			super("Render using Simple Cylindrical Projection (Experimental)");
		}

		@Override
		public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
		{
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) e.getSource();
			rootPanel.getCurrentView().getRenderer().set2DMode(mi.isSelected());
		}
	}

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
				deleteFile(new File(Configuration.getApplicationDataDir() + File.separator + "cache\\2"));
			}
			else
			{
				return;
			}
		}

		private void deleteFile(File file)
		{
			if (file.isDirectory())
				for (File subDir : file.listFiles())
					deleteFile(subDir);

			file.delete();
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
		StateSerializer serializer = GsonFileStateSerializer.of(file);
		try
		{
			serializer.save(rootPanel.getStateManager().getState());
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void loadFrom(File file)
	{
		StateSerializer serializer = GsonFileStateSerializer.of(file);
		try
		{
			rootPanel.getStateManager().setState(serializer.load());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
