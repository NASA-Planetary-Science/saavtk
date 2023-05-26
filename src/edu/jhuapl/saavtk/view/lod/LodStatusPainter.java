package edu.jhuapl.saavtk.view.lod;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.view.ViewActionListener;
import edu.jhuapl.saavtk.view.ViewChangeReason;
import edu.jhuapl.saavtk.vtk.VtkResource;
import vtk.vtkProp;

/**
 * Painter used to track the status of the (instantaneous) {@link LodMode}.
 *
 * @author lopeznr1
 */
public class LodStatusPainter implements ViewActionListener, VtkPropProvider, VtkResource
{
	// Reference vars
	private final Renderer refRenderer;

	// State vars
	private boolean isVisible;

	// Vtk vars
	private EmptyLodActor vEmptyLodA;

	/**
	 * Standard Constructor
	 */
	public LodStatusPainter(Renderer aRenderer)
	{
		refRenderer = aRenderer;

		isVisible = false;

		vEmptyLodA = new EmptyLodActor();

		// Register for events of interest
		refRenderer.addViewChangeListener(this);
	}

	/**
	 * Returns the last {@link LodMode} that was set.
	 */
	public LodMode getLastLodMode()
	{
		// Delegate
		return vEmptyLodA.getLodMode();
	}

	/**
	 * Returns the painter's visibility.
	 */
	public boolean getIsVisible()
	{
		return isVisible;
	}

	@Override
	public void handleViewAction(Object aSource, ViewChangeReason aReason)
	{
		vtkUpdateState();
	}

	@Override
	public void vtkDispose()
	{
		vEmptyLodA.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		; // Nothing to do
	}

	/**
	 * Sets the painter's visibility.
	 */
	public void setIsVisible(boolean aBool)
	{
		// Bail if no state change
		if (isVisible == aBool)
			return;
		isVisible = aBool;

		// Send out the update notification
		refRenderer.notifySceneChange();
	}

	@Override
	public Collection<vtkProp> getProps()
	{
		return ImmutableList.of(vEmptyLodA);
	}

}
