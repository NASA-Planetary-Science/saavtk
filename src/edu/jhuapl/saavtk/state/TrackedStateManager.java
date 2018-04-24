package edu.jhuapl.saavtk.state;

public abstract class TrackedStateManager implements StateManager
{
	private final StateKey<State> key;
	private final StateSerializer serializer;
	private boolean registered;

	protected TrackedStateManager(StateKey<State> key)
	{
		this(key, Serializers.getDefault());
	}

	protected TrackedStateManager(StateKey<State> key, StateSerializer serializer)
	{
		this.key = key;
		this.serializer = serializer;
		this.registered = false;
	}

	public abstract State doGetState();

	public abstract void doSetState(State state);

	@Override
	public final State getState()
	{
		registerOnce();
		return doGetState();
	}

	@Override
	public final void setState(State state)
	{
		registerOnce();
		try
		{
			doSetState(state);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public final void registerOnce()
	{
		if (!registered)
		{
			serializer.register(key, this);
			registered = true;
		}
	}
}
