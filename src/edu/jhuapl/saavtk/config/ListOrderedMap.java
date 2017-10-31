package edu.jhuapl.saavtk.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Extensible utility class that provides an immutable map-type lookup of values by key,
 * but also provides an ordered list of keys in the map.
 * 
 * @author peachjm1
 *
 * @param <K> the type of the key used to get values from the map
 * @param <V> the type of value stored in the map
 */
public class ListOrderedMap<K, V>
{
	/**
	 * Returns a new {@link ListOrderedMap} instance using the supplied arguments. 
	 * @param list the list of map keys in order; duplicate keys are OK but will be dropped
	 * @param map the map of keys to values
	 * @return the new {@link ListOrderedMap}
	 * @throws NullPointerException if either argument is null or contains any null elements
	 * @throws IllegalArgumentException if the list of keys contains a key not in the keySet of the map,
	 * or if the keySet of the map contains a key that is not in the list
	 */
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
		// Eliminate duplicate keys in the input list.
		Set<K> keysInList = Sets.newHashSet();
		ImmutableList.Builder<K> builder = ImmutableList.builder();
		for (K key: list)
		{
			if (keysInList.contains(key)) continue;
			builder.add(key);
			keysInList.add(key);
		}
		this.list = builder.build();
		this.map = ImmutableMap.copyOf(map);
	}

	/**
	 * Returns the list of map keys in order.
	 * @return the list of keys
	 */
	public ImmutableList<K> getKeys()
	{
		return list;
	}

	/**
	 * Returns the key indicated by the index.
	 * @param index the position of the key to retrieve
	 * @return the key in that position
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size of list)
	 */
	public K getKey(int index)
	{
		return list.get(index);
	}

	/**
	 * Returns the value in the map associated with the supplied key 
	 * @param key the key whose value to retrieve
	 * @return the value in the map, or null if no value is present with the supplied key
	 * @throws NullPointerException if the supplied key is null
	 */
	public V getValue(K key)
	{
		if (key == null)
			throw new NullPointerException();
		return map.get(key);
	}

	/**
	 * Return the value in the map for the key at the supplied position (index) in the list
	 * @param index the position of the key 
	 * @return the value in the map, or null if no value is present with the supplied key
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size of list)
	 */
	public V getValue(int index)
	{
		return getValue(getKey(index));
	}
}
