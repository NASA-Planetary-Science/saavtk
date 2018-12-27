package edu.jhuapl.saavtk2.mappable;

import edu.jhuapl.saavtk2.event.EventListener;

public interface Mappable extends Showable, EventListener {
	public void map(boolean map);
}
