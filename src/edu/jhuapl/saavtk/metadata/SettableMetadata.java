package edu.jhuapl.saavtk.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class SettableMetadata extends BasicMetadata
{
	public static SettableMetadata of(Version version)
	{
		return new SettableMetadata(version, new ArrayList<>(), new HashMap<>());
	}

	public static SettableMetadata of(Metadata metadata)
	{
		Preconditions.checkNotNull(metadata);
		SettableMetadata result = SettableMetadata.of(metadata.getVersion());
		for (Key<?> key : metadata.getKeys())
		{
			@SuppressWarnings("unchecked")
			Key<Object> objectKey = (Key<Object>) key;
			result.put(objectKey, metadata.get(key));
		}
		return result;
	}

	private final List<Key<?>> keys;
	private final Map<Key<?>, Object> map;

	private SettableMetadata(Version version, List<Key<?>> keys, Map<Key<?>, Object> map)
	{
		super(version);
		Preconditions.checkNotNull(keys);
		Preconditions.checkNotNull(map);
		this.keys = keys;
		this.map = map;
	}

	@Override
	public ImmutableList<Key<?>> getKeys()
	{
		return ImmutableList.copyOf(keys);
	}

	@Override
	public SettableMetadata copy()
	{
		return new SettableMetadata(getVersion(), new ArrayList<>(keys), new HashMap<>(map));
	}

	public <V> SettableMetadata put(Key<V> key, V value)
	{
		Preconditions.checkNotNull(key);
		if (!map.containsKey(key))
		{
			keys.add(key);
		}
		if (value == null)
		{
			map.put(key, getNullObject());
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
	protected Map<Key<?>, Object> getMap()
	{
		return map;
	}

}
