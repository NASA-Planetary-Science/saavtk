package edu.jhuapl.saavtk.structure.gui.load;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import glum.task.SilentTask;
import glum.unit.NumberUnit;
import glum.unit.TimeCountUnit;

/**
 * Task that provides update progress for the structure load panel.
 *
 * @author lopeznr1
 */
class LoadTask extends SilentTask
{
	// Ref vars
	private final JLabel refInfoL;

	// State vars
	private long begTime;
	private long endTime;
	private boolean isAborted;
	private boolean isInit;

	/**
	 * Standard Constructor
	 */
	LoadTask(JLabel aInfoL)
	{
		refInfoL = aInfoL;

		begTime = 0L;
		endTime = 0L;
		isAborted = false;
		isInit = true;
	}

	/**
	 * Forces an update to our reference UI element.
	 */
	public void forceUpdate()
	{
		if (isActive() == true)
			endTime = System.currentTimeMillis();
		String tmpMsg = formInfoMsg();
		refInfoL.setText(tmpMsg);
	}

	/**
	 * Returns true if the task has been aborted.
	 */
	public boolean isAborted()
	{
		return isAborted;
	}

	/**
	 * Return true if the task has reached completion or has been aborted.
	 */
	public boolean isDone()
	{
		if (isAborted() == true)
			return true;

		if (getProgress() >= 1.0)
			return true;

		return false;
	}

	/**
	 * Return true if the task has not left the initial state.
	 * <P>
	 * A task should be considered in the initial state if the progress has not
	 * moved past 0% and / or if it has not been manually transitioned out.
	 */
	public boolean isInit()
	{
		return isInit;
	}

	/**
	 * Moves the task from the init state to the in-progress state
	 */
	public void markInitDone()
	{
		isInit = false;
	}

	@Override
	public void abort()
	{
		super.abort();

		isInit = false;
		isAborted = true;
		SwingUtilities.invokeLater(() -> forceUpdate());
	}

	@Override
	public void reset()
	{
		super.reset();

		begTime = System.currentTimeMillis();
		endTime = 0L;
		isAborted = false;
		isInit = true;
	}

	@Override
	public void setProgress(double aProgress)
	{
		super.setProgress(aProgress);
		isInit = false;

		// Update UI at ~20 times per second
		long currTime = System.currentTimeMillis();
		if (currTime < endTime + 47)
			return;

		SwingUtilities.invokeLater(() -> forceUpdate());
	}

	/**
	 * Helper method that returns the appropriate info string
	 */
	private String formInfoMsg()
	{
		// Display nothing if the task is in the init state
		if (isInit() == true)
			return "";

		double progress = getProgress();
		NumberUnit perU = new NumberUnit("", "", 1.0, "0.00 %");
		String retStr = "Progress: " + perU.getString(progress);

		Long runTime = null;
		if (isActive() == true || progress >= 0)
			runTime = endTime - begTime;
		TimeCountUnit timeU = new TimeCountUnit(2);
		retStr += "    Time: " + timeU.getString(runTime);

		return retStr;
	}

}
