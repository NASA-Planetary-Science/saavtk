package edu.jhuapl.saavtk.pick;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;

/**
 * A MouseInputListener that is used to wrap another MouseInputListener so that
 * the "drag" sensitivity can be configured.
 * <P>
 * Inspiration:
 * https://stackoverflow.com/questions/522244/making-a-component-less-sensitive-to-dragging-in-swing/19003495#19003495
 */
public class DragSensitiveListener implements MouseInputListener
{
	// Attributes
	private final MouseInputListener refTarget;
	private final int maxClickDistSqr;
	private final long maxClickTimeMS;

	// State vars
	private MouseEvent pressedEvent;

	/**
	 * Constructor
	 * 
	 * @param aTarget           The target MouseInputListener that will be wrapped.
	 * @param aMaxClickDistance A value in pixels that provides a sensitivity
	 *                          threshold to allow differentiation between mouse
	 *                          clicks and mouse drags. In order for mouse input to
	 *                          be registered as a click the mouse movement must
	 *                          occur within this distance.
	 * @param aMaxClickTimeMS   A value in milliseconds that provides a sensitivity
	 *                          threshold to allow differentiation between mouse
	 *                          clicks and mouse drags. In order for mouse input to
	 *                          be registered as a click the mouse must be pressed
	 *                          and released within this time frame. If value is
	 *                          zero or negative then this threshold will be
	 *                          ignored.
	 */
	public DragSensitiveListener(MouseInputListener aTarget, int aMaxClickDistance, long aMaxClickTimeMS)
	{
		refTarget = aTarget;
		maxClickDistSqr = aMaxClickDistance * aMaxClickDistance;
		maxClickTimeMS = aMaxClickTimeMS;
	}

	@Override
	public final void mousePressed(MouseEvent aEvent)
	{
		pressedEvent = aEvent;
		refTarget.mousePressed(aEvent);
	}

	@Override
	public final void mouseReleased(MouseEvent aEvent)
	{
		refTarget.mouseReleased(aEvent);

		// If the click threshold has been violated then bail
		if (pressedEvent == null || isClickThresholdViolated(aEvent) == true)
		{
			pressedEvent = null;
			return;
		}

		// Fire off the click event
		MouseEvent clickEvent = new MouseEvent((Component) pressedEvent.getSource(), MouseEvent.MOUSE_CLICKED,
				aEvent.getWhen(), pressedEvent.getModifiers(), pressedEvent.getX(), pressedEvent.getY(),
				pressedEvent.getXOnScreen(), pressedEvent.getYOnScreen(), pressedEvent.getClickCount(),
				pressedEvent.isPopupTrigger(), pressedEvent.getButton());

		pressedEvent = null;
		refTarget.mouseClicked(clickEvent);
	}

	@Override
	public void mouseClicked(MouseEvent aEvent)
	{
		// Do nothing - if necessary will be handled by mouseReleased()
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Ignore drag events if the click threshold has not been violated
		if (pressedEvent != null && isClickThresholdViolated(aEvent) == false)
			return;

		pressedEvent = null;
		refTarget.mouseDragged(aEvent);
	}

	@Override
	public void mouseEntered(MouseEvent aEvent)
	{
		refTarget.mouseEntered(aEvent);
	}

	@Override
	public void mouseExited(MouseEvent aEvent)
	{
		refTarget.mouseExited(aEvent);
	}

	@Override
	public void mouseMoved(MouseEvent aEvent)
	{
		refTarget.mouseMoved(aEvent);
	}

	/**
	 * Helper method to determine if the click threshold has been violated
	 */
	private boolean isClickThresholdViolated(MouseEvent aEvent)
	{
		// Check the time threshold
		long diffTimeMS = aEvent.getWhen() - pressedEvent.getWhen();
		if (maxClickTimeMS > 0 && diffTimeMS > maxClickTimeMS)
			return true;

		// Check the distance threshold
		int deltaX = Math.abs(pressedEvent.getXOnScreen() - aEvent.getXOnScreen());
		int deltaY = Math.abs(pressedEvent.getYOnScreen() - aEvent.getYOnScreen());
		double diffDistSqr = (deltaX * deltaX) + (deltaY * deltaY);
		if (diffDistSqr > maxClickDistSqr)
			return true;

		return false;
	}

}
