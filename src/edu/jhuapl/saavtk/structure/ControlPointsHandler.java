package edu.jhuapl.saavtk.structure;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;

/**
 * Interface that defines the methods needed to provide activation capability.
 * <P>
 * Activation capability is defined as allowing a user to to manipulate the
 * control points associated with some object in order to allow them to edit it
 * via a projected visual representation (aka editing a structure on a shape
 * model).
 *
 * @author lopeznr1
 */
public interface ControlPointsHandler<G1>
{
	/**
	 * Notification that an item should be created and installed with the specified
	 * control points.
	 */
	public G1 addItemWithControlPoints(int aId, List<Vector3D> aPointL);

	/**
	 * Notification that a control point should be added at the specified index. All
	 * control points at or past the specified index should be shifted down.
	 *
	 * @param aIdx   The index associated with the control point of interest. If the
	 *               value, -1, is provided then the control point will be added at
	 *               the very front of all control points.
	 * @param aPoint The position of the control point.
	 */
	public void addControlPoint(int aIdx, Vector3D aPoint);

	/**
	 * Notification that the control point at the specified index should be deleted.
	 *
	 * @param aIdx The index associated with the control point of interest.
	 */
	public void delControlPoint(int aIdx);

	/**
	 * Notification that the control point at the specified index should be moved to
	 * the specified position.
	 *
	 * @param aIdx     The index associated with the activation point of interest.
	 * @param aPoint   The updated position.
	 * @param aIsFinal This method may be called many times until the user is done
	 *                 with the edit action. At completion of the edit action this
	 *                 parameter will be set to true.
	 */
	public void moveControlPoint(int aIdx, Vector3D aPoint, boolean aIsFinal);

	/**
	 * Returns the control point (index) that has been activated.
	 */
	public int getActivatedControlPoint();

	/**
	 * Returns the item that has been activated.
	 */
	public G1 getActivatedItem();

	/**
	 * Returns control point (index) corresponding to the provided cell id.
	 * <P>
	 * The cell id will correspond to that of the activation actor.
	 */
	public int getControlPointIndexFromActivationCellId(int aCellId);

	/**
	 * Returns the structure corresponding to the provided cell id.
	 * <P>
	 * The cell id will correspond to that of the activation actor.
	 */
	public G1 getItemFromActivationCellId(int aCellId);

	/**
	 * Returns the number of points needed to be defined in order for an item to be
	 * created.
	 */
	public int getNumPointsNeededForNewItem();

	/**
	 * Returns the VTK actor associated with the display of the control points.
	 */
	public vtkActor getVtkControlPointActor();

	/**
	 * Returns the VTK actor associated with the display of the actual items.
	 */
	public vtkActor getVtkItemActor();

	/**
	 * Sets in the activated control point (index).
	 */
	public void setActivatedControlPoint(int aIdx);

	/**
	 * Sets in the activated item.
	 */
	public void setActivatedItem(G1 aItem);

	// TODO: Consider removing this method
	public boolean hasProfileMode();

}
