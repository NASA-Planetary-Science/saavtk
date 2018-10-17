package edu.jhuapl.saavtk.metadata.serialization;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.Serializer;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.metadata.serialization.gson.GsonSerializer;

public class Serializers
{
	private static final Serializer INSTANCE = GsonSerializer.of();

	public static Serializer getDefault()
	{
		return INSTANCE;
	}

	public static Serializer of()
	{
		return GsonSerializer.of();
	}

	public static void serialize(String metadataId, MetadataManager manager, File file) throws IOException
	{
		Preconditions.checkNotNull(metadataId);
		Preconditions.checkNotNull(manager);
		Preconditions.checkNotNull(file);

		Serializer serializer = of();
		serializer.register(Key.of(metadataId), manager);
		serializer.save(file);
	}

	public static void serialize(String metadataId, Metadata metadata, File file) throws IOException
	{
		Preconditions.checkNotNull(metadataId);
		Preconditions.checkNotNull(metadata);
		Preconditions.checkNotNull(file);

		serialize(metadataId, new MetadataManager() {

			@Override
			public Metadata store()
			{
				return metadata;
			}

			@Override
			public void retrieve(@SuppressWarnings("unused") Metadata source)
			{
				throw new UnsupportedOperationException();
			}

		}, file);
	}

	public static void deserialize(File file, String metadataId, MetadataManager manager) throws IOException
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkNotNull(metadataId);
		Preconditions.checkNotNull(manager);

		Serializer serializer = of();
		Key<Metadata> key = Key.of(metadataId);
		serializer.register(key, manager);
		serializer.load(file);
		serializer.deregister(key);
	}

	public static FixedMetadata deserialize(File file, String metadataId) throws IOException
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkNotNull(metadataId);

		class DirectMetadataManager implements MetadataManager
		{
			final SettableMetadata metadata = SettableMetadata.of(Version.of(0, 1));

			@Override
			public Metadata store()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void retrieve(Metadata source)
			{
				for (Key<?> key : source.getKeys())
				{
					put(key, source.get(key), metadata);
				}
			}

		}

		DirectMetadataManager manager = new DirectMetadataManager();
		deserialize(file, metadataId, manager);
		return FixedMetadata.of(manager.metadata);
	}

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

	@SuppressWarnings("unchecked")
	private static <T> void put(Key<?> key, T object, SettableMetadata metadata)
	{
		metadata.put((Key<T>) key, object);
	}
}
