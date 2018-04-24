package edu.jhuapl.saavtk.state;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

public class StateManagerCollection implements StateManager
{
	public static StateManagerCollection of()
	{
		return new StateManagerCollection();
	}

	private final List<StateKey<State>> keysInOrder;
	private final SortedMap<StateKey<State>, StateManager> managers;

	protected StateManagerCollection()
	{
		this.keysInOrder = new ArrayList<>();
		this.managers = new TreeMap<>();
	}

	public void add(StateKey<State> key, StateManager manager)
	{
		if (!managers.containsKey(key))
		{
			keysInOrder.add(key);
		}
		managers.put(key, manager);
	}

	@Override
	public State getState()
	{
		State state = State.of();
		for (StateKey<State> key : keysInOrder)
		{
			state.put(key, managers.get(key).getState());
		}
		return state;
	}

	@Override
	public void setState(State state)
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			for (StateKey<State> key : keysInOrder)
			{
				State subState = state.get(key);
				if (subState != null)
				{
					try
					{
						SwingUtilities.invokeAndWait(() -> {

							managers.get(key).setState(subState);
						});
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
		executor.shutdown();
	}

}
