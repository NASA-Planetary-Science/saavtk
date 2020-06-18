package edu.jhuapl.saavtk.view.lod;

import vtk.vtkActor;

/**
 * {@link LodActor} which is utilized to track the "instantaneous"
 * {@link LodMode}.
 * <P>
 * The instantaneous LodMode should typically not be utilized by non-actor code.
 * This actor allows the UI to be updated with the actual instantaneous LodMode
 * to allow informative UI updates.
 *
 * @author lopeznr1
 */
class EmptyLodActor extends vtkActor implements LodActor
{
	// State vars
	private LodMode lastLodMode;

	/** Standard Constructor */
	public EmptyLodActor()
	{
		lastLodMode = null;
	}

	/**
	 * Return the last recorded instantaneous {@link LodMode}.
	 */
	public LodMode getLodMode()
	{
		return lastLodMode;
	}

	@Override
	public void setLodMode(LodMode aLodMode)
	{
		lastLodMode = aLodMode;
	}

}
