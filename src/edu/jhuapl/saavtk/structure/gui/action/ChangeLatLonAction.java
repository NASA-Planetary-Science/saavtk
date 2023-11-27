package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Point;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.gui.LatLonPanel;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the lat,lon of a single {@link Structure}.
 *
 * @author lopeznr1
 */
public class ChangeLatLonAction extends PopAction<Structure>
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final Component refParent;
	private final PolyhedralModel refSmallBody;

	// Gui vars
	private LatLonPanel inputPanel;

	/** Standard Constructor */
	public ChangeLatLonAction(AnyStructureManager aManager, Component aParent, PolyhedralModel aSmallBody)
	{
		refManager = aManager;
		refParent = aParent;
		refSmallBody = aSmallBody;

		inputPanel = null;
	}

	@Override
	public void executeAction(List<Structure> aItemL)
	{
		// Bail if a single item is not selected
		if (aItemL.size() != 1)
			return;
		var tmpItem = aItemL.get(0);

		// Lazy init
		if (inputPanel == null)
			inputPanel = new LatLonPanel(refParent, "Change Center LatLon");

		// Prompt the user for the new LatLon
		var oldCenterArr = refManager.getCenter(tmpItem).toArray();
		var centerLL = MathUtil.reclat(oldCenterArr);
		var lat = centerLL.lat;
		var lon = centerLL.lon;
		if (lon < 0.0)
			lon += 2.0 * Math.PI;
		lat = Math.toDegrees(lat);
		lon = Math.toDegrees(lon);
		inputPanel.setLatLon(lat, lon);

		inputPanel.setVisibleAsModal();
		if (inputPanel.isAccepted() == false)
			return;

		// Update the model to reflect the user input
		lat = Math.toRadians(inputPanel.getLat());
		lon = Math.toRadians(inputPanel.getLon());

		var newCenterArr = new double[3];
		refSmallBody.getPointAndCellIdFromLatLon(lat, lon, newCenterArr);
		var newCenterPt = new Vector3D(newCenterArr);

		StructureMiscUtil.setCenter(refSmallBody, tmpItem, newCenterPt);
		refManager.notifyItemsMutated(ImmutableList.of(tmpItem));
	}

	@Override
	public void setChosenItems(Collection<Structure> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enabled the menu if only 1 item and it is an Ellipse
		var isEnabled = aItemC.size() == 1;
		if (isEnabled == true)
		{
			var tmpItem = aItemC.iterator().next();
			isEnabled &= tmpItem instanceof Ellipse || tmpItem instanceof Point;
		}

		aAssocMI.setEnabled(isEnabled);
	}

}
