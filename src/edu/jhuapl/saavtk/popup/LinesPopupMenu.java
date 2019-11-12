package edu.jhuapl.saavtk.popup;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.LineModel;

public class LinesPopupMenu extends StructuresPopupMenu<Line>
{
    private JMenuItem saveProfileAction;

    public LinesPopupMenu(ModelManager modelManager, Renderer renderer)
    {
   	 super(modelManager, renderer, ModelNames.LINE_STRUCTURES);

        saveProfileAction = new JMenuItem(new SaveProfileAction());
        saveProfileAction.setText("Save Profile...");
        add(saveProfileAction);
    }

    @Override 
    public void show(Component invoker, int x, int y)
    {
        // Disable certain items if more than one structure is selected
        boolean isEnabled = getManager().getSelectedItems().size() == 1;
        saveProfileAction.setEnabled(isEnabled);

        super.show(invoker, x, y);
    }

    private class SaveProfileAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
      	  List<Line> pickL = getManager().getSelectedItems().asList();
            if (pickL.size() != 1)
                return;

            try
            {
                File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Profile", "profile.csv");
                if (file != null)
               	 ((LineModel<?>)getManager()).saveProfile(pickL.get(0), file);
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
