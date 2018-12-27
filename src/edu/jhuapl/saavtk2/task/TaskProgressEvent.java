package edu.jhuapl.saavtk2.task;

import edu.jhuapl.saavtk2.event.BasicEvent;

public class TaskProgressEvent extends BasicEvent<Double> {

	public TaskProgressEvent(Task source, double progressPercent) {
		super(source, progressPercent);
	}

	
}
