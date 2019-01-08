package edu.jhuapl.saavtk2.task;

import edu.jhuapl.saavtk2.event.BasicEvents.StringEvent;

public class TaskFailedEvent extends StringEvent {

	public TaskFailedEvent(Task source, String reason) {
		super(source, reason);
		// TODO Auto-generated constructor stub
	}

}
