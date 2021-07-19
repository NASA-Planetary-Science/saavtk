package edu.jhuapl.saavtk.pick;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.util.Configuration;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Utility class for configuration of Picker related code.
 * <p>
 * Please note the following:
 * <ul>
 * <li>The method {@link #formNonDefaultPickerMap(Renderer, ModelManager)} is
 * marked for removal.
 * <li>The method {@link #setPickingEnabled(boolean)} should be investigated
 * further and eventually removed if possible.
 * <ul>
 *
 * @author lopeznr1
 */
public class PickUtil
{
	// not sure if volatile is really needed, but just to be sure
	private static volatile boolean pickingEnabled = true;

	/**
	 * Utility method that will automatically setup a listener on aComponent that
	 * deactivates the specified Picker when the aComponent is no longer showing.
	 */
	public static void autoDeactivatePickerWhenComponentHidden(PickManager aPickManager, Picker aPicker,
			JComponent aComponent)
	{
		PickerHierarchyDeactivator tmpHandler = new PickerHierarchyDeactivator(aPickManager, aPicker, aComponent);
		aComponent.addHierarchyListener(tmpHandler);
	}

	/**
	 * Utility method that forms the default collection of non-default Pickers to
	 * their corresponding PickMode.
	 */
	@Deprecated
	protected static Map<PickMode, Picker> formNonDefaultPickerMap(Renderer aRenderer, ModelManager aModelManager)
	{
		StructureManager<?> tmpCircleSelectionManager = (StructureManager<?>) aModelManager
				.getModel(ModelNames.CIRCLE_SELECTION).get(0);

		Map<PickMode, Picker> retMap = new HashMap<>();
		if (aModelManager.getModel(ModelNames.CIRCLE_STRUCTURES).get(0) != null)
			retMap.put(PickMode.CIRCLE_SELECTION,
					new CircleSelectionPicker(aRenderer, aModelManager.getPolyhedralModel(), tmpCircleSelectionManager));

		return retMap;
	}

	/**
	 * Determines if the specified {@link InputEvent} is a valid popup trigger.
	 * <p>
	 * To be considered a valid popup trigger, the following must be true:
	 * <ul>
	 * <li>InputEvent must be of type {@link MouseEvent}
	 * <li>On Linux / Windows systems, the event must be associated with the
	 * right-mouse-button.
	 * <li>On Mac systems the event must be either associated with the
	 * {@link MouseEvent#BUTTON3} or <CTRL> key is pressed while
	 * {@link MouseEvent#BUTTON1} is pressed.
	 * </ul>
	 */
	public static boolean isPopupTrigger(InputEvent aEvent)
	{
		// Only MouseEvents are valid popup triggers
		if (aEvent instanceof MouseEvent == false)
			return false;
		MouseEvent mouseEvent = (MouseEvent) aEvent;

		// We do not rely on the OS for the popup trigger in the renderer (as explained
		// in a comment in the DefaultPicker.mouseClicked function), we need to role out
		// our own popup trigger logic. That's we why have the following complicated
		// function. It's easier on non-macs. On macs we try to mimic the default
		// behaviour where a Control + left mouse click is a popup trigger. Also for
		// some reason, if you left mouse click while holding down the Command button,
		// then SwingUtilities.isRightMouseButton() returns true. We therefore also
		// prevent a popup from showing in this situation.
		if (Configuration.isMac())
		{
			if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.isControlDown())
				return true;

			if (mouseEvent.getButton() == MouseEvent.BUTTON3)
				return true;
		}
		else if (SwingUtilities.isRightMouseButton(mouseEvent) == true)
		{
			return true;
		}

		return false;
	}

	/**
	 * Unfortunately, crashes sometimes occur if the user drags around the mouse
	 * during a long running operation (e.g. changing to a high resolution). To
	 * prevent this, the following global function is provided to allow disabling of
	 * picking during such operations. Note that if the picking is requested to be
	 * enabled, a delay of half a second is made before enabling picking.
	 *
	 * TODO This is just a hack, investigate the cause of the crash more fully.
	 *
	 * @param b
	 */
	public static void setPickingEnabled(boolean b)
	{
		if (b == false)
		{
			pickingEnabled = false;
		}
		else
		{
			// Delay half a second before enabling picking. This helps prevent some crashes.

			int delay = 500; // milliseconds
			ActionListener taskPerformer = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt)
				{
					pickingEnabled = true;
				}
			};

			Timer timer = new Timer(delay, taskPerformer);
			timer.setRepeats(false);
			timer.start();
		}
	}

	/**
	 * Utility method that return true if the provided {@link vtkCellPicker} has
	 * selected something at the specified screen coordinates.
	 */
	public static boolean isPicked(vtkCellPicker aCellPicker, vtkJoglPanelComponent aRenComp, int aPosX, int aPosY,
			double aTolerance)
	{
		if (PickUtil.pickingEnabled == false)
			return false;

		aRenComp.getVTKLock().lock();

		aCellPicker.SetTolerance(aTolerance);

		// Note that on some displays, such as a retina display, the height used by
		// OpenGL is different than the height used by Java. Therefore we need
		// scale the mouse coordinates to get the right position for OpenGL.
		// double openGlHeight = aRenComp.getComponent().getSurfaceHeight();
		// double openGlHeight = aRenComp.getComponent().getHeight();
		double javaHeight = aRenComp.getComponent().getHeight();
		// double scale = openGlHeight / javaHeight;
		double scale = 1.0;
		int tmpVal = aCellPicker.Pick(scale * aPosX, scale * (javaHeight - aPosY - 1), 0.0, aRenComp.getRenderer());

		aRenComp.getVTKLock().unlock();

		boolean isPicked = tmpVal != 0;
		return isPicked;
	}

	/**
	 * Utility method that return true if the provided {@link vtkCellPicker} has
	 * selected something at the specified screen coordinates.
	 */
	public static boolean isPicked(vtkCellPicker aCellPicker, vtkJoglPanelComponent aRenComp, MouseEvent aEvent,
			double aPickTolerance)
	{
		// Delegate
		return isPicked(aCellPicker, aRenComp, aEvent.getX(), aEvent.getY(), aPickTolerance);
	}

}
