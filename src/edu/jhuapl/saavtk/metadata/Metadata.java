package edu.jhuapl.saavtk.metadata;

import com.google.common.collect.ImmutableList;

public interface Metadata
{
	Version getVersion();

	ImmutableList<Key<?>> getKeys();

	boolean hasKey(Key<?> key);

	<V> V get(Key<V> key);

	Metadata copy();

	@Override
	int hashCode();

	@Override
	boolean equals(Object other);
}
