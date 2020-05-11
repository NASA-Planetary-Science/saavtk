package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.LatLonPanel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will change the lat,lon of a single {@link Structure}.
 *
 * @author lopeznr1
 */
public class ChangeLatLonAction<G1 extends Structure> extends PopAction<G1>
{
	// Ref vars
	private final StructureManager<G1> refManager;
	private final Component refParent;
	private final PolyhedralModel refSmallBody;

	// Gui vars
	private LatLonPanel inputPanel;

	/**
	 * Standard Constructor
	 */
	public ChangeLatLonAction(StructureManager<G1> aManager, Component aParent, PolyhedralModel aSmallBody)
	{
		refManager = aManager;
		refParent = aParent;
		refSmallBody = aSmallBody;

		inputPanel = null;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if a single item is not selected
		if (aItemL.size() != 1)
			return;
		G1 tmpItem = aItemL.get(0);

		// Lazy init
		if (inputPanel == null)
			inputPanel = new LatLonPanel(refParent, "Change Center LatLon");

		// Prompt the user for the new LatLon
		double[] centerPos = refManager.getCenter(tmpItem).toArray();
		LatLon centerLL = MathUtil.reclat(centerPos);
		double lat = centerLL.lat;
		double lon = centerLL.lon;
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

		double[] newCenterArr = new double[3];
		refSmallBody.getPointAndCellIdFromLatLon(lat, lon, newCenterArr);
		refManager.setCenter(tmpItem, new Vector3D(newCenterArr));
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable the item if the number of selected items == 1
		boolean isEnabled = aItemC.size() == 1;
		aAssocMI.setEnabled(isEnabled);
	}

}
