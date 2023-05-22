package edu.jhuapl.saavtk.structure.gui;

import java.awt.Component;

import javax.swing.JCheckBoxMenuItem;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.structure.EllipseManager;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.action.CenterStructureAction;
import edu.jhuapl.saavtk.structure.gui.action.ChangeColorAction;
import edu.jhuapl.saavtk.structure.gui.action.ChangeLatLonAction;
import edu.jhuapl.saavtk.structure.gui.action.DeleteAction;
import edu.jhuapl.saavtk.structure.gui.action.DisplayInteriorAction;
import edu.jhuapl.saavtk.structure.gui.action.ExportPlateDataInsidePolygonAction;
import edu.jhuapl.saavtk.structure.gui.action.SaveProfileAction;
import edu.jhuapl.saavtk.structure.gui.action.SetLabelAction;
import edu.jhuapl.saavtk.structure.gui.action.ShowAction;
import edu.jhuapl.saavtk.structure.gui.action.ShowPlateStatisticsInfoAction;
import edu.jhuapl.saavtk.view.AssocActor;
import glum.gui.action.PopupMenu;
import vtk.vtkProp;

/**
 * Collection of structure UI utility methods.
 *
 * @author lopeznr1
 */
public class StructureGuiUtil
{
	/**
	 * Utility method that forms the popup menu associated with {@link Structure}
	 * items.
	 */
	public static <G1 extends Structure> PopupMenu<G1> formPopupMenu(StructureManager<G1> aManager, Renderer aRenderer,
			PolyhedralModel aSmallBody, Component aParent)
	{
		PopupMenu<G1> retPM = new PopupMenu<>(aManager);

		ShowAction<G1> showHideAction = new ShowAction<>(aManager, aParent);
		JCheckBoxMenuItem showHideCBMI = new JCheckBoxMenuItem(showHideAction);
		showHideCBMI.setText("Show");

		retPM.installPopAction(new ChangeColorAction<>(aManager, aParent), "Change Color...");
		retPM.installPopAction(showHideAction, showHideCBMI);
		retPM.installPopAction(new SetLabelAction<>(aManager, aParent), "Edit Label Text");
		retPM.installPopAction(new DeleteAction<>(aManager, aParent), "Delete");
		retPM.installPopAction(new CenterStructureAction<>(aManager, aRenderer, aSmallBody, false),
				"Center in Window (Close Up)");
		retPM.installPopAction(new CenterStructureAction<>(aManager, aRenderer, aSmallBody, true),
				"Center in Window (Preserve Distance)");

		boolean showChangeLatLon = aManager instanceof AbstractEllipsePolygonModel;
		if (showChangeLatLon == true)
			retPM.installPopAction(new ChangeLatLonAction<>(aManager, aParent, aSmallBody),
					"Change Latitude/Longitude...");

		boolean showExportPlateDataInsidePolygon = false;
		if (aManager instanceof AbstractEllipsePolygonModel aManager2)
		{
			showExportPlateDataInsidePolygon |= aManager2.getMode() == Mode.CIRCLE_MODE;
			showExportPlateDataInsidePolygon |= aManager2.getMode() == Mode.ELLIPSE_MODE;
		}
		showExportPlateDataInsidePolygon |= aManager instanceof PolygonModel;
		if (showExportPlateDataInsidePolygon == true)
		{
			retPM.installPopAction(new ExportPlateDataInsidePolygonAction<>(aManager, aSmallBody, aParent),
					"Save plate data inside structure...");
			retPM.installPopAction(new ShowPlateStatisticsInfoAction<>(aManager, aSmallBody, aParent),
					"Show plate data statistics inside structure...");
		}

		boolean showDisplayInterior = aManager instanceof PolygonModel;
		if (showDisplayInterior == true)
		{
			DisplayInteriorAction<G1> displayInteriorAction = new DisplayInteriorAction<>(aManager, aParent);
			JCheckBoxMenuItem displayInteriorCBMI = new JCheckBoxMenuItem(displayInteriorAction);
			displayInteriorCBMI.setText("Display Interior");

			retPM.installPopAction(displayInteriorAction, displayInteriorCBMI);
		}

		boolean showSaveProfile = aManager.getClass() == LineModel.class;
		if (showSaveProfile == true)
			retPM.installPopAction(new SaveProfileAction<G1>(aManager, aSmallBody, aParent), "Save Profile...");

		return retPM;
	}

	/**
	 * Utility method that returns true if the specified {@link PickTarget} is
	 * associated with the provided {@link StructureManager}.
	 */
	public static boolean isAssociatedPickTarget(PickTarget aPickTarget, StructureManager<?> aManager)
	{
		// Bail if the actor is not the right type
		vtkProp tmpProp = aPickTarget.getActor();
		if (tmpProp instanceof AssocActor == false)
			return false;

		// EllipseManager's painting is delegated to it's painter
		Object assocObj = aManager;
		if (aManager instanceof EllipseManager aEllipseManager)
			assocObj = aEllipseManager.getVtkMultiPainter();

		// Bail if tmpProp is not associated with the StructureManager
		Object tmpObj = ((AssocActor) tmpProp).getAssocModel(Object.class);
		if (tmpObj != assocObj)
			return false;

		return true;
	}

}
