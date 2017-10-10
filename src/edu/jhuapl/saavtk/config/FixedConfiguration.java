package edu.jhuapl.saavtk.config;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class FixedConfiguration extends ListOrderedMap<Key<?>, Object> implements BodyConfiguration
{

	public static Builder builder(Key<Builder> builderKey)
	{
		return new Builder(builderKey, Lists.newArrayList(), Maps.newHashMap());
	}

	private FixedConfiguration(List<Key<?>> list, Map<Key<?>, Object> map)
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

	public static final class Builder implements BodyConfiguration.Builder
	{
		private final Key<Builder> builderKey;
		private final List<Key<?>> list;
		private final Map<Key<?>, Object> map;

		private Builder(Key<Builder> builderKey, List<Key<?>> list, Map<Key<?>, Object> map)
		{
			this.builderKey = builderKey;
			this.list = list;
			this.map = map;
		}

		@Override
		public Builder put(ConfigurationEntry<?> entry)
		{
			if (entry == null)
				throw new NullPointerException();
			Key<?> key = entry.getKey();
			if (map.containsKey(key))
			{
				throw new IllegalArgumentException("Duplicate key entered in map.");
			}
			list.add(key);
			map.put(key, entry.getValue());
			return this;
		}

		@Override
		public FixedConfiguration build()
		{
			return new FixedConfiguration(list, map);
		}

		boolean matches(Key<Builder> builderKey)
		{
			return this.builderKey.equals(builderKey);
		}
	}
}
