package edu.jhuapl.saavtk.state;

public abstract class TrackedStateManager implements StateManager
{
	protected TrackedStateManager(StateKey<State> key, StateManagerCollection collection)
	{
		collection.add(key, this);
	}

}
