package edu.jhuapl.saavtk.gui.render.toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.camera.Camera;
import edu.jhuapl.saavtk.camera.CameraUtil;
import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer.AxisType;

public class RenderToolbar extends JToolBar implements ActionListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private RenderPanel refRenderPanel;
	private Camera refCamera;

	// GUI vars
	private JToggleButton showOrientationAxesTB, pickRotationCenterTB;
	private JButton resetViewB;

	private JToggleButton xAxisLockTB, yAxisLockTB, zAxisLockTB;

	private JButton xAlignPosB, xAlignNegB, yAlignPosB, yAlignNegB, zAlignPosB, zAlignNegB;

//	// ImageIcon dragToolbarIcon=new
//	// ImageIcon(RenderToolbar.class.getResource("grab.png"));
//
//	// TODO: add dropdown for out-of-bounds components
//	List<Component> componentsToShow=Lists.newArrayList();
//	List<Component> visibleComponents=Lists.newArrayList();
//	List<Component> outOfBoundsComponents=Lists.newArrayList();

	public RenderToolbar(RenderPanel aRenderPanel, Camera aCamera)
	{
		refRenderPanel = aRenderPanel;
		refCamera = aCamera;
		if (refCamera == null)
			throw new NullPointerException();

		setFloatable(false);
		setRollover(true);
		setOrientation(JToolBar.HORIZONTAL);
		setAutoscrolls(true);

		BufferedImage secImage, priImage;
		Color dyeColor = new Color(255, 0, 0, 48);
		int iconSize = 32;

		// Set up the buttons: showOrientationB, pickRotationCenterB, resetOrientationB
		priImage = loadImage("pqShowOrientationAxes.png", iconSize, iconSize);
		priImage = ImageUtil.replaceAlpha(priImage, Color.WHITE, 0);
		secImage = ImageUtil.colorize(priImage, dyeColor);
		showOrientationAxesTB = GuiUtil.formToggleButton(this, priImage, secImage, "Show Orientation Axes");
		showOrientationAxesTB.setSelected(true);

		priImage = loadImage("pqPickCenter.png", iconSize, iconSize);
//		priImage = ImageUtil.replaceAlpha(secImage, Color.WHITE, 0);
		secImage = ImageUtil.colorize(priImage, dyeColor);
		pickRotationCenterTB = GuiUtil.formToggleButton(this, priImage, secImage, "Pick Center");
		pickRotationCenterTB.setEnabled(false);

		priImage = loadImage("pqResetCamera.png", iconSize, iconSize);
		resetViewB = GuiUtil.formButton(this, priImage, "Reset View");

		addSeparator();
		add(showOrientationAxesTB);
		add(pickRotationCenterTB);
		add(resetViewB);

		// Set up the x,y,z view align buttons
		priImage = loadImage("pqXMinus.png", iconSize, iconSize);
		xAlignNegB = GuiUtil.formButton(this, priImage, "Set View to Axis: -X");

		priImage = loadImage("pqXPlus.png", iconSize, iconSize);
		xAlignPosB = GuiUtil.formButton(this, priImage, "Set View to Axis: +X");

		priImage = loadImage("pqYMinus.png", iconSize, iconSize);
		yAlignNegB = GuiUtil.formButton(this, priImage, "Set View to Axis: -Y");

		priImage = loadImage("pqYPlus.png", iconSize, iconSize);
		yAlignPosB = GuiUtil.formButton(this, priImage, "Set View to Axis: +Y");

		priImage = loadImage("pqZMinus.png", iconSize, iconSize);
		zAlignNegB = GuiUtil.formButton(this, priImage, "Set View to Axis: -Z");

		priImage = loadImage("pqZPlus.png", iconSize, iconSize);
		zAlignPosB = GuiUtil.formButton(this, priImage, "Set View to Axis: +Z");

		addSeparator();
		add(xAlignPosB);
		add(xAlignNegB);
		add(yAlignPosB);
		add(yAlignNegB);
		add(zAlignPosB);
		add(zAlignNegB);

		// Set up the xAxisLockTB, yAxisLockTB, zAxisLockTB;
		priImage = loadImage("lock-orientation-x.png", iconSize, iconSize);
		secImage = ImageUtil.colorize(priImage, dyeColor);
		xAxisLockTB = GuiUtil.formToggleButton(this, priImage, secImage, "Lock X-Axis");

		priImage = loadImage("lock-orientation-y.png", iconSize, iconSize);
		secImage = ImageUtil.colorize(priImage, dyeColor);
		yAxisLockTB = GuiUtil.formToggleButton(this, priImage, secImage, "Lock Y-Axis");

		priImage = loadImage("lock-orientation-z.png", iconSize, iconSize);
		secImage = ImageUtil.colorize(priImage, dyeColor);
		zAxisLockTB = GuiUtil.formToggleButton(this, priImage, secImage, "Lock Z-Axis");

		addSeparator();
		add(xAxisLockTB);
		add(yAxisLockTB);
		add(zAxisLockTB);
	}
	
	public JToggleButton getAxesButton()
	{
		return showOrientationAxesTB;
	}

	/**
	 * Returns the Show Orientation Axis toggle state.
	 */
	public boolean getOrientationAxesToggleState()
	{
		return showOrientationAxesTB.isSelected();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		if (source == pickRotationCenterTB)
			;
//			fire(new RenderToolbarEvent.PickRotationCenterEvent(this, pickRotationCenterTB.isSelected()));
//			firePropertyChange(pickRotationCenterProperty, null, null);
		else if (source == resetViewB)
			refCamera.reset();

		// Set View along axis...
		else if (source == xAlignPosB)
			CameraUtil.setOrientationInDirectionOfAxis(refCamera, AxisType.POSITIVE_X);
		else if (source == xAlignNegB)
			CameraUtil.setOrientationInDirectionOfAxis(refCamera, AxisType.NEGATIVE_X);
		else if (source == yAlignPosB)
			CameraUtil.setOrientationInDirectionOfAxis(refCamera, AxisType.POSITIVE_Y);
		else if (source == yAlignNegB)
			CameraUtil.setOrientationInDirectionOfAxis(refCamera, AxisType.NEGATIVE_Y);
		else if (source == zAlignPosB)
			CameraUtil.setOrientationInDirectionOfAxis(refCamera, AxisType.POSITIVE_Z);
		else if (source == zAlignNegB)
			CameraUtil.setOrientationInDirectionOfAxis(refCamera, AxisType.NEGATIVE_Z);

		// Logic: Lock Axis
		else if (source == xAxisLockTB)
		{
			yAxisLockTB.setSelected(false);
			zAxisLockTB.setSelected(false);

			Vector3D targAxis = Vector3D.ZERO;
			if (xAxisLockTB.isSelected() == true)
				targAxis = refCamera.getCoordinateSystem().getAxisX();

			Vector3D targOrig = refCamera.getFocalPoint();

			refRenderPanel.constrainRotationAxis(targAxis, targOrig);
		}
		else if (source == yAxisLockTB)
		{
			xAxisLockTB.setSelected(false);
			zAxisLockTB.setSelected(false);

			Vector3D targAxis = Vector3D.ZERO;
			if (yAxisLockTB.isSelected() == true)
				targAxis = refCamera.getCoordinateSystem().getAxisY();

			Vector3D targOrig = refCamera.getFocalPoint();

			refRenderPanel.constrainRotationAxis(targAxis, targOrig);
		}
		else if (source == zAxisLockTB)
		{
			xAxisLockTB.setSelected(false);
			yAxisLockTB.setSelected(false);

			Vector3D targAxis = Vector3D.ZERO;
			if (zAxisLockTB.isSelected() == true)
				targAxis = refCamera.getCoordinateSystem().getAxisZ();

			Vector3D targOrig = refCamera.getFocalPoint();

			refRenderPanel.constrainRotationAxis(targAxis, targOrig);
		}
	}

	/**
	 * Helper method to load the image at the specified resource. The specified
	 * resource must be located in the same folder/package as this
	 * RenderToolbar.class.
	 */
	private BufferedImage loadImage(String aResourceName, int aImageW, int aImageH)
	{
		return ImageUtil.loadImage(RenderToolbar.class.getResource(aResourceName), aImageW, aImageH);
	}

}
