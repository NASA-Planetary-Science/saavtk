package edu.jhuapl.saavtk.pick;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.Model;
import vtk.vtkActor;

/**
 * Immutable class that defines the target that was picked.
 * 
 * @author lopeznr1
 */
public class PickTarget
{
	// Constants
	public static final PickTarget Invalid = new PickTarget(null, null, null, -1);

	// Attributes
	private final Model model;
	private final vtkActor actor;
	private final Vector3D position;
	private final int cellId;

	/**
	 * Standard Constructor
	 * 
	 * @param aModel    The model associated with the pick action
	 * @param aActor    The VTK actor that was picked
	 * @param aCellId   Corresponds to the cell id that was picked.
	 * @param aPosition Corresponds to the 3D position that was picked.
	 */
	public PickTarget(Model aModel, vtkActor aActor, Vector3D aPosition, int aCellId)
	{
		model = aModel;
		actor = aActor;
		position = aPosition;
		cellId = aCellId;
	}

	public Model getModel()
	{
		return model;
	}

	public vtkActor getActor()
	{
		return actor;
	}

	public Vector3D getPosition()
	{
		return position;
	}

	public int getCellId()
	{
		return cellId;
	}

}
