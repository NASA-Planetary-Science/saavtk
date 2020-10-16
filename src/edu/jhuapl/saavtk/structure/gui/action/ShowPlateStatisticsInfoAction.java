package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.saavtk.gui.plateColoring.ColoringInfoWindow;
import edu.jhuapl.saavtk.model.FacetColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.PlateUtil;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.gui.action.PopAction;
import glum.task.SilentTask;
import glum.task.Task;
import vtk.vtkPolyData;

/**
 * {@link PopAction} that will show the statistics of plate data associated with
 * the list of {@link Structure}s.
 * 
 * @author lopeznr1
 */
public class ShowPlateStatisticsInfoAction<G1 extends Structure> extends PopAction<G1>
{
	// Ref vars
	private final StructureManager<G1> refManager;
	private final PolyhedralModel refSmallBody;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public ShowPlateStatisticsInfoAction(StructureManager<G1> aManager, PolyhedralModel aSmallBody, Component aParent)
	{
		refManager = aManager;
		refSmallBody = aSmallBody;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		Task tmpTask = new SilentTask();

		// Retrieve the unified vtkPolyData associated with the structures
		vtkPolyData tmpPolyData = PlateUtil.formUnifiedStructurePolyData(tmpTask, refManager, aItemL);
		if (tmpPolyData == null)
			return;

		// Transform vtkPolyData into FacetColoringData
		FacetColoringData[] data = refSmallBody.getPlateDataInsidePolydata(tmpPolyData);

		try
		{
			ColoringInfoWindow window = new ColoringInfoWindow(data);
		}
		catch (IOException aExp)
		{
			aExp.printStackTrace();
		}
	}

}
