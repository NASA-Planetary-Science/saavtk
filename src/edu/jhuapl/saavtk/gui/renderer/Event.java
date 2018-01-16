package edu.jhuapl.saavtk.gui.renderer;

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
