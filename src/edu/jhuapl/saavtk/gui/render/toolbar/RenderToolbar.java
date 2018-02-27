package edu.jhuapl.saavtk.gui.render.toolbar;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.render.axes.CartesianAxis;
import edu.jhuapl.saavtk.gui.render.axes.CartesianViewDirection;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSeparator;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class RenderToolbar extends JToolBar implements ItemListener, ActionListener, PropertyChangeListener, HierarchyBoundsListener
{
	JToggleButton				constrainRotationXButton		= new JToggleButton();
	JToggleButton				constrainRotationYButton		= new JToggleButton();
	JToggleButton				constrainRotationZButton		= new JToggleButton();
	protected final ButtonGroup	constrainRotationButtonGroup	= new UnselectableButtonGroup();

	JToggleButton				showOriginButton		= new JToggleButton();

	ImageIcon					constrainRotationXIcon			= new ImageIcon(
			RenderToolbar.class.getResource("lock-orientation-x.png"));
	ImageIcon					constrainRotationYIcon			= new ImageIcon(
			RenderToolbar.class.getResource("lock-orientation-y.png"));
	ImageIcon					constrainRotationZIcon			= new ImageIcon(
			RenderToolbar.class.getResource("lock-orientation-z.png"));

	JButton						viewAlignPlusXButton			= new JButton();
	JButton						viewAlignMinusXButton			= new JButton();
	JButton						viewAlignPlusYButton			= new JButton();
	JButton						viewAlignMinusYButton			= new JButton();
	JButton						viewAlignPlusZButton			= new JButton();
	JButton						viewAlignMinusZButton			= new JButton();

	ImageIcon					viewAlignPlusXIcon				= new ImageIcon(
			RenderToolbar.class.getResource("pqXPlus.png"));
	ImageIcon					viewAlignMinusXIcon				= new ImageIcon(
			RenderToolbar.class.getResource("pqXMinus.png"));
	ImageIcon					viewAlignPlusYIcon				= new ImageIcon(
			RenderToolbar.class.getResource("pqYPlus.png"));
	ImageIcon					viewAlignMinusYIcon				= new ImageIcon(
			RenderToolbar.class.getResource("pqYMinus.png"));
	ImageIcon					viewAlignPlusZIcon				= new ImageIcon(
			RenderToolbar.class.getResource("pqZPlus.png"));
	ImageIcon					viewAlignMinusZIcon				= new ImageIcon(
			RenderToolbar.class.getResource("pqZMinus.png"));

	JToggleButton				showOrientationAxesToggleButton	= new JToggleButton();
	JButton						viewAllButton					= new JButton();
	JToggleButton				pickRotationCenterButton		= new JToggleButton();

	ImageIcon					showOrientationAxesIcon			= new ImageIcon(
			RenderToolbar.class.getResource("pqShowOrientationAxes.png"));
	ImageIcon					viewAllIcon						= new ImageIcon(
			RenderToolbar.class.getResource("pqResetCamera.png"));
	ImageIcon					pickRotationIcon				= new ImageIcon(
			RenderToolbar.class.getResource("pqPickCenter.png"));
	// ImageIcon dragToolbarIcon=new
	// ImageIcon(RenderToolbar.class.getResource("grab.png"));

	public static final String	orientationAxesStateProperty	= "ORIENTATION_AXES_STATE_CHANGED";
	public static final String	viewAllProperty					= "VIEW_CHANGED";
	public static final String	pickRotationCenterProperty		= "ROTATION_CENTER_CHANGED";
	
	// TODO: add dropdown for out-of-bounds components
	List<Component> componentsToShow=Lists.newArrayList();
	List<Component> visibleComponents=Lists.newArrayList();
	List<Component> outOfBoundsComponents=Lists.newArrayList();

	public RenderToolbar()
	{
		setRollover(true);
		setOrientation(JToolBar.HORIZONTAL);
		setAutoscrolls(true);
		
		addPropertyChangeListener(this);
		addHierarchyBoundsListener(this);

		constrainRotationButtonGroup.add(constrainRotationXButton);
		constrainRotationButtonGroup.add(constrainRotationYButton);
		constrainRotationButtonGroup.add(constrainRotationZButton);

		int iconSize = 32;
		constrainRotationXIcon
				.setImage(constrainRotationXIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		constrainRotationYIcon
				.setImage(constrainRotationYIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		constrainRotationZIcon
				.setImage(constrainRotationZIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));

		constrainRotationXButton.setIcon(constrainRotationXIcon);
		constrainRotationYButton.setIcon(constrainRotationYIcon);
		constrainRotationZButton.setIcon(constrainRotationZIcon);

		addSeparator();

		// dragToolbarIcon.setImage(dragToolbarIcon.getImage().getScaledInstance(iconSize/2,
		// iconSize/2, Image.SCALE_SMOOTH));
		// setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		add(constrainRotationXButton);
		add(constrainRotationYButton);
		add(constrainRotationZButton);

		constrainRotationXButton.addItemListener(this);
		constrainRotationYButton.addItemListener(this);
		constrainRotationZButton.addItemListener(this);

		//

		viewAlignPlusXIcon
				.setImage(viewAlignPlusXIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAlignMinusXIcon
				.setImage(viewAlignMinusXIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAlignPlusYIcon
				.setImage(viewAlignPlusYIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAlignMinusYIcon
				.setImage(viewAlignMinusYIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAlignPlusZIcon
				.setImage(viewAlignPlusZIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));

		//
		addSeparator();

		showOrientationAxesToggleButton.setIcon(showOrientationAxesIcon);
		add(showOrientationAxesToggleButton);
		showOrientationAxesToggleButton.addItemListener(this);
		showOrientationAxesToggleButton.setSelected(false);

		showOriginButton.setText("<html><center><b>Cube<br>Axes</b></center></html>");
		add(showOriginButton);
		showOriginButton.addItemListener(this);;
		showOriginButton.setSelected(false);
		
		pickRotationCenterButton.setIcon(pickRotationIcon);
		add(pickRotationCenterButton);
		pickRotationCenterButton.addItemListener(this);
		pickRotationCenterButton.setEnabled(false);

		//

		viewAllButton.setIcon(viewAllIcon);
		add(viewAllButton);
		viewAllButton.addActionListener(this);

		addSeparator();

		viewAlignPlusXButton.setIcon(viewAlignPlusXIcon);

		add(viewAlignPlusXButton);

		viewAlignPlusXButton.addActionListener(this);
		viewAlignMinusXButton.setIcon(viewAlignMinusXIcon);
		add(viewAlignMinusXButton);
		viewAlignMinusXButton.addActionListener(this);
		viewAlignPlusYButton.setIcon(viewAlignPlusYIcon);
		add(viewAlignPlusYButton);
		viewAlignPlusYButton.addActionListener(this);
		viewAlignMinusYButton.setIcon(viewAlignMinusYIcon);
		add(viewAlignMinusYButton);
		viewAlignMinusYButton.addActionListener(this);
		viewAlignPlusZButton.setIcon(viewAlignPlusZIcon);
		add(viewAlignPlusZButton);
		viewAlignPlusZButton.addActionListener(this);

		//

		pickRotationIcon
				.setImage(pickRotationIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));

		//

		showOrientationAxesIcon
				.setImage(showOrientationAxesIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAllIcon.setImage(viewAllIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		showOrientationAxesToggleButton.setSelected(true);
		viewAlignMinusZButton.setIcon(viewAlignMinusZIcon);
		add(viewAlignMinusZButton);
		viewAlignMinusZButton.addActionListener(this);

	}

	public void addToolbarListener(RenderToolbarListener listener)
	{
		listeners.add(listener);
	}

	List<RenderToolbarListener> listeners = Lists.newArrayList();

	public void fire(RenderToolbarEvent event)
	{
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).handle(event);
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getSource() == constrainRotationXButton)
		{
			if (constrainRotationXButton.isSelected())
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, CartesianAxis.X));
			else
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, CartesianAxis.NONE));
		} else if (e.getSource() == constrainRotationYButton)
		{
			if (constrainRotationYButton.isSelected())
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, CartesianAxis.Y));
			else
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, CartesianAxis.NONE));
		} else if (e.getSource() == constrainRotationZButton)
		{
			if (constrainRotationZButton.isSelected())
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, CartesianAxis.Z));
			else
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this, CartesianAxis.NONE));
		} else if (e.getSource() == showOrientationAxesToggleButton)
		{
			fire(new RenderToolbarEvent.ToggleAxesVisibilityEvent(this, showOrientationAxesToggleButton.isSelected()));
		}
		else if (e.getSource() == showOriginButton)
			fire (new RenderToolbarEvent.ToggleOriginVisibilityEvent(this, showOriginButton.isSelected()));

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == viewAlignPlusXButton)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.PLUS_X));
		else if (e.getSource() == viewAlignMinusXButton)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.MINUS_X));
		else if (e.getSource() == viewAlignPlusYButton)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.PLUS_Y));
		else if (e.getSource() == viewAlignMinusYButton)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.MINUS_Y));
		else if (e.getSource() == viewAlignPlusZButton)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.PLUS_Z));
		else if (e.getSource() == viewAlignMinusZButton)
			fire(new RenderToolbarEvent.LookAlongAxisEvent(this, CartesianViewDirection.MINUS_Z));
		else if (e.getSource() == viewAllButton)
			fire(new RenderToolbarEvent.ViewAllEvent(this));
		// else if (e.getSource() == pickRotationCenterButton)
		// firePropertyChange(pickRotationCenterProperty, null, null);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
//		for (Component c : getComponents())
//			System.out.println(this.getBounds()+" "+c.getBounds());
	}
	
	@Override
	public void ancestorResized(HierarchyEvent e)
	{
//		for (Component c : getComponents())
//			System.out.println(this.getBounds()+" "+c.getBounds());
	}
	
	@Override
	public void ancestorMoved(HierarchyEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
}
