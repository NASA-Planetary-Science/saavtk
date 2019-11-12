package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.StructurePanel;

/**
 * Object that defines the behavior associated with this action.
 * <P>
 * This class was originally part of the single monolithic file:
 * edu.jhuaple.saavtk.gui.panel.AbstractStructureMappingControlPanel.
 * <P>
 * Subclasses that were implementations of {@link Action} have been refactored
 * to the package edu.jhuapl.saavtk.structure.gui.action on ~2019Sep09.
 */
public class SaveSbmtStructuresFileAction<G1 extends Structure> extends AbstractAction
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final StructurePanel<G1> refParent;
	private final StructureManager<G1> refStructureManager;

	public SaveSbmtStructuresFileAction(StructurePanel<G1> aParent, StructureManager<G1> aManager)
	{
		super("SBMT Structures File...");

		refParent = aParent;
		refStructureManager = aManager;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		File file = refParent.getStructureFile();
		if (file != null)
		{
			// File already exists, use it as the default filename
			file = CustomFileChooser.showSaveDialog(refParent, "Select File", file.getName());
		}
		else
		{
			// We don't have a default filename to provide
			file = CustomFileChooser.showSaveDialog(refParent, "Select File");
		}

		if (file != null)
		{
			try
			{
				refStructureManager.saveModel(file);
				refParent.notifyFileLoaded(file);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(refParent),
						"There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

				ex.printStackTrace();
			}
		}

	}
}
