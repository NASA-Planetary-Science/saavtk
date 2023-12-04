package edu.jhuapl.saavtk.vtk;

/**
 * Interface that defines the methods used to manage the life cycle of VTK based data objects.
 * <p>
 * VTK objects may utilize resources that are not handled automatically via the garbage collector. Such objects should
 * release their resources via the {@link #vtkDispose()} method.
 *
 * @author lopeznr1
 */
public interface VtkResource
{
	/**
	 * Method to notify the object that it will no longer be used.
	 * <p>
	 * The implementing object should release all VTK based resources.
	 */
	public void vtkDispose();

	/**
	 * Method to mark the internal VTK state as stale.
	 */
	public default void vtkMarkStale()
	{
	}

	/**
	 * Method to update internal VTK state.
	 */
	public void vtkUpdateState();

}
