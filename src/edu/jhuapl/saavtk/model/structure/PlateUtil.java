package edu.jhuapl.saavtk.model.structure;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import edu.jhuapl.saavtk.model.FacetColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.vtk.VtkEllipsePainter;
import edu.jhuapl.saavtk.structure.vtk.VtkPolygonPainter;
import vtk.vtkAppendPolyData;
import vtk.vtkPolyData;

/**
 * Collection of utility that provides the following functionality for Ellipse
 * and Polygon structures:
 * <UL>
 * <LI>Retrieval of plate data
 * <LI>Saving of plate data
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
	 * Utility method to retrieve the plate data associated with the specified
	 * structure, aItem to the file aFile.
	 * <P>
	 * This method supports {@link StructureManager}s of type
	 * {@link AbstractEllipsePolygonModel} and {@link PolygonModel}.
	 * {@link StructureManager}s that do not match that type will result in an
	 * {@link UnsupportedOperationException}.
	 * <P>
	 * The provided item, aItem, must be managed by the provided
	 * {@link StructureManager}.
	 */
	@SuppressWarnings("unchecked")
	public static <G1 extends Structure> FacetColoringData[] getPlateDataInsideStructure(PolyhedralModel aSmallBody,
			StructureManager<G1> aManager, Collection<G1> aItemC)
	{
		if (aManager instanceof AbstractEllipsePolygonModel)
			return getPlateDataInsideEllipse(aSmallBody, (AbstractEllipsePolygonModel) aManager,
					(Collection<Ellipse>) aItemC);
		else if (aManager instanceof PolygonModel)
			return getPlateDataInsidePolygon(aSmallBody, (PolygonModel) aManager, (Collection<Polygon>) aItemC);
		else
			throw new UnsupportedOperationException(
					"StructureManager type is not recognized. Type: " + aManager.getClass());
	}

	/**
	 * Utility method to save the plate data associated with the specified
	 * structure, aItem to the file aFile.
	 * <P>
	 * This method supports {@link StructureManager}s of type
	 * {@link AbstractEllipsePolygonModel} and {@link PolygonModel}.
	 * {@link StructureManager}s that do not match that type will result in an
	 * {@link UnsupportedOperationException}.
	 * <P>
	 * The provided item, aItem, must be managed by the provided
	 * {@link StructureManager}.
	 */
	@SuppressWarnings("unchecked")
	public static <G1 extends Structure> void savePlateDataInsideStructure(PolyhedralModel aSmallBody,
			StructureManager<G1> aManager, Collection<G1> aItemC, File aFile) throws IOException
	{
		if (aManager instanceof AbstractEllipsePolygonModel)
			savePlateDataInsideEllipse(aSmallBody, (AbstractEllipsePolygonModel) aManager, (Collection<Ellipse>) aItemC,
					aFile);
		else if (aManager instanceof PolygonModel)
			savePlateDataInsidePolygon(aSmallBody, (PolygonModel) aManager, (Collection<Polygon>) aItemC, aFile);
		else
			throw new UnsupportedOperationException(
					"StructureManager type is not recognized. Type: " + aManager.getClass());
	}

	/**
	 * TODO: Add javadoc
	 */
	private static FacetColoringData[] getPlateDataInsideEllipse(PolyhedralModel aSmallBody,
			AbstractEllipsePolygonModel aManager, Collection<Ellipse> aItemC)
	{
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (Ellipse aItem : aItemC)
		{
			VtkEllipsePainter tmpPainter = aManager.getOrCreateVtkMainPainterFor(aItem);
			tmpPainter.vtkUpdateState();
			appendFilter.AddInputData(tmpPainter.getVtkInteriorPolyData());
		}
		appendFilter.Update();

		return aSmallBody.getPlateDataInsidePolydata(appendFilter.GetOutput());
	}

//	/**
//	 * TODO: Add javadoc
//	 */
//	private static FacetColoringData[] getPlateDataInsideEllipse(PolyhedralModel aSmallBody,
//			AbstractEllipsePolygonModel aManager, EllipsePolygon aItem)
//	{
//		VtkEllipsePainter tmpPainter = aManager.getOrCreateVtkMainPainterFor(aItem);
//		tmpPainter.vtkUpdateState();
//
//		vtkPolyData polydata = tmpPainter.getVtkInteriorPolyData();
//		return aSmallBody.getPlateDataInsidePolydata(polydata);
//	}

	/**
	 * TODO: Add javadoc
	 */
	private static FacetColoringData[] getPlateDataInsidePolygon(PolyhedralModel aSmallBody, PolygonModel aManager,
			Collection<Polygon> aItemC)
	{
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (Polygon aItem : aItemC)
		{
			aManager.configurePolygonInterior(aItem, true);

			VtkPolygonPainter tmpPainter = aManager.getOrCreateMainPainter(aItem);
			vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();

			appendFilter.AddInputData(tmpInteriorRegPD);
			appendFilter.Update();

			aManager.configurePolygonInterior(aItem, false);
		}

		FacetColoringData[] data = aSmallBody.getPlateDataInsidePolydata(appendFilter.GetOutput());
		return data;
	}

//	/**
//	 * TODO: Add javadoc
//	 */
//	private static FacetColoringData[] getPlateDataInsidePolygon(PolyhedralModel aSmallBody, PolygonModel aManager,
//			Polygon aItem)
//	{
//		if (aItem.getShowInterior() == true)
//		{
//			VtkPolygonPainter tmpPainter = aManager.getOrCreateMainPainter(aItem);
//			vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();
//			return aSmallBody.getPlateDataInsidePolydata(tmpInteriorRegPD);
//		}
//		else
//		{
//			aManager.configurePolygonInterior(aItem, true);
//
//			VtkPolygonPainter tmpPainter = aManager.getOrCreateMainPainter(aItem);
//			vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();
//			FacetColoringData[] data = aSmallBody.getPlateDataInsidePolydata(tmpInteriorRegPD);
//
//			aManager.configurePolygonInterior(aItem, false);
//
//			return data;
//		}
//	}

	/**
	 * TODO: Add javadoc
	 */
	private static void savePlateDataInsideEllipse(PolyhedralModel aSmallBody, AbstractEllipsePolygonModel aManager,
			Collection<Ellipse> aItemC, File aFile) throws IOException
	{
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (Ellipse aItem : aItemC)
		{
			VtkEllipsePainter tmpPainter = aManager.getOrCreateVtkMainPainterFor(aItem);
			tmpPainter.vtkUpdateState();
			appendFilter.AddInputData(tmpPainter.getVtkInteriorPolyData());
		}
		appendFilter.Update();

		aSmallBody.savePlateDataInsidePolydata(appendFilter.GetOutput(), aFile);
	}

//	/**
//	 * TODO: Add javadoc
//	 */
//	private static void savePlateDataInsideEllipse(PolyhedralModel aSmallBody, AbstractEllipsePolygonModel aManager,
//			Ellipse aItem, File aFile) throws IOException
//	{
//		VtkEllipsePainter tmpPainter = aManager.getOrCreateVtkMainPainterFor(aItem);
//		tmpPainter.vtkUpdateState();
//
//		vtkPolyData polydata = tmpPainter.getVtkInteriorPolyData();
//		aSmallBody.savePlateDataInsidePolydata(polydata, aFile);
//	}

	/**
	 * TODO: Add javadoc
	 */
	private static void savePlateDataInsidePolygon(PolyhedralModel aSmallBody, PolygonModel aManager,
			Collection<Polygon> aItemC, File aFile) throws IOException
	{
		vtkAppendPolyData appendFilter = new vtkAppendPolyData();
		for (Polygon aItem : aItemC)
		{
			aManager.configurePolygonInterior(aItem, true);

			VtkPolygonPainter tmpPainter = aManager.getOrCreateMainPainter(aItem);
			vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();
			appendFilter.AddInputData(tmpInteriorRegPD);
			appendFilter.Update();

			aManager.configurePolygonInterior(aItem, false);
		}
		aSmallBody.savePlateDataInsidePolydata(appendFilter.GetOutput(), aFile);
	}

//	/**
//	 * TODO: Add javadoc
//	 */
//	private static void savePlateDataInsidePolygon(PolyhedralModel aSmallBody, PolygonModel aManager, Polygon aItem,
//			File aFile) throws IOException
//	{
//		if (aItem.getShowInterior())
//		{
//			VtkPolygonPainter tmpPainter = aManager.getOrCreateMainPainter(aItem);
//			vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();
//			aSmallBody.savePlateDataInsidePolydata(tmpInteriorRegPD, aFile);
//		}
//		else
//		{
//			aManager.configurePolygonInterior(aItem, true);
//
//			VtkPolygonPainter tmpPainter = aManager.getOrCreateMainPainter(aItem);
//			vtkPolyData tmpInteriorRegPD = tmpPainter.getVtkInteriorRegPD();
//			aSmallBody.savePlateDataInsidePolydata(tmpInteriorRegPD, aFile);
//
//			aManager.configurePolygonInterior(aItem, false);
//		}
//	}

}
