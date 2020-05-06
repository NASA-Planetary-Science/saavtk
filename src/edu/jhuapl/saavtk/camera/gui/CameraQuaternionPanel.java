package edu.jhuapl.saavtk.camera.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.math3.geometry.euclidean.threed.NotARotationMatrixException;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import edu.jhuapl.saavtk.camera.View;
import edu.jhuapl.saavtk.camera.ViewActionListener;
import edu.jhuapl.saavtk.colormap.SigFigNumberFormat;
import edu.jhuapl.saavtk.gui.util.Colors;
import glum.gui.GuiUtil;
import glum.gui.action.ActionComponentProvider;
import glum.gui.component.GNumberField;
import glum.gui.component.GNumberFieldSlider;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure a {@link View}'s camera via the
 * following parameters:
 * <UL>
 * <LI>Camera position (x-position,y-position,z-position)
 * <LI>Camera quaternion (q0, q1, q2, q3)
 * </UL>
 * <P>
 * TODO: Currently the camera can not be configured via the quaternion.
 *
 * @author lopeznr1
 */
public class CameraQuaternionPanel extends JPanel implements ActionComponentProvider, ActionListener, ViewActionListener
{
	// Constants
	private static final Range<Double> ScalarRange = Range.closed(-1.0, 1.0);
	private static final Rotation RotationNaN = new Rotation(Double.NaN, Double.NaN, Double.NaN, Double.NaN, false);
	private static final int NumCols = 14;

	// Ref vars
	private final View refView;

	// State vars
	private Vector3D currPos;
	private Rotation currRot;
	private String quaternionErrMsg;

	// Gui vars
	private final GNumberField xPosNF, yPosNF, zPosNF;
	private final GNumberFieldSlider quat0NFS, quat1NFS, quat2NFS, quat3NFS;

	private final JLabel statusL;
	private final JButton dumpB;

	/**
	 * Standard Constructor
	 */
	public CameraQuaternionPanel(View aView)
	{
		refView = aView;

		currPos = Vector3D.NaN;
		currRot = RotationNaN;
		quaternionErrMsg = null;

		setLayout(new MigLayout("", "[right][grow][]", "[]"));

		// Position area
		JLabel positionL = new JLabel("Position", JLabel.CENTER);
		add(positionL, "growx,span,wrap");

		NumberFormat tmpNF = new SigFigNumberFormat(14, "---");

		JLabel xPosL = new JLabel("X-Pos:");
		xPosNF = new GNumberField(this, tmpNF);
		add(xPosL, "");
		add(xPosNF, "growx");
		add(new JLabel("km"), "wrap");

		JLabel yPosL = new JLabel("Y-Pos:");
		yPosNF = new GNumberField(this, tmpNF);
		add(yPosL, "");
		add(yPosNF, "growx");
		add(new JLabel("km"), "wrap");

		JLabel zPosL = new JLabel("Z-Pos:");
		zPosNF = new GNumberField(this, tmpNF);
		add(zPosL, "");
		add(zPosNF, "growx");
		add(new JLabel("km"), "wrap");

		add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");

		// Orientation area
		JLabel orientationL = new JLabel("Orientation (Quaternion)", JLabel.CENTER);
		add(orientationL, "growx,sgy G1,span,wrap");

		JLabel quat0L = new JLabel("Quat-0:");
		quat0NFS = new GNumberFieldSlider(this, tmpNF, ScalarRange, NumCols);
		add(quat0L, "");
		add(quat0NFS, "growx,wrap");

		JLabel quat1L = new JLabel("Quat-1:");
		quat1NFS = new GNumberFieldSlider(this, tmpNF, ScalarRange, NumCols);
		add(quat1L, "");
		add(quat1NFS, "growx,wrap");

		JLabel quat2L = new JLabel("Quat-2:");
		quat2NFS = new GNumberFieldSlider(this, tmpNF, ScalarRange, NumCols);
		add(quat2L, "");
		add(quat2NFS, "growx,wrap");

		JLabel quat3L = new JLabel("Quat-3:");
		quat3NFS = new GNumberFieldSlider(this, tmpNF, ScalarRange, NumCols);
		add(quat3L, "");
		add(quat3NFS, "growx,wrap");

		statusL = new JLabel("");
		add(statusL, "growx,sgy G1,span,w 0::,wrap");

		dumpB = GuiUtil.createJButton("Log to Console", this);
		dumpB.setToolTipText("Log position and orientation to console.");

		// Disable quaternion components - updating of view is not supported
		GuiUtil.setEnabled(false, quat0NFS, quat1NFS, quat2NFS, quat3NFS);
		GuiUtil.setEnabled(false, quat0L, quat1L, quat2L, quat3L);

		// Register for events of interest
		refView.addViewChangeListener(this);

		handleViewAction(null);
	}

	@Override
	public Collection<? extends Component> getActionButtons()
	{
		return ImmutableList.of(dumpB);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == dumpB)
			doActionLogQuaternion();
		else
			doActionCamera();

		updateGui();
	}

	@Override
	public void handleViewAction(Object aSource)
	{
		updateState();

		syncGuiToModel();
	}

	/**
	 * Helper method that updates the Renderer's camera to reflect the GUI input.
	 * <P>
	 * If there are any errors then nothing will be changed.
	 */
	private void doActionCamera()
	{
		// Bail if there are any errors
		if (getErrorMsg() != null)
			return;

		Vector3D tmpPosition = new Vector3D(xPosNF.getValue(), yPosNF.getValue(), zPosNF.getValue());
		refView.getCamera().setPosition(tmpPosition);

		// TODO: Add ability to set in quaternion also
	}

	/**
	 * Helper method that will output the camera position and orientation
	 * (quaternion) to the console..
	 * <P>
	 * If there are any errors then nothing will be changed.
	 */
	private void doActionLogQuaternion()
	{
		String tmpStr = "Camera position and orientation (quaternion):\n";
		tmpStr += "   " + currPos.getX() + " " + currPos.getY() + " " + currPos.getZ() + "\n";
		if (currRot != RotationNaN)
			tmpStr += "   " + currRot.getQ0() + " " + currRot.getQ1() + " " + currRot.getQ2() + " " + currRot.getQ3();
		else
			tmpStr += "   " + "Failed to compute orientation (quaternion)";
		// tmpStr += " " + m[0][0] + " " + m[0][1] + " " + m[0][2] + "\n";
		// tmpStr += " " + m[1][0] + " " + m[1][1] + " " + m[1][2] + "\n";
		// tmpStr += " " + m[2][0] + " " + m[2][1] + " " + m[2][2] + "\n";

		System.out.println(tmpStr);
	}

	/**
	 * Helper method that will return a string describing invalid user input.
	 * <P>
	 * If all input is valid then null will be returned.
	 */
	private String getErrorMsg()
	{
		if (xPosNF.isValidInput() == false)
			return String.format("Invalid X-Pos.");

		if (yPosNF.isValidInput() == false)
			return String.format("Invalid Y-Pos.");

		if (zPosNF.isValidInput() == false)
			return String.format("Invalid Z-Pos.");

		if (currRot == RotationNaN)
			return "Quaternioun error: " + quaternionErrMsg;

		if (quat0NFS.isValidInput() == false)
			return String.format("Invalid Quaternion-0. Range: [%1.0f, %1.0f]", ScalarRange.lowerEndpoint(),
					ScalarRange.upperEndpoint());

		if (quat1NFS.isValidInput() == false)
			return String.format("Invalid Quaternion-1. Range: [%1.0f, %1.0f]", ScalarRange.lowerEndpoint(),
					ScalarRange.upperEndpoint());

		if (quat2NFS.isValidInput() == false)
			return String.format("Invalid Quaternion-2. Range: [%1.0f, %1.0f]", ScalarRange.lowerEndpoint(),
					ScalarRange.upperEndpoint());

		if (quat3NFS.isValidInput() == false)
			return String.format("Invalid Quaternion-3. Range: [%1.0f, %1.0f]", ScalarRange.lowerEndpoint(),
					ScalarRange.upperEndpoint());

		return null;
	}

	/**
	 * Helper method that will synchronize the GUI with the model.
	 */
	private void syncGuiToModel()
	{
		xPosNF.setValue(currPos.getX());
		yPosNF.setValue(currPos.getY());
		zPosNF.setValue(currPos.getZ());

		quat0NFS.setValue(currRot.getQ0());
		quat1NFS.setValue(currRot.getQ1());
		quat2NFS.setValue(currRot.getQ2());
		quat3NFS.setValue(currRot.getQ3());

		updateGui();
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

	/**
	 * Helper method that keeps the state synchronized with the model.
	 */
	private void updateState()
	{
		currPos = refView.getCamera().getPosition();

		double[] cx = new double[3];
		double[] cy = new double[3];
		double[] cz = new double[3];
		refView.getCamera().getOrientationMatrix(cx, cy, cz);

		currRot = RotationNaN;
		quaternionErrMsg = null;
		try
		{
			double[][] m = { { cx[0], cx[1], cx[2] }, { cy[0], cy[1], cy[2] }, { cz[0], cz[1], cz[2] } };
			currRot = new Rotation(m, 1.0e-6);
		}
		catch (NotARotationMatrixException aExp)
		{
			quaternionErrMsg = aExp.getMessage();
		}
	}

}
