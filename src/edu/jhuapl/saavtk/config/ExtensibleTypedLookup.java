package edu.jhuapl.saavtk.config;

public abstract class ExtensibleTypedLookup implements TypedLookup
{
	public static abstract class Builder<T extends ExtensibleTypedLookup> implements TypedLookup.TypedBuilder {

		private final FixedTypedLookup.Builder fixedBuilder;
		private T built;

		protected Builder(FixedTypedLookup.Builder builder) {
			if (builder == null) throw new NullPointerException();
			this.fixedBuilder = builder;
			this.built = null;
		}

		@Override
		public <ValueType> Builder<T> put(Key<ValueType> key, ValueType value) {
			if (built != null)
			{
				throw new UnsupportedOperationException("Cannot put a new entry in the map after it was already built");
			}
			fixedBuilder.put(key, value);
			return this;
		}

		public abstract T doBuild();

		@Override
		public final T build() {
			if (built == null)
			{
				built = doBuild();
			}
			return built;
		}

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
