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

	public <V> V get(StateKey<V> key)
	{
		Preconditions.checkNotNull(key);
		Class<V> valueClass = key.getValueClass();
		return convert(getObject(key), valueClass);
	}

	public <V> void put(StateKey<V> key, V value)
	{
		putObject(key, value);
	}

}
