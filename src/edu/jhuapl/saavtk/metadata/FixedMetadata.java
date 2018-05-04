package edu.jhuapl.saavtk.metadata;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public final class FixedMetadata implements MetadataInterface
{
	public static MetadataInterface of(Metadata metadata)
	{
		Version version = metadata.getVersion();
		ImmutableList<Key<?>> keys = metadata.getKeys();
		ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
		for (Key<?> key : keys)
		{
			builder.put(key, metadata.get(key));
		}
		return new FixedMetadata(version, keys, builder.build());
	}

	private final Version version;
	private final ImmutableList<Key<?>> keys;
	private final ImmutableMap<Key<?>, Object> map;

	private FixedMetadata(Version version, ImmutableList<Key<?>> keys, ImmutableMap<Key<?>, Object> map)
	{
		this.version = version;
		this.keys = keys;
		this.map = map;
	}

	@Override
	public Version getVersion()
	{
		return version;
	}

	@Override
	public ImmutableList<Key<?>> getKeys()
	{
		return keys;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V get(Key<V> key)
	{
		Preconditions.checkNotNull(key);
		Object object = map.get(key);
		if (object == null)
		{
			throw new IllegalArgumentException("FixedMetadata does not contain key " + key);
		}
		if (Metadata.isNullObject(object))
		{
			return null;
		}
		return (V) object;
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
		if (other instanceof FixedMetadata)
		{
			FixedMetadata that = (FixedMetadata) other;
			return this.map.equals(that.map);
		}
		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("(FixedMetadata) version ");
		builder.append(getVersion());
		for (Key<?> key : getKeys())
		{
			builder.append("\n");
			builder.append(key + " = " + get(key));
		}
		return builder.toString();
	}

}
