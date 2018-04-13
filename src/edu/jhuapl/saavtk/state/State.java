package edu.jhuapl.saavtk.state;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;

public final class State implements Attribute
{
	private static final ValueType VALUE_TYPE = () -> {
		return "State";
	};

	public static State of()
	{
		return new State(new TreeMap<>());
	}

	public static State of(Map<StateKey<?>, ? extends Attribute> map)
	{
		Preconditions.checkArgument(!map.containsKey(null));
		Preconditions.checkArgument(!map.containsValue(null));
		return new State(new TreeMap<>(map));
	}

	public static ValueType getValueType()
	{
		return VALUE_TYPE;
	}

	private final SortedMap<StateKey<?>, Attribute> map;

	private State(TreeMap<StateKey<?>, Attribute> map)
	{
		Preconditions.checkNotNull(map);
		this.map = map;
	}

	public <A extends Attribute> A get(StateKey<A> key)
	{
		Preconditions.checkNotNull(key);
		@SuppressWarnings("unchecked")
		A result = (A) map.get(key);
		return result;
	}

	public <A extends Attribute> void put(StateKey<A> key, A state)
	{
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(state);
		map.put(key, state);
	}

	public ImmutableSortedMap<StateKey<?>, Attribute> getMap()
	{
		return ImmutableSortedMap.copyOf(map);
	}

	@Override
	public String toString()
	{
		return "(" + VALUE_TYPE.getId() + ") " + map;
	}

}
