package edu.jhuapl.saavtk.metadata.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.metadata.api.Key;
import edu.jhuapl.saavtk.metadata.api.Metadata;
import edu.jhuapl.saavtk.metadata.api.StorableAsMetadata;
import edu.jhuapl.saavtk.metadata.api.Version;

public class SettableMetadata extends AbstractMetadata
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

	protected SettableMetadata(Version version, List<Key<?>> keys, Map<Key<?>, Object> map)
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

	@Override
	public ImmutableMap<Key<?>, Object> getMap()
	{
		return ImmutableMap.copyOf(map);
	}

	public final <V> SettableMetadata put(Key<V> key, V value)
	{
		Preconditions.checkNotNull(key);
		validate(value);
		if (!hasKey(key))
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

	protected void validate(Object object)
	{
		if (object == null)
			return;
		if (object instanceof Key)
			return;
		if (object instanceof Metadata)
			return;
		if (object instanceof StorableAsMetadata)
			return;
		if (object instanceof Version)
			return;
		if (object instanceof List)
		{
			validateIterable((Iterable<?>) object);
			return;
		}
		if (object instanceof Map)
		{
			validateMap((Map<?, ?>) object);
			return;
		}
		if (object instanceof Set)
		{
			validateIterable((Iterable<?>) object);
			return;
		}
		if (object instanceof String)
			return;
		if (object instanceof Character)
			return;
		if (object instanceof Boolean)
			return;
		if (object instanceof Double)
			return;
		if (object instanceof Float)
			return;
		if (object instanceof Integer)
			return;
		if (object instanceof Long)
			return;
		if (object instanceof Short)
			return;
		if (object instanceof Byte)
			return;
		if (object instanceof Date)
			return;
		if (object instanceof String[])
			return;
		if (object instanceof Character[])
			return;
		if (object instanceof Boolean[])
			return;
		if (object instanceof Double[])
			return;
		if (object instanceof Float[])
			return;
		if (object instanceof Integer[])
			return;
		if (object instanceof Long[])
			return;
		if (object instanceof Short[])
			return;
		if (object instanceof Byte[])
			return;
		if (object instanceof Date[])
			return;
		if (object instanceof Metadata[])
			return;
		if (object instanceof char[])
			return;
		if (object instanceof boolean[])
			return;
		if (object instanceof double[])
			return;
		if (object instanceof float[])
			return;
		if (object instanceof int[])
			return;
		if (object instanceof long[])
			return;
		if (object instanceof short[])
			return;
		if (object instanceof byte[])
			return;
		throw new IllegalArgumentException("Cannot directly represent objects of type " + object.getClass().getSimpleName() + " as metadata");
	}

	protected void validateIterable(Iterable<?> iterable)
	{
		for (Object item : iterable)
		{
			validate(item);
		}
	}

	protected void validateMap(Map<?, ?> map)
	{
		for (Entry<?, ?> entry : map.entrySet())
		{
			validate(entry.getKey());
			validate(entry.getValue());
		}
	}

}
