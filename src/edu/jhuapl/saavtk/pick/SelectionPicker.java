package edu.jhuapl.saavtk.pick;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.status.QuietStatusNotifier;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import glum.item.ItemEventListener;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

/**
 * Implementation of {@link Picker} responsible for providing a selection capability.
 * <p>
 * This object contains a {@link AnyStructureManager} responsible for managing the structures used for the selection.
 *
 * @author lopeznr1
 */
public class SelectionPicker extends Picker
{
	// Reference vars
	private final StatusNotifier refStatusNotifier;
	private final PolyhedralModel refSmallBody;
	private final vtkJoglPanelComponent refRenWin;

	// VTK vars
	private final vtkCellPicker vSmallBodyCP;

	// State vars
	private final AnyStructureManager selectionManager;
	private Color workColor;
	private boolean isActive;

	/** Standard Constructor */
	public SelectionPicker(Renderer aRenderer, StatusNotifier aStatusNotifier, PolyhedralModel aSmallBody)
	{
		refStatusNotifier = aStatusNotifier;
		refSmallBody = aSmallBody;
		refRenWin = aRenderer.getRenderWindowPanel();

		vSmallBodyCP = PickUtilEx.formSmallBodyPicker(refSmallBody);

		selectionManager = new AnyStructureManager(aRenderer, QuietStatusNotifier.Instance, aSmallBody);
		aRenderer.addVtkPropProvider(selectionManager);
		isActive = false;
		workColor = new Color(0, 0, 255);

		// Register for events of interest
		selectionManager.addListener((aSource, aEvent) -> handleItemChangeEvent());
		aSmallBody.addPropertyChangeListener(aEvent -> handlePropertyChangeEvent(aEvent));
	}

	/**
	 * Removes the current selection region from this Picker.
	 */
	public void clearSelection()
	{
		// Delegate to our StructureManager
		selectionManager.setAllItems(ImmutableList.of());
	}

	/**
	 * Returns the backing {@link AnyStructureManager} used to manage region selections.
	 */
	public AnyStructureManager getSelectionManager()
	{
		return selectionManager;
	}

	@Override
	public int getCursorType()
	{
		return Cursor.CROSSHAIR_CURSOR;
	}

	@Override
	public boolean isExclusiveMode()
	{
		if (isActive == true)
			return true;

		return false;
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Bail if we are not active (or have no structures)
		if (isActive == false || selectionManager.getAllItems().size() == 0)
			return;
		var tmpItem = (Ellipse) selectionManager.getItem(0);

		// Bail if we failed to pick something
		var isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		var pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Handle the action
		var lastDragPositionArr = vSmallBodyCP.GetPickPosition();
		var lastDragPosition = new Vector3D(lastDragPositionArr);

		var tmpRadius = EllipseUtil.computeRadius(refSmallBody, tmpItem.getCenter(), lastDragPosition);
		tmpItem.setRadius(tmpRadius);

		var tmpItemL = ImmutableList.<Structure>of(tmpItem);
		selectionManager.notifyItemsMutated(tmpItemL);
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// if (e.getButton() != MouseEvent.BUTTON1)
		// return;

		selectionManager.setAllItems(ImmutableList.of());
		isActive = false;

		// Bail if we failed to pick something
		var isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		var pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Handle the action
		var posArr = vSmallBodyCP.GetPickPosition();
		if (aEvent.getClickCount() == 1)
		{
			var radius = EllipseUtil.getPointSizeDef(refSmallBody);
			var centerPt = new Vector3D(posArr);
			var tmpEllipse = new Ellipse(1, null, centerPt, radius, workColor);
			tmpEllipse.setShowInterior(true);
			selectionManager.setAllItems(ImmutableList.of(tmpEllipse));
			isActive = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		isActive = false;
	}

	/**
	 * Helper method to provide support for {@link ItemEventListener}.
	 */
	private void handleItemChangeEvent()
	{
		var decimalFormat = new DecimalFormat("#.#####");

		var itemL = selectionManager.getAllItems();
		var tmpMsg = "";
		if (itemL.size() == 1)
			tmpMsg = "Selection, " + StructureMiscUtil.getStatusTextGeo(itemL.get(0), decimalFormat);
		else if (itemL.size() > 1)
			tmpMsg = "Multiple selections selected: " + itemL.size();

		refStatusNotifier.setPriStatus(tmpMsg, null);
	}

	/**
	 * Helper method to handle: MODEL_RESOULTION_CHANGED events
	 */
	private void handlePropertyChangeEvent(PropertyChangeEvent aEvent)
	{
		if (Properties.MODEL_RESOLUTION_CHANGED.equals(aEvent.getPropertyName()) == true)
			selectionManager.notifyItemsMutated(selectionManager.getAllItems());
	}

}
