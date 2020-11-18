package edu.jhuapl.saavtk.status;

import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.view.AssocActor;
import vtk.vtkProp;

/**
 * {@link PickListener} that is responsible for sending out status updates when
 * a model has been picked.
 * <p>
 * This listener supports objects registered with the {@link ModelManager}.
 * <p>
 * The logic for this class is based off of the removed:
 * edu.jhuapl.saavtk.gui.StatusBar
 * <p>
 * This class is a bridge class between the old StatusBar and the new
 * {@link StatusNotifier} mechanism. This class should eventually be removed.
 *
 * @author lopeznr1
 */
public class LegacyStatusHandler implements PickListener, PropertyChangeListener
{
	// Ref vars
	private final StatusNotifier refStatusNotifier;
	private final ModelManager refModelManager;

	// State vars
	private Model leftModel;
	private vtkProp leftProp;
	private int leftCellId;
	private double[] leftPickPosition;

	/** Standard Constructor */
	public LegacyStatusHandler(StatusNotifier aStatusNotifier, ModelManager aModelManager)
	{
		refModelManager = aModelManager;
		refStatusNotifier = aStatusNotifier;

		leftModel = null;
		leftProp = null;
		leftCellId = 0;
		leftPickPosition = null;
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if not a primary pick action
		if (aMode != PickMode.ActivePri)
			return;

		// Just update the status bar's left size with status if
		// aPrimaryTarg is associated with a StatusProvider
		vtkProp priActor = aPrimaryTarg.getActor();
		if (priActor instanceof AssocActor)
		{
			StatusProvider tmpSP = ((AssocActor) priActor).getAssocModel(StatusProvider.class);
			if (tmpSP != null)
			{
				String tmpStr = tmpSP.getDisplayInfo(aPrimaryTarg);
				setLeftText(tmpStr, true);
				return;
			}
		}

		// Retrieve the model associated with the actor
		Model tmpModel = refModelManager.getModel(priActor);

		// Otherwise, update the left side
		int priCellId = aPrimaryTarg.getCellId();
		if (tmpModel == null)
			setLeftText(" ", true);
		else
			setLeftTextSource(tmpModel, priActor, priCellId, aPrimaryTarg.getPosition().toArray());
	}

	// Sets up auto-refreshing left status text
	public void setLeftTextSource(Model model, vtkProp prop, int cellId, double[] pickPosition)
	{
		// Determine if model has changed
		boolean isNewModel = (leftModel != model);
		if (isNewModel && leftModel != null)
		{
			// This status bar is no longer the property change listener for the old left
			// model
			leftModel.removePropertyChangeListener(this);
		}

		// Save references to arguments
		leftModel = model;
		leftProp = prop;
		leftCellId = cellId;
		leftPickPosition = pickPosition.clone();
		if (isNewModel && leftModel != null)
		{
			// Set the status bar as the property change listener for the new left model
			leftModel.addPropertyChangeListener(this);
		}

		// Regenerate left status text
		setLeftText(leftModel.getClickStatusBarText(leftProp, leftCellId, leftPickPosition), false);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			if (leftModel != null)
			{
				// Regenerate left status text
				setLeftText(leftModel.getClickStatusBarText(leftProp, leftCellId, leftPickPosition), false);
			}
		}
	}

	private void setLeftText(String text, boolean removeModel)
	{
		if (removeModel && leftModel != null)
		{
			leftModel.removePropertyChangeListener(this);
			leftModel = null;
		}

		if (text.length() == 0)
			text = "Ready.";

		refStatusNotifier.setPriStatus(text, null);
	}

}
