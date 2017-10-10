package edu.jhuapl.saavtk.config;

public class ExtensibleConfiguration implements BodyConfiguration
{
	private final FixedConfiguration configuration;

	protected ExtensibleConfiguration(Key<FixedConfiguration.Builder> builderKey, FixedConfiguration.Builder builder)
	{
		if (builder.matches(builderKey)) throw new IllegalArgumentException();
		this.configuration = builder.build();
	}

	@Override
	public <ValueType> ValueType get(Key<ValueType> key)
	{
		return configuration.get(key);
	}
}
