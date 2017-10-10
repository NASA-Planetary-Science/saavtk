package edu.jhuapl.saavtk.config;

public interface BodyConfiguration
{
	public static interface Builder
	{
		Builder put(ConfigurationEntry<?> entry);

		BodyConfiguration build();
	}

	<ValueType> ValueType get(Key<ValueType> key);
}
