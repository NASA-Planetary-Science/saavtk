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
		return new SettableMetadata(version);
	}

	private final List<Key<?>> keys;
	private final Map<Key<?>, Object> map;

	private SettableMetadata(Version version)
	{
		super(version);
		this.keys = new ArrayList<>();
		this.map = new HashMap<>();
	}

	@Override
	public ImmutableList<Key<?>> getKeys()
	{
		return ImmutableList.copyOf(keys);
	}

	@Override
	protected Map<Key<?>, Object> getMap()
	{
		return map;
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
			setNullObject(key, map);
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

}
