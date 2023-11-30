package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.util.Preferences;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Object that manages a collection of {@link Picker}s..
 * <p>
 * The PickManager supports the following:
 * <ul>
 * <li>A {@link DefaultPicker} which is always active
 * <li>A shared/common {@link SelectionPicker}
 * <li>Any custom {@link Picker}
 * </ul>
 * Only 1 non-default {@link Picker}s may be active at any time.The non-default {@link Picker}s are are activated via
 * {@link #setActivePicker(Picker)}.
 * <p>
 * This class still relies on VTK's Interactor to accomplish manipulation of the ShapeModel. It would be ideal to do
 * away with the reliance of this VTK code and have a custom "Picker" that performs the manipulations natively via Java.
 *
 * @author lopeznr1
 */
public class PickManager
{
	// Ref vars
	private Renderer refRenderer;
	private vtkJoglPanelComponent refRenWin;

	// State vars
	private final List<PickManagerListener> listenerL;
	private final DefaultPicker defaultPicker;
	private final SelectionPicker selectionPicker;
	private Picker activePicker;
	private boolean currExclusiveMode;
	private double pickTolerance;

	/** Standard Constructor */
	public PickManager(Renderer aRenderer, PolyhedralModel aSmallBody)
	{
		refRenderer = aRenderer;
		refRenWin = aRenderer.getRenderWindowPanel();

		listenerL = new ArrayList<>();
		defaultPicker = new DefaultPicker(aRenderer, aSmallBody);
		selectionPicker = null; // Note: There is no shared/common SelectionPicker
		activePicker = NonePicker.Instance;
		currExclusiveMode = false;
		pickTolerance = Picker.DEFAULT_PICK_TOLERANCE;

		// Set the pick tolerance to the default value
		double tmpPickTolerance = Preferences.getInstance().getAsDouble(Preferences.PICK_TOLERANCE,
				Picker.DEFAULT_PICK_TOLERANCE);
		setPickTolerance(tmpPickTolerance);

		// Register for events of interest
		registerEventHandler();
	}

	/** Legacy Constructor */
	public PickManager(Renderer aRenderer, StatusNotifier aStatusNotifier, PolyhedralModel aSmallBody,
			ModelManager aModelManager)
	{
		refRenderer = aRenderer;
		refRenWin = aRenderer.getRenderWindowPanel();

		listenerL = new ArrayList<>();
		defaultPicker = new DefaultPicker(aRenderer, aModelManager);
		selectionPicker = new SelectionPicker(aRenderer, aStatusNotifier, aSmallBody);
		activePicker = NonePicker.Instance;
		currExclusiveMode = false;
		pickTolerance = Picker.DEFAULT_PICK_TOLERANCE;

		// Set the pick tolerance to the default value
		double tmpPickTolerance = Preferences.getInstance().getAsDouble(Preferences.PICK_TOLERANCE,
				Picker.DEFAULT_PICK_TOLERANCE);
		setPickTolerance(tmpPickTolerance);

		// Register for events of interest
		registerEventHandler();
	}

	/**
	 * Registers a listener with this PickManager
	 */
	public synchronized void addListener(PickManagerListener aListener)
	{
		listenerL.add(aListener);
	}

	/**
	 * Deregisters a listener with this PickManager
	 */
	public synchronized void delListener(PickManagerListener aListener)
	{
		listenerL.remove(aListener);
	}

	/**
	 * Returns the active Picker
	 */
	public Picker getActivePicker()
	{
		return activePicker;
	}

	/**
	 * Returns the shared/common {@link SelectionPicker} used for selecting a region.
	 */
	public SelectionPicker getSelectionPicker()
	{
		return selectionPicker;
	}

	/**
	 * Sets the active Picker.
	 * <p>
	 * If null is specified a no-operation Picker will be installed.
	 */
	public void setActivePicker(Picker aPicker)
	{
		// Bail if the Picker has not changed
		if (aPicker == activePicker)
			return;

		// Bail if the Picker would default to the NonePicker and already is
		// the NonePicker
		if (aPicker == null && activePicker == NonePicker.Instance)
			return;

		// Switch to the proper activePicker
		activePicker = aPicker;
		if (activePicker == null)
			activePicker = NonePicker.Instance;

		// Switch to the proper cursor
		int targCursorType = activePicker.getCursorType();
		if (refRenWin.getComponent().getCursor().getType() != targCursorType)
			refRenWin.getComponent().setCursor(new Cursor(targCursorType));

		// TODO: This is a poor design and should be passed in via the event handlers
		activePicker.setTolerance(pickTolerance);

		notifyListeners();
	}

	public DefaultPicker getDefaultPicker()
	{
		return defaultPicker;
	}

	public double getPickTolerance()
	{
		// All the pickers managed by this class should have the same
		// tolerance so just return tolerance of the default picker.
		return defaultPicker.getTolerance();
	}

	public void setPickTolerance(double aPickTolerance)
	{
		pickTolerance = aPickTolerance;

		defaultPicker.setTolerance(aPickTolerance);
		if (selectionPicker != null)
			selectionPicker.setTolerance(aPickTolerance);
	}

	/**
	 * Helper method that determines if the active Picker has entered into an activated state.
	 * <p>
	 * An activated state is a state where the Picker is in the process of a complex action and wants exclusive "control"
	 * of the mouse / keyboard.
	 */
	private void updateActiveState()
	{
		// Always request the focus so that we will get keyboard events
		refRenWin.getComponent().requestFocusInWindow();

		// Bail if there is no change in exclusive mode
		boolean tmpExclusiveMode = activePicker.isExclusiveMode();
		if (tmpExclusiveMode == currExclusiveMode)
			return;

		// If the activePicker requests to be exclusive then:
		// - disable secondary actions (suppress popups)
		// - disable ShapeModel manipulations
		currExclusiveMode = tmpExclusiveMode;
		if (currExclusiveMode == false)
		{
			defaultPicker.setSuppressModeActiveSec(false);
			refRenderer.setInteractorEnableState(true);
		}
		else
		{
			defaultPicker.setSuppressModeActiveSec(true);
			refRenderer.setInteractorEnableState(false);
		}
	}

	/**
	 * Helper method that will properly register our private event handler with the component associated with the
	 * vtkJoglPanelComponent.
	 */
	private void registerEventHandler()
	{
		// Register our internal event handler
		EventHandler tmpHandler = new EventHandler();
		DragSensitiveListener wrapHandler = new DragSensitiveListener(tmpHandler, 4, 500L);

		JComponent tmpComp = refRenWin.getComponent();

		// Remove the (previously registered) MouseListener / MouseMotionListener
		// We do this since we want to have first dibs on processing the events
		MouseListener[] mlTmpArr = tmpComp.getMouseListeners();
		for (MouseListener aListener : mlTmpArr)
			tmpComp.removeMouseListener(aListener);

		MouseMotionListener[] mmlTmpArr = tmpComp.getMouseMotionListeners();
		for (MouseMotionListener aListener : mmlTmpArr)
			tmpComp.removeMouseMotionListener(aListener);

		// Register our custom event handler
		tmpComp.addKeyListener(tmpHandler);
		tmpComp.addMouseListener(wrapHandler);
		tmpComp.addMouseMotionListener(wrapHandler);
		tmpComp.addMouseWheelListener(tmpHandler);

		// Re-add the (previously registered) MouseListener / MouseMotionListener
		for (MouseListener aListener : mlTmpArr)
			tmpComp.addMouseListener(aListener);

		for (MouseMotionListener aListener : mmlTmpArr)
			tmpComp.addMouseMotionListener(aListener);

		// Make the corresponding component focusable and request the focus
		tmpComp.setFocusable(true);
		tmpComp.requestFocusInWindow();
	}

	/**
	 * Helper method to send out notification to the listeners.
	 */
	private void notifyListeners()
	{
		List<PickManagerListener> tmpL;
		synchronized (this)
		{
			tmpL = new ArrayList<>(listenerL);
		}
		for (PickManagerListener aListener : tmpL)
			aListener.pickerChanged();
	}

	/**
	 * Internal class used to process and dispatch all keyboard / mouse events to the actual Pickers.
	 */
	private class EventHandler implements KeyListener, MouseInputListener, MouseWheelListener
	{
		@Override
		public void keyPressed(KeyEvent aEvent)
		{
			// Only forward key events to the defaultPicker when there is no active picker
			if (activePicker == NonePicker.Instance)
				defaultPicker.keyPressed(aEvent);

			activePicker.keyPressed(aEvent);
			updateActiveState();
		}

		@Override
		public void keyReleased(KeyEvent aEvent)
		{
			// Only forward key events to the defaultPicker when there is no active picker
			if (activePicker == NonePicker.Instance)
				defaultPicker.keyReleased(aEvent);

			activePicker.keyReleased(aEvent);
			updateActiveState();
		}

		@Override
		public void keyTyped(KeyEvent aEvent)
		{
			// Only forward key events to the defaultPicker when there is no active picker
			if (activePicker == NonePicker.Instance)
				defaultPicker.keyTyped(aEvent);

			activePicker.keyTyped(aEvent);
			updateActiveState();
		};

		@Override
		public void mouseClicked(MouseEvent aEvent)
		{
			defaultPicker.mouseClicked(aEvent);
			activePicker.mouseClicked(aEvent);
			updateActiveState();
		}

		@Override
		public void mousePressed(MouseEvent aEvent)
		{
			defaultPicker.mousePressed(aEvent);
			activePicker.mousePressed(aEvent);
			updateActiveState();
		}

		@Override
		public void mouseReleased(MouseEvent aEvent)
		{
			defaultPicker.mouseReleased(aEvent);
			activePicker.mouseReleased(aEvent);
			updateActiveState();
		}

		@Override
		public void mouseEntered(MouseEvent aEvent)
		{
			defaultPicker.mouseEntered(aEvent);
			activePicker.mouseEntered(aEvent);
			updateActiveState();
		}

		@Override
		public void mouseExited(MouseEvent aEvent)
		{
			defaultPicker.mouseExited(aEvent);
			activePicker.mouseExited(aEvent);
			updateActiveState();
		}

		@Override
		public void mouseDragged(MouseEvent aEvent)
		{
			defaultPicker.mouseDragged(aEvent);
			activePicker.mouseDragged(aEvent);
			updateActiveState();
		}

		@Override
		public void mouseMoved(MouseEvent aEvent)
		{
			defaultPicker.mouseMoved(aEvent);
			activePicker.mouseMoved(aEvent);
			updateActiveState();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent aEvent)
		{
			defaultPicker.mouseWheelMoved(aEvent);
			activePicker.mouseWheelMoved(aEvent);
			updateActiveState();
		}

	}

	/**
	 * Picker which is equivalent to a no-operation Picker.
	 * <p>
	 * This class (should have) has no state data and thus is a singleton. Currently since Picker is an class (with
	 * implementation details) rather than an interface this is not strictly true.
	 */
	private static class NonePicker extends Picker
	{
		// Singleton instance
		public static final NonePicker Instance = new NonePicker();

		@Override
		public boolean isExclusiveMode()
		{
			return false;
		}
	}

}
