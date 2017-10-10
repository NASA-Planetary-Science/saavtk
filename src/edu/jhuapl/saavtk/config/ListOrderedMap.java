package edu.jhuapl.saavtk.config;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ListOrderedMap<K, V>
{

	public static <K, V> ListOrderedMap<K, V> of(List<K> list, Map<K, V> map)
	{
		return new ListOrderedMap<>(list, map);
	}

	private final ImmutableList<K> list;
	private final ImmutableMap<K, V> map;

	protected ListOrderedMap(List<K> list, Map<K, V> map)
	{
		if (list == null || map == null)
			throw new NullPointerException();
		if (list.size() != map.size())
			throw new IllegalArgumentException();
		ImmutableSet<K> keys = ImmutableSet.copyOf(list);
		if (!keys.equals(map.keySet()))
		{
			throw new IllegalArgumentException();
		}
		this.list = ImmutableList.copyOf(list);
		this.map = ImmutableMap.copyOf(map);
	}

	public ImmutableList<K> getKeys()
	{
		return list;
	}

	public final K getKey(int index)
	{
		return list.get(index);
	}

	public final V getValue(K key)
	{
		if (key == null)
			throw new NullPointerException();
		return map.get(key);
	}

	public V getValue(int index)
	{
		return getValue(getKey(index));
	}
}
