package edu.jhuapl.saavtk.gui.renderer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JToggleButton;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSeparator;
import java.awt.GridLayout;
import java.awt.FlowLayout;

public class RenderToolbar extends JPanel implements ItemListener, ActionListener
{
	JToggleButton				constrainRotationXButton		= new JToggleButton();
	JToggleButton				constrainRotationYButton		= new JToggleButton();
	JToggleButton				constrainRotationZButton		= new JToggleButton();
	protected final ButtonGroup	constrainRotationButtonGroup	= new UnselectableButtonGroup();

	ImageIcon					constrainRotationXIcon			= new ImageIcon(RenderToolbar.class.getResource("lock-orientation-x.png"));
	ImageIcon					constrainRotationYIcon			= new ImageIcon(RenderToolbar.class.getResource("lock-orientation-y.png"));
	ImageIcon					constrainRotationZIcon			= new ImageIcon(RenderToolbar.class.getResource("lock-orientation-z.png"));

	JButton						viewAlignPlusXButton			= new JButton();
	JButton						viewAlignMinusXButton			= new JButton();
	JButton						viewAlignPlusYButton			= new JButton();
	JButton						viewAlignMinusYButton			= new JButton();
	JButton						viewAlignPlusZButton			= new JButton();
	JButton						viewAlignMinusZButton			= new JButton();

	ImageIcon					viewAlignPlusXIcon				= new ImageIcon(RenderToolbar.class.getResource("pqXPlus.png"));
	ImageIcon					viewAlignMinusXIcon				= new ImageIcon(RenderToolbar.class.getResource("pqXMinus.png"));
	ImageIcon					viewAlignPlusYIcon				= new ImageIcon(RenderToolbar.class.getResource("pqYPlus.png"));
	ImageIcon					viewAlignMinusYIcon				= new ImageIcon(RenderToolbar.class.getResource("pqYMinus.png"));
	ImageIcon					viewAlignPlusZIcon				= new ImageIcon(RenderToolbar.class.getResource("pqZPlus.png"));
	ImageIcon					viewAlignMinusZIcon				= new ImageIcon(RenderToolbar.class.getResource("pqZMinus.png"));

	JToggleButton				showOrientationAxesToggleButton	= new JToggleButton();
	JButton						viewAllButton					= new JButton();
	JToggleButton				pickRotationCenterButton		= new JToggleButton();

	ImageIcon					showOrientationAxesIcon			= new ImageIcon(RenderToolbar.class.getResource("pqShowOrientationAxes.png"));
	ImageIcon					viewAllIcon						= new ImageIcon(RenderToolbar.class.getResource("pqResetCamera.png"));
	ImageIcon					pickRotationIcon				= new ImageIcon(RenderToolbar.class.getResource("pqPickCenter.png"));

	
	
	public static final String	orientationAxesStateProperty	= "ORIENTATION_AXES_STATE_CHANGED";
	public static final String	viewAllProperty					= "VIEW_CHANGED";
	public static final String	pickRotationCenterProperty		= "ROTATION_CENTER_CHANGED";

	public RenderToolbar()
	{

		constrainRotationButtonGroup.add(constrainRotationXButton);
		constrainRotationButtonGroup.add(constrainRotationYButton);
		constrainRotationButtonGroup.add(constrainRotationZButton);

		int iconSize = 32;
		constrainRotationXIcon.setImage(constrainRotationXIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		constrainRotationYIcon.setImage(constrainRotationYIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		constrainRotationZIcon.setImage(constrainRotationZIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));

		constrainRotationXButton.setIcon(constrainRotationXIcon);
		constrainRotationYButton.setIcon(constrainRotationYIcon);
		constrainRotationZButton.setIcon(constrainRotationZIcon);
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		add(constrainRotationXButton);
		add(constrainRotationYButton);
		add(constrainRotationZButton);

		constrainRotationXButton.addItemListener(this);
		constrainRotationYButton.addItemListener(this);
		constrainRotationZButton.addItemListener(this);

		//

		viewAlignPlusXIcon.setImage(viewAlignPlusXIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAlignMinusXIcon.setImage(viewAlignMinusXIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAlignPlusYIcon.setImage(viewAlignPlusYIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAlignMinusYIcon.setImage(viewAlignMinusYIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		viewAlignPlusZIcon.setImage(viewAlignPlusZIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
		
		add(separator_1);

		//

		showOrientationAxesToggleButton.setIcon(showOrientationAxesIcon);
		add(showOrientationAxesToggleButton);
		showOrientationAxesToggleButton.addItemListener(this);
		showOrientationAxesToggleButton.setSelected(false);
		
				pickRotationCenterButton.setIcon(pickRotationIcon);
				add(pickRotationCenterButton);
				pickRotationCenterButton.addItemListener(this);
				pickRotationCenterButton.setEnabled(false);
		
				//
		
				viewAllButton.setIcon(viewAllIcon);
				add(viewAllButton);
				viewAllButton.addActionListener(this);
		
		add(separator);
		
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

		pickRotationIcon.setImage(pickRotationIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));

		//

		showOrientationAxesIcon.setImage(showOrientationAxesIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
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
	
	List<RenderToolbarListener> listeners=Lists.newArrayList();
	private final JSeparator separator = new JSeparator();
	private final JSeparator separator_1 = new JSeparator();

	public void fire(RenderToolbarEvent event)
	{
		for (int i=0; i<listeners.size(); i++)
			listeners.get(i).handle(event);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getSource() == constrainRotationXButton)
		{
			if (constrainRotationXButton.isSelected())
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this,CartesianAxis.X));
			else
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this,CartesianAxis.NONE));
		}
		else if (e.getSource() == constrainRotationYButton)
		{
			if (constrainRotationYButton.isSelected())
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this,CartesianAxis.Y));
			else
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this,CartesianAxis.NONE));
		}
		else if (e.getSource() == constrainRotationZButton)
		{
			if (constrainRotationZButton.isSelected())
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this,CartesianAxis.Z));
			else
				fire(new RenderToolbarEvent.ConstrainRotationAxisEvent(this,CartesianAxis.NONE));
		}
		else if (e.getSource() == showOrientationAxesToggleButton)
		{
			fire(new RenderToolbarEvent.ToggleAxesVisibilityEvent(this, showOrientationAxesToggleButton.isSelected()));
		}

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
		//else if (e.getSource() == pickRotationCenterButton)
		//	firePropertyChange(pickRotationCenterProperty, null, null);
	}

}
