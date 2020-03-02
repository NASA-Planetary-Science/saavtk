package edu.jhuapl.saavtk.pick;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.StatusBarDefaultPickHandler;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.util.Configuration;

/**
 * Utility class for configuration of Picker related code.
 * <P>
 * Please note the following:
 * <UL>
 * <LI>The method {@link #formNonDefaultPickerMap(Renderer, ModelManager)} is
 * marked for removal.
 * <LI>The method {@link #setPickingEnabled(boolean)} should be investigated
 * further and eventually removed if possible.
 * <UL>
 *
 * @author lopeznr1
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
	@Deprecated
	protected static Map<PickMode, Picker> formNonDefaultPickerMap(Renderer aRenderer, ModelManager aModelManager)
	{
		Map<PickMode, Picker> retMap = new HashMap<>();
		if (aModelManager.getModel(ModelNames.CIRCLE_STRUCTURES) != null)
			retMap.put(PickMode.CIRCLE_SELECTION, new CircleSelectionPicker(aRenderer, aModelManager));

		return retMap;
	}

	/**
	 * Registers a {@link StatusBarDefaultPickHandler} with the
	 * {@link PickManager}'s {@link DefaultPicker}.
	 * <P>
	 * The installed {@link StatusBarDefaultPickHandler} will handle updates
	 * relevant to the status bar.
	 */
	public static void installDefaultPickHandler(PickManager aPickManager, StatusBar aStatusBar, Renderer aRenderer,
			ModelManager aModelManager)
	{
		DefaultPicker tmpDefaultPicker = aPickManager.getDefaultPicker();
		StatusBarDefaultPickHandler tmpStatusBarHandler = new StatusBarDefaultPickHandler(tmpDefaultPicker, aStatusBar,
				aRenderer, aModelManager);
		tmpDefaultPicker.addListener(tmpStatusBarHandler);
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
