package edu.jhuapl.saavtk2.task;

import edu.jhuapl.saavtk2.event.BasicEventSource;

public abstract class BasicTask extends BasicEventSource implements Task {


	@Override
	public void paused() {
		fire(new TaskPausedEvent(this));
	}

	@Override
	public void canceled() {
		fire(new TaskCanceledEvent(this));
	}
	
	@Override
	public void progress(double progressPercent) {
		fire(new TaskProgressEvent(this, progressPercent));
	}

	@Override
	public void finished() {
		fire(new TaskFinishedEvent(this));
	}
	
	@Override
	public void failed(String reason) {
		fire(new TaskFailedEvent(this, reason));
	}
	
	@Override
	public void start() {
		fire(new TaskStartedEvent(this));
	}
}
