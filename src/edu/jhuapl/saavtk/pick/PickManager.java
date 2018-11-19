package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Map;

import javax.swing.JComponent;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.util.Preferences;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Object that manages a collection of Pickers.
 * <P>
 * The PickManager supports the following:
 * <UL>
 * <LI>A default Picker which is always active
 * <LI>A set of non-default Pickers of which at most only 1 is active at any
 * time.
 * <LI>The non-default Picker is enabled via the method
 * {@link #setPickMode(PickMode)}.
 * </UL>
 * <P>
 * This class still relies on VTK's Interactor to accomplish manipulation of the
 * ShapeModel. It would be ideal to do away with the reliance of this VTK code
 * and have a custom "Picker" that performs the manipulations natively via Java.
 */
public class PickManager
{
	public enum PickMode
	{
		DEFAULT,
		CIRCLE_SELECTION,
		POLYGON_DRAW,
		LINE_DRAW,
		CIRCLE_DRAW,
		ELLIPSE_DRAW,
		POINT_DRAW,
		LIDAR_SHIFT
	}

	// Ref vars
	private Renderer refRenderer;
	private vtkJoglPanelComponent refRenWin;
	private PopupManager refPopupManager;

	// State vars
	private Map<PickMode, Picker> nondefaultPickers;
	private Picker activePicker;
	private DefaultPicker defaultPicker;
	private PickMode pickMode;
	private boolean currExclusiveMode;

	public PickManager(Renderer aRenderer, PopupManager aPopupManager, Map<PickMode, Picker> aNonDefaultPickers,
			DefaultPicker aDefaultPicker)
	{
		refRenderer = aRenderer;
		refRenWin = aRenderer.getRenderWindowPanel();
		refPopupManager = aPopupManager;

		nondefaultPickers = aNonDefaultPickers;
		activePicker = NonePicker.Instance;
		defaultPicker = aDefaultPicker;
		pickMode = PickMode.DEFAULT;
		currExclusiveMode = false;

		// Set the pick tolerance to the default value
		double tmpPickTolerance = Preferences.getInstance().getAsDouble(Preferences.PICK_TOLERANCE,
				Picker.DEFAULT_PICK_TOLERANCE);
		setPickTolerance(tmpPickTolerance);

		// Register for events of interest
		registerEventHandler();
	}

	public PickManager(Renderer aRenderer, StatusBar aStatusBar, ModelManager aModelManager, PopupManager aPopupManager)
	{
		this(aRenderer, aPopupManager, PickUtil.formNonDefaultPickerMap(aRenderer, aModelManager),
				new DefaultPicker(aRenderer, aStatusBar, aModelManager, aPopupManager));
	}

	/**
	 * Sets the PickMode which will activate the corresponding (non-default) Picker.
	 * 
	 * @param aMode
	 */
	public void setPickMode(PickMode aMode)
	{
		// Bail if the PickMode has not changed
		if (pickMode == aMode)
			return;
		pickMode = aMode;

		// Switch to the proper activePicker
		activePicker = nondefaultPickers.get(pickMode);
		if (activePicker == null)
			activePicker = NonePicker.Instance;

		// Switch to the proper cursor
		int targCursorType = activePicker.getCursorType();
		if (refRenWin.getComponent().getCursor().getType() != targCursorType)
			refRenWin.getComponent().setCursor(new Cursor(targCursorType));
	}

	public DefaultPicker getDefaultPicker()
	{
		return defaultPicker;
	}

	public double getPickTolerance()
	{
		// All the pickers managed by this class should have the same
		// tolerance so just return tolerance of the default picker.
		return defaultPicker.getPickTolerance();
	}

	public void setPickTolerance(double pickTolerance)
	{
		defaultPicker.setPickTolerance(pickTolerance);
		for (PickMode pm : nondefaultPickers.keySet())
			nondefaultPickers.get(pm).setPickTolerance(pickTolerance);
	}

	public PopupManager getPopupManager()
	{
		return refPopupManager;
	}

	/**
	 * Helper method that determines if the active Picker has entered into an
	 * activated state.
	 * <P>
	 * An activated state is a state where the Picker is in the process of a complex
	 * action and wants exclusive "control" of the mouse / keyboard.
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
		// - disable popups (supress them)
		// - disable ShapeModel manipulations
		currExclusiveMode = tmpExclusiveMode;
		if (currExclusiveMode == false)
		{
			defaultPicker.setSuppressPopups(false);
			refRenderer.setInteractorEnableState(true);
		}
		else
		{
			defaultPicker.setSuppressPopups(true);
			refRenderer.setInteractorEnableState(false);
		}
	}

	/**
	 * Helper method that will properly register our private event handler with the
	 * component associated with the vtkJoglPanelComponent.
	 */
	private void registerEventHandler()
	{
		// Register our internal event handler
		EventHandler tmpHandler = new EventHandler();

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
		tmpComp.addMouseListener(tmpHandler);
		tmpComp.addMouseMotionListener(tmpHandler);
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
	 * Internal class used to process and dispatch all keyboard / mouse events to
	 * the actual Pickers.
	 */
	private class EventHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
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
	 * <P>
	 * This class (should have) has no state data and thus is a singleton. Currently
	 * since Picker is an class (with implementation details) rather than an
	 * interface this is not strictly true.
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