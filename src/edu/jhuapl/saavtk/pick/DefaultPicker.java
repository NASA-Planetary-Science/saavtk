package edu.jhuapl.saavtk.pick;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.Properties;

/**
 * This class is provided to support legacy support for the former "default
 * picker" (via {@link PropertyChangeListener}) notification mechanism. This
 * class will eventually be removed so new code should be developed via the
 * {@link #addListener(PickListener)} instead.
 *
 * @author lopeznr1
 */
public class DefaultPicker extends BaseDefaultPicker implements PickListener, PropertyChangeListener
{
	// State vars
	private final PropertyChangeSupport pcs;

	/** Standard Constructor */
	public DefaultPicker(Renderer aRenderer, PolyhedralModel aSmallBody)
	{
		super(aRenderer, aSmallBody);

		pcs = new PropertyChangeSupport(this);

		// Register ourself as a listener so we can propagate relevant events
		// to the registered PropertyChangeListeners
		addListener(this);
	}

	/** Legacy Constructor */
	@Deprecated
	public DefaultPicker(Renderer aRenderer, ModelManager aModelManager)
	{
		super(aRenderer, aModelManager);

		pcs = new PropertyChangeSupport(this);

		// Register ourself as a listener so we can propagate relevant events
		// to the registered PropertyChangeListeners
		addListener(this);
	}

	/**
	 * Notification of default pick events via this method are deprecated.
	 * <p>
	 * Please use {@link #addListener(PickListener)} instead.
	 */
	@Deprecated
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Notification of default pick events via this method are deprecated.
	 * <p>
	 * Please use {@link #delListener(PickListener)} instead.
	 */
	@Deprecated
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail since only MouseEvents are propagated (via the former design)
		if (aEvent instanceof MouseEvent == false)
			return;

		// Bail since only primary actions are propagated (via the former design)
		if (aMode != PickMode.ActivePri)
			return;

		// Bail if no valid target position
		Vector3D targetPos = aPrimaryTarg.getPosition();
		if (targetPos == null)
			return;

		pcs.firePropertyChange(Properties.MODEL_PICKED, null,
				new PickEvent((MouseEvent) aEvent, aPrimaryTarg.getActor(), aPrimaryTarg.getCellId(), targetPos.toArray()));
	}

}
