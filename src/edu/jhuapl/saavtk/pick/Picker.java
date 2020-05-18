package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * The Picker class responds to (mouse / keyboard) events on the renderer. There
 * can be more than 1 picker active at any given time.
 * <P>
 * The PickManager class is responsible for managing all the Pickers.
 */
public abstract class Picker implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	// Constants
	public static final double DEFAULT_PICK_TOLERANCE = 0.002;
	public static final double MINIMUM_PICK_TOLERANCE = 0.0002;
	public static final double MAXIMUM_PICK_TOLERANCE = 0.005;

	// State vars
	private double pickTolerance = DEFAULT_PICK_TOLERANCE;

	/**
	 * Method which returns the appropriate cursor to be used by this Picker.
	 * <P>
	 * The returned value may differ over time based on the state of the Picker.
	 */
	public int getCursorType()
	{
		return Cursor.DEFAULT_CURSOR;
	}

	public double getTolerance()
	{
		return pickTolerance;
	}

	public void setTolerance(double aPickTolerance)
	{
		pickTolerance = aPickTolerance;
	}

	/**
	 * Method that returns true whenever a Picker wants to have exclusive control of
	 * the mouse events.
	 * <P>
	 * This typically occurs during some mutation action where if the ShapeModel was
	 * mutated at the same time then the user's current edit would be messed up.
	 */
	public boolean isExclusiveMode()
	{
		return true;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{}

	@Override
	public void mouseEntered(MouseEvent e)
	{}

	@Override
	public void mouseExited(MouseEvent e)
	{}

	@Override
	public void mousePressed(MouseEvent e)
	{}

	@Override
	public void mouseReleased(MouseEvent e)
	{}

	@Override
	public void mouseDragged(MouseEvent e)
	{}

	@Override
	public void mouseMoved(MouseEvent e)
	{}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{}

	@Override
	public void keyTyped(KeyEvent e)
	{}

	@Override
	public void keyPressed(KeyEvent e)
	{}

	@Override
	public void keyReleased(KeyEvent e)
	{}

}
