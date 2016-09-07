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
    public static File showOpenDialog(Component parent)
    {
        // On a Mac show a native directory chooser. On other platforms use Swing.
        if (Configuration.isMac())
        {
            try
            {
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
                FileDialog fd = new FileDialog(JOptionPane.getFrameForComponent(parent), "Select Output Folder");
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
            fc.setDialogTitle("Select Output Folder");
            fc.setCurrentDirectory(getLastDirectory());
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            int result = fc.showOpenDialog(JOptionPane.getFrameForComponent(parent));
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

}
