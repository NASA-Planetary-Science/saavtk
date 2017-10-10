package edu.jhuapl.saavtk.config;

public class ExtensibleConfiguration implements BodyConfiguration
{
	public static abstract class Builder<T extends ExtensibleConfiguration> implements BodyConfiguration.Builder {

		private final FixedConfiguration.Builder fixedBuilder;

		protected Builder(FixedConfiguration.Builder builder) {
			if (builder == null) throw new NullPointerException();
			this.fixedBuilder = builder;
		}

		@Override
		public Builder<T> put(ConfigurationEntry<?> entry) {
			fixedBuilder.put(entry);
			return this;
		}

		@Override
		public abstract T build();

		protected FixedConfiguration.Builder getFixedBuilder() {
			return fixedBuilder;
		}
	}

	private final FixedConfiguration configuration;

	protected ExtensibleConfiguration(Key<FixedConfiguration.Builder> builderKey, FixedConfiguration.Builder builder)
	{
		if (!builder.matches(builderKey))
			throw new IllegalArgumentException();
		this.configuration = builder.build();
	}

	@Override
	public <ValueType> ValueType get(Key<ValueType> key)
	{
		return configuration.get(key);
	}
}
