package edu.jhuapl.saavtk2.event;

public interface EventSource
{
    public void addListener(EventListener l);
    public void removeListener(EventListener l);
    public void fire(Event e);
}
