package edu.jhuapl.saavtk.config;

public interface Configuration
{
	public static interface Builder
	{
		Builder put(ConfigurationEntry<?> entry);

		Configuration build();
	}

	<ValueType> ValueType get(Key<ValueType> key);
}
