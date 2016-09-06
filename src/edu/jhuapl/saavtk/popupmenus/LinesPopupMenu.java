package edu.jhuapl.saavtk.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.CustomFileChooser;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.LineModel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;

public class LinesPopupMenu extends StructuresPopupMenu
{
    private LineModel model = null;
    private JMenuItem saveProfileAction;

    public LinesPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES), modelManager.getPolyhedralModel(), renderer, false, false, false);

        this.model = (LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);

        saveProfileAction = new JMenuItem(new SaveProfileAction());
        saveProfileAction.setText("Save Profile...");
        this.add(saveProfileAction);
    }

    @Override
    public void show(Component invoker, int x, int y)
    {
        // Disable certain items if more than one structure is selected
        boolean exactlyOne = model.getSelectedStructures().length == 1;
        saveProfileAction.setEnabled(exactlyOne);

        super.show(invoker, x, y);
    }

    private class SaveProfileAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            if (selectedStructures.length != 1)
                return;

            try
            {
                File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Profile", "profile.csv");
                if (file != null)
                    model.saveProfile(selectedStructures[0], file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(getInvoker(),
                        e1.getMessage()!=null ? e1.getMessage() : "An error occurred saving the profile.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
