package edu.jhuapl.saavtk.gui.dialog;

import java.awt.Component;
import java.awt.FileDialog;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.FileChooserBase;
import edu.jhuapl.saavtk.util.Configuration;

public class DirectoryChooser extends FileChooserBase
{
	/**
	 * Utility method that prompts the user to select a folder.
	 * 
	 * @param aParent
	 * @param aTitle
	 * @return
	 */
	public static File showOpenDialog(Component aParent, String aTitle)
	{
		// On a Mac show a native directory chooser. On other platforms use Swing.
		if (Configuration.isMac())
		{
			try
			{
				System.setProperty("apple.awt.fileDialogForDirectories", "true");
				FileDialog fd = new FileDialog(JOptionPane.getFrameForComponent(aParent), aTitle);
				if (getLastDirectory() != null)
					fd.setDirectory(getLastDirectory().getAbsolutePath());
				fd.setVisible(true);
				String returnedFile = fd.getFile();
				String returnedDirectory = fd.getDirectory();
				if (returnedFile != null)
				{
					if (returnedDirectory != null)
						setLastDirectory(new File(returnedDirectory));
					return new File(returnedDirectory, returnedFile);
				}
				else
				{
					return null;
				}
			}
			finally
			{
				System.setProperty("apple.awt.fileDialogForDirectories", "false");
			}
		}
		else
		{
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle(aTitle);
			fc.setCurrentDirectory(getLastDirectory());
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setAcceptAllFileFilterUsed(false);
			int result = fc.showOpenDialog(JOptionPane.getFrameForComponent(aParent));
			if (result == JFileChooser.APPROVE_OPTION)
			{
				setLastDirectory(fc.getSelectedFile());
				return fc.getSelectedFile();
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * Utility method that prompts the user to select a folder.
	 * <P>
	 * The default title is: 'Select Output Folder'
	 * 
	 * @param aParent
	 * @return
	 */
	public static File showOpenDialog(Component aParent)
	{
		// Delegate
		return showOpenDialog(aParent, "Select Output Folder");
	}

}
