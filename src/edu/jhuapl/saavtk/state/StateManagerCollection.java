package edu.jhuapl.saavtk.state;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class StateManagerCollection implements StateManager
{
	private final SortedMap<StateKey<State>, StateManager> managers;

	protected StateManagerCollection()
	{
		this.managers = new TreeMap<>();
	}

	public void add(StateKey<State> key, StateManager manager)
	{
		managers.put(key, manager);
	}

	@Override
	public State getState()
	{
		State state = State.of();
		for (Entry<StateKey<State>, StateManager> entry : managers.entrySet())
		{
			state.put(entry.getKey(), entry.getValue().getState());
		}
		return state;
	}

	@Override
	public void setState(State state)
	{
		for (Entry<StateKey<State>, StateManager> entry : managers.entrySet())
		{
			StateKey<State> key = entry.getKey();
			State subState = state.get(key);
			if (subState != null)
			{
				entry.getValue().setState(subState);
			}
		}
	}

}
