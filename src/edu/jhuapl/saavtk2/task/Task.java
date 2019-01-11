package edu.jhuapl.saavtk2.task;

import edu.jhuapl.saavtk2.event.EventSource;

public interface Task extends EventSource, Runnable {
	public void progress(double progress);
	public void finished();
	public void failed(String reason);
	public void paused();
	public void canceled(); 
	public void start();
	public String getDisplayName();
}
