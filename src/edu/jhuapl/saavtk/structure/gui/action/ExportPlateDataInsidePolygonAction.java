package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.PlateUtil;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.gui.action.PopAction;
import glum.task.SilentTask;
import glum.task.Task;
import vtk.vtkPolyData;

/**
 * {@link PopAction} that exports the plate data of the list of
 * {@link Structure}s to a user specified file.
 * 
 * @author lopeznr1
 */
public class ExportPlateDataInsidePolygonAction<G1 extends Structure> extends PopAction<G1>
{
	// Ref vars
	private final StructureManager<G1> refManager;
	private final PolyhedralModel refSmallBody;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public ExportPlateDataInsidePolygonAction(StructureManager<G1> aManager, PolyhedralModel aSmallBody,
			Component aParent)
	{
		refManager = aManager;
		refSmallBody = aSmallBody;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no file selected
		File tmpFile = CustomFileChooser.showSaveDialog(refParent, "Save Plate Data", "platedata.csv");
		if (tmpFile == null)
			return;

		Task tmpTask = new SilentTask();
		try
		{
			vtkPolyData tmpPolyData = PlateUtil.formUnifiedStructurePolyData(tmpTask, refManager, aItemL);
			if (tmpPolyData == null)
				return;

			refSmallBody.savePlateDataInsidePolydata(tmpPolyData, tmpFile);
		}
		catch (Exception aExp)
		{
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(refParent),
					"Unable to save file to " + tmpFile.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
			aExp.printStackTrace();
		}
	}

}
