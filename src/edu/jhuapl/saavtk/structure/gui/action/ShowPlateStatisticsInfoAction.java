package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.plateColoring.ColoringInfoWindow;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.PlateUtil;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.ClosedShape;
import edu.jhuapl.saavtk.structure.Structure;
import glum.gui.action.PopAction;
import glum.task.SilentTask;

/**
 * {@link PopAction} that will show the statistics of plate data associated with the list of {@link Structure}s.
 *
 * @author lopeznr1
 */
public class ShowPlateStatisticsInfoAction extends PopAction<Structure>
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final PolyhedralModel refSmallBody;

	/** Standard Constructor */
	public ShowPlateStatisticsInfoAction(AnyStructureManager aManager, PolyhedralModel aSmallBody, Component aParent)
	{
		refManager = aManager;
		refSmallBody = aSmallBody;
	}

	@Override
	public void executeAction(List<Structure> aItemL)
	{
		var tmpTask = new SilentTask();

		// Retrieve the unified vtkPolyData associated with the structures
		var tmpPolyData = PlateUtil.formUnifiedStructurePolyData(tmpTask, refManager, aItemL);
		if (tmpPolyData == null)
			return;

		try
		{
			// Transform vtkPolyData into FacetColoringData
			var data = refSmallBody.getPlateDataInsidePolydata(tmpPolyData);

			ColoringInfoWindow window = new ColoringInfoWindow(data);
		}
		catch (IOException aExp)
		{
			aExp.printStackTrace();
		}
	}

	@Override
	public void setChosenItems(Collection<Structure> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable if at least 1 item is a ClosedShape (with an interior shown)
		var isEnabled = aItemC.size() >= 1;
		if (isEnabled == true)
			isEnabled &= aItemC.stream().anyMatch(aItem -> aItem instanceof ClosedShape aClosedShape //
					&& aClosedShape.getShowInterior()) == true;

		aAssocMI.setEnabled(isEnabled);
	}

}
