package edu.jhuapl.saavtk.model.structure;

import java.util.Collection;

import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.task.Task;
import vtk.vtkAppendPolyData;
import vtk.vtkPolyData;

/**
 * Collection of utility methods that provides the following functionality for {@link Ellipse} and {@link Polygon}
 * structures:
 * <ul>
 * <li>Generation of unified (interior) vtkPolyData from a list of {@link Structure}s.
 * <li>Progress mechanism via a {@link Task}
 * <li>TODO: Retrieval of plate data
 * </ul>
 * <p>
 * The logic for these utility methods (~2019Oct07) is based off: </br>
 * edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel
 *
 * @author lopeznr1
 */
public class PlateUtil
{
	/**
	 * Utility method that returns a unified {@link vtkPolyData} consisting of all the interior {@link vtkPolyData} of
	 * the list of {@link Structure}s.
	 * <p>
	 * This method will return null if the provided {@link Task} is aborted before completion.
	 * <p>
	 * The provided {@link Structure}s must be managed by the provided {@link StructureManager}.
	 */
	public static vtkPolyData formUnifiedStructurePolyData(Task aTask, AnyStructureManager aManager,
			Collection<? extends Structure> aItemC)
	{
		var tmpIdx = 0;
		var appendFilter = new vtkAppendPolyData();
		for (var aItem : aItemC)
		{
			// Bail if the task has been aborted
			if (aTask.isAborted() == true)
				return null;
			aTask.setProgress(tmpIdx, aItemC.size());
			tmpIdx++;

			appendFilter.AddInputData(aManager.getVtkInteriorPolyDataFor(aItem));
		}
		appendFilter.Update();

		var retPolyData = appendFilter.GetOutput();
		aTask.setProgress(1.0);

		return retPolyData;
	}

}
