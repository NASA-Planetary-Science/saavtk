package edu.jhuapl.saavtk.state;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;

public final class State
{
	public static State of()
	{
		return new State(new TreeMap<>());
	}

	public static State of(Map<StateKey<?>, Object> map)
	{
		return new State(new TreeMap<>(map));
	}

	private final SortedMap<StateKey<?>, Object> map;

	private State(TreeMap<StateKey<?>, Object> map)
	{
		Preconditions.checkNotNull(map);
		this.map = map;
	}

	public <V> V get(StateKey<V> key)
	{
		Preconditions.checkNotNull(key);
		@SuppressWarnings("unchecked")
		V result = (V) map.get(key);
		return result;
	}

	public <V> void put(StateKey<V> key, V state)
	{
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(state);
		map.put(key, state);
	}

	public ImmutableSortedMap<StateKey<?>, Object> getMap()
	{
		return ImmutableSortedMap.copyOf(map);
	}

	@Override
	public int hashCode()
	{
		return map.hashCode();
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof State)
		{
			State that = (State) other;
			return this.map.equals(that.map);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "(State) " + map;
	}

}
