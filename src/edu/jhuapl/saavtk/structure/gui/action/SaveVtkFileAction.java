package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.StructuresExporter;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;

/**
 * Object that defines the behavior associated with this action.
 * <P>
 * This class was originally part of the single monolithic file:
 * edu.jhuaple.saavtk.gui.panel.AbstractStructureMappingControlPanel.
 * <P>
 * Subclasses that were implementations of {@link Action} have been refactored
 * to the package edu.jhuapl.saavtk.structure.gui.action on ~2019Sep09.
 */
public class SaveVtkFileAction<G1 extends Structure> extends AbstractAction
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final StructureManager<G1> refStructureManager;

	public SaveVtkFileAction(StructureManager<G1> aManager)
	{
		super("VTK Polydata...");

		refStructureManager = aManager;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		JCheckBox multipleFileCB = new JCheckBox("Save as multiple files");
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(multipleFileCB, BorderLayout.EAST);
		File file = CustomFileChooser.showSaveDialogWithCustomSouthComponent(null, "Save Structure (VTK)",
				"structures.vtk", "vtk", panel);
		if (file != null)
		{
			try
			{
				StructuresExporter.exportToVtkFile((LineModel) refStructureManager, file.toPath(),
						multipleFileCB.isSelected());
			}
			catch (Exception e1)
			{
				JOptionPane.showMessageDialog(null, "Unable to save file to " + file.getAbsolutePath(), "Error Saving File",
						JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}

}
