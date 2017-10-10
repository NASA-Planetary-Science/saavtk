package edu.jhuapl.saavtk.config;

public class ExtensibleConfiguration implements Configuration
{
	private final FixedConfiguration configuration;

	protected ExtensibleConfiguration(FixedConfiguration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public <ValueType> ValueType get(Key<ValueType> key)
	{
		return configuration.get(key);
	}
}
