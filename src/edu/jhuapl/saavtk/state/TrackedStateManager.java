package edu.jhuapl.saavtk.state;

import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;

/**
 * This is a helper implementation designed to wrap another implementation of
 * the {@link StateManager} interface.
 */
public final class TrackedStateManager implements StateManager
{
	public static TrackedStateManager of(String stateId)
	{
		return new TrackedStateManager(stateId, Serializers.getDefault());
	}

	private static final SortedSet<String> STATE_MANAGER_IDENTIFIERS = new TreeSet<>();
	private final StateKey<State> stateKey;
	private final StateSerializer serializer;
	private StateManager manager;

	/**
	 * Construct a new manager that will use the provided serializer. The
	 * identifying string provided must be unique within the currently running
	 * application. Note that the new manager is not added to the serializer by this
	 * constructor. That will be done when another manager is registered with this
	 * manager.
	 * 
	 * @param stateId string used to idenfity this manager within the serializer
	 * @param serializer the serializer
	 * @throws IllegalStateException if the serializer already has a state manager
	 *             associated with the provided string
	 * @throws NullPointerException if any argument is null
	 */
	private TrackedStateManager(String stateId, StateSerializer serializer)
	{
		Preconditions.checkNotNull(stateId);
		Preconditions.checkNotNull(serializer);
		Preconditions.checkState(!STATE_MANAGER_IDENTIFIERS.contains(stateId), "Duplicated state manager identifier " + stateId);
		STATE_MANAGER_IDENTIFIERS.add(stateId);
		this.stateKey = serializer.getKey(stateId);
		this.serializer = serializer;
		this.manager = null;
	}

	/**
	 * Perform all initializations required prior to serializing/deserializing
	 * states managed by this manager. Note that all this manager's functions as a
	 * StateManager work just fine without calling this method. Moreover, it is safe
	 * to call this method multiple times.
	 * 
	 * A separate initialize method is provided so that source objects may be
	 * serialized in the natural order established by the runtime function of the
	 * tool, rather than the order in which source objects were instantiated, which
	 * may not be the same.
	 * 
	 * @throws IllegalStateException if a manager was already registered, or if the
	 *             serializer fails to register this manager cleanly.
	 */
	public void register(StateManager manager)
	{
		Preconditions.checkNotNull(manager);
		Preconditions.checkState(this.manager == null);
		serializer.register(stateKey, this);
		this.manager = manager;
	}

	public boolean isRegistered()
	{
		return manager != null;
	}

	public StateKey<State> getStateKey()
	{
		return stateKey;
	}

	/**
	 * Return a key that is furnished by the serializer, based on the provided
	 * identification string.
	 * 
	 * @param keyId the identification string for the key
	 * @return the key
	 */
	public <T> StateKey<T> getKey(String keyId)
	{
		// Note being registered is not required for this method to work correctly.
		return serializer.getKey(keyId);
	}

	@Override
	public final State store()
	{
		Preconditions.checkState(manager != null);
		return manager.store();
	}

	@Override
	public final void retrieve(State sourceState)
	{
		Preconditions.checkNotNull(sourceState);
		Preconditions.checkState(manager != null);

		manager.retrieve(sourceState);
	}

}
