package edu.jhuapl.saavtk.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class Metadata
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

	public static Metadata of(Version version)
	{
		return new Metadata(version);
	}

	private final Version version;
	private final List<Key<?>> keys;
	private final Map<Key<?>, Object> map;

	private Metadata(Version version)
	{
		this.version = version;
		this.keys = new ArrayList<>();
		this.map = new HashMap<>();
	}

	public Version getVersion()
	{
		return version;
	}

	public ImmutableList<Key<?>> getKeys()
	{
		return ImmutableList.copyOf(keys);
	}

	@SuppressWarnings("unchecked")
	public <V> V get(Key<V> key)
	{
		Preconditions.checkNotNull(key);
		Object object = map.get(key);
		if (object == null)
		{
			throw new IllegalArgumentException("Metadata does not contain key " + key);
		}
		if (object == NULL_OBJECT)
		{
			return null;
		}
		return (V) object;
	}

	public <V> Metadata put(Key<V> key, V value)
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

	public void clear()
	{
		keys.clear();
		map.clear();
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
		if (other instanceof Metadata)
		{
			Metadata that = (Metadata) other;
			return this.map.equals(that.map);
		}
		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("(Metadata) version ");
		builder.append(version);
		for (Key<?> key : keys)
		{
			builder.append("\n");
			builder.append(key + " = " + get(key));
		}
		return builder.toString();
	}

}
