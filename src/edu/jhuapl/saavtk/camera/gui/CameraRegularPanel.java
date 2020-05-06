package edu.jhuapl.saavtk.camera.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.camera.View;
import edu.jhuapl.saavtk.camera.ViewActionListener;
import edu.jhuapl.saavtk.gui.util.Colors;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import glum.gui.GuiUtil;
import glum.gui.component.GNumberField;
import glum.gui.component.GNumberFieldSlider;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure a {@link View}'s camera via the
 * following parameters:
 * <UL>
 * <LI>Field of view
 * <LI>Camera position (lat,lon,alt,roll)
 * <LI>Target position (lat, lon, line-of-sight)
 * </UL>
 *
 * @author lopeznr1
 */
public class CameraRegularPanel extends JPanel implements ActionListener, ViewActionListener
{
	// Constants
	private static final Range<Double> LatRange = Range.closed(-90.0, 90.0);
	private static final Range<Double> LonRange = Range.closed(-180.0, 180.0);
	private static final Range<Double> RollRange = Range.closed(-360.0, 360.0);
	private static final Range<Double> FovRange = Range.closed(0.01, 179.0);
	private static final int NumCols = 6;

	// Ref vars
	private final View refView;

	// Gui vars
	private final GNumberFieldSlider fovNFS;

	private final GNumberField cameraAltNF;
	private final GNumberFieldSlider cameraLatNFS;
	private final GNumberFieldSlider cameraLonNFS;
	private final GNumberFieldSlider cameraRollNFS;

	private final GNumberFieldSlider targetLatNFS;
	private final GNumberFieldSlider targetLonNFS;
	private final GNumberField targetLineOfSightNF;

	private final JLabel statusL;

	/**
	 * Standard Constructor
	 */
	public CameraRegularPanel(View aView)
	{
		refView = aView;

		setLayout(new MigLayout("", "[right][grow][]", "[]"));

		// Field of view area
		JLabel fovLabel = new JLabel("Vertical Field of View:");
		fovNFS = new GNumberFieldSlider(this, new DecimalFormat("#.###"), FovRange, 6);
		add(fovLabel, "growx,span,split");
		add(fovNFS, "");
		add(new JLabel("deg"), "wrap");

		add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");

		DecimalFormat tmpFormat = new DecimalFormat("#.###");

		// Camera area (aka spacecraft)
		JLabel cameraL = new JLabel("Camera (Spacecraft)", JLabel.CENTER);
		add(cameraL, "growx,sgy G1,span,wrap");

		cameraLatNFS = new GNumberFieldSlider(this, tmpFormat, LatRange, NumCols);
		add(new JLabel("Lat:"), "");
		add(cameraLatNFS, "growx");
		add(new JLabel("deg"), "wrap");

		cameraLonNFS = new GNumberFieldSlider(this, tmpFormat, LonRange, NumCols);
		add(new JLabel("Lon:"), "");
		add(cameraLonNFS, "growx");
		add(new JLabel("deg"), "wrap");

		cameraAltNF = new GNumberField(this);
		JLabel kmLabel = new JLabel("km");
		add(new JLabel("Alt:"), "");
		add(cameraAltNF, "growx");
		add(kmLabel, "wrap");

		cameraRollNFS = new GNumberFieldSlider(this, new DecimalFormat("#.###"), RollRange, NumCols);
		add(new JLabel("Roll:"), "");
		add(cameraRollNFS, "growx");
		add(new JLabel("deg"), "wrap");

		add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");

		// Target area (aka Boresight)
		JLabel tmpL = new JLabel("Target (Boresight)", JLabel.CENTER);
		add(tmpL, "growx,span,wrap");

		JLabel targetLatL = new JLabel("Lat:");
		targetLatNFS = new GNumberFieldSlider(this, tmpFormat, LatRange, NumCols);
		add(targetLatL, "");
		add(targetLatNFS, "growx");
		add(new JLabel("deg"), "wrap");

		JLabel targetLonL = new JLabel("Lon:");
		targetLonNFS = new GNumberFieldSlider(this, tmpFormat, LonRange, NumCols);
		add(targetLonL, "");
		add(targetLonNFS, "growx");
		add(new JLabel("deg"), "wrap");

		JLabel targetLineOfSightL = new JLabel("Line of Sight:");
		targetLineOfSightNF = new GNumberField(this);
		add(targetLineOfSightL, "span 2,split 2");
		add(targetLineOfSightNF, "growx");
		add(new JLabel("km"), "wrap");

		statusL = new JLabel("");
		add(statusL, "growx,sgy G1,span,w 0::,wrap");

		// Register for events of interest
		refView.addViewChangeListener(this);

		handleViewAction(null);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		// Flip LOD flag for better performance
		if (source instanceof GNumberFieldSlider)
		{
			boolean lodFlag = ((GNumberFieldSlider) source).getValueIsAdjusting() == true;
			refView.setLodFlag(lodFlag);
		}

		// Process the event
		if (source == fovNFS)
			doActionCameraFov();
		else if (source == cameraLatNFS || source == cameraLonNFS || source == cameraAltNF)
			doActionCameraLocation();
		else if (source == cameraRollNFS)
			doActionCameraRoll();
		else if (source == targetLatNFS || source == targetLonNFS || source == targetLineOfSightNF)
			doActionTargetFocalPoint();

		updateGui();
	}

	@Override
	public void handleViewAction(Object aSource)
	{
		syncGuiToModel();
	}

	/**
	 * Helper method to handle actions associated with field of view UI.
	 */
	private void doActionCameraFov()
	{
		// Bail if there are any errors
		if (getErrorMsg() != null)
			return;

		double newFov = fovNFS.getValue();
		refView.getCamera().setViewAngle(newFov);
	}

	/**
	 * Helper method to handle actions associated with the camera location UI.
	 */
	private void doActionCameraLocation()
	{
		// Bail if there are any errors
		if (getErrorMsg() != null)
			return;

		// Convert lat/lon to unit vector
		double tmpLat = cameraLatNFS.getValue();
		double tmpLon = cameraLonNFS.getValue();
		LatLon tmpLL = new LatLon(tmpLat, tmpLon);

		double[] pos = MathUtil.latrec(tmpLL.toRadians());
		MathUtil.unorm(pos, pos);

		// Compute the distance
		double spacecraftAltitude = cameraAltNF.getValue();
		double cameraRadius = calculateCameraRadius();

		double distance = cameraRadius + spacecraftAltitude;
		pos[0] *= distance;
		pos[1] *= distance;
		pos[2] *= distance;

		Vector3D tmpPosition = new Vector3D(pos[0], pos[1], pos[2]);
		refView.getCamera().setPosition(tmpPosition);
	}

	/**
	 * Helper method to handle actions associated with the camera roll UI.
	 */
	private void doActionCameraRoll()
	{
		// Bail if there are any errors
		if (getErrorMsg() != null)
			return;

		double roll = cameraRollNFS.getValue();
		refView.getCamera().setRoll(roll);
	}

	/**
	 * Helper method to handle actions associated with the target focal point..
	 */
	private void doActionTargetFocalPoint()
	{
		// Bail if there are any errors
		if (getErrorMsg() != null)
			return;

		// TODO: The below logic has defects
		// Set camera view point
//		GenericPolyhedralModel model = refRenderer..getGenericPolyhedralModel();
		double lineOfSightAltitude = targetLineOfSightNF.getValue();
		LatLon viewpointLatLong = new LatLon(targetLatNFS.getValue(), targetLonNFS.getValue());
		double[] pos = MathUtil.latrec(viewpointLatLong.toRadians());
		double[] viewDirection = new double[3];
		double[] origin = new double[3];
		MathUtil.unorm(pos, pos);
		MathUtil.vscl(JupiterScale, pos, origin);
		MathUtil.vscl(-1.0, pos, viewDirection);
//		pos[0] *= altitude;
//		pos[1] *= altitude;
//		pos[2] *= altitude;
//		int result = model.computeRayIntersection(origin, viewDirection, pos);
		double radius = MathUtil.vnorm(pos);
//		System.out.println("RadiusPoint: " + radius);
		MathUtil.unorm(pos, pos);
		MathUtil.vscl(radius + lineOfSightAltitude, pos, pos);
		refView.getCamera().setFocalPoint(new Vector3D(pos));
	}

	/**
	 * Helper method that will return a string describing invalid user input.
	 * <P>
	 * If all input is valid then null will be returned.
	 */
	private String getErrorMsg()
	{
		if (fovNFS.isValidInput() == false)
			return String.format("Invalid FOV. Range: [%1.2f, %1.2f]", FovRange.lowerEndpoint(), FovRange.upperEndpoint());

		if (cameraAltNF.isValidInput() == false)
			return String.format("Invalid Camera Altitude.");

		if (cameraLatNFS.isValidInput() == false)
			return String.format("Invalid Camera Latitude. Range: [%1.0f, %1.0f]", LatRange.lowerEndpoint(),
					LatRange.upperEndpoint());

		if (cameraLonNFS.isValidInput() == false)
			return String.format("Invalid Camera Longitude. Range: [%1.0f, %1.0f]", LonRange.lowerEndpoint(),
					LonRange.upperEndpoint());

		if (cameraRollNFS.isValidInput() == false)
			return String.format("Invalid Camera Roll. Range: [%1.0f, %1.0f]", RollRange.lowerEndpoint(),
					RollRange.upperEndpoint());

		if (targetLatNFS.isValidInput() == false)
			return String.format("Invalid Target Latitude. Range: [%1.0f, %1.0f]", LatRange.lowerEndpoint(),
					LatRange.upperEndpoint());

		if (targetLonNFS.isValidInput() == false)
			return String.format("Invalid Target Longitude. Range: [%1.0f, %1.0f]", LonRange.lowerEndpoint(),
					LonRange.upperEndpoint());

		if (targetLineOfSightNF.isValidInput() == false)
			return String.format("Invalid Target Line-Of-Sight.");

		return null;
	}

	/**
	 * Helper method that will synchronize the GUI with the model.
	 */
	private void syncGuiToModel()
	{
		fovNFS.setValue(refView.getCamera().getViewAngle());

		double cameraRadius = calculateCameraRadius();
		double cameraAltitude = cameraGetDistance() - cameraRadius;
		cameraAltNF.setValue(cameraAltitude);

		Vector3D cameraPos = refView.getCamera().getPosition();
		LatLon cameraLL = MathUtil.reclat(cameraPos.toArray()).toDegrees();
		cameraLatNFS.setValue(cameraLL.lat);
		cameraLonNFS.setValue(cameraLL.lon);
		cameraRollNFS.setValue(refView.getCamera().getRoll());

		Vector3D targetPos = refView.getCamera().getFocalPoint();
		double[] targetPosArr = targetPos.toArray();

		LatLon targetLL = MathUtil.reclat(targetPosArr).toDegrees();
		targetLatNFS.setValue(targetLL.lat);
		targetLonNFS.setValue(targetLL.lon);

		double viewRadius = calculateViewRadius(targetPosArr);
		double lineOfSightDistance = MathUtil.vnorm(targetPosArr) - viewRadius;
		targetLineOfSightNF.setValue(lineOfSightDistance);
	}

	/**
	 * Helper method that keeps the GUI synchronized with user input.
	 */
	private void updateGui()
	{
		// Update the status area
		String tmpMsg = null;
		String errMsg = getErrorMsg();
		if (errMsg != null)
			tmpMsg = errMsg;
		statusL.setText(tmpMsg);

		Color fgColor = Colors.getPassFG();
		if (errMsg != null)
			fgColor = Colors.getFailFG();
		statusL.setForeground(fgColor);
	}

	// -----------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------
	// TODO: The below logic is NOT valid!
	// TODO: It will need to be completely redone.

	// Constants
	@Deprecated
	private static final double JupiterScale = 75000;

	private double calculateCameraRadius()
	{
//		GenericPolyhedralModel model = refRenderer..getGenericPolyhedralModel();
		double[] pos = refView.getCamera().getPosition().toArray();
		double[] viewDirection = new double[3];
		double[] origin = new double[3];
		MathUtil.unorm(pos, pos);
		MathUtil.vscl(JupiterScale, pos, origin);
		MathUtil.vscl(-1.0, pos, viewDirection);
//		pos[0] *= altitude;
//		pos[1] *= altitude;
//		pos[2] *= altitude;
//		int result = model.computeRayIntersection(origin, viewDirection, pos);

		double cameraRadius = MathUtil.vnorm(pos);
		return cameraRadius;
	}

	private static double calculateViewRadius(double[] aPos)
	{
		double[] uPos = new double[3];
		double[] origin = new double[3];
		double[] viewDirection = new double[3];

		MathUtil.unorm(aPos, uPos);
		MathUtil.vscl(JupiterScale, uPos, origin);
		MathUtil.vscl(-1.0, uPos, viewDirection);
//		pos[0] *= altitude;
//		pos[1] *= altitude;
//		pos[2] *= altitude;
//		int result = model.computeRayIntersection(origin, viewDirection, pos);

		double retRadius = MathUtil.vnorm(uPos);
		return retRadius;
	}

	private double cameraGetDistance()
	{
		double[] posArr = refView.getCamera().getPosition().toArray();
		return MathUtil.vnorm(posArr);
	}

}
