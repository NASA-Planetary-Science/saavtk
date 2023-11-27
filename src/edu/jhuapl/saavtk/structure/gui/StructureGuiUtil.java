package edu.jhuapl.saavtk.structure.gui;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.funk.PopupButton;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.StructureType;
import edu.jhuapl.saavtk.structure.gui.action.CenterStructureAction;
import edu.jhuapl.saavtk.structure.gui.action.ChangeColorAction;
import edu.jhuapl.saavtk.structure.gui.action.ChangeLatLonAction;
import edu.jhuapl.saavtk.structure.gui.action.DeleteAction;
import edu.jhuapl.saavtk.structure.gui.action.DisplayInteriorAction;
import edu.jhuapl.saavtk.structure.gui.action.ExportPlateDataInsidePolygonAction;
import edu.jhuapl.saavtk.structure.gui.action.LoadEsriShapeFileAction;
import edu.jhuapl.saavtk.structure.gui.action.SaveProfileAction;
import edu.jhuapl.saavtk.structure.gui.action.SetLabelAction;
import edu.jhuapl.saavtk.structure.gui.action.ShowAction;
import edu.jhuapl.saavtk.structure.gui.action.ShowPlateStatisticsInfoAction;
import edu.jhuapl.saavtk.view.AssocActor;
import glum.gui.action.PopupMenu;
import glum.gui.panel.generic.PromptPanel;
import vtk.vtkProp;

/**
 * Collection of structure UI utility methods.
 *
 * @author lopeznr1
 */
public class StructureGuiUtil
{
	/**
	 * Utility method to form the PopupButton used to load ESRI data structures.
	 */
	public static PopupButton formEsriLoadButton(PolyhedralModel aSmallBody, StatusNotifier aStatusNotifier,
			AnyStructureManager aStructureManager, Component aParent)
	{
		var retB = new PopupButton("ESRI...");

		retB.getPopup().add(new JMenuItem(new LoadEsriShapeFileAction<>(aParent, aSmallBody, aStructureManager,
				aStatusNotifier, StructureType.Path, "Load Path Shapefile Datastore")));

		retB.getPopup().add(new JMenuItem(new LoadEsriShapeFileAction<>(aParent, aSmallBody, aStructureManager,
				aStatusNotifier, StructureType.Polygon, "Load Polygon Shapefile Datastore")));

		var circleMI = new JMenuItem("Load Circle Shapefile Datastore");
		circleMI.setToolTipText("ESRI circles can be imported as a Polygon Shapefile Datastore");
		circleMI.setEnabled(false);
		retB.getPopup().add(circleMI);

		var ellipseMI = new JMenuItem("Load Ellipse Shapefile Datastore");
		ellipseMI.setToolTipText("ESRI ellipses can be imported as a Polygon Shapefile Datastore");
		ellipseMI.setEnabled(false);
		retB.getPopup().add(ellipseMI);

		retB.getPopup().add(new JMenuItem(new LoadEsriShapeFileAction<>(aParent, aSmallBody, aStructureManager,
				aStatusNotifier, StructureType.Point, "Load Point Shapefile Datastore")));

		return retB;
	}

	/**
	 * Utility method that forms the popup menu associated with {@link Structure} items.
	 */
	public static PopupMenu<Structure> formPopupMenu(AnyStructureManager aManager, Renderer aRenderer,
			PolyhedralModel aSmallBody, Component aParent)
	{
		var retPM = new PopupMenu<>(aManager);

		var showHideAction = new ShowAction(aManager, aParent);
		var showHideCBMI = new JCheckBoxMenuItem(showHideAction);
		showHideCBMI.setText("Show");

		retPM.installPopAction(new ChangeColorAction(aManager, aParent), "Change Color...");
		retPM.installPopAction(showHideAction, showHideCBMI);
		retPM.installPopAction(new SetLabelAction(aManager, aParent), "Edit Label Text");
		retPM.installPopAction(new DeleteAction(aManager, aParent), "Delete");
		retPM.installPopAction(new CenterStructureAction<>(aManager, aRenderer, aSmallBody, false),
				"Center in Window (Close Up)");
		retPM.installPopAction(new CenterStructureAction<>(aManager, aRenderer, aSmallBody, true),
				"Center in Window (Preserve Distance)");

		retPM.installPopAction(new ChangeLatLonAction(aManager, aParent, aSmallBody), "Change Latitude/Longitude...");

		retPM.installPopAction(new ExportPlateDataInsidePolygonAction(aManager, aSmallBody, aParent),
				"Save plate data inside structure...");
		retPM.installPopAction(new ShowPlateStatisticsInfoAction(aManager, aSmallBody, aParent),
				"Show plate data statistics inside structure...");

		var displayInteriorAction = new DisplayInteriorAction(aManager, aParent);
		var displayInteriorCBMI = new JCheckBoxMenuItem(displayInteriorAction);
		displayInteriorCBMI.setText("Display Interior");
		retPM.installPopAction(displayInteriorAction, displayInteriorCBMI);

		retPM.installPopAction(new SaveProfileAction(aManager, aSmallBody, aParent), "Save Profile...");

		return retPM;
	}

	/**
	 * Utility method that returns the "unified" fontSize value for the provided list of {@link Structure}s.
	 * <p>
	 * If all items do not have the same value then NaN will be returned.
	 */
	public static int getUnifiedFontSizeFor(Collection<Structure> aItemC)
	{
		if (aItemC.size() == 0)
			return -1;

		// Retrieve the fontSize of the first item
		var tmpItem = aItemC.iterator().next();
		var retSize = tmpItem.getLabelFontAttr().getSize();

		// Ensure all items have the same value
		var isSameValue = true;
		for (var aItem : aItemC)
		{
			var tmpSize = aItem.getLabelFontAttr().getSize();
			isSameValue &= tmpSize == retSize;
		}

		if (isSameValue == false)
			return -1;

		return retSize;
	}

	/**
	 * Utility method that returns true if the specified {@link PickTarget} is associated with the provided
	 * {@link StructureManager}.
	 */
	public static boolean isAssociatedPickTarget(PickTarget aPickTarget, StructureManager<?> aManager)
	{
		// Bail if the actor is not the right type
		vtkProp tmpProp = aPickTarget.getActor();
		if (tmpProp instanceof AssocActor == false)
			return false;

		// Bail if tmpProp is not associated with the StructureManager
		Object assocObj = aManager;
		Object tmpObj = ((AssocActor) tmpProp).getAssocModel(Object.class);
		if (tmpObj != assocObj)
			return false;

		return true;
	}

	/**
	 * Utility method that will delete the specified {@link Structure}s. The user will be prompted for confirmation
	 * first.
	 */
	public static <G1 extends Structure> boolean promptAndDelete(Component aParent, AnyStructureManager aManager,
			Collection<Structure> aItemC)
	{
		// Bail if there are no items to delete
		if (aItemC.size() <= 0)
			return true;

		// Prompt the user
		var infoMsg = "Are you sure you want to delete " + aItemC.size() + " structures?";
		var promptPanel = new PromptPanel(aParent, "Confirm Deletion", 350, 160);
		promptPanel.setInfo(infoMsg);
		promptPanel.setVisibleAsModal();
		if (promptPanel.isAccepted() == false)
			return false;

		aManager.removeItems(aItemC);
		return true;
	}

}
