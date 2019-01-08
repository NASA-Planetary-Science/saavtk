package edu.jhuapl.saavtk2.event;

public class BasicEvent<T> implements Event
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
