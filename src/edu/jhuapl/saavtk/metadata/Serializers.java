package edu.jhuapl.saavtk.metadata;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.saavtk.metadata.gson.GsonSerializer;

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
		Serializer serializer = of();
		serializer.register(Key.of(metadataId), manager);
		serializer.save(file);
	}

	public static void serialize(String metadataId, Metadata metadata, File file) throws IOException
	{
		serialize(metadataId, new MetadataManager() {

			@Override
			public Metadata store()
			{
				return metadata;
			}

			@Override
			public void retrieve(Metadata source)
			{
				throw new UnsupportedOperationException();
			}

		}, file);
	}

	public static void deserialize(File file, String metadataId, MetadataManager manager) throws IOException
	{
		Serializer serializer = of();
		serializer.register(Key.of(metadataId), manager);
		serializer.load(file);
	}

	public static FixedMetadata deserialize(File file, String metadataId) throws IOException
	{
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

	@SuppressWarnings("unchecked")
	private static <T> void put(Key<?> key, T object, SettableMetadata metadata)
	{
		metadata.put((Key<T>) key, object);
	}
}
