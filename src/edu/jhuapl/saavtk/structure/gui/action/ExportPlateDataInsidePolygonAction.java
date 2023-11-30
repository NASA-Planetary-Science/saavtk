package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.PlateUtil;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.ClosedShape;
import edu.jhuapl.saavtk.structure.Structure;
import glum.gui.action.PopAction;
import glum.task.SilentTask;
import glum.task.Task;
import vtk.vtkPolyData;

/**
 * {@link PopAction} that exports the plate data of the list of {@link Structure}s to a user specified file.
 *
 * @author lopeznr1
 */
public class ExportPlateDataInsidePolygonAction extends PopAction<Structure>
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final PolyhedralModel refSmallBody;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public ExportPlateDataInsidePolygonAction(AnyStructureManager aManager, PolyhedralModel aSmallBody,
			Component aParent)
	{
		refManager = aManager;
		refSmallBody = aSmallBody;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<Structure> aItemL)
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

	@Override
	public void setChosenItems(Collection<Structure> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable the item if at least one object is a ClosedShape (and the interior is shown):
		var isEnabled = aItemC.stream().anyMatch(aItem -> aItem instanceof ClosedShape aClosedShape //
				&& aClosedShape.getShowInterior() == true);
		aAssocMI.setEnabled(isEnabled);
	}

}
