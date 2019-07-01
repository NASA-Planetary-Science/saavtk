package edu.jhuapl.saavtk.pick;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import edu.jhuapl.saavtk.util.Configuration;

/**
 * Utility class for Picker related code.
 */
public class PickUtil
{
	// not sure if volatile is really needed, but just to be sure
	protected static volatile boolean pickingEnabled = true;

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
	public static Map<PickMode, Picker> formNonDefaultPickerMap(Renderer aRenderer, ModelManager aModelManager)
	{
		Map<PickMode, Picker> retMap;

		retMap = new HashMap<>();
		if (aModelManager.getModel(ModelNames.LINE_STRUCTURES) != null)
			retMap.put(PickMode.LINE_DRAW, new ControlPointsStructurePicker(aRenderer, aModelManager, ModelNames.LINE_STRUCTURES));
		if (aModelManager.getModel(ModelNames.POLYGON_STRUCTURES) != null)
			retMap.put(PickMode.POLYGON_DRAW, new ControlPointsStructurePicker(aRenderer, aModelManager, ModelNames.POLYGON_STRUCTURES));
		if (aModelManager.getModel(ModelNames.CIRCLE_STRUCTURES) != null)
			retMap.put(PickMode.CIRCLE_DRAW, new CirclePicker(aRenderer, aModelManager));
		if (aModelManager.getModel(ModelNames.ELLIPSE_STRUCTURES) != null)
			retMap.put(PickMode.ELLIPSE_DRAW, new EllipsePicker(aRenderer, aModelManager));
		if (aModelManager.getModel(ModelNames.POINT_STRUCTURES) != null)
			retMap.put(PickMode.POINT_DRAW, new PointPicker(aRenderer, aModelManager));
		if (aModelManager.getModel(ModelNames.CIRCLE_STRUCTURES) != null)
			retMap.put(PickMode.CIRCLE_SELECTION, new CircleSelectionPicker(aRenderer, aModelManager));

		return retMap;
	}

	/**
	 * Returns true if the specified MouseEvent has the "primary" modifier key
	 * activated.
	 * <P>
	 * For Linux and Windows systems the primary modifier key is defined as control
	 * key (Ctrl) button.
	 * <P>
	 * For Apple systems the primary modifier key is defined as the meta key
	 * (Command) button.
	 */
	public static boolean isModifyKey(MouseEvent aEvent)
	{
		if (Configuration.isMac() == true && aEvent.isMetaDown() == true)
			return true;
		else if (Configuration.isMac() == false && aEvent.isControlDown() == true)
			return true;

		return false;
	}

	// We do not rely on the OS for the popup trigger in the renderer (as explained
	// in a comment in the DefaultPicker.mouseClicked function), we need to role out
	// our own popup trigger logic. That's we why have the following complicated
	// function. It's easier on non-macs. On macs we try to mimic the default
	// behaviour where a Control + left mouse click is a popup trigger. Also for
	// some reason, if you left mouse click while holding down the Command button,
	// then SwingUtilities.isRightMouseButton() returns true. We therefore also
	// prevent a popup from showing in this situation.
	public static boolean isPopupTrigger(MouseEvent e)
	{
		if (Configuration.isMac())
		{
			if (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())
				return true;

			if (e.getButton() == MouseEvent.BUTTON3)
				return true;
		}
		else if (SwingUtilities.isRightMouseButton(e))
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

}
