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
		private final SimpleBuilder simpleBuilder;
		private FixedTypedLookup built;

		private Builder(List<Key<?>> list, Map<Key<?>, Object> map)
		{
			simpleBuilder = new SimpleBuilder(list, map);
			built = null;
		}

		@Override
		public <ValueType> Builder put(Key<ValueType> key, ValueType value)
		{
			if (built != null)
				throw new UnsupportedOperationException("Cannot add entry to map after it was already built");
			if (containsKey(key))
				throw new IllegalArgumentException("Duplicate key entered in map.");

			simpleBuilder.put(key, value);
			return this;
		}

		@Override
		public FixedTypedLookup build()
		{
			if (built == null)
				built = simpleBuilder.build();
			return built;
		}

		protected boolean containsKey(Key<?> key)
		{
			return simpleBuilder.containsKey(key);
		}
	}

	static final class SimpleBuilder implements TypedLookup.TypedBuilder
	{
		private final List<Key<?>> list;
		private final Map<Key<?>, Object> map;

		private SimpleBuilder(List<Key<?>> list, Map<Key<?>, Object> map)
		{
			this.list = list;
			this.map = map;
		}

		@Override
		public <ValueType> SimpleBuilder put(Key<ValueType> key, ValueType value)
		{
			if (key == null || value == null)
				throw new NullPointerException();
			list.add(key);
			map.put(key, value);
			return this;
		}

		@Override
		public FixedTypedLookup build()
		{
			return new FixedTypedLookup(list, map);
		}
		
		private boolean containsKey(Key<?> key)
		{
			if (key == null)
				throw new NullPointerException();
			return map.containsKey(key);
		}
	}
}
