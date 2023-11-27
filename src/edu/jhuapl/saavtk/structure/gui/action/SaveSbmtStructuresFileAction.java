package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.StructurePanel;
import edu.jhuapl.saavtk.structure.io.StructureSaveUtil;
import edu.jhuapl.saavtk.structure.io.XmlLoadUtil;

/**
 * Action that will save the list of structures associated with the reference
 * {@link StructureManager}.
 * <P>
 * TODO: In the future consider saving only the selected structures rather than
 * all the structures.
 *
 * @author lopeznr1
 */
public class SaveSbmtStructuresFileAction<G1 extends Structure> extends AbstractAction
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final StructurePanel<G1> refParent;
	private final PolyhedralModel refSmallBody;
	private final StructureManager<G1> refStructureManager;

	public SaveSbmtStructuresFileAction(StructurePanel<G1> aParent, StructureManager<G1> aManager,
			PolyhedralModel aSmallBody)
	{
		super("SBMT Structures File...");

		refParent = aParent;
		refSmallBody = aSmallBody;
		refStructureManager = aManager;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Determine the default file name to use
		String fileName = null;
		File file = refParent.getStructureFile();
		if (file != null)
			fileName = file.getName();

		// Prompt the user for the file
		file = CustomFileChooser.showSaveDialog(refParent, "Select File", fileName);
		if (file == null)
			return;

		// Save the file
		try
		{
			saveStructureManagerToFile(file, refStructureManager, refSmallBody);
		}
		catch (Exception aExp)
		{
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(refParent),
					"There was an error saving the file.", "Error", JOptionPane.ERROR_MESSAGE);

			aExp.printStackTrace();
		}
	}

	/**
	 * Utility helper method that will save the {@link Structure}s from the provided
	 * manager to the specified file.
	 * <P>
	 * This method will throw {@link UnsupportedOperationException} if the provided
	 * {@link StructureManager} is not recognized.
	 *
	 * @param aFile      The file of interest.
	 * @param aManager   The relevant {@link StructureManager}
	 * @param aSmallBody The {@link PolyhedralModel} associated with the
	 *                   StructureManager
	 */
	private static void saveStructureManagerToFile(File aFile, StructureManager<?> aManager, PolyhedralModel aSmallBody)
			throws Exception
	{
		// Delegate
		if (aManager instanceof LineModel)
			XmlLoadUtil.saveManager(aFile, (LineModel<?>) aManager, aSmallBody);
		else if (aManager instanceof AbstractEllipsePolygonModel)
		{
			AbstractEllipsePolygonModel tmpManager = (AbstractEllipsePolygonModel) aManager;
			StructureSaveUtil.saveModel(aFile, tmpManager, tmpManager.getAllItems(), aSmallBody);
		}
		else
			throw new UnsupportedOperationException("Unknown manager type: " + aManager.getClass());
	}

}
