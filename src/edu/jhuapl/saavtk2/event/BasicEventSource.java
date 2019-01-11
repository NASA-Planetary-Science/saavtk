package edu.jhuapl.saavtk2.event;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class BasicEventSource implements EventSource {

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
        {
            listeners.get(i).handle(e);
        }
    }
}
