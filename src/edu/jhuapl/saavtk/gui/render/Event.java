package edu.jhuapl.saavtk.gui.render;

public class Event<S>
{
	S source;
	
	public Event(S source)
	{
		this.source=source;
	}
	
	public S getSource()
	{
		return source;
	}
}
