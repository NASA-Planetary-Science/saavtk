package edu.jhuapl.saavtk.status;

import java.awt.event.InputEvent;
import java.text.DecimalFormat;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Strings;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

/**
 * Object that is responsible for sending out status updates of the mouse
 * location over the {@link Renderer}.
 *
 * @author lopeznr1
 */
public class LocationStatusHandler implements PickListener
{
	// Constants
	private static final char DegreeChar = '\u00B0';

	// Ref vars
	private final StatusNotifier refStatusNotifier;
	private final Renderer refRenderer;

	/** Standard Constructor */
	public LocationStatusHandler(StatusNotifier aStatusNotifier, Renderer aRenderer)
	{
		refStatusNotifier = aStatusNotifier;
		refRenderer = aRenderer;
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Ignore primary actions
		if (aMode == PickMode.ActivePri)
			return;

		// Form the rangeStr
		Vector3D cameraPos = refRenderer.getCamera().getPosition();
		double distance = cameraPos.distance(Vector3D.ZERO);

		DecimalFormat decimalFormatter = new DecimalFormat("##0.000");
		String rangeStr = decimalFormatter.format(distance) + " km";
		rangeStr = Strings.padStart(rangeStr, 10, ' ');
		rangeStr = "Range: " + rangeStr + " ";

		// Form the llrStr
		String llrStr = "";
		Vector3D cSurfacePos = aSurfaceTarg.getPosition();
		if (cSurfacePos != null)
		{
			LatLon llr = MathUtil.reclat(cSurfacePos.toArray());

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
		refStatusNotifier.setSecStatus(llrStr + rangeStr, null);
	}

}
