package edu.jhuapl.saavtk.metadata;

import com.google.common.collect.ImmutableList;

// TODO rename to just Metadata (Metadata class will need to be renamed at the same time).
// Then use this interface where appropriate in calling code.
public interface MetadataInterface
{
	Version getVersion();

	ImmutableList<Key<?>> getKeys();

	<V> V get(Key<V> key);

}
