package edu.jhuapl.saavtk.pick;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.gui.render.camera.Camera;
import edu.jhuapl.saavtk.gui.render.camera.CameraUtil;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderer;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Picker that provides the core functionality for the default {@link Picker}.
 * <P>
 * This picker provide the following functionality:
 * <UL>
 * <LI>Listener mechanism to provide notification of when objects are picked
 * <LI>Keyboard support for various view transformations
 * </UL>
 * TODO: Eventually this Picker should not be a ProperyChangeListener but rather
 * relevant objects should be responsible for registering their
 * {@link vtkProp}s.
 * 
 * @author lopeznr1
 */
public class BaseDefaultPicker extends Picker implements PropertyChangeListener
{
	// Reference vars
	private final Renderer refRenderer;
	private final vtkJoglPanelComponent refRenWin;
	private final PolyhedralModel refSmallBody;
	private final ModelManager refModelManager;

	// State vars
	private ImmutableList<PickListener> pickListenerL;
	private ImmutableList<VtkPropProvider> propProviderL;
	private PickTarget lastClickedTarget;
	private boolean suppressModeActiveSec;
	private boolean isDragged;

	// VTK vars
	private final vtkCellPicker vNonSmallBodyCP; // includes all props EXCEPT the small body
	private final vtkCellPicker vSmallBodyCP; // only includes small body prop

	/**
	 * Standard Constructor
	 */
	public BaseDefaultPicker(Renderer aRenderer, ModelManager aModelManager)
	{
		refRenderer = aRenderer;
		refRenWin = aRenderer.getRenderWindowPanel();
		refSmallBody = aModelManager.getPolyhedralModel();
		refModelManager = aModelManager;

		pickListenerL = ImmutableList.of();
		propProviderL = ImmutableList.of();
		lastClickedTarget = null;
		suppressModeActiveSec = false;
		isDragged = false;

		// See comment in the propertyChange function below as to why
		// we use a custom pick list for these pickers.
		vNonSmallBodyCP = PickUtilEx.formEmptyPicker();

		vSmallBodyCP = PickUtilEx.formSmallBodyPicker(refSmallBody);

		// Register for events of interest
		aModelManager.addPropertyChangeListener(this);
	}

	/**
	 * Registers a {@link PickListener} with this PickManager
	 */
	public synchronized void addListener(PickListener aListener)
	{
		List<PickListener> tmpL = new ArrayList<>(pickListenerL);
		tmpL.add(aListener);

		pickListenerL = ImmutableList.copyOf(tmpL);
	}

	/**
	 * Deregisters a {@link PickListener} with this PickManager
	 */
	public synchronized void delListener(PickListener aListener)
	{
		List<PickListener> tmpL = new ArrayList<>(pickListenerL);
		tmpL.remove(aListener);

		pickListenerL = ImmutableList.copyOf(tmpL);
	}

	/**
	 * Registers a {@link VtkPropProvider} with this PickManager
	 */
	public synchronized void addPropProvider(VtkPropProvider aProvider)
	{
		List<VtkPropProvider> tmpL = new ArrayList<>(propProviderL);
		tmpL.add(aProvider);

		propProviderL = ImmutableList.copyOf(tmpL);

		buildCellPickers();
	}

	/**
	 * Deregisters a {@link VtkPropProvider} with this PickManager
	 */
	public synchronized void delPropProvider(VtkPropProvider aProvider)
	{
		List<VtkPropProvider> tmpL = new ArrayList<>(propProviderL);
		tmpL.remove(aProvider);

		propProviderL = ImmutableList.copyOf(tmpL);

		buildCellPickers();
	}

	/**
	 * Configures the picker to disable secondary actions.
	 * <P>
	 * {@link PickEvent} with a mode of type {@link PickMode#ActiveSec} will not be
	 * sent.
	 */
	public void setSuppressModeActiveSec(boolean aBool)
	{
		suppressModeActiveSec = aBool;
	}

	/**
	 * This method exists to support retrieval of the small body picker. The
	 * returned object should be utilized in a read-only fashion.
	 * <P>
	 * TODO: This method is needed for "compute pixel size computations", however
	 * the DefaultPicker should not be responsible for such computations. This
	 * method should eventually be removed.
	 */
	protected vtkCellPicker getSmallBodyPicker()
	{
		return vSmallBodyCP;
	}

	@Override
	public void keyPressed(KeyEvent aEvent)
	{
		// Ignore all key presses if we do not have an installed interactor
		// Ex: When drawing structures, we will ignore all key presses
		if (refRenWin.getRenderWindowInteractor().GetInteractorStyle() == null)
			return;

		// Bail if no actors
		vtkRenderer tmpRenderer = refRenWin.getRenderer();
		if (tmpRenderer.VisibleActorCount() == 0)
			return;

		// Define the surface position that was clicked on
		PickTarget surfaceTarg = PickTarget.Invalid;
		Point pt = refRenWin.getComponent().getMousePosition();
		if (pt != null)
			surfaceTarg = getPickTarget(vSmallBodyCP, pt.x, pt.y);

		// Handle the key codes
		int keyCode = aEvent.getKeyCode();
		if (keyCode == KeyEvent.VK_C)
		{
			// Select the 3D point associated with the mouse point
			// Prefer non small body targets
			PickTarget primaryTarg = getPickTarget(vNonSmallBodyCP, pt.x, pt.y);
			Vector3D tmpPos = primaryTarg.getPosition();
			if (tmpPos == null)
				tmpPos = surfaceTarg.getPosition();

			// Bail if no 3D position is associated with the mouse point
			if (tmpPos == null)
				return;

			// Set the camera to focus on the picked position
			refRenderer.setCameraFocalPoint(tmpPos.toArray());

			notifyListeners(aEvent, PickMode.Passive, PickTarget.Invalid, surfaceTarg);
		}
		else if (keyCode == KeyEvent.VK_F)
		{
			// Bail if no target has been clicked on
			if (lastClickedTarget == null)
				return;

			CameraUtil.setFocalPosition(refRenderer, lastClickedTarget.getPosition(), lastClickedTarget.getNormal());

			notifyListeners(aEvent, PickMode.Passive, PickTarget.Invalid, surfaceTarg);
		}
		else if (keyCode == KeyEvent.VK_N)
		{
			CameraUtil.spinBoresightForNormalAxisZ(refRenderer);

			notifyListeners(aEvent, PickMode.Passive, PickTarget.Invalid, surfaceTarg);
		}
		else if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_Y || keyCode == KeyEvent.VK_Z)
		{
			char keyChar = aEvent.getKeyChar();

			Camera tmpCamera = refRenderer.getCamera();
			if ('X' == keyChar)
				CameraUtil.setOrientationInDirectionOfAxis(tmpCamera, AxisType.NEGATIVE_X);
			else if ('x' == keyChar)
				CameraUtil.setOrientationInDirectionOfAxis(tmpCamera, AxisType.POSITIVE_X);
			else if ('Y' == keyChar)
				CameraUtil.setOrientationInDirectionOfAxis(tmpCamera, AxisType.NEGATIVE_Y);
			else if ('y' == keyChar)
				CameraUtil.setOrientationInDirectionOfAxis(tmpCamera, AxisType.POSITIVE_Y);
			else if ('Z' == keyChar)
				CameraUtil.setOrientationInDirectionOfAxis(tmpCamera, AxisType.NEGATIVE_Z);
			else if ('z' == keyChar)
				CameraUtil.setOrientationInDirectionOfAxis(tmpCamera, AxisType.POSITIVE_Z);

			notifyListeners(aEvent, PickMode.Passive, PickTarget.Invalid, surfaceTarg);
		}
		else if (keyCode == KeyEvent.VK_R)
		{
			Camera tmpCamera = refRenderer.getCamera();
			tmpCamera.reset();

			notifyListeners(aEvent, PickMode.Passive, PickTarget.Invalid, surfaceTarg);
		}
		else if (keyCode == KeyEvent.VK_S)
		{
			refSmallBody.setRepresentationToSurface();
		}
		else if (keyCode == KeyEvent.VK_W)
		{
			refSmallBody.setRepresentationToWireframe();
		}
	}

	@Override
	public void mouseClicked(MouseEvent aEvent)
	{
		if (refRenWin.getRenderWindow().GetNeverRendered() > 0)
			return;

		// need to shut off LODs to make sure pick is done on correct geometry
		boolean wasShowingLODs = refRenderer.showingLODs;
		refRenderer.hideLODs();

		// Synthesize the primary and surface target
		PickTarget primaryTarg = getPickTarget(vNonSmallBodyCP, aEvent.getX(), aEvent.getY());
		PickTarget surfaceTarg = getPickTarget(vSmallBodyCP, aEvent.getX(), aEvent.getY());

		// Keep track of the (regular) last clicked target
		boolean isRegClick = aEvent.getClickCount() == 1;
		isRegClick &= (aEvent.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;
		if (isRegClick == true && surfaceTarg != PickTarget.Invalid)
			lastClickedTarget = surfaceTarg;

		// show LODs again if they were shown before picking; view-menu enabling of LODs
		// is handled by the renderer so we don't need to worry about it here
		if (wasShowingLODs == true)
			refRenderer.showLODs();

		// Send out notification (if appropriate)
		PickMode tmpMode = PickMode.ActiveSec;
		if (suppressModeActiveSec == false)
			notifyListeners(aEvent, tmpMode, primaryTarg, surfaceTarg);

		isDragged = false;
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		isDragged = true;
		PickTarget surfaceTarg = getPickTarget(vSmallBodyCP, aEvent.getX(), aEvent.getY());
		notifyListeners(aEvent, PickMode.Passive, PickTarget.Invalid, surfaceTarg);
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		PickTarget surfaceTarg = getPickTarget(vSmallBodyCP, aEvent.getX(), aEvent.getY());
		notifyListeners(aEvent, PickMode.Passive, PickTarget.Invalid, surfaceTarg);
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		isDragged = false;
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		if (refRenWin.getRenderWindow().GetNeverRendered() > 0)
			return;

		// need to shut off LODs to make sure pick is done on correct geometry
		boolean wasShowingLODs = refRenderer.showingLODs;
		refRenderer.hideLODs();

		// Synthesize the primary and surface target
		PickTarget primaryTarg = getPickTarget(vNonSmallBodyCP, aEvent.getX(), aEvent.getY());
		PickTarget surfaceTarg = getPickTarget(vSmallBodyCP, aEvent.getX(), aEvent.getY());

		// show LODs again if they were shown before picking; view-menu enabling of LODs
		// is handled by the renderer so we don't need to worry about it here
		if (wasShowingLODs == true)
			refRenderer.showLODs();

		// Mark as primary action as long as the mouse was not dragged
		PickMode tmpMode = PickMode.ActivePri;
		if (isDragged == true)
			tmpMode = PickMode.Passive;

		notifyListeners(aEvent, tmpMode, primaryTarg, surfaceTarg);

		isDragged = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent aEvent)
	{
		PickTarget surfaceTarg = getPickTarget(vSmallBodyCP, aEvent.getX(), aEvent.getY());
		notifyListeners(aEvent, PickMode.Passive, PickTarget.Invalid, surfaceTarg);
	}

	@Override
	public void propertyChange(PropertyChangeEvent aEvent)
	{
		if (aEvent.getPropertyName().equals(Properties.MODEL_CHANGED) == false)
			return;

		// Time to rebuild the vtkCellPickers
		buildCellPickers();
	}

	/**
	 * Helper method that will configure the non small body cell picker with the
	 * relevant (available) {@link vtkProp}s.
	 */
	private void buildCellPickers()
	{
		// Whenever the model actors change, we need to update the pickers
		// internal list of all actors to pick from. The small body actor is excluded
		// from this list since many other actors occupy the same position
		// as parts of the small body and we want the picker to pick these other
		// actors rather than the small body. Note that this exclusion only applies
		// to the following picker.
		vtkPropCollection mousePressNonSmallBodyCellPickList = vNonSmallBodyCP.GetPickList();
		mousePressNonSmallBodyCellPickList.RemoveAllItems();

		List<vtkProp> actorL = refModelManager.getPropsExceptSmallBody();
		for (vtkProp aProp : actorL)
			vNonSmallBodyCP.AddPickList(aProp);

		for (VtkPropProvider aPropProvider : propProviderL)
		{
			for (vtkProp aProp : aPropProvider.getProps())
				vNonSmallBodyCP.AddPickList(aProp);
		}

	}

	/**
	 * Helper method that returns the {@link PickTarget} that was picked (via the
	 * provided {@link vtkCellPicker}) corresponding to the specified 2D screen
	 * position.
	 * <P>
	 * Returns {@link PickTarget#Invalid}. if the screen position does not represent
	 * a successful pick action on aCellPicker.
	 */
	private PickTarget getPickTarget(vtkCellPicker aCellPicker, int aPosX, int aPosY)
	{
		// Bail if nothing was picked
		int pickSucceeded = doPick(0L, aPosX, aPosY, aCellPicker, refRenWin);
		if (pickSucceeded != 1)
			return PickTarget.Invalid;

		vtkActor pickedActor = aCellPicker.GetActor();
		Vector3D targetNorm = new Vector3D(aCellPicker.GetPickNormal());
		Vector3D targetPos = new Vector3D(aCellPicker.GetPickPosition());
		int cellId = aCellPicker.GetCellId();

		PickTarget retTarget = new PickTarget(pickedActor, targetNorm, targetPos, cellId);
		return retTarget;
	}

	/**
	 * Helper method to send out notification to the listeners.
	 */
	private void notifyListeners(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if we have never been rendered
		if (refRenWin.getRenderWindow().GetNeverRendered() > 0)
			return;

		for (PickListener aListener : pickListenerL)
			aListener.handlePickAction(aEvent, aMode, aPrimaryTarg, aSurfaceTarg);
	}

}
