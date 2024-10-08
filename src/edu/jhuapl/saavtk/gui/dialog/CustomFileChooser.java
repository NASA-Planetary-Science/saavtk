package edu.jhuapl.saavtk.gui.dialog;

import java.awt.Component;
import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.jhuapl.saavtk.gui.FileChooserBase;
import edu.jhuapl.saavtk.gui.util.FileExtensionsAndDescriptions;

public class CustomFileChooser extends FileChooserBase
{
	private static class OpenFileFilter extends FileFilter
	{

		String description = "";
		String fileExt = "";

		public OpenFileFilter(Object extension, boolean includeExt)
		{
			if (extension instanceof String)
			{
				fileExt = (String) extension;
				description = (String) extension;
			}
			else if (extension instanceof FileExtensionsAndDescriptions)
			{
				FileExtensionsAndDescriptions temp = (FileExtensionsAndDescriptions) extension;
				fileExt = temp.getExtension();
				description = temp.getDescription(includeExt);
			}
			else
			{
				throw new IllegalArgumentException("File extension filter must be a string or FileExtensionsAndDescriptions");
			}

		}

		@Override
		public boolean accept(File f)
		{
			if (f.isDirectory())
				return true;
			return (f.getName().toLowerCase().endsWith(fileExt));
		}

		@Override
		public String getDescription()
		{
			return description;
		}

	}

	private static class CustomExtensionFilter implements FilenameFilter
	{
		private List<String> extensions;

		public CustomExtensionFilter(String extension)
		{
			extensions = new LinkedList<>();

			if (extension != null)
			{
				extensions.add(extension.toLowerCase());
			}
		}

		public CustomExtensionFilter(List<String> extensions)
		{
			this.extensions = new LinkedList<>();

			if (extensions != null)
			{
				for (String s : extensions)
				{
					this.extensions.add(s.toLowerCase());
				}
			}
		}

		// Accept all directories and all files with specified extension.
		@Override
		public boolean accept(File dir, String filename)
		{
			File f = new File(dir, filename);
			if (f.isDirectory() || (extensions == null || extensions.isEmpty()))
			{
				return true;
			}

			String ext = getExtension(f);
			if (ext != null)
			{
				for (String s : extensions)
				{
					if (ext.equals(s))
					{
						return true;
					}
				}
			}

			return false;
		}

		private String getExtension(File f)
		{
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1)
			{
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}
	}

	public static File showUserFilterableOpenDialog(Component parent, String title)
	{
		return showUserFilterableOpenDialog(parent, title, null, null);
	}

	public static File showUserFilterableOpenDialog(Component parent, String title, String defaultFileName)
	{
		return showUserFilterableOpenDialog(parent, title, null, defaultFileName);
	}

	public static File showUserFilterableOpenDialog(Component parent, String title, List<?> extensions)
	{
		return showUserFilterableOpenDialog(parent, title, extensions, null);
	}

	public static File showUserFilterableOpenDialog(Component parent, String title, List<?> extensions, String defaultFileName)
	{
		File[] files = showUserFilterableOpenDialog(parent, title, extensions, false, defaultFileName);
		if (files == null || files.length < 1)
			return null;
		else
			return files[0];
	}

	public static File[] showUserFilterableOpenDialog(Component parent, String title, List<?> extensions, boolean multiSelectionEnabled)
	{
		return showUserFilterableOpenDialog(parent, title, extensions, multiSelectionEnabled, null);
	}

	public static File[] showUserFilterableOpenDialog(Component parent, String title, List<?> extensions, boolean multiSelectionEnabled, String defaultFileName)
	{
		JFileChooser jfc = new JFileChooser();

		jfc.setDialogTitle(title);
		jfc.setMultiSelectionEnabled(multiSelectionEnabled);
		if (extensions != null)
		{
			for (Object extension : extensions)
			{
				jfc.addChoosableFileFilter(new OpenFileFilter(extension, true));
			}
			jfc.setAcceptAllFileFilterUsed(true);
		}

		if (getLastDirectory() != null)
		{
			jfc.setCurrentDirectory(getLastDirectory().getAbsoluteFile());
		}
		if (defaultFileName != null)
		{
			File defaultFile = new File(defaultFileName);
			File defaultDirectory = defaultFile.getParentFile();
			if (defaultDirectory.isDirectory())
			{
				jfc.setCurrentDirectory(defaultDirectory.getAbsoluteFile());
			}
			jfc.setSelectedFile(defaultFile);
		}
		jfc.showOpenDialog(parent);
		jfc.setVisible(true);

		File returnedFile = jfc.getSelectedFile();
		if (returnedFile != null)
		{
			setLastDirectory(jfc.getCurrentDirectory());
			if (multiSelectionEnabled)
			{
				return jfc.getSelectedFiles();
			} else
			{
				return new File[] { returnedFile };
			}
		} else
		{
			return null;
		}
	}
	
	public static File showOpenDialog(Component parent, String title)
	{
		return showOpenDialog(parent, title, null, null);
	}

	public static File showOpenDialog(Component parent, String title, String defaultFileName)
	{
		return showOpenDialog(parent, title, null, defaultFileName);
	}

	public static File showOpenDialog(Component parent, String title, List<?> extensions)
	{
		return showOpenDialog(parent, title, extensions, null);
	}

	public static File showOpenDialog(Component parent, String title, List<?> extensions, String defaultFileName)
	{
		File[] files = showOpenDialog(parent, title, extensions, false, defaultFileName);
		if (files == null || files.length < 1)
			return null;
		else
			return files[0];
	}
	
	public static File[] showOpenDialog(Component parent, String title, List<?> extensions, boolean multiSelectionEnabled)
	{
		return showOpenDialog(parent, title, extensions, multiSelectionEnabled, null);
	}
	
	public static File[] showOpenDialog(Component parent, String title, List<?> extensions, boolean multiSelectionEnabled, String defaultFileName)
	{
		FileDialog fc = new FileDialog(JOptionPane.getFrameForComponent(parent), title, FileDialog.LOAD);
		// fc.setAcceptAllFileFilterUsed(true);
		fc.setMultipleMode(multiSelectionEnabled);
		if (extensions != null)
		{
			List<String> extAsString = extensions.stream().map(item -> item.toString()).toList();
			fc.setFilenameFilter(new CustomExtensionFilter(extAsString));
		}
		if (getLastDirectory() != null)
			fc.setDirectory(getLastDirectory().getAbsolutePath());
		if (defaultFileName != null)
		{
			File defaultFile = new File(defaultFileName);
			File defaultDirectory = defaultFile.getParentFile();
			if (defaultDirectory.isDirectory())
			{
				fc.setDirectory(defaultDirectory.getAbsolutePath());
			}
			fc.setFile(defaultFile.getName());
		}
		fc.setVisible(true);
		String returnedFile = fc.getFile();
		if (returnedFile != null)
		{
			setLastDirectory(new File(fc.getDirectory()));
			if (multiSelectionEnabled)
			{
				return fc.getFiles();
			} else
			{
				File file = new File(fc.getDirectory(), fc.getFile());
				return new File[] { file };
			}
		} else
		{
			return null;
		}
	}

	public static File showSaveDialog(Component parent, String title)
	{
		return showSaveDialog(parent, title, null, null);
	}

	public static File showSaveDialog(Component parent, String title, String defaultFilename)
	{
		return showSaveDialog(parent, title, defaultFilename, null);
	}

	public static File showSaveDialog(Component parent, String title, String defaultFilename, String extension)
	{
		FileDialog fc = new FileDialog(JOptionPane.getFrameForComponent(parent), title, FileDialog.SAVE);
		// fc.setAcceptAllFileFilterUsed(true);
		if (extension != null)
			fc.setFilenameFilter(new CustomExtensionFilter(extension));
		if (getLastDirectory() != null)
			fc.setDirectory(getLastDirectory().getAbsolutePath());
		if (defaultFilename != null)
			fc.setFile(defaultFilename);
		fc.setVisible(true);
		String returnedFile = fc.getFile();
		if (returnedFile != null)
		{
			setLastDirectory(new File(fc.getDirectory()));
			File file = new File(fc.getDirectory(), fc.getFile());

			String filename = file.getAbsolutePath();
			if (extension != null && !extension.isEmpty())
			{
				if (!filename.toLowerCase().endsWith("." + extension))
					filename += "." + extension;
			}
			file = new File(filename);

			// if (file.exists())
			// {
			// int response = JOptionPane.showConfirmDialog
			// (JOptionPane.getFrameForComponent(parent),
			// "Overwrite existing file?","Confirm Overwrite",
			// JOptionPane.OK_CANCEL_OPTION,
			// JOptionPane.QUESTION_MESSAGE);
			// if (response == JOptionPane.CANCEL_OPTION)
			// return null;
			// }

			return file;
		} else
		{
			return null;
		}

	}

	public static File showSaveDialogWithCustomSouthComponent(Component parent, String title, String defaultFilename, String extension, JComponent component)
	{
		JFileChooser fc = new JFileChooser();

		
		// fc.setAcceptAllFileFilterUsed(true);
		if (extension != null)
			fc.setFileFilter(new FileNameExtensionFilter("." + extension + " files", extension));
		if (getLastDirectory() != null)
			fc.setCurrentDirectory(getLastDirectory());
		//if (defaultFilename != null)
		//	fc.setSelectedFile(new File(defaultFilename));

		fc.setAccessory(component);
		int status = fc.showSaveDialog(parent);
		if (status == JFileChooser.APPROVE_OPTION)
		{

			File returnedFile = fc.getSelectedFile();
			if (returnedFile != null)
			{
				setLastDirectory(fc.getCurrentDirectory());

				String filename = returnedFile.getAbsolutePath();
				if (extension != null && !extension.isEmpty())
				{
					if (!filename.toLowerCase().endsWith("." + extension))
						filename += "." + extension;
				}
				return new File(filename);

				// if (file.exists())
				// {
				// int response = JOptionPane.showConfirmDialog
				// (JOptionPane.getFrameForComponent(parent),
				// "Overwrite existing file?","Confirm Overwrite",
				// JOptionPane.OK_CANCEL_OPTION,
				// JOptionPane.QUESTION_MESSAGE);
				// if (response == JOptionPane.CANCEL_OPTION)
				// return null;
				// }
			}
		}
		return null;

	}

	private CustomFileChooser()
	{
		throw new AssertionError("This class is intended to be used statically");
	}
}
