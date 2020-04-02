package edu.jhuapl.saavtk.popup;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import vtk.vtkProp;

/**
 * This class provides the traditional mechanism for registering a
 * {@link PopupMenu} with the PopupManager. It provides the mechanism for
 * registering a {@link PopupMenu} with a {@link Model}.
 * <P>
 * This class has been retrofitted to be decoupled from the {@link PickManager}.
 * 
 * @author lopeznr1
 */
public class PopupManager implements PickListener
{
	private ModelManager modelManager;
	private HashMap<Model, PopupMenu> modelToPopupMap;

	public PopupManager(ModelManager aModelManager)
	{
		modelManager = aModelManager;
		modelToPopupMap = new HashMap<>();
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if not the proper mode
		if (aMode != PickMode.ActiveSec)
			return;

		// Bail if no valid PickTarget
		if (aPrimaryTarg == PickTarget.Invalid)
			return;

		// Bail if not proper event type
		if (aEvent instanceof MouseEvent == false)
			return;

		// Bail if not valid for a popup
		if (PickUtil.isPopupTrigger(aEvent) == false)
			return;

		// Show the popup
		vtkProp pickedProp = aPrimaryTarg.getActor();
		int pickedCellId = aPrimaryTarg.getCellId();
		double[] pickedPosition = aPrimaryTarg.getPosition().toArray();
		PopupMenu popup = modelToPopupMap.get(modelManager.getModel(pickedProp));
		if (popup != null)
			popup.showPopup((MouseEvent) aEvent, pickedProp, pickedCellId, pickedPosition);
	}

	/**
	 * Registers a {@link PopupMenu} to be associated with the {@link Model}.
	 */
	public void registerPopup(Model model, PopupMenu menu)
	{
		modelToPopupMap.put(model, menu);
	}
}