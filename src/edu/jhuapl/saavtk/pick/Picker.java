package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

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

	public double getPickTolerance()
	{
		return pickTolerance;
	}

	public void setPickTolerance(double pickTolerance)
	{
		this.pickTolerance = pickTolerance;
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

	/*
	 * protected int doPick(MouseEvent e, vtkCellPicker picker, vtksbmtJoglCanvas
	 * renWin) { if (pickingEnabled == false) return 0;
	 * 
	 * // Don't do a pick if the event is more than a third of a second old final
	 * long currentTime = System.currentTimeMillis(); final long when = e.getWhen();
	 * 
	 * //System.err.println("elapsed time " + (currentTime - when)); if (currentTime
	 * - when > 333) return 0;
	 * 
	 * // When picking, choosing the right tolerance is not simple. If it's too
	 * small, then // the pick will only work well if we are zoomed in very close to
	 * the object. If it's // too large, the pick will only work well when we are
	 * zoomed out a lot. To deal // with this situation, do a series of picks
	 * starting out with a low tolerance // and increase the tolerance after each
	 * new pick. Stop as soon as the pick succeeds // or we reach the maximum
	 * tolerance.
	 * 
	 * int pickSucceeded = 0; double tolerance = 0.0002; final double
	 * originalTolerance = picker.GetTolerance(); final double maxTolerance = 0.004;
	 * final double incr = 0.0002; renWin.lock(); picker.SetTolerance(tolerance);
	 * while (tolerance <= maxTolerance) { picker.SetTolerance(tolerance);
	 * 
	 * pickSucceeded = picker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0,
	 * renWin.GetRenderer());
	 * 
	 * if (pickSucceeded == 1) break;
	 * 
	 * tolerance += incr; } picker.SetTolerance(originalTolerance); renWin.unlock();
	 * 
	 * return pickSucceeded; }
	 */

	protected int doPick(MouseEvent e, vtkCellPicker picker, vtkJoglPanelComponent renWin)
	{
		return doPick(e.getWhen(), e.getX(), e.getY(), picker, renWin);
	}

	protected int doPick(final long when, int x, int y, vtkCellPicker picker, vtkJoglPanelComponent renWin)
	{
		if (PickUtil.pickingEnabled == false)
			return 0;

		// 2018-04-30 Peachey. Commenting out the elapsed time check below for now to address redmine #1165.
		// In that issue, a user was unable to create stuctures when using very high resolution
		// shape models while images are mapped. Debugging revealed that the time lag computed below
		// in such cases was greater than the 333 ms threshold (frequently much greater), which led doPick
		// to return 0 (failure), causing the application in turn to ignore mouse clicks.
		//
		// Before commenting out, I traced this change all the way back to a very early version of Picker
		// while it was still in sbmt:
		//
		//     commit e008025eb0dc574305f828680b1cbb9f98a460ab
		//     Author: Eli Kahn <eliezer.kahn@jhuapl.edu> 2010-11-29 18:06:04
		//
		// The commit message suggests this *may* have been to prevent unmapped images from continuing
		// to respond to events. However it's not clear whether this check is now necessary (taking it out
		// seems to make no difference in anything else I tested).

		//		// Don't do a pick if the event is more than a third of a second old
		//		final long currentTime = System.currentTimeMillis();
		//
		//		//System.err.println("elapsed time " + (currentTime - when));
		//		if (currentTime - when > 333)
		//			return 0;

		renWin.getVTKLock().lock();

		picker.SetTolerance(pickTolerance);

		// Note that on some displays, such as a retina display, the height used by
		// OpenGL is different than the height used by Java. Therefore we need
		// scale the mouse coordinates to get the right position for OpenGL.
		//       double openGlHeight = renWin.getComponent().getSurfaceHeight();
		//        double openGlHeight = renWin.getComponent().getHeight();
		double javaHeight = renWin.getComponent().getHeight();
		//        double scale = openGlHeight / javaHeight;
		double scale = 1.0;
		int pickSucceeded = picker.Pick(scale * x, scale * (javaHeight - y - 1), 0.0, renWin.getRenderer());

		renWin.getVTKLock().unlock();

		return pickSucceeded;
	}

}
