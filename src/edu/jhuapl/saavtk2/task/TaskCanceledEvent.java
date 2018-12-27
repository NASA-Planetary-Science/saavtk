package edu.jhuapl.saavtk2.task;

import edu.jhuapl.saavtk2.event.BasicEvents.VoidEvent;

public class TaskCanceledEvent extends VoidEvent {

	public TaskCanceledEvent(Task source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

}
