package edu.jhuapl.saavtk2.mappable;

import edu.jhuapl.saavtk2.event.Event;

public abstract class AbstractMappable implements Mappable {

	@Override
	public void show(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handle(Event event) {
		if (event instanceof MappableEvent)
			map((Boolean)event.getValue());
		else if (event instanceof ShowableEvent)
			show((Boolean)event.getValue());
	}

	@Override
	public void map(boolean map) {
		// TODO Auto-generated method stub
		
	}

}
