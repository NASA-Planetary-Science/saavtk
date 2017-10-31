package edu.jhuapl.saavtk.config;

public abstract class ExtensibleTypedLookup implements TypedLookup
{
	public static abstract class Builder<T extends ExtensibleTypedLookup> implements TypedLookup.TypedBuilder
	{

		private final FixedTypedLookup.Builder fixedBuilder;

		protected Builder(FixedTypedLookup.Builder builder)
		{
			if (builder == null) throw new NullPointerException();
			this.fixedBuilder = builder;
		}

		@Override
		public <ValueType> Builder<T> put(Key<ValueType> key, ValueType value)
		{
			fixedBuilder.put(key, value);
			return this;
		}

		public abstract T doBuild();

		@Override
		public final T build()
		{
			return doBuild();
		}

		protected FixedTypedLookup.Builder getFixedBuilder()
		{
			return fixedBuilder;
		}

		protected boolean containsKey(Key<?> key)
		{
			return fixedBuilder.containsKey(key);
		}
	}

	private final FixedTypedLookup fixedLookup;

	protected ExtensibleTypedLookup(FixedTypedLookup.Builder builder)
	{
		this.fixedLookup = builder.build();
	}

	@Override
	public <ValueType> ValueType get(Key<ValueType> key)
	{
		return fixedLookup.get(key);
	}
}
