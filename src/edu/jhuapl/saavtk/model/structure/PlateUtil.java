package edu.jhuapl.saavtk.model.structure;

import java.util.Collection;

import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.vtk.VtkPolygonPainter;
import glum.task.Task;
import vtk.vtkAppendPolyData;
import vtk.vtkPolyData;

/**
 * Collection of utility methods that provides the following functionality for
 * {@link Ellipse} and {@link Polygon} structures:
 * <UL>
 * <LI>Generation of unified (interior) vtkPolyData of a list of structures
 * <LI>TODO: Retrieval of plate data
 * <LI>Progress mechanism via a {@link Task}
 * </UL>
 * <P>
 * The logic for these utility methods (~2019Oct07) is based off of :
 * <UL>
 * <LI>edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel
 * <LI>edu.jhuapl.saavtk.model.structure.PolygonModel
 * </UL>
 *
 * @author lopeznr1
 */
public class PlateUtil
{
	/**
	 * Utility method that returns a unified {@link vtkPolyData} consisting of all
	 * the interior {@link vtkPolyData} of the list of {@link Structure}s.
	 * <P>
	 * This method will return null if the provided {@link Task} is aborted before
	 * completion.
	 * <P>
	 * This method supports {@link StructureManager}s of type
	 * {@link AbstractEllipsePolygonModel} and {@link PolygonModel}.
	 * {@link StructureManager}s that do not match that type will result in an
	 * {@link UnsupportedOperationException}.
	 * <P>
	 * The provided structures must be managed by the provided
	 * {@link StructureManager}.
	 */
	@SuppressWarnings("unchecked")
	public static <G1 extends Structure> vtkPolyData formUnifiedStructurePolyData(Task aTask,
			StructureManager<G1> aManager, Collection<G1> aItemC)
	{
		// Delegate
		if (aManager instanceof AbstractEllipsePolygonModel)
			return formUnifiedEllipsePolyData(aTask, (AbstractEllipsePolygonModel) aManager, (Collection<Ellipse>) aItemC);
		else if (aManager instanceof PolygonModel)
			return formUnifiedPolgonPloyData(aTask, (PolygonModel) aManager, (Collection<Polygon>) aItemC);
		else
			throw new UnsupportedOperationException(
					"StructureManager type is not recognized. Type: " + aManager.getClass());
	}

	/**
	 * Utility helper method that returns a unified {@link vtkPolyData} consisting
	 * of all the interior {@link vtkPolyData} of the list of {@link Ellipse}s.
	 */
	private static vtkPolyData formUnifiedEllipsePolyData(Task aTask, AbstractEllipsePolygonModel aManager,
			Collection<Ellipse> aItemC)
	{
		int tmpIdx = 0;
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (Ellipse aItem : aItemC)
		{
			// Bail if the task has been aborted
			if (aTask.isAborted() == true)
				return null;
			aTask.setProgress(tmpIdx, aItemC.size());
			tmpIdx++;

			appendFilter.AddInputData(aManager.getVtkInteriorPolyDataFor(aItem));
		}
		appendFilter.Update();

		vtkPolyData retPolyData = appendFilter.GetOutput();
		aTask.setProgress(1.0);

		return retPolyData;
	}

	/**
	 * Utility helper method that returns a unified {@link vtkPolyData} consisting
	 * of all the interior {@link vtkPolyData} of the list of {@link Polygon}s.
	 */
	private static vtkPolyData formUnifiedPolgonPloyData(Task aTask, PolygonModel aManager, Collection<Polygon> aItemC)
	{
		int tmpIdx = 0;
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (Polygon aItem : aItemC)
		{
			// Bail if the task has been aborted
			if (aTask.isAborted() == true)
				return null;
			aTask.setProgress(tmpIdx, aItemC.size());
			tmpIdx++;

			aManager.configurePolygonInterior(aItem, true);

			VtkPolygonPainter tmpPainter = aManager.getOrCreateMainPainter(aItem);
			vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();

			appendFilter.AddInputData(tmpInteriorRegPD);
			appendFilter.Update();

			aManager.configurePolygonInterior(aItem, false);
		}

		vtkPolyData retPolyData = appendFilter.GetOutput();
		aTask.setProgress(1.0);

		return retPolyData;
	}

}
