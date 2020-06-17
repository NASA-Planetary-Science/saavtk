package edu.jhuapl.saavtk.view.lod;

import vtk.vtkAlgorithmOutput;
import vtk.vtkDataObject;
import vtk.vtkPolyDataMapper;
import vtk.vtkQuadricClustering;

/**
 * Collection of utility methods associated with LOD configuration and
 * utilization.
 * <P>
 * The basis for the logic of the create*() methods originated from the
 * edu.jhuapl.saavtk.util.SaavtkLODActor class. That class was overloaded and
 * has been refactored - thus core generation of decimated
 * {@link vtkPolyDataMapper} logic is here.
 *
 * @author lopeznr1
 */
public class LodUtil
{

	/**
	 * Returns a user friendly display string corresponding to the specified
	 * {@link LodMode}.
	 * <P>
	 * Null values will return the string: "---".s
	 */
	public static String getDisplayString(LodMode aMode)
	{
		if (aMode == null)
			return "---";

		switch (aMode)
		{
			case MaxSpeed:
				return "Max Speed";

			case MaxQuality:
				return "Max Quality";

			default:
				return "" + aMode;
		}

	}

	/**
	 * Takes {@link vtkAlgorithmOutput} and creates the {@link vtkPolyDataMapper} as
	 * a decimated version using quadric clustering where the number of divisions in
	 * each x,y,z dimension is specified as input.
	 */
	public static vtkPolyDataMapper createQuadricDecimatedMapper(vtkAlgorithmOutput aAlgorithmOutput, int divX, int divY,
			int divZ)
	{
		// Set decimator input
		vtkQuadricClustering decimator = new vtkQuadricClustering();
		decimator.SetInputConnection(aAlgorithmOutput);

		// Delegate
		return createQuadricDecimatedMapper(decimator, divX, divY, divZ);
	}

	/**
	 * Takes {@link vtkAlgorithmOutput} and creates the {@link vtkPolyDataMapper} as
	 * a decimated version using quadric clustering where the number of divisions is
	 * auto selected
	 */
	public static vtkPolyDataMapper createQuadricDecimatedMapper(vtkAlgorithmOutput aAlgorithmOutput)
	{
		// Set decimator input
		vtkQuadricClustering decimator = new vtkQuadricClustering();
		decimator.SetInputConnection(aAlgorithmOutput);

		// Delegate
		return createQuadricDecimatedMapper(decimator);
	}

	/**
	 * Takes {@link vtkDataObject} and creates the {@link vtkPolyDataMapper} as a
	 * decimated version using quadric clustering where the number of divisions in
	 * each x,y,z dimension is specified as input
	 *
	 * @param numDivisions
	 * @return
	 */
	public static vtkPolyDataMapper createQuadricDecimatedMapper(vtkDataObject dataObject, int divX, int divY, int divZ)
	{
		// Set decimator input
		vtkQuadricClustering decimator = new vtkQuadricClustering();
		decimator.SetInputDataObject(dataObject);

		// Call helper
		return createQuadricDecimatedMapper(decimator, divX, divY, divZ);
	}

	/**
	 * Takes {@link vtkDataObject} and creates the {@link vtkPolyDataMapper} as a
	 * decimated version using quadric clustering where the number of divisions is
	 * auto selected
	 *
	 * @return
	 */
	public static vtkPolyDataMapper createQuadricDecimatedMapper(vtkDataObject dataObject)
	{
		// Set decimator input
		vtkQuadricClustering decimator = new vtkQuadricClustering();
		decimator.SetInputDataObject(dataObject);

		// Delegate
		return createQuadricDecimatedMapper(decimator);
	}

	/**
	 * Takes the {@link vtkQuadricClustering} and creates the
	 * {@link vtkPolyDataMapper} as a decimated version using quadric clustering
	 * where the number of divisions is auto selected
	 *
	 * @param decimator Assumed to already have input data/connection set
	 */
	private static vtkPolyDataMapper createQuadricDecimatedMapper(vtkQuadricClustering decimator)
	{
		// Decimate the input data
		decimator.CopyCellDataOn();
		decimator.AutoAdjustNumberOfDivisionsOn();
		decimator.Update();

		// Link to mapper
		vtkPolyDataMapper retPDM = new vtkPolyDataMapper();
		retPDM.SetInputConnection(decimator.GetOutputPort());

		return retPDM;
	}

	/**
	 * Takes the {@link vtkQuadricClustering} and creates the
	 * {@link vtkPolyDataMapper} as a decimated version using quadric clustering
	 * where the number of divisions in each x,y,z dimension is specified as input
	 *
	 * @param decimator Assumed to already have input data/connection set
	 * @param divX
	 * @param divY
	 * @param divZ
	 */
	private static vtkPolyDataMapper createQuadricDecimatedMapper(vtkQuadricClustering decimator, int divX, int divY,
			int divZ)
	{
		// Return null on invalid division args (legacy behavoir)
		if (divX <= 1 || divY <= 1 || divZ <= 1)
			return null;

		// Decimate the input data
		decimator.CopyCellDataOn();
		decimator.SetNumberOfDivisions(divX, divY, divZ);
		decimator.Update();

		// Link to mapper
		vtkPolyDataMapper retPDM = new vtkPolyDataMapper();
		retPDM.SetInputConnection(decimator.GetOutputPort());

		return retPDM;
	}

}
