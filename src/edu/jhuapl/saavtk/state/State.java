package edu.jhuapl.saavtk.state;

import com.google.common.base.Preconditions;

public final class State extends StateBase
{
	public static State of()
	{
		return new State();
	}

	private State()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public <V> V get(StateKey<V> key)
	{
		Preconditions.checkNotNull(key);
		//		return convert(getObject(key), key);
		return (V) getObject(key);
	}

	public <V> void put(StateKey<V> key, V value)
	{
		putObject(key, value);
	}

}
