package edu.jhuapl.saavtk.pick;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;

/**
 * Immutable class that defines the target that was picked.
 * 
 * @author lopeznr1
 */
public class PickTarget
{
	// Constants
	public static final PickTarget Invalid = new PickTarget(null, Vector3D.ZERO, Vector3D.ZERO, -1);

	// Attributes
	private final vtkActor actor;
	private final Vector3D normal;
	private final Vector3D position;
	private final int cellId;

	/**
	 * Standard Constructor
	 * 
	 * @param aActor    The VTK actor that was picked
	 * @param aNormal   Corresponds to the normal associated with the position.
	 * @param aPosition Corresponds to the 3D position that was picked.
	 * @param aCellId   Corresponds to the cell id that was picked.
	 * @param aModel    The model associated with the pick action
	 */
	public PickTarget(vtkActor aActor, Vector3D aNormal, Vector3D aPosition, int aCellId)
	{
		actor = aActor;
		normal = aNormal;
		position = aPosition;
		cellId = aCellId;
	}

	public vtkActor getActor()
	{
		return actor;
	}

	public Vector3D getNormal()
	{
		return normal;
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
