package edu.jhuapl.saavtk.metadata;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class MetadataView implements Metadata
{
	public static MetadataView of(Metadata metadata)
	{
		if (metadata instanceof MetadataView)
		{
			return (MetadataView) metadata;
		}
		return new MetadataView(metadata);
	}

	private final Metadata metadata;

	private MetadataView(Metadata metadata)
	{
		Preconditions.checkNotNull(metadata);
		this.metadata = metadata;
	}

	@Override
	public Version getVersion()
	{
		return metadata.getVersion();
	}

	@Override
	public boolean hasKey(Key<?> key)
	{
		return metadata.hasKey(key);
	}

	@Override
	public ImmutableList<Key<?>> getKeys()
	{
		return ImmutableList.copyOf(metadata.getKeys());
	}

	@Override
	public <V> V get(Key<V> key)
	{
		return metadata.get(key);
	}

	@Override
	public MetadataView copy()
	{
		return this;
	}

	@Override
	public int hashCode()
	{
		return metadata.hashCode();
	}

	@Override
	public boolean equals(Object object)
	{
		if (object == this)
		{
			return true;
		}
		if (object instanceof Metadata)
		{
			return metadata.equals(object);
		}
		return false;
	}
}
