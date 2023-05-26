package edu.jhuapl.saavtk.animator;

/**
 * Runnable subclass that can act like a completion block once the animator is done with a single frame
 * @author steelrj1
 *
 */
public abstract class AnimatorFrameRunnable implements Runnable
{
	/**
	 * The AnimationFrame currently being processed
	 */
	AnimationFrame frame;

	/**
	 * Default Constructor
	 */
	public AnimatorFrameRunnable()
	{

	}

	/**
	 * Run method that allows setting of the frame.  Usually, in this method definition,
	 * the overriden run() method from the interface will be called.
	 * @param frame
	 */
	public void run(AnimationFrame frame)
	{
		this.frame = frame;
	}

	/**
	 * Returns the AnimationFrame for this runnable
	 * @return
	 */
	public AnimationFrame getFrame()
	{
		return frame;
	}
}