package edu.jhuapl.saavtk.config;

public interface TypedLookup
{
	public static interface TypedBuilder
	{
		TypedBuilder put(Entry<?> entry);

		TypedLookup build();
	}

	<ValueType> ValueType get(Key<ValueType> key);
}
