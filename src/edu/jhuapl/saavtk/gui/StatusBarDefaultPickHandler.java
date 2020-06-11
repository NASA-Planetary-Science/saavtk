package edu.jhuapl.saavtk.gui;

import java.awt.event.InputEvent;
import java.text.DecimalFormat;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Strings;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.status.StatusProvider;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import vtk.vtkProp;

/**
 * {@link PickListener} that is responsible for updating the status bar whenever
 * something has been picked.
 *
 * @author lopeznr1
 */
public class StatusBarDefaultPickHandler implements PickListener
{
	// Constants
	private final char DegreeChar = '\u00B0';

	// Ref vars
	private final StatusBar refStatusBar;
	private final Renderer refRenderer;
	private final ModelManager refModelManager;

	// State vars
	private String rangeStr;

	// Cache vars
	Vector3D cSurfacePos;

	/**
	 * Standard Constructor
	 *
	 * @param aStatusBar
	 */
	public StatusBarDefaultPickHandler(StatusBar aStatusBar, Renderer aRenderer, ModelManager aModelManager)
	{
		refStatusBar = aStatusBar;
		refRenderer = aRenderer;
		refModelManager = aModelManager;

		rangeStr = null;

		cSurfacePos = null;
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Just update the status bar's position if not a primary action
		if (aMode != PickMode.ActivePri)
		{
			cSurfacePos = aSurfaceTarg.getPosition();
			updatePositionInStatusBar(cSurfacePos);
			return;
		}

		// Just update the status bar's left size with status if
		// aPrimaryTarg is associated with a StatusProvider
		vtkProp priActor = aPrimaryTarg.getActor();
		if (priActor instanceof SaavtkLODActor)
		{
			StatusProvider tmpSP = ((SaavtkLODActor) priActor).getAssocModel(StatusProvider.class);
			if (tmpSP != null)
			{
				String tmpStr = tmpSP.getDisplayInfo(aPrimaryTarg);
				refStatusBar.setLeftText(tmpStr);
				return;
			}
		}

		// Retrieve the model associated with the actor
		Model tmpModel = refModelManager.getModel(priActor);

		// Otherwise, update the left side
		int priCellId = aPrimaryTarg.getCellId();
		if (tmpModel == null)
			refStatusBar.setLeftText(" ");
		else
			refStatusBar.setLeftTextSource(tmpModel, priActor, priCellId, aPrimaryTarg.getPosition().toArray());
	}

	/**
	 * Helper method that will update the status bar's postion to reflect the
	 * provided position.
	 */
	private void updatePositionInStatusBar(Vector3D aPosition)
	{
		// Form the rangeStr
		Vector3D cameraPos = refRenderer.getCamera().getPosition();
		double distance = cameraPos.distance(Vector3D.ZERO);

		DecimalFormat decimalFormatter = new DecimalFormat("##0.000");
		rangeStr = decimalFormatter.format(distance) + " km";
		rangeStr = Strings.padStart(rangeStr, 10, ' ');
		rangeStr = "Range: " + rangeStr + " ";

		// Form the llrStr
		String llrStr = "";
		if (aPosition != null)
		{
			LatLon llr = MathUtil.reclat(aPosition.toArray());

			double lat = Math.toDegrees(llr.lat);
			String latStr = decimalFormatter.format(lat) + DegreeChar;
			latStr = Strings.padStart(latStr, 8, ' ');

			// Longitude is displayed in East longitude: [0, 360]
			double lon = Math.toDegrees(llr.lon);
			if (lon < 0.0)
				lon += 360.0;
			String lonStr = decimalFormatter.format(lon) + DegreeChar;
			lonStr = Strings.padStart(lonStr, 8, ' ');

			double rad = llr.rad;
			String radStr = decimalFormatter.format(rad) + " km";
			radStr = Strings.padStart(radStr, 9, ' ');

			llrStr = "Lat: " + latStr + "  Lon: " + lonStr + "  Radius: " + radStr + "  ";
		}

		// Update the status bar
		refStatusBar.setRightText(llrStr + rangeStr);
	}

}
