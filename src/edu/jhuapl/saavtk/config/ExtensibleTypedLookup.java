package edu.jhuapl.saavtk.config;

public abstract class ExtensibleTypedLookup implements TypedLookup
{
	public static abstract class Builder<T extends ExtensibleTypedLookup> implements TypedLookup.TypedBuilder {

		private final FixedTypedLookup.Builder fixedBuilder;

		protected Builder(FixedTypedLookup.Builder builder) {
			if (builder == null) throw new NullPointerException();
			this.fixedBuilder = builder;
		}

		@Override
		public <ValueType> Builder<T> put(Key<ValueType> key, ValueType value) {
			fixedBuilder.put(key, value);
			return this;
		}

		@Override
		public abstract T build();

		protected FixedTypedLookup.Builder getFixedBuilder() {
			return fixedBuilder;
		}

		protected boolean containsKey(Key<?> key) {
			return fixedBuilder.containsKey(key);
		}
	}

	private final FixedTypedLookup configuration;

	protected ExtensibleTypedLookup(FixedTypedLookup.Builder builder)
	{
		this.configuration = builder.build();
	}

	@Override
	public <ValueType> ValueType get(Key<ValueType> key)
	{
		return configuration.get(key);
	}
}
