package edu.jhuapl.saavtk.state;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class StateManagerCollection
{
	public static StateManagerCollection of()
	{
		return new StateManagerCollection();
	}

	private final List<StateKey<State>> keysInOrder;
	private final SortedMap<StateKey<State>, StateManager> managers;

	private StateManagerCollection()
	{
		this.keysInOrder = new ArrayList<>();
		this.managers = new TreeMap<>();
	}

	/**
	 * Add a manager to this collection of managers. The manager will be associated
	 * with States identified by the supplied key.
	 * 
	 * @param key the key to State objects this manager will manage
	 * @param manager the manager
	 * @throws IllegalStateException if there is already a manager associated with
	 *             the supplied key
	 * @throws NullPointerException if any argument is null
	 */
	public void add(StateKey<State> key, StateManager manager)
	{
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(manager);
		Preconditions.checkState(!managers.containsKey(key));

		keysInOrder.add(key);
		managers.put(key, manager);
	}

	public ImmutableList<StateKey<State>> getKeys()
	{
		return ImmutableList.copyOf(keysInOrder);
	}

	public StateManager getManager(StateKey<State> key)
	{
		Preconditions.checkNotNull(key);
		Preconditions.checkArgument(managers.containsKey(key));
		return managers.get(key);
	}

	//	public void store(StateSerializer serializer, State destination)
	//	{
	//		Preconditions.checkNotNull(serializer);
	//		Preconditions.checkNotNull(destination);
	//		for (StateKey<State> key : keysInOrder)
	//		{
	//			// Create a new element to hold each manager's info.
	//			State element = State.of();
	//
	//			// Have each manager store its info.
	//			StateManager manager = managers.get(key);
	//			manager.store(element);
	//
	//			// Add element's information to the destination.
	//			destination.put(key, element);
	//		}
	//	}
	//
	//	public void retrieve(State source, Version version)
	//	{
	//		Preconditions.checkNotNull(source);
	//		if (SwingUtilities.isEventDispatchThread())
	//		{
	//			retrieveInSwingContext(source);
	//		}
	//		else
	//		{
	//			retrieveInSingleThread(source);
	//		}
	//	}
	//
	//	private void retrieveInSwingContext(State source)
	//	{
	//		ExecutorService executor = Executors.newSingleThreadExecutor();
	//		executor.execute(() -> {
	//			try
	//			{
	//				for (StateKey<State> key : keysInOrder)
	//				{
	//					State element = source.get(key);
	//					if (element != null)
	//					{
	//						Version elementVersion = element.get(versionKey);
	//						SwingUtilities.invokeAndWait(() -> {
	//							managers.get(key).retrieve(element, elementVersion);
	//						});
	//					}
	//				}
	//			}
	//			catch (Exception e)
	//			{
	//				e.printStackTrace();
	//			}
	//		});
	//		executor.shutdown();
	//	}
	//
	//	private void retrieveInSingleThread(State source)
	//	{
	//		for (StateKey<State> key : keysInOrder)
	//		{
	//			State element = source.get(key);
	//			if (element != null)
	//			{
	//				Version elementVersion = element.get(versionKey);
	//				managers.get(key).retrieve(element, elementVersion);
	//			}
	//		}
	//	}

}
