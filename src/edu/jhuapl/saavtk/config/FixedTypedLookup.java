package edu.jhuapl.saavtk.config;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class FixedTypedLookup extends ListOrderedMap<Key<?>, Object> implements TypedLookup
{

	public static Builder builder()
	{
		return new Builder(Lists.newArrayList(), Maps.newHashMap());
	}

	private FixedTypedLookup(List<Key<?>> list, Map<Key<?>, Object> map)
	{
		super(list, map);
	}

	@Override
	public <ValueType> ValueType get(Key<ValueType> key)
	{
		if (key == null)
			throw new NullPointerException();
		// Class is final and constructed only by Builder in order to guarantee
		// object stored has the same type as the key, so this cast will always
		// be safe.
		@SuppressWarnings("unchecked")
		ValueType result = (ValueType) getValue(key);
		return result;
	}

	public static final class Builder implements TypedLookup.TypedBuilder
	{
		private final List<Key<?>> list;
		private final Map<Key<?>, Object> map;

		private Builder(List<Key<?>> list, Map<Key<?>, Object> map)
		{
			this.list = list;
			this.map = map;
		}

		@Override
		public <ValueType> Builder put(Key<ValueType> key, ValueType value)
		{
			if (key == null || value == null)
				throw new NullPointerException();
			if (map.containsKey(key))
			{
				throw new IllegalArgumentException("Duplicate key entered in map.");
			}
			list.add(key);
			map.put(key, value);
			return this;
		}

		@Override
		public FixedTypedLookup build()
		{
			return new FixedTypedLookup(list, map);
		}

		boolean containsKey(Key<?> key) {
			return map.containsKey(key);
		}
	}
}
