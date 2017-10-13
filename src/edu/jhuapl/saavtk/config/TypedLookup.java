package edu.jhuapl.saavtk.config;

public interface TypedLookup
{
	public static interface TypedBuilder
	{
		<ValueType> TypedBuilder put(Key<ValueType> key, ValueType value);

		TypedLookup build();
	}

	<ValueType> ValueType get(Key<ValueType> key);
}
