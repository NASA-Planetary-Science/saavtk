package edu.jhuapl.saavtk.gui.event;

import java.util.List;

import com.google.common.collect.Lists;

public class EventSource
{
    List<EventListener> listeners=Lists.newArrayList();

    public void addListener(EventListener l)
    {
        listeners.add(l);
    }

    public void removeListener(EventListener l)
    {
        listeners.remove(l);
    }

    public void fire(Event e)
    {
        for (int i=0; i<listeners.size(); i++)
            listeners.get(i).handle(e);
    }
}
