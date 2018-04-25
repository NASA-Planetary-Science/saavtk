package edu.jhuapl.saavtk.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class State
{
	public static final class InaccurateConversionException extends RuntimeException
	{
		private static final long serialVersionUID = 1332273620795657017L;

		public InaccurateConversionException(String msg)
		{
			super(msg);
		}
	}

	private static final Object NULL_OBJECT = new Object() {
		@Override
		public String toString()
		{
			// Deliberately capitalizing this so that the astute debugger has a chance of
			// noticing that this object is not actually a null pointer.
			return "Null";
		}
	};

	public static State of()
	{
		return new State();
	}

	private final List<StateKey<?>> keys;
	private final Map<StateKey<?>, Object> map;

	private State()
	{
		this.keys = new ArrayList<>();
		this.map = new HashMap<>();
	}

	public ImmutableList<StateKey<?>> getKeys()
	{
		return ImmutableList.copyOf(keys);
	}

	@SuppressWarnings("unchecked")
	public <V> V get(StateKey<V> key)
	{
		Preconditions.checkNotNull(key);
		Object object = map.get(key);
		if (object == null)
		{
			throw new IllegalArgumentException("State does not contain key " + key);
		}
		if (object == NULL_OBJECT)
		{
			return null;
		}
		return (V) object;
	}

	public <V> State put(StateKey<V> key, V value)
	{
		Preconditions.checkNotNull(key);
		if (!map.containsKey(key))
		{
			keys.add(key);
		}
		if (value == null)
		{
			map.put(key, NULL_OBJECT);
		}
		else
		{
			map.put(key, value);
		}
		return this;
	}

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + map.hashCode();
		return result;
	}

	@Override
	public final boolean equals(Object other)
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
		StringBuilder builder = new StringBuilder("(State):");
		for (StateKey<?> key : new TreeSet<>(map.keySet()))
		{
			builder.append("\n");
			builder.append(key + " = " + get(key));
		}
		return builder.toString();
	}

}
