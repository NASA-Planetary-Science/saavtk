package edu.jhuapl.saavtk.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.util.DownloadableFileManager.StateListener;

public class FileStateListenerTracker
{
    public static FileStateListenerTracker of(DownloadableFileManager manager)
    {
        return new FileStateListenerTracker(manager);
    }

    private final DownloadableFileManager manager;
    private final Map<String, Set<StateListener>> stateListeners;

    protected FileStateListenerTracker(DownloadableFileManager manager)
    {
        this.manager = manager;
        this.stateListeners = new HashMap<>();
    }

    public void addStateChangeListener(String urlString, StateListener listener)
    {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkNotNull(listener);

        synchronized (this.stateListeners)
        {
            Set<StateListener> urlListeners = stateListeners.get(urlString);
            if (urlListeners == null)
            {
                urlListeners = new HashSet<>();
                stateListeners.put(urlString, urlListeners);
            }

            if (!urlListeners.contains(listener))
            {
                urlListeners.add(listener);
                manager.addStateListener(urlString, listener);
            }
        }
    }

    public void removeStateChangeListeners(String urlString)
    {
        Preconditions.checkNotNull(urlString);

        synchronized (this.stateListeners)
        {
            Set<StateListener> urlListeners = stateListeners.get(urlString);
            if (urlListeners != null)
            {
                for (StateListener listener : urlListeners)
                {
                    manager.removeStateListener(urlString, listener);
                }
                stateListeners.remove(urlString);
            }
        }
    }

    public void removeAllStateChangeListeners()
    {
        synchronized (this.stateListeners)
        {
            for (String urlString : ImmutableSet.copyOf(stateListeners.keySet()))
            {
                removeStateChangeListeners(urlString);
            }
        }
    }
}
