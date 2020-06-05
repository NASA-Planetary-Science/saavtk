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
import edu.jhuapl.saavtk.model.PolyModel;
import edu.jhuapl.saavtk.model.PolyModelUtil;
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
	private static final String ErrMsgNoTargetIntercept = "No target intercept with surface.";
	private static final String ToolTipCameraAlt = "Altitude is distance between camera position and camera intercept (or if a DEM then it's geometric center).";
	private static final String ToolTipLineOfSight = "Line of sight is distance between camera position and target intercept.";

	private static final Range<Double> LatRange = Range.closed(-90.0, 90.0);
	private static final Range<Double> LonRange = Range.closed(0.0, 360.0);
	private static final Range<Double> RollRange = Range.closed(-180.0, 180.0);
	private static final Range<Double> FovRange = Range.closed(0.01, 179.0);
	private static final int NumCols = 6;

	// Ref vars
	private final View refView;
	private final PolyModel refPolyModel;

	// State vars
	private String failMsgExt;

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
	public CameraRegularPanel(View aView, PolyModel aPolyModel)
	{
		refView = aView;
		refPolyModel = aPolyModel;

		failMsgExt = null;

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

		JLabel cameraAltL = new JLabel("Alt*:");
		cameraAltL.setToolTipText(ToolTipCameraAlt);
		cameraAltNF = new GNumberField(this);
		JLabel kmLabel = new JLabel("km");
		add(cameraAltL, "");
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

		JLabel targetLineOfSightL = new JLabel("Line of Sight*:");
		targetLineOfSightL.setToolTipText(ToolTipLineOfSight);
		targetLineOfSightNF = new GNumberField(this);
		targetLineOfSightNF.setEditable(false);
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
		// Flip LOD flag for better performance
		Object source = aEvent.getSource();
		if (source instanceof GNumberFieldSlider)
		{
			boolean lodFlag = ((GNumberFieldSlider) source).getValueIsAdjusting() == true;
			refView.setLodFlag(lodFlag);
		}

		// Clear out any prior extended fail message
		failMsgExt = null;

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

		failMsgExt = null;
		updateGui();
	}

	/**
	 * Helper method to handle actions associated with field of view UI.
	 */
	private void doActionCameraFov()
	{
		// Bail if there are any errors
		if (getErrorMsgForViewUI() != null)
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
		if (getErrorMsgForCameraUI() != null)
			return;

		// Compute the distance between the camera's (surface) intercept and the shape
		// model's origin (geometric center).
		Vector3D cameraPos = refView.getCamera().getPosition();
		Vector3D cameraInterceptPos = PolyModelUtil.calcInterceptPosition(refPolyModel, cameraPos);

		Vector3D geoCenterPos = refPolyModel.getGeometricCenterPoint();
		double cameraInterceptDist = cameraInterceptPos.distance(geoCenterPos);

		// Compute the (new) unit camera direction vector. The unit camera direction
		// vector is provided via user input.
		double tmpLat = cameraLatNFS.getValue();
		double tmpLon = cameraLonNFS.getValue();
		LatLon tmpLL = new LatLon(tmpLat, tmpLon).toRadians();
		double[] tmpPosArr = MathUtil.latrec(tmpLL);
		Vector3D unitCameraDir = new Vector3D(tmpPosArr).normalize();

		// The new camera position will be a function of the following:
		// - camera's unit vector described by user input (camera) lat, lon
		// - camera's unit vector scaled by a (proper) scalar factor.
		//
		// Note the scalar factor is the distance along unit camera direction vector.
		//
		// Currently we approximate the scalar factor as:
		// ---> scalarFact = cameraAltitude + cameraInterceptDist <---
		//
		// This approximation is not sufficient if the shape model's geometric center
		// is not equal to the coordinate system origin (0, 0, 0)

		// Retrieve the (user provided) cameras altitude and compute the scalar factor
		double cameraAltitude = cameraAltNF.getValue();
		double scaleFact = cameraAltitude + cameraInterceptDist;

		// If the shape model's geometric center is not equal to the origin
		// then just utilize the scalar factor from the (old) camera position. Note we
		// thus ignore the (user provided) camera altitude.
		//
		// TODO: Do not ignore (user provided) camera altitude for this special case.
		if (isShapeModelAtOrigin() == false)
			scaleFact = refView.getCamera().getPosition().getNorm();

		// Update the camera to reflect the new position
		Vector3D newCameraPos = unitCameraDir.scalarMultiply(scaleFact);
		refView.getCamera().setPosition(newCameraPos);
	}

	/**
	 * Helper method to handle actions associated with the camera roll UI.
	 */
	private void doActionCameraRoll()
	{
		// Bail if there are any errors
		if (getErrorMsgForCameraUI() != null)
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
		if (getErrorMsgForTargetUI() != null)
			return;

		// Compute the (new) unit target direction vector. The unit target direction
		// vector is provided via user input.
		double tmpLat = targetLatNFS.getValue();
		double tmpLon = targetLonNFS.getValue();
		LatLon tmpLL = new LatLon(tmpLat, tmpLon).toRadians();
		double[] tmpPosArr = MathUtil.latrec(tmpLL);
		Vector3D unitTargetDir = new Vector3D(tmpPosArr).normalize();

		// Calculate the target intercept (aka line of sight intercept) between the
		// origin and the target (direction) vector
		//
		// Note an extend vector is utilized to ensure we will have a vector that does
		// not fall short of the required length of the intercept. The extension amount
		// is 2X the summation of:
		// - bounding box diagonal
		// - distance between the origin and geometric center
		Vector3D geoCenterPos = refPolyModel.getGeometricCenterPoint();
		double extAmt = refPolyModel.getBoundingBoxDiagonalLength();
		extAmt += geoCenterPos.distance(Vector3D.ZERO);
		extAmt *= 2;

		Vector3D extPos = unitTargetDir.scalarMultiply(extAmt);
		Vector3D targetIntercept = refPolyModel.calcInterceptBetween(Vector3D.ZERO, extPos);
		if (targetIntercept == null)
		{
			failMsgExt = ErrMsgNoTargetIntercept;
			return;
		}

		// Set the camera's focal point to be at the target intercept
		refView.getCamera().setFocalPoint(targetIntercept);
	}

	/**
	 * Helper method that will return a string describing invalid user input
	 * associated with the camera UI elements.
	 * <P>
	 * If all input is valid then null will be returned.
	 */
	private String getErrorMsgForCameraUI()
	{
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

		return null;
	}

	/**
	 * Helper method that will return a string describing invalid user input
	 * associated with the target UI elements.
	 * <P>
	 * If all input is valid then null will be returned.
	 */
	private String getErrorMsgForTargetUI()
	{
		if (targetLatNFS.isValidInput() == false)
			return String.format("Invalid Target Latitude. Range: [%1.0f, %1.0f]", LatRange.lowerEndpoint(),
					LatRange.upperEndpoint());

		if (targetLonNFS.isValidInput() == false)
			return String.format("Invalid Target Longitude. Range: [%1.0f, %1.0f]", LonRange.lowerEndpoint(),
					LonRange.upperEndpoint());

		return null;
	}

	/**
	 * Helper method that will return a string describing invalid user input
	 * associated with the view UI elements.
	 * <P>
	 * If all input is valid then null will be returned.
	 */
	private String getErrorMsgForViewUI()
	{
		if (fovNFS.isValidInput() == false)
			return String.format("Invalid FOV. Range: [%1.2f, %1.2f]", FovRange.lowerEndpoint(), FovRange.upperEndpoint());

		return null;
	}

	/**
	 * Helper method that returns true if the reference shape model's geometric
	 * center lies at the origin.
	 */
	private boolean isShapeModelAtOrigin()
	{
		boolean retBool = Vector3D.ZERO.equals(refPolyModel.getGeometricCenterPoint()) == true;
		return retBool;
	}

	/**
	 * Helper method that will synchronize the GUI with the model.
	 */
	private void syncGuiToModel()
	{
		fovNFS.setValue(refView.getCamera().getViewAngle());

		// Camera position + roll
		Vector3D cameraPos = refView.getCamera().getPosition();
		LatLon cameraLL = MathUtil.reclat(cameraPos.toArray()).toDegrees();
		double cameraLat = cameraLL.lat;
		double cameraLon = cameraLL.lon;
		if (cameraLon < 0)
			cameraLon += 360;
		cameraLatNFS.setValue(cameraLat);
		cameraLonNFS.setValue(cameraLon);

		double cameraRoll = refView.getCamera().getRoll() + 0.0;
		cameraRollNFS.setValue(cameraRoll);

		// Camera altitude
		double altDist = PolyModelUtil.calcAltitudeFor(refPolyModel, cameraPos);
		cameraAltNF.setValue(altDist);

		boolean isEnabled = isShapeModelAtOrigin() == true;
		cameraAltNF.setEnabled(isEnabled);

		// Target position
		Vector3D targetPos = refView.getCamera().getFocalPoint();
		LatLon targetLL = MathUtil.reclat(targetPos.toArray()).toDegrees();
		double targetLat = targetLL.lat;
		double targetLon = targetLL.lon;
		if (targetLon < 0)
			targetLon += 360;
		targetLatNFS.setValue(targetLat);
		targetLonNFS.setValue(targetLon);

		// Target line of sight
		// It is necessary to extend the targetPos to ensure an intercept can be
		// computed. This is necessary in particular for a polygonal surfaces.
		double extAmt = refPolyModel.getBoundingBoxDiagonalLength();
		if (extAmt < 2.0)
			extAmt = 2.0;
		Vector3D dirVect = targetPos.subtract(cameraPos);
		Vector3D targetPosExt = cameraPos.add(dirVect.scalarMultiply(extAmt));

		double losDist = Double.NaN;
		Vector3D losInterceptPos = refPolyModel.calcInterceptBetween(cameraPos, targetPosExt);
		if (losInterceptPos != null)
			losDist = losInterceptPos.distance(cameraPos);

		targetLineOfSightNF.setValue(losDist);
	}

	/**
	 * Helper method that keeps the GUI synchronized with user input.
	 */
	private void updateGui()
	{
		// Retrieve various errors / info message
		String failMsg = getErrorMsgForViewUI();
		if (failMsg == null)
			failMsg = getErrorMsgForCameraUI();
		if (failMsg == null)
			failMsg = getErrorMsgForTargetUI();
		if (failMsg == null)
			failMsg = failMsgExt;

		String infoMsg = null;
		if (targetLineOfSightNF.isValidInput() == false)
			infoMsg = String.format("Line of sight is undefined.");

		// Update the status area
		String tmpMsg = infoMsg;
		if (failMsg != null)
			tmpMsg = failMsg;
		statusL.setText(tmpMsg);

		Color fgColor = Colors.getPassFG();
		if (failMsg != null)
			fgColor = Colors.getFailFG();
		else if (infoMsg != null)
			fgColor = Colors.getInfoFG();
		statusL.setForeground(fgColor);
	}

}
