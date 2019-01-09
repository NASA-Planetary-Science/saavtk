package edu.jhuapl.saavtk.gui.dialog;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.math3.geometry.euclidean.threed.NotARotationMatrixException;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import edu.jhuapl.saavtk.gui.GNumberField;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;
import edu.jhuapl.saavtk.gui.render.Renderer.ProjectionType;
import edu.jhuapl.saavtk.gui.render.camera.Camera;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

public class CameraDialog extends JDialog implements ActionListener
{
	// Constants
	private static final long serialVersionUID = 1L;
	private static final double JupiterScale = 75000;

	// State vars
	private Renderer renderer;
	private boolean mutateFlag = false;
	private boolean nadirFocalPoint;
	private double cameraRadius = 0.0, viewRadius = 0.0;

	// Gui vars
	private JButton applyButton;
	private JButton resetButton;
	private JButton okayButton;
	private JButton cancelButton;
	private GNumberField fovNF;
	private GNumberField spacecraftAltNF;
	private JComboBox<ProjectionType> projComboBox;
	private GNumberField subSpacecraftLatNF;
	private GNumberField subspacecraftLonNF;
	private GNumberField cameraRollNF;
	private GNumberField boresightLatNF;
	private GNumberField boresightLonNF;
	private GNumberField lineOfSightDistanceNF;

	public CameraDialog()
	{
		renderer = null;

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[right][grow][]", "[]"));

		// Create "Vertical Field of View" text entry box and add to 1st row
		JLabel fovLabel = new JLabel("Vertical Field of View");
		fovNF = new GNumberField(this, 0.00000001, 179.0);
		panel.add(fovLabel, "");
		panel.add(fovNF, "growx");
		panel.add(new JLabel("degrees"), "wrap");

		// Create "View Point Latitude" text entry box and add to 2nd row
		JLabel vpLat = new JLabel("Boresight Latitude");
		boresightLatNF = new GNumberField(this, -90, 90);
		panel.add(vpLat, "");
		panel.add(boresightLatNF, "growx");
		panel.add(new JLabel("degrees"), "wrap");

		// Create "View Point Longitude" text entry box and add to 3rd row
		JLabel vpLong = new JLabel("Boresight Longitude");
		boresightLonNF = new GNumberField(this, -180, 180);
		JLabel vpLongDegree = new JLabel("degrees east");
		panel.add(vpLong, "");
		panel.add(boresightLonNF, "growx");
		panel.add(vpLongDegree, "wrap");

		// Create "View Point Altitude" text entry box and add to 4th row
		JLabel vpAlt = new JLabel("Line of Sight Distance");
		lineOfSightDistanceNF = new GNumberField(this);
		JLabel vpAltDistance = new JLabel("km");
		panel.add(vpAlt, "");
		panel.add(lineOfSightDistanceNF, "growx");
		panel.add(vpAltDistance, "wrap");

		// Create "Projection Type" combo box and add to 5th row
		JLabel projLabel = new JLabel("Projection Type");
		projComboBox = new JComboBox<>(Renderer.ProjectionType.values());
		panel.add(projLabel, "");
		panel.add(projComboBox, "growx,wrap");

		// Create "Camera Latitude" text entry box and add to 6th row
		subSpacecraftLatNF = new GNumberField(this, -90, 90);
		panel.add(new JLabel("Sub-Spacecraft Latitude"), "");
		panel.add(subSpacecraftLatNF, "growx");
		panel.add(new JLabel("degrees"), "wrap");

		// Create "Camera Longitude" text entry box and add to 7th row
		subspacecraftLonNF = new GNumberField(this, -180, 180);
		panel.add(new JLabel("Sub-Spacecraft Longitude"), "");
		panel.add(subspacecraftLonNF, "growx");
		panel.add(new JLabel("degrees east"), "wrap");

		// Create "Camera Altitude" text entry box and add to 8th row
		JLabel altLabel = new JLabel("Spacecraft Altitude");
		spacecraftAltNF = new GNumberField(this);
		JLabel kmLabel = new JLabel("km");
		panel.add(altLabel, "");
		panel.add(spacecraftAltNF, "growx");
		panel.add(kmLabel, "wrap");

		// Create "Camera Roll" text entry box and add to 9th row
		cameraRollNF = new GNumberField(this, -360, 360);
		panel.add(new JLabel("Camera Roll"), "");
		panel.add(cameraRollNF, "growx");
		panel.add(new JLabel("degrees"), "wrap");

		// Create the action area: Buttons: Apply, Reset, OK, Cancel
		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		okayButton = new JButton("OK");
		okayButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		panel.add(applyButton, "span,split,align right");
		panel.add(resetButton);
		panel.add(okayButton);
		panel.add(cancelButton);

		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setTitle("Camera");
		getContentPane().add(panel);
		pack();
	}

	/**
	 * Sets in the Renderer associated with this dialog.
	 */
	public void setRenderer(Renderer aRenderer)
	{
		this.renderer = aRenderer;
		printCameraOrientation();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if (source == applyButton || source == okayButton)
		{
			updateModel();
		}
		else if (source == resetButton)
		{
			Camera tmpCamera = renderer.getCamera();
			renderer.setCameraViewAngle(30.0);
			tmpCamera.reset();
			updateGuiMain();
			updateModel();
		}
		else
		{
			mutateFlag = true;
			updateGuiAction();
		}

		if (source == okayButton || source == cancelButton)
		{
			setVisible(false);
		}
	}

	@Override
	public void setVisible(boolean aBool)
	{
		if (aBool == true)
		{
			updateGuiMain();
			updateGuiAction();
		}

		super.setVisible(aBool);
	}

	private void printCameraOrientation()
	{
		double[] position = new double[3];
		double[] cx = new double[3];
		double[] cy = new double[3];
		double[] cz = new double[3];
		double[] viewAngle = new double[1];
		renderer.getCameraOrientation(position, cx, cy, cz, viewAngle);

		try
		{
			double[][] m = { { cx[0], cx[1], cx[2] }, { cy[0], cy[1], cy[2] }, { cz[0], cz[1], cz[2] } };

			Rotation rotation = new Rotation(m, 1.0e-6);

			String str = "Camera position and orientation (quaternion):\n";
			str += position[0] + " " + position[1] + " " + position[2] + "\n";
			str += rotation.getQ0() + " " + rotation.getQ1() + " " + rotation.getQ2() + " " + rotation.getQ3();

			// str += "\n" + m[0][0] + " " + m[0][1] + " " + m[0][2];
			// str += "\n" + m[1][0] + " " + m[1][1] + " " + m[1][2];
			// str += "\n" + m[2][0] + " " + m[2][1] + " " + m[2][2];

			System.out.println(str);
		}
		catch (NotARotationMatrixException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Helper method that updates the action area of the GUI. If any numerical input
	 * is invalid then the applyB and okayB will be disabled.
	 */
	private void updateGuiAction()
	{
		boolean isValid = true;
		isValid &= fovNF.isValidInput();
		isValid &= spacecraftAltNF.isValidInput();
		isValid &= subSpacecraftLatNF.isValidInput();
		isValid &= subspacecraftLonNF.isValidInput();
		isValid &= cameraRollNF.isValidInput();
		isValid &= boresightLatNF.isValidInput();
		isValid &= boresightLonNF.isValidInput();
		isValid &= lineOfSightDistanceNF.isValidInput();

		applyButton.setEnabled(isValid);
		okayButton.setEnabled(isValid);
	}

	/**
	 * Helper method that updates the main area of the GUI to reflect the input of
	 * the Renderer model.
	 */
	private void updateGuiMain()
	{
		fovNF.setValue(renderer.getCameraViewAngle());
		calculateCameraRadius();
//		System.out.println("Camera distance: " + renderer.getCameraDistance() + " cameraRadius: " + cameraRadius);
		spacecraftAltNF.setValue(renderer.getCameraDistance() - cameraRadius);
		projComboBox.setSelectedItem(renderer.getProjectionType());
		LatLon cameraLatLon = renderer.getCameraLatLon();
		subSpacecraftLatNF.setValue(cameraLatLon.lat);
		subspacecraftLonNF.setValue(cameraLatLon.lon);
		cameraRollNF.setValue(renderer.getCameraRoll());

		LatLon viewPointLatLon = MathUtil.reclat(renderer.getCameraFocalPoint()).toDegrees();
		double lat = viewPointLatLon.lat;
		double lon = viewPointLatLon.lon;

		calculateViewRadius();
		if (nadirFocalPoint)
		{
			lat = cameraLatLon.lat;
			lon = cameraLatLon.lon;
		}
		double[] focalPoint = renderer.getCameraFocalPoint();
		lineOfSightDistanceNF.setValue(MathUtil.vnorm(focalPoint) - viewRadius);
		boresightLatNF.setValue(lat);
		boresightLonNF.setValue(lon);
	}

	/**
	 * Helper method that updates the Renderer model with the GUI input. This method
	 * will do nothing if the GUI has not been mutated (by the user).
	 * <P>
	 * A Side effect of this call is that he mutateFlag will be set to false.
	 */
	private void updateModel()
	{
		// Bail if the GUI has not been mutated by the user
		if (mutateFlag == false)
			return;
		mutateFlag = false;

		double newFov = fovNF.getValue();
		renderer.setCameraViewAngle(newFov);

		double spacecraftAltitude = spacecraftAltNF.getValue();
		calculateCameraRadius();

//		System.out.println("RadiusCamera: " + cameraRadius);
		// MathUtil.unorm(pos, pos);
		// MathUtil.vscl(radius + altitude, pos, pos);
		renderer.setCameraDistance(cameraRadius + spacecraftAltitude);

		renderer.setProjectionType((ProjectionType) projComboBox.getSelectedItem());

		// Set camera position latitude/longitude fields
		// Compute camera position
		double latitude = subSpacecraftLatNF.getValue();
		double longitude = subspacecraftLonNF.getValue();
		renderer.setCameraLatLon(new LatLon(latitude, longitude));

		// Set camera attitude
		// Point camera Nadir (toward origin) with specified roll angle
//		renderer.setCameraFocalPoint(new double[] {0,0,0});
		double roll = cameraRollNF.getValue();
		renderer.setCameraRoll(roll);

		// Set camera view point
//		GenericPolyhedralModel model = renderer.getGenericPolyhedralModel();
		double lineOfSightAltitude = lineOfSightDistanceNF.getValue();
		LatLon viewpointLatLong = new LatLon(boresightLatNF.getValue(), boresightLonNF.getValue());
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
		renderer.setCameraFocalPoint(pos);

		printCameraOrientation();
	}

	private void calculateCameraRadius()
	{
//		GenericPolyhedralModel model = renderer.getGenericPolyhedralModel();
		LatLon cameraLatLong = renderer.getCameraLatLon();
//		System.out.println("Lat: " + cameraLatLong.lat + " Lon: " + cameraLatLong.lon);
		double[] pos = MathUtil.latrec(cameraLatLong.toRadians());
		double[] viewDirection = new double[3];
		double[] origin = new double[3];
		MathUtil.unorm(pos, pos);
		MathUtil.vscl(JupiterScale, pos, origin);
		MathUtil.vscl(-1.0, pos, viewDirection);
//		pos[0] *= altitude;
//		pos[1] *= altitude;
//		pos[2] *= altitude;
//		int result = model.computeRayIntersection(origin, viewDirection, pos);
		cameraRadius = MathUtil.vnorm(pos);
	}

	private void calculateViewRadius()
	{
//		GenericPolyhedralModel model = renderer.getGenericPolyhedralModel();
//		LatLon viewPointLatLong = MathUtil.reclat(renderer.getCameraFocalPoint()).toDegrees();
//		System.out.println(viewPointLatLong.lat + " " + viewPointLatLong.lon);
//		System.out.println(MathUtil.latrec(viewPointLatLong.toRadians())[0] + MathUtil.latrec(viewPointLatLong.toRadians())[1] + MathUtil.latrec(viewPointLatLong.toRadians())[2]);
		double[] pos = renderer.getCameraFocalPoint();
		if (MathUtil.vnorm(pos) == 0)
		{
			pos = renderer.getCameraPosition();
			nadirFocalPoint = true;
		}
		// pos = renderer.getCameraFocalPoint();
		// //MathUtil.latrec(viewPointLatLong.toRadians());
		double[] viewDirection = new double[3];
		double[] origin = new double[3];
		MathUtil.unorm(pos, pos);
		MathUtil.vscl(JupiterScale, pos, origin);
		MathUtil.vscl(-1.0, pos, viewDirection);
//		pos[0] *= altitude;
//		pos[1] *= altitude;
//		pos[2] *= altitude;
//		int result = model.computeRayIntersection(origin, viewDirection, pos);
		viewRadius = MathUtil.vnorm(pos);
	}

}
