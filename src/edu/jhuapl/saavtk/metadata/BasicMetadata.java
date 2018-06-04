package edu.jhuapl.saavtk.metadata;

import java.util.Map;

import com.google.common.base.Preconditions;

public abstract class BasicMetadata implements Metadata
{
	private static final Object NULL_OBJECT = new Object() {
		@Override
		public String toString()
		{
			// Deliberately capitalizing this so that the astute debugger has a chance of
			// noticing that this object is not actually a null pointer.
			return "Null";
		}
	};

	private final Version version;

	protected BasicMetadata(Version version)
	{
		Preconditions.checkNotNull(version);
		this.version = version;
	}

	protected abstract Map<Key<?>, Object> getMap();

	@Override
	public final Version getVersion()
	{
		return version;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <V> V get(Key<V> key)
	{
		Preconditions.checkNotNull(key);
		Object object = getMap().get(key);
		if (object == null)
		{
			throw new IllegalArgumentException("FixedMetadata does not contain key " + key);
		}
		if (NULL_OBJECT == object)
		{
			return null;
		}
		return (V) object;
	}

	@Override
	public abstract BasicMetadata copy();

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getMap().hashCode();
		return result;
	}

	@Override
	public final boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof BasicMetadata)
		{
			BasicMetadata that = (BasicMetadata) other;
			return this.getMap().equals(that.getMap());
		}
		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("(Metadata) version ");
		builder.append(getVersion());
		for (Key<?> key : getKeys())
		{
			builder.append("\n");
			builder.append(key + " = " + get(key));
		}
		return builder.toString();
	}

	protected static final void putNullObject(Key<?> key, Map<Key<?>, Object> map)
	{
		map.put(key, NULL_OBJECT);
	}
}
