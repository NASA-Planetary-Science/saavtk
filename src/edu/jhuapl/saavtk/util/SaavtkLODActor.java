package edu.jhuapl.saavtk.util;

import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkDataObject;
import vtk.vtkMapper;
import vtk.vtkPolyDataMapper;
import vtk.vtkQuadricClustering;

/**
 * VTK actor which stores a LOD mapper and where user can switch between them
 * dynamically.
 *
 * For Paraview-like LOD switching, add observers to the render window
 * interactor for StartInteractionEvent and EndInteractionEvent that searches
 * for SaavtkLODActors in the scene and calls set methods on them
 *
 * @author twupy1
 */
public class SaavtkLODActor extends vtkActor
{
	// Reference vars
	private final Object refModel;

	// Keeps track of the actor's LOD mapper
	private vtkMapper normalMapper;
	private vtkMapper lodMapper;
	private boolean useLodMapper;

	/**
	 * Standard Constructor
	 *
	 * @param aModel Object that will be associated with this actor. May be null.
	 */
	public SaavtkLODActor(Object aModel)
	{
		refModel = aModel;

		// By default no mapper has been assigned
		normalMapper = null;
		lodMapper = null;
		useLodMapper = false;
	}

	/**
	 * Simplified Constructor
	 */
	public SaavtkLODActor()
	{
		this(null);
	}

	/**
	 * Returns the associated model.
	 * <P>
	 * The associated model is set at construction time to allow an object to be
	 * associated with this actor. The returned value will never change.
	 * <P>
	 * If the associated model does not match that of the specified type then null
	 * will be returned.
	 */
	public <G1> G1 getAssocModel(Class<G1> aType)
	{
		if (aType.isInstance(refModel) == true)
			return aType.cast(refModel);

		return null;
	}

	/**
	 * Method that returns the LOD flag.
	 * <P>
	 * LOD flags set to true will result in more coarse rendering but improved
	 * rendering speed.
	 */
	public boolean getLodFlag()
	{
		return useLodMapper;
	}

	/**
	 * Method that changes LOD flag.
	 * <P>
	 * LOD flags set to true will result in more coarse rendering but improved
	 * rendering speed.
	 */
	public void setLodFlag(boolean aFlag)
	{
		useLodMapper = aFlag;

		// Update mapper actually being used by actor
		updateMapper();
	}

	/**
	 * Saves passed in argument as the actor's LOD mapper and then applies a mapper
	 * to the actor The actual mapper that is applied depends based on whether LODs
	 * are enabled or not
	 *
	 * @param mapper
	 * @return
	 */
	public void setLODMapper(vtkMapper mapper)
	{
		// Keep track of the LOD actor
		lodMapper = mapper;

		// Update mapper actually being used by actor
		updateMapper();
	}

	/**
	 * Saves passed in argument as the actor's regular mapper and then applies a
	 * mapper to the actor The actual mapper that is applied depends on whether LODs
	 * are enabled or not Naming of this method matches that of parent class instead
	 * of standard Java convention
	 *
	 * @override Same as parent class's SetMapper() but also adds mapper to list
	 */
	@Override
	public void SetMapper(vtkMapper mapper)
	{
		// Keep track of it
		normalMapper = mapper;

		// Update mapper actually being used by actor
		updateMapper();
	}

	private void updateMapper()
	{
		if (useLodMapper && lodMapper != null)
		{
			super.SetMapper(lodMapper);
		}
		else if (normalMapper != null)
		{
			// Use the normal mapper by default if either LOD
			// is not activated or LOD is activated but no LOD
			// mapper has been set for this actor
			super.SetMapper(normalMapper);
		}
	}

//	/**
//	 * Activates the mapper with largest cell count that does not exceed
//	 * maxCellCount If no such mapper is found, will try to use mapper with smallest
//	 * cell count If that also does not work, then will not set any mapper
//	 *
//	 * @param maxCellCount
//	 * @return
//	 */
//	public boolean selectMapper(int maxCellCount)
//	{
//		System.out.println("START");
//		// Variables for keeping track of mappers
//		vtkMapper currMaxMapper = null;
//		vtkMapper currMinMapper = null;
//		int currMaxCells = Integer.MIN_VALUE;
//		int currMinCells = Integer.MAX_VALUE;
//
//		// Check out each mapper
//		for (vtkMapper m : lodMappers)
//		{
//			// Only consider non-null mappers
//			if (m != null)
//			{
//				// Update the mapper first to get the correct dataset
//				m.Update();
//				vtkDataSet dataSet = m.GetInputAsDataSet();
//
//				// Proceed if dataset is also non-null
//				if (dataSet != null)
//				{
//					// Save number of cells
//					int numCells = dataSet.GetNumberOfCells();
//
//					// Keep track of mapper with max constrained cells
//					if (numCells <= maxCellCount && currMaxCells < numCells)
//					{
//						currMaxMapper = m;
//						currMaxCells = numCells;
//					}
//
//					// Keep track of mapper with min cells
//					if (numCells < currMinCells)
//					{
//						currMinMapper = m;
//						currMinCells = numCells;
//					}
//				}
//			}
//		}
//
//		// Determine which mapper, if any, to set
//		if (currMaxMapper != null)
//		{
//			// Found a mapper with max cell count that satisfies constraints, set it
//			super.SetMapper(currMaxMapper);
//			Modified();
//			return true;
//		}
//		else if (currMinMapper != null)
//		{
//			// No mapper found with cell count that could satisfy constraint
//			// Going with mapper that has smallest cell coutn instead
//			super.SetMapper(currMinMapper);
//			Modified();
//			return false;
//		}
//		else
//		{
//			// No valid mappers (with datasets) have been found in general
//			// Can't do anything
//			return false;
//		}
//	}

	/**
	 * Takes input data object and sets the LOD mapper as a decimated version using
	 * quadric clustering where the number of divisions is auto selected
	 *
	 * @return
	 */
	public vtkPolyDataMapper setQuadricDecimatedLODMapper(vtkDataObject dataObject)
	{
		// Set decimator input
		vtkQuadricClustering decimator = new vtkQuadricClustering();
		decimator.SetInputDataObject(dataObject);

		// Call helper
		return setQuadricDecimatedLODMapper(decimator);
	}

	/**
	 * Takes algorithm output and sets the LOD mapper as a decimated version using
	 * quadric clustering where the number of divisions is auto selected
	 *
	 * @return
	 */
	public vtkPolyDataMapper setQuadricDecimatedLODMapper(vtkAlgorithmOutput algorithmOutput)
	{
		// Set decimator input
		vtkQuadricClustering decimator = new vtkQuadricClustering();
		decimator.SetInputConnection(algorithmOutput);

		// Call helper
		return setQuadricDecimatedLODMapper(decimator);
	}

	/**
	 * Takes algorithm output and sets the LOD mapper as a decimated version using
	 * quadric clustering where the number of divisions is auto selected
	 *
	 * @param decimator Assumed to already have input data/connection set
	 */
	private vtkPolyDataMapper setQuadricDecimatedLODMapper(vtkQuadricClustering decimator)
	{
		// Decimate the input data
		decimator.CopyCellDataOn();
		decimator.AutoAdjustNumberOfDivisionsOn();
		decimator.Update();

		// Link to mapper and save
		vtkPolyDataMapper lodMapper = new vtkPolyDataMapper();
		lodMapper.SetInputConnection(decimator.GetOutputPort());

		// Add the mapper
		setLODMapper(lodMapper);
		return lodMapper;
	}

	/**
	 * Takes its input data object and sets the LOD mapper as a decimated version
	 * using quadric clustering where the number of divisions in each x,y,z
	 * dimension is specified as input
	 *
	 * @param numDivisions
	 * @return
	 */
	public vtkPolyDataMapper setQuadricDecimatedLODMapper(vtkDataObject dataObject, int divX, int divY, int divZ)
	{
		// Set decimator input
		vtkQuadricClustering decimator = new vtkQuadricClustering();
		decimator.SetInputDataObject(dataObject);

		// Call helper
		return setQuadricDecimatedLODMapper(decimator, divX, divY, divZ);
	}

	/**
	 * Takes algorithm output and sets the LOD mapper as a decimated version using
	 * quadric clustering where the number of divisions in each x,y,z dimension is
	 * specified as input
	 *
	 * @param numDivisions
	 * @return
	 */
	public vtkPolyDataMapper setQuadricDecimatedLODMapper(vtkAlgorithmOutput algorithmOutput, int divX, int divY,
			int divZ)
	{
		// Set decimator input
		vtkQuadricClustering decimator = new vtkQuadricClustering();
		decimator.SetInputConnection(algorithmOutput);

		// Call helper
		return setQuadricDecimatedLODMapper(decimator, divX, divY, divZ);
	}

	/**
	 * Takes algorithm output and sets the LOD mapper as a decimated version using
	 * quadric clustering where the number of divisions in each x,y,z dimension is
	 * specified as input
	 *
	 * @param decimator Assumed to already have input data/connection set
	 * @param divX
	 * @param divY
	 * @param divZ
	 * @return
	 */
	private vtkPolyDataMapper setQuadricDecimatedLODMapper(vtkQuadricClustering decimator, int divX, int divY, int divZ)
	{
		if (divX > 1 && divY > 1 && divZ > 1)
		{
			// Decimate the input data
			decimator.CopyCellDataOn();
			decimator.SetNumberOfDivisions(divX, divY, divZ);
			decimator.Update();

			// Link to mapper and save
			vtkPolyDataMapper lodMapper = new vtkPolyDataMapper();
			lodMapper.SetInputConnection(decimator.GetOutputPort());

			// Add the mapper
			setLODMapper(lodMapper);

			// Let user know we were successful
			return lodMapper;
		}
		else
		{
			// Divisions are invalid
			return null;
		}
	}
}
