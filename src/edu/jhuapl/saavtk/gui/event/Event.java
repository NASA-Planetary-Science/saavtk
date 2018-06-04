package edu.jhuapl.saavtk.gui.event;

public interface Event<T>
{
	
	public EventSource getSource();
	public T getValue();


}
