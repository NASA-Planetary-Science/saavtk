package edu.jhuapl.saavtk.metadata;

import com.google.common.collect.ImmutableList;

public interface Metadata
{
	Version getVersion();

	ImmutableList<Key<?>> getKeys();

	<V> V get(Key<V> key);

	Metadata copy();
}
