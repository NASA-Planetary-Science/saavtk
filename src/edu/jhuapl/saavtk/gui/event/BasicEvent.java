package edu.jhuapl.saavtk.gui.event;

public class BasicEvent<T> implements Event<T>
{
    EventSource source;
    T value;

    public BasicEvent(EventSource source)
    {
    	this.source=source;
    	this.value=null;
    }

    public BasicEvent(EventSource source, T value)
    {
        this.source=source;
        this.value=value;
    }

    public EventSource getSource()
    {
        return source;
    }

    public T getValue()
    {
    	return value;
    }

}
