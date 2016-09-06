package edu.jhuapl.saavtk.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.util.Configuration;


public class FileMenu extends JMenu
{
    private PreferencesDialog preferencesDialog;
    private ViewManager rootPanel;
    public JFrame frame;

    public FileMenu(ViewManager rootPanel)
    {
        super("File");
        this.rootPanel = rootPanel;

        JMenuItem mi = new JMenuItem(new SaveImageAction());
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
        mi = new JMenuItem(new CopyToClipboardAction());
        //this.add(mi);
//        mi = new JCheckBoxMenuItem(new ShowSimpleCylindricalProjectionAction());
//        this.add(mi);
        if (Configuration.useFileCache())
        {
            mi= new JMenuItem(new ClearCacheAction());
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
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("showPreferences", (Class[])null));
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("exitTool", (Class[])null));
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

    private class SaveImageAction extends AbstractAction
    {
        public SaveImageAction()
        {
            super("Export to Image...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            rootPanel.getCurrentView().getRenderer().saveToFile();
        }
    }

    private class Save6AxesViewsAction extends AbstractAction
    {
        public Save6AxesViewsAction()
        {
            super("Export Six Views along Axes to Images...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            rootPanel.getCurrentView().getRenderer().save6ViewsToFile();
        }
    }

    private class SaveShapeModelAsPLTAction extends AbstractAction
    {
        public SaveShapeModelAsPLTAction()
        {
            super("PLT (Gaskell Format)...");
        }

        public void actionPerformed(ActionEvent actionEvent)
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
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SaveShapeModelAsOBJAction extends AbstractAction
    {
        public SaveShapeModelAsOBJAction()
        {
            super("OBJ...");
        }

        public void actionPerformed(ActionEvent actionEvent)
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
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SaveShapeModelAsSTLAction extends AbstractAction
    {
        public SaveShapeModelAsSTLAction()
        {
            super("STL...");
        }

        public void actionPerformed(ActionEvent actionEvent)
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
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class ShowCameraOrientationAction extends AbstractAction
    {
        public ShowCameraOrientationAction()
        {
            super("Camera...");
        }

        public void actionPerformed(ActionEvent e)
        {
            new CameraDialog(rootPanel.getCurrentView().getRenderer()).setVisible(true);
        }
    }

    private class CopyToClipboardAction extends AbstractAction
    {
        public CopyToClipboardAction()
        {
            super("Copy to Clipboard...");
        }

        public void actionPerformed(ActionEvent e)
        {

        }
    }

    private class ShowSimpleCylindricalProjectionAction extends AbstractAction
    {
        public ShowSimpleCylindricalProjectionAction()
        {
            super("Render using Simple Cylindrical Projection (Experimental)");
        }

        public void actionPerformed(ActionEvent e)
        {
            JCheckBoxMenuItem mi = (JCheckBoxMenuItem)e.getSource();
            rootPanel.getCurrentView().getRenderer().set2DMode(mi.isSelected());
        }
    }

    private class PreferencesAction extends AbstractAction
    {
        public PreferencesAction()
        {
            super("Preferences...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            showPreferences();
        }
    }

    private class ClearCacheAction extends AbstractAction
    {
        public ClearCacheAction()
        {
            super("Clear Cache");
        }
        public void actionPerformed(ActionEvent actionEvent)
        {
            int option = JOptionPane.showOptionDialog(frame, "Do you wish to clear your local data cache? \nIf you do, all remotely loaded data will need to be reloaded "
                    + "from the server the next time you wish to view it. \nThis may take a few moments.", "Clear cache", 1, 3, null, null, null);
            if(option == 0)
            {
                deleteFile(new File(Configuration.getApplicationDataDir()+File.separator+"cache\\2"));
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
        public ExitAction()
        {
            super("Exit");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            exitTool();
        }
    }
}
