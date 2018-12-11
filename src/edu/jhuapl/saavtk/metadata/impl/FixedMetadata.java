package edu.jhuapl.saavtk.metadata.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.metadata.api.Key;
import edu.jhuapl.saavtk.metadata.api.Metadata;
import edu.jhuapl.saavtk.metadata.api.Version;

public class FixedMetadata extends BasicMetadata
{
	public static FixedMetadata of(Metadata metadata)
	{
		Preconditions.checkNotNull(metadata);

		if (metadata instanceof FixedMetadata)
		{
			return (FixedMetadata) metadata;
		}

		Version version = metadata.getVersion();
		ImmutableList<Key<?>> keys = ImmutableList.copyOf(metadata.getKeys());

		ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
		for (Key<?> key : keys)
		{
			Object object = metadata.get(key);
			if (object == null)
			{
				object = getNullObject();
			}
			builder.put(key, object);
		}

		return new FixedMetadata(version, keys, builder.build());
	}

	private final ImmutableList<Key<?>> keys;
	private final ImmutableMap<Key<?>, Object> map;

	protected FixedMetadata(Version version, ImmutableList<Key<?>> keys, ImmutableMap<Key<?>, Object> map)
	{
		super(version);
		this.keys = keys;
		this.map = map;
	}

	@Override
	public ImmutableList<Key<?>> getKeys()
	{
		return keys;
	}

	@Override
	public FixedMetadata copy()
	{
		return this;
	}

	@Override
	public ImmutableMap<Key<?>, Object> getMap()
	{
		return map;
	}
}
