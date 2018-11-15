package edu.jhuapl.saavtk.gui.render.toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import edu.jhuapl.saavtk.gui.GuiUtil;
import edu.jhuapl.saavtk.gui.render.axes.CartesianAxis;
import edu.jhuapl.saavtk.gui.render.axes.CartesianViewDirection;

public class RenderToolbar extends JToolBar implements ActionListener
{
	// Constants
	private static final long serialVersionUID = 1L;
//	public static final String	orientationAxesStateProperty	= "ORIENTATION_AXES_STATE_CHANGED";
//	public static final String	viewAllProperty					= "VIEW_CHANGED";
//	public static final String	pickRotationCenterProperty		= "ROTATION_CENTER_CHANGED";

	// State vars
	private List<RenderToolbarListener> listeners;

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

	public RenderToolbar()
	{
		listeners = new ArrayList<>();

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
		xAxisLockTB = GuiUtil.formToggleButton(this, priImage, secImage, "Lock x-Axis");

		priImage = loadImage("lock-orientation-y.png", iconSize, iconSize);
		secImage = ImageUtil.colorize(priImage, dyeColor);
		yAxisLockTB = GuiUtil.formToggleButton(this, priImage, secImage, "Lock y-Axis");

		priImage = loadImage("lock-orientation-z.png", iconSize, iconSize);
		secImage = ImageUtil.colorize(priImage, dyeColor);
		zAxisLockTB = GuiUtil.formToggleButton(this, priImage, secImage, "Lock z-Axis");

		addSeparator();
		add(xAxisLockTB);
		add(yAxisLockTB);
		add(zAxisLockTB);
	}

	/**
	 * Registers a listener with this toolbar.
	 */
	public void addListener(RenderToolbarListener aListener)
	{
		listeners.add(aListener);
	}

	/**
	 * Returns the Show Orientation Axis toggle state.
	 */
	public boolean getOrientationAxesToggleState()
	{
		return showOrientationAxesTB.isSelected();
	}

	/**
	 * Sets the Show Orientation Axis toggle state.
	 */
	public void setOrientationAxesToggleState(boolean aBool)
	{
		showOrientationAxesTB.setSelected(aBool);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		if (source == showOrientationAxesTB)
			fire(new RenderToolbarEvent.ToggleAxesVisibilityEvent(this, showOrientationAxesTB.isSelected()));
		else if (source == pickRotationCenterTB)
			;
//			fire(new RenderToolbarEvent.PickRotationCenterEvent(this, pickRotationCenterTB.isSelected()));
//			firePropertyChange(pickRotationCenterProperty, null, null);
		else if (source == pickRotationCenterTB)
			fire(new RenderToolbarEvent.ViewAllEvent(this));

		// Set View along axis...
		else if (source == xAlignPosB)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.PLUS_X));
		else if (source == xAlignNegB)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.MINUS_X));
		else if (source == yAlignPosB)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.PLUS_Y));
		else if (source == yAlignNegB)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.MINUS_Y));
		else if (source == zAlignPosB)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.PLUS_Z));
		else if (source == zAlignNegB)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.MINUS_Z));
		else if (source == resetViewB)
			fire(new RenderToolbarEvent.ViewAllEvent(this));

		// Logic: Lock Axis
		else if (source == xAxisLockTB)
		{
			yAxisLockTB.setSelected(false);
			zAxisLockTB.setSelected(false);

			CartesianAxis targAxis = CartesianAxis.NONE;
			if (xAxisLockTB.isSelected() == true)
				targAxis = CartesianAxis.X;
			fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, targAxis));
		}
		else if (source == yAxisLockTB)
		{
			xAxisLockTB.setSelected(false);
			zAxisLockTB.setSelected(false);

			CartesianAxis targAxis = CartesianAxis.NONE;
			if (yAxisLockTB.isSelected() == true)
				targAxis = CartesianAxis.Y;
			fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, targAxis));
		}
		else if (source == zAxisLockTB)
		{
			xAxisLockTB.setSelected(false);
			yAxisLockTB.setSelected(false);

			CartesianAxis targAxis = CartesianAxis.NONE;
			if (zAxisLockTB.isSelected() == true)
				targAxis = CartesianAxis.Z;
			fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, targAxis));
		}
	}

	/**
	 * Helper method that will fire off the specified event to all the registered
	 * listeners.
	 * 
	 * @param aEvent
	 */
	private void fire(RenderToolbarEvent aEvent)
	{
		for (RenderToolbarListener aListener : listeners)
			aListener.handle(aEvent);
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
