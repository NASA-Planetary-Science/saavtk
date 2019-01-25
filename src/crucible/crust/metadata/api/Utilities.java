package edu.jhuapl.saavtk.metadata;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class Utilities
{

	public static <K> ImmutableMap<K, Metadata> bulkStore(Map<K, MetadataManager> managers)
	{
		Preconditions.checkNotNull(managers);

		ImmutableMap.Builder<K, Metadata> builder = ImmutableMap.builder();
		for (Entry<K, MetadataManager> entry : managers.entrySet())
		{
			builder.put(entry.getKey(), entry.getValue().store());
		}

		return builder.build();
	}

	public static <K> void bulkRetrieve(Map<K, MetadataManager> managers, Map<K, Metadata> metadata)
	{
		Preconditions.checkNotNull(managers);
		Preconditions.checkNotNull(metadata);
		for (Entry<K, MetadataManager> entry : managers.entrySet())
		{
			K key = entry.getKey();
			if (metadata.containsKey(key))
			{
				entry.getValue().retrieve(metadata.get(key));
			}
		}
	}

}
